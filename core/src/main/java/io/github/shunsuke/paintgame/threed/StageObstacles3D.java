package io.github.shunsuke.paintgame.threed;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Small fixed obstacle set for the early 3D battle arena.
 * Some blocks are tall walls, while some are low enough to be used as jump platforms.
 */
public class StageObstacles3D implements Disposable {
    private static final float TOP_PLATE_HEIGHT = 0.05f;
    private static final float TOP_PLATE_INSET = 0.14f;
    private static final float CLIMB_HINT_HEIGHT = 0.3f;
    private static final float CLIMB_HINT_WIDTH = 0.16f;
    private static final float CLIMB_HINT_Y_OFFSET = 0.22f;
    private static final float PERIMETER_WALL_HEIGHT = 0.72f;
    private static final float PERIMETER_WALL_THICKNESS = 0.38f;
    private static final float SPAWN_PAD_HEIGHT = 0.08f;
    private static final float SPAWN_PAD_RADIUS = 0.62f;
    private static final float SPAWN_PAD_CENTER_RADIUS = 0.28f;
    private static final Color OBSTACLE_COLOR = new Color(0.31f, 0.37f, 0.46f, 1f);
    private static final Color CLIMBABLE_WALL_COLOR = new Color(0.42f, 0.48f, 0.58f, 1f);
    private static final Color PLATFORM_TOP_COLOR = new Color(0.76f, 0.81f, 0.88f, 1f);
    private static final Color PLAYER_PAINTED_OBSTACLE_COLOR = new Color(0.12f, 0.78f, 1f, 1f);
    private static final Color PLAYER_PAINTED_TOP_COLOR = new Color(0.62f, 0.94f, 1f, 1f);
    private static final Color ENEMY_PAINTED_OBSTACLE_COLOR = new Color(0.98f, 0.35f, 0.62f, 1f);
    private static final Color ENEMY_PAINTED_TOP_COLOR = new Color(1f, 0.76f, 0.86f, 1f);
    private static final Color CLIMB_HINT_COLOR = new Color(0.92f, 0.98f, 1f, 0.95f);
    private static final Color CLIMB_HINT_IDLE_COLOR = new Color(0.7f, 0.78f, 0.88f, 1f);
    private static final Color CLIMB_HINT_ENEMY_COLOR = new Color(1f, 0.72f, 0.84f, 1f);
    private static final Color PERIMETER_WALL_COLOR = new Color(0.18f, 0.22f, 0.28f, 1f);
    private static final Color PERIMETER_WALL_CAP_COLOR = new Color(0.46f, 0.54f, 0.64f, 1f);
    private static final Color PLAYER_SPAWN_PAD_COLOR = new Color(0.11f, 0.56f, 0.8f, 1f);
    private static final Color PLAYER_SPAWN_PAD_CENTER_COLOR = new Color(0.72f, 0.94f, 1f, 1f);
    private static final Color ENEMY_SPAWN_PAD_COLOR = new Color(0.8f, 0.26f, 0.5f, 1f);
    private static final Color ENEMY_SPAWN_PAD_CENTER_COLOR = new Color(1f, 0.8f, 0.88f, 1f);
    private static final float PLATFORM_HEIGHT_EPSILON = 0.04f;

    private final Array<Obstacle3D> obstacles = new Array<>();
    private final Array<Decoration3D> decorations = new Array<>();

    public StageObstacles3D(StageConfig3D stageConfig) {
        for (StageConfig3D.ObstacleSpec obstacleSpec : stageConfig.getObstacleSpecs()) {
            addObstacle(
                obstacleSpec.getCenterX(),
                obstacleSpec.getCenterZ(),
                obstacleSpec.getWidth(),
                obstacleSpec.getDepth(),
                obstacleSpec.getHeight(),
                obstacleSpec.isStandable()
            );
        }
        addPerimeterDecorations(stageConfig);
        addSpawnPadDecorations(stageConfig);
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        for (Obstacle3D obstacle : obstacles) {
            modelBatch.render(obstacle.instance, environment);
            if (obstacle.topPlateInstance != null) {
                modelBatch.render(obstacle.topPlateInstance, environment);
            }
            if (shouldRenderClimbHint(obstacle)) {
                modelBatch.render(obstacle.climbHintInstance, environment);
            }
        }
        for (Decoration3D decoration : decorations) {
            modelBatch.render(decoration.instance, environment);
        }
    }

