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
    private static final Color OBSTACLE_COLOR = new Color(0.45f, 0.52f, 0.6f, 1f);
    private static final Color PLAYER_PAINTED_OBSTACLE_COLOR = new Color(0.2f, 0.78f, 0.98f, 1f);
    private static final Color ENEMY_PAINTED_OBSTACLE_COLOR = new Color(0.98f, 0.38f, 0.62f, 1f);
    private static final float PLATFORM_HEIGHT_EPSILON = 0.04f;

    private final Array<Obstacle3D> obstacles = new Array<>();

    public StageObstacles3D() {
        // Keep the center readable while still giving both sides a solid line-of-sight break.
        addObstacle(0f, 0f, 1.65f, 1.65f, 1.35f, false);

        // Low side platforms are a little wider now so jump routes are easier to read.
        addObstacle(-2.9f, 0f, 1.45f, 2.7f, 0.82f, true);
        addObstacle(2.9f, 0f, 1.45f, 2.7f, 0.82f, true);

        // Slightly narrower climb walls leave clearer side lanes for beginner playtests.
        addObstacle(0f, 3.1f, 2.6f, 1.2f, 1.75f, false);
        addObstacle(0f, -3.1f, 2.6f, 1.2f, 1.75f, false);
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        for (Obstacle3D obstacle : obstacles) {
            modelBatch.render(obstacle.instance, environment);
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

        float halfWidth = width / 2f;
        float halfDepth = depth / 2f;
        obstacles.add(new Obstacle3D(
            model,
            instance,
            centerX - halfWidth,
            centerX + halfWidth,
            centerZ - halfDepth,
            centerZ + halfDepth,
            height,
            standable
        ));
    }

    private void updateObstacleColor(Obstacle3D obstacle) {
        obstacle.instance.materials.get(0).set(ColorAttribute.createDiffuse(getObstacleColor(obstacle.paintCellState)));
    }

    private Color getObstacleColor(int paintCellState) {
        if (paintCellState == FloorGrid3D.CELL_STATE_PLAYER) {
            return PLAYER_PAINTED_OBSTACLE_COLOR;
        }
        if (paintCellState == FloorGrid3D.CELL_STATE_ENEMY) {
            return ENEMY_PAINTED_OBSTACLE_COLOR;
        }
        return OBSTACLE_COLOR;
    }

    private boolean isWalkableForPlayer(Obstacle3D obstacle) {
        return obstacle.standable || obstacle.paintCellState == FloorGrid3D.CELL_STATE_PLAYER;
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
            float minX,
            float maxX,
            float minZ,
            float maxZ,
            float topY,
            boolean standable
        ) {
            this.model = model;
            this.instance = instance;
            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;
            this.topY = topY;
            this.standable = standable;
            this.paintCellState = FloorGrid3D.CELL_STATE_EMPTY;
        }
    }
}
