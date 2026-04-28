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
    private static final float PLATFORM_HEIGHT_EPSILON = 0.04f;

    private final Array<Obstacle3D> obstacles = new Array<>();

    public StageObstacles3D() {
        // Keep the player spawn near the center and the CPU spawn near the top-right corner clear.
        addObstacle(-2.4f, -1.6f, 1.5f, 1.8f, 0.72f, true);
        addObstacle(2.3f, -1.4f, 1.8f, 1.2f, 1.7f, false);
        addObstacle(-1.7f, 2.0f, 1.2f, 1.9f, 1.4f, false);
        addObstacle(1.7f, 1.7f, 1.9f, 1.1f, 0.86f, true);
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
            if (!obstacle.standable || playerBaseY < obstacle.topY - PLATFORM_HEIGHT_EPSILON) {
                return true;
            }
        }
        return false;
    }

    public float getSupportHeightAt(float centerX, float centerZ, float radius, float actorBaseY) {
        float supportHeight = 0f;
        for (Obstacle3D obstacle : obstacles) {
            if (!obstacle.standable || !overlapsCircle(obstacle, centerX, centerZ, radius)) {
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
            if (!obstacle.standable || !overlapsCircle(obstacle, centerX, centerZ, radius)) {
                continue;
            }
            if (previousBaseY >= obstacle.topY - PLATFORM_HEIGHT_EPSILON
                && nextBaseY <= obstacle.topY + PLATFORM_HEIGHT_EPSILON) {
                landingHeight = Math.max(landingHeight, obstacle.topY);
            }
        }
        return landingHeight;
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

    private boolean overlapsCircle(Obstacle3D obstacle, float centerX, float centerZ, float radius) {
        float nearestX = MathUtils.clamp(centerX, obstacle.minX, obstacle.maxX);
        float nearestZ = MathUtils.clamp(centerZ, obstacle.minZ, obstacle.maxZ);
        float deltaX = centerX - nearestX;
        float deltaZ = centerZ - nearestZ;
        return deltaX * deltaX + deltaZ * deltaZ <= radius * radius;
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
        }
    }
}