    public boolean collidesCircle(float centerX, float centerZ, float radius) {
        for (Obstacle3D obstacle : obstacles) {
            if (overlapsCircle(obstacle, centerX, centerZ, radius)) {
                return true;
            }
        }
        return false;
    }

    public boolean collidesCircleForPlayer(float centerX, float centerZ, float radius, float playerBaseY) {
        for (Obstacle3D obstacle : obstacles) {
            if (!overlapsCircle(obstacle, centerX, centerZ, radius)) {
                continue;
            }

            // A low platform should block the player from the side, but once the player is
            // already above the top surface, it becomes walkable instead of a solid wall.
            if (!isWalkableForPlayer(obstacle) || playerBaseY < obstacle.topY - PLATFORM_HEIGHT_EPSILON) {
                return true;
            }
        }
        return false;
    }

    public float getSupportHeightAt(float centerX, float centerZ, float radius, float actorBaseY) {
        float supportHeight = 0f;
        for (Obstacle3D obstacle : obstacles) {
            if (!isWalkableForPlayer(obstacle) || !overlapsCircle(obstacle, centerX, centerZ, radius)) {
                continue;
            }
            if (actorBaseY >= obstacle.topY - PLATFORM_HEIGHT_EPSILON) {
                supportHeight = Math.max(supportHeight, obstacle.topY);
            }
        }
        return supportHeight;
    }

    public float getLandingHeightAt(float centerX, float centerZ, float radius, float previousBaseY, float nextBaseY) {
        float landingHeight = Float.NEGATIVE_INFINITY;
        for (Obstacle3D obstacle : obstacles) {
            if (!isWalkableForPlayer(obstacle) || !overlapsCircle(obstacle, centerX, centerZ, radius)) {
                continue;
            }
            if (previousBaseY >= obstacle.topY - PLATFORM_HEIGHT_EPSILON
                && nextBaseY <= obstacle.topY + PLATFORM_HEIGHT_EPSILON) {
                landingHeight = Math.max(landingHeight, obstacle.topY);
            }
        }
        return landingHeight;
    }

    public float getPlayerPaintedClimbTopY(float centerX, float centerZ, float radius, float extraDistance, float actorBaseY) {
        float climbTopY = Float.NEGATIVE_INFINITY;
        float climbRadius = radius + extraDistance;
        for (Obstacle3D obstacle : obstacles) {
            if (obstacle.paintCellState != FloorGrid3D.CELL_STATE_PLAYER) {
                continue;
            }
            if (!overlapsCircle(obstacle, centerX, centerZ, climbRadius)) {
                continue;
            }
            if (actorBaseY < obstacle.topY - PLATFORM_HEIGHT_EPSILON) {
                climbTopY = Math.max(climbTopY, obstacle.topY);
            }
        }
        return climbTopY;
    }

    public ClimbContact getPlayerPaintedClimbContact(
        float centerX,
        float centerZ,
        float radius,
        float extraDistance,
        float actorBaseY
    ) {
        ClimbContact bestContact = null;
        float bestDistanceSquared = Float.MAX_VALUE;
        float climbRadius = radius + extraDistance;

        for (Obstacle3D obstacle : obstacles) {
            if (obstacle.paintCellState != FloorGrid3D.CELL_STATE_PLAYER) {
                continue;
            }
            if (actorBaseY >= obstacle.topY - PLATFORM_HEIGHT_EPSILON) {
                continue;
            }

            float nearestX = MathUtils.clamp(centerX, obstacle.minX, obstacle.maxX);
            float nearestZ = MathUtils.clamp(centerZ, obstacle.minZ, obstacle.maxZ);
            float deltaX = centerX - nearestX;
            float deltaZ = centerZ - nearestZ;
            float distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
            if (distanceSquared > climbRadius * climbRadius) {
                continue;
            }

            float minDistanceToFace = Math.abs(centerX - obstacle.minX);
            float normalX = -1f;
            float normalZ = 0f;

            float distanceToMaxX = Math.abs(centerX - obstacle.maxX);
            if (distanceToMaxX < minDistanceToFace) {
                minDistanceToFace = distanceToMaxX;
                normalX = 1f;
                normalZ = 0f;
            }

            float distanceToMinZ = Math.abs(centerZ - obstacle.minZ);
            if (distanceToMinZ < minDistanceToFace) {
                minDistanceToFace = distanceToMinZ;
                normalX = 0f;
                normalZ = -1f;
            }

            float distanceToMaxZ = Math.abs(centerZ - obstacle.maxZ);
            if (distanceToMaxZ < minDistanceToFace) {
                normalX = 0f;
                normalZ = 1f;
            }

            float tangentX = normalZ;
            float tangentZ = -normalX;

            if (distanceSquared < bestDistanceSquared) {
                bestDistanceSquared = distanceSquared;
                bestContact = new ClimbContact(obstacle.topY, normalX, normalZ, tangentX, tangentZ);
            }
        }

        return bestContact;
    }

