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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

/**
 * Small beginner-friendly CPU enemy for the 3D prototype.
 * It only moves on the floor, turns now and then, and stays away from the edges.
 */
public class EnemyCpu3D implements Disposable {
    public static final float MOVE_SPEED = 2.8f;

    private static final float ENEMY_DIAMETER = 0.7f;
    private static final float EDGE_MARGIN = 0.35f;
    private static final float DIRECTION_CHANGE_MIN_SECONDS = 1.2f;
    private static final float DIRECTION_CHANGE_MAX_SECONDS = 2.4f;
    private static final float SPAWN_INSET = 1.3f;
    private static final Color ENEMY_COLOR = new Color(0.95f, 0.45f, 0.7f, 1f);

    private final Model model;
    private final ModelInstance instance;
    private final Vector3 position = new Vector3();
    private final Vector3 facingDirection = new Vector3(0f, 0f, -1f);
    private final Vector3 moveDirection = new Vector3(0f, 0f, -1f);
    private final Vector3 nextPosition = new Vector3();
    private final Vector3 centerDirection = new Vector3();

    private float directionChangeTimer;

    public EnemyCpu3D() {
        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createSphere(
            ENEMY_DIAMETER,
            ENEMY_DIAMETER,
            ENEMY_DIAMETER,
            16,
            16,
            new Material(ColorAttribute.createDiffuse(ENEMY_COLOR)),
            Usage.Position | Usage.Normal
        );
        instance = new ModelInstance(model);
    }

    public void update(float delta, FloorGrid3D floorGrid) {
        directionChangeTimer -= delta;
        if (directionChangeTimer <= 0f) {
            chooseRandomDirection();
        }

        nextPosition.set(position).mulAdd(moveDirection, MOVE_SPEED * delta);

        float radius = ENEMY_DIAMETER / 2f;
        float minX = floorGrid.getMinX() + radius;
        float maxX = floorGrid.getMaxX() - radius;
        float minZ = floorGrid.getMinZ() + radius;
        float maxZ = floorGrid.getMaxZ() - radius;
        boolean hitWall = nextPosition.x < minX || nextPosition.x > maxX || nextPosition.z < minZ || nextPosition.z > maxZ;

        position.x = MathUtils.clamp(nextPosition.x, minX, maxX);
        position.z = MathUtils.clamp(nextPosition.z, minZ, maxZ);

        // If the enemy reaches an edge, point it back toward the middle so it keeps moving around the arena.
        if (hitWall || isNearFloorEdge(floorGrid, radius)) {
            chooseDirectionTowardCenter();
        }

        facingDirection.set(moveDirection).nor();
        updateTransform();
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        modelBatch.render(instance, environment);
    }

    public void reset(FloorGrid3D floorGrid) {
        float radius = ENEMY_DIAMETER / 2f;
        float minX = floorGrid.getMinX() + radius;
        float maxX = floorGrid.getMaxX() - radius;
        float minZ = floorGrid.getMinZ() + radius;
        float maxZ = floorGrid.getMaxZ() - radius;

        position.set(
            MathUtils.clamp(maxX - SPAWN_INSET, minX, maxX),
            radius,
            MathUtils.clamp(maxZ - SPAWN_INSET, minZ, maxZ)
        );
        chooseDirectionTowardCenter();
        updateTransform();
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getFacingDirection() {
        return facingDirection;
    }

    @Override
    public void dispose() {
        model.dispose();
    }

    private boolean isNearFloorEdge(FloorGrid3D floorGrid, float radius) {
        return position.x < floorGrid.getMinX() + radius + EDGE_MARGIN
            || position.x > floorGrid.getMaxX() - radius - EDGE_MARGIN
            || position.z < floorGrid.getMinZ() + radius + EDGE_MARGIN
            || position.z > floorGrid.getMaxZ() - radius - EDGE_MARGIN;
    }

    private void chooseDirectionTowardCenter() {
        centerDirection.set(-position.x, 0f, -position.z);
        centerDirection.x += MathUtils.random(-0.7f, 0.7f);
        centerDirection.z += MathUtils.random(-0.7f, 0.7f);
        setMoveDirection(centerDirection);
    }

    private void chooseRandomDirection() {
        centerDirection.set(MathUtils.random(-1f, 1f), 0f, MathUtils.random(-1f, 1f));
        setMoveDirection(centerDirection);
    }

    private void setMoveDirection(Vector3 newDirection) {
        if (newDirection.isZero(0.0001f)) {
            moveDirection.set(0f, 0f, -1f);
        } else {
            moveDirection.set(newDirection).nor();
        }
        directionChangeTimer = MathUtils.random(DIRECTION_CHANGE_MIN_SECONDS, DIRECTION_CHANGE_MAX_SECONDS);
    }

    private void updateTransform() {
        instance.transform.setToTranslation(position);
    }
}