    public boolean paintObstacleAtWorldPosition(float centerX, float centerZ, float radius, int paintCellState) {
        for (Obstacle3D obstacle : obstacles) {
            if (!overlapsCircle(obstacle, centerX, centerZ, radius)) {
                continue;
            }

            obstacle.paintCellState = paintCellState;
            updateObstacleColor(obstacle);
            return true;
        }
        return false;
    }

    public void resetPaint() {
        for (Obstacle3D obstacle : obstacles) {
            obstacle.paintCellState = FloorGrid3D.CELL_STATE_EMPTY;
            updateObstacleColor(obstacle);
        }
    }

    @Override
    public void dispose() {
        for (Obstacle3D obstacle : obstacles) {
            obstacle.model.dispose();
            if (obstacle.topPlateModel != null) {
                obstacle.topPlateModel.dispose();
            }
            if (obstacle.climbHintModel != null) {
                obstacle.climbHintModel.dispose();
            }
        }
        for (Decoration3D decoration : decorations) {
            decoration.model.dispose();
        }
    }

    private void addObstacle(float centerX, float centerZ, float width, float depth, float height, boolean standable) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createBox(
            width,
            height,
            depth,
            new Material(ColorAttribute.createDiffuse(OBSTACLE_COLOR)),
            Usage.Position | Usage.Normal
        );
        ModelInstance instance = new ModelInstance(model);
        instance.transform.setToTranslation(centerX, height / 2f, centerZ);

        Model topPlateModel = null;
        ModelInstance topPlateInstance = null;
        if (standable) {
            topPlateModel = modelBuilder.createBox(
                Math.max(0.18f, width - TOP_PLATE_INSET),
                TOP_PLATE_HEIGHT,
                Math.max(0.18f, depth - TOP_PLATE_INSET),
                new Material(ColorAttribute.createDiffuse(PLATFORM_TOP_COLOR)),
                Usage.Position | Usage.Normal
            );
            topPlateInstance = new ModelInstance(topPlateModel);
            topPlateInstance.transform.setToTranslation(centerX, height + TOP_PLATE_HEIGHT / 2f, centerZ);
        }

        Model climbHintModel = null;
        ModelInstance climbHintInstance = null;
        if (!standable) {
            climbHintModel = modelBuilder.createBox(
                CLIMB_HINT_WIDTH,
                CLIMB_HINT_HEIGHT,
                CLIMB_HINT_WIDTH,
                new Material(ColorAttribute.createDiffuse(CLIMB_HINT_COLOR)),
                Usage.Position | Usage.Normal
            );
            climbHintInstance = new ModelInstance(climbHintModel);
            climbHintInstance.transform.setToTranslation(centerX, height + CLIMB_HINT_Y_OFFSET, centerZ);
        }

        float halfWidth = width / 2f;
        float halfDepth = depth / 2f;
        Obstacle3D obstacle = new Obstacle3D(
            model,
            instance,
            topPlateModel,
            topPlateInstance,
            climbHintModel,
            climbHintInstance,
            centerX - halfWidth,
            centerX + halfWidth,
            centerZ - halfDepth,
            centerZ + halfDepth,
            height,
            standable
        );
        obstacles.add(obstacle);
        updateObstacleColor(obstacle);
    }

    private void updateObstacleColor(Obstacle3D obstacle) {
        obstacle.instance.materials.get(0).set(ColorAttribute.createDiffuse(getObstacleBodyColor(obstacle)));
        if (obstacle.topPlateInstance != null) {
            obstacle.topPlateInstance.materials.get(0).set(ColorAttribute.createDiffuse(getObstacleTopColor(obstacle.paintCellState)));
        }
        if (obstacle.climbHintInstance != null) {
            obstacle.climbHintInstance.materials.get(0).set(ColorAttribute.createDiffuse(getClimbHintColor(obstacle.paintCellState)));
        }
    }

    private Color getObstacleBodyColor(Obstacle3D obstacle) {
        int paintCellState = obstacle.paintCellState;
        if (paintCellState == FloorGrid3D.CELL_STATE_PLAYER) {
            return PLAYER_PAINTED_OBSTACLE_COLOR;
        }
        if (paintCellState == FloorGrid3D.CELL_STATE_ENEMY) {
            return ENEMY_PAINTED_OBSTACLE_COLOR;
        }
        return obstacle.standable ? OBSTACLE_COLOR : CLIMBABLE_WALL_COLOR;
    }

    private Color getObstacleTopColor(int paintCellState) {
        if (paintCellState == FloorGrid3D.CELL_STATE_PLAYER) {
            return PLAYER_PAINTED_TOP_COLOR;
        }
        if (paintCellState == FloorGrid3D.CELL_STATE_ENEMY) {
            return ENEMY_PAINTED_TOP_COLOR;
        }
        return PLATFORM_TOP_COLOR;
    }

    private Color getClimbHintColor(int paintCellState) {
        if (paintCellState == FloorGrid3D.CELL_STATE_PLAYER) {
            return CLIMB_HINT_COLOR;
        }
        if (paintCellState == FloorGrid3D.CELL_STATE_ENEMY) {
            return CLIMB_HINT_ENEMY_COLOR;
        }
        return CLIMB_HINT_IDLE_COLOR;
    }

    private void addPerimeterDecorations(StageConfig3D stageConfig) {
        float halfWidth = stageConfig.getColumns() / 2f;
        float halfDepth = stageConfig.getRows() / 2f;
        float wallY = PERIMETER_WALL_HEIGHT / 2f - 0.02f;
        float horizontalLength = stageConfig.getColumns() + PERIMETER_WALL_THICKNESS * 2f;
        float verticalLength = stageConfig.getRows() + PERIMETER_WALL_THICKNESS * 2f;
        float capHeight = 0.06f;

        addDecorationBox(0f, wallY, -halfDepth - PERIMETER_WALL_THICKNESS / 2f, horizontalLength, PERIMETER_WALL_HEIGHT, PERIMETER_WALL_THICKNESS, PERIMETER_WALL_COLOR);
        addDecorationBox(0f, wallY, halfDepth + PERIMETER_WALL_THICKNESS / 2f, horizontalLength, PERIMETER_WALL_HEIGHT, PERIMETER_WALL_THICKNESS, PERIMETER_WALL_COLOR);
        addDecorationBox(-halfWidth - PERIMETER_WALL_THICKNESS / 2f, wallY, 0f, PERIMETER_WALL_THICKNESS, PERIMETER_WALL_HEIGHT, verticalLength, PERIMETER_WALL_COLOR);
        addDecorationBox(halfWidth + PERIMETER_WALL_THICKNESS / 2f, wallY, 0f, PERIMETER_WALL_THICKNESS, PERIMETER_WALL_HEIGHT, verticalLength, PERIMETER_WALL_COLOR);

        addDecorationBox(0f, PERIMETER_WALL_HEIGHT - capHeight / 2f, -halfDepth - PERIMETER_WALL_THICKNESS / 2f, horizontalLength, capHeight, PERIMETER_WALL_THICKNESS + 0.02f, PERIMETER_WALL_CAP_COLOR);
        addDecorationBox(0f, PERIMETER_WALL_HEIGHT - capHeight / 2f, halfDepth + PERIMETER_WALL_THICKNESS / 2f, horizontalLength, capHeight, PERIMETER_WALL_THICKNESS + 0.02f, PERIMETER_WALL_CAP_COLOR);
        addDecorationBox(-halfWidth - PERIMETER_WALL_THICKNESS / 2f, PERIMETER_WALL_HEIGHT - capHeight / 2f, 0f, PERIMETER_WALL_THICKNESS + 0.02f, capHeight, verticalLength, PERIMETER_WALL_CAP_COLOR);
        addDecorationBox(halfWidth + PERIMETER_WALL_THICKNESS / 2f, PERIMETER_WALL_HEIGHT - capHeight / 2f, 0f, PERIMETER_WALL_THICKNESS + 0.02f, capHeight, verticalLength, PERIMETER_WALL_CAP_COLOR);
    }

    private void addSpawnPadDecorations(StageConfig3D stageConfig) {
        addSpawnPad(stageConfig.getPlayerSpawnX(), stageConfig.getPlayerSpawnZ(), PLAYER_SPAWN_PAD_COLOR, PLAYER_SPAWN_PAD_CENTER_COLOR);
        for (int i = 0; i < stageConfig.getCpuSpawnCount(); i++) {
            boolean playerTeamSpawn = i == 0;
            addSpawnPad(
                stageConfig.getCpuSpawnX(i),
                stageConfig.getCpuSpawnZ(i),
                playerTeamSpawn ? PLAYER_SPAWN_PAD_COLOR : ENEMY_SPAWN_PAD_COLOR,
                playerTeamSpawn ? PLAYER_SPAWN_PAD_CENTER_COLOR : ENEMY_SPAWN_PAD_CENTER_COLOR
            );
        }
    }

    private void addSpawnPad(float centerX, float centerZ, Color outerColor, Color centerColor) {
        addDecorationCylinder(centerX, SPAWN_PAD_HEIGHT / 2f - 0.04f, centerZ, SPAWN_PAD_RADIUS, SPAWN_PAD_HEIGHT, outerColor);
        addDecorationCylinder(centerX, SPAWN_PAD_HEIGHT * 0.72f, centerZ, SPAWN_PAD_CENTER_RADIUS, SPAWN_PAD_HEIGHT * 0.5f, centerColor);
    }

    private void addDecorationBox(float centerX, float centerY, float centerZ, float width, float height, float depth, Color color) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createBox(
            width,
            height,
            depth,
            new Material(ColorAttribute.createDiffuse(color)),
            Usage.Position | Usage.Normal
        );
        ModelInstance instance = new ModelInstance(model);
        instance.transform.setToTranslation(centerX, centerY, centerZ);
        decorations.add(new Decoration3D(model, instance));
    }

    private void addDecorationCylinder(float centerX, float centerY, float centerZ, float radius, float height, Color color) {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createCylinder(
            radius * 2f,
            height,
            radius * 2f,
            24,
            new Material(ColorAttribute.createDiffuse(color)),
            Usage.Position | Usage.Normal
        );
        ModelInstance instance = new ModelInstance(model);
        instance.transform.setToTranslation(centerX, centerY, centerZ);
        decorations.add(new Decoration3D(model, instance));
    }

    private boolean isWalkableForPlayer(Obstacle3D obstacle) {
        return obstacle.standable || obstacle.paintCellState == FloorGrid3D.CELL_STATE_PLAYER;
    }

    private boolean shouldRenderClimbHint(Obstacle3D obstacle) {
        return obstacle.climbHintInstance != null;
    }

    private boolean overlapsCircle(Obstacle3D obstacle, float centerX, float centerZ, float radius) {
        float nearestX = MathUtils.clamp(centerX, obstacle.minX, obstacle.maxX);
        float nearestZ = MathUtils.clamp(centerZ, obstacle.minZ, obstacle.maxZ);
        float deltaX = centerX - nearestX;
        float deltaZ = centerZ - nearestZ;
        return deltaX * deltaX + deltaZ * deltaZ <= radius * radius;
    }

    public static class ClimbContact {
        public final float topY;
        public final float outwardNormalX;
        public final float outwardNormalZ;
        public final float tangentX;
        public final float tangentZ;

        private ClimbContact(float topY, float outwardNormalX, float outwardNormalZ, float tangentX, float tangentZ) {
            this.topY = topY;
            this.outwardNormalX = outwardNormalX;
            this.outwardNormalZ = outwardNormalZ;
            this.tangentX = tangentX;
            this.tangentZ = tangentZ;
        }
    }

    private static class Obstacle3D {
        private final Model model;
        private final ModelInstance instance;
        private final Model topPlateModel;
        private final ModelInstance topPlateInstance;
        private final Model climbHintModel;
        private final ModelInstance climbHintInstance;
        private final float minX;
        private final float maxX;
        private final float minZ;
        private final float maxZ;
        private final float topY;
        private final boolean standable;
        private int paintCellState;

        private Obstacle3D(
            Model model,
            ModelInstance instance,
            Model topPlateModel,
            ModelInstance topPlateInstance,
            Model climbHintModel,
            ModelInstance climbHintInstance,
            float minX,
            float maxX,
            float minZ,
            float maxZ,
            float topY,
            boolean standable
        ) {
            this.model = model;
            this.instance = instance;
            this.topPlateModel = topPlateModel;
            this.topPlateInstance = topPlateInstance;
            this.climbHintModel = climbHintModel;
            this.climbHintInstance = climbHintInstance;
            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;
            this.topY = topY;
            this.standable = standable;
            this.paintCellState = FloorGrid3D.CELL_STATE_EMPTY;
        }
    }

    private static class Decoration3D {
        private final Model model;
        private final ModelInstance instance;

        private Decoration3D(Model model, ModelInstance instance) {
            this.model = model;
            this.instance = instance;
        }
    }
}
