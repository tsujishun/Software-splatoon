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
    public static final int MAX_HP = 3;
    public static final float HIT_RADIUS = 0.42f;
    public static final float RESPAWN_SECONDS = 2.6f;
    public static final float INVINCIBLE_SECONDS = 1.1f;

    private static final float ENEMY_DIAMETER = 0.7f;
    private static final float EDGE_MARGIN = 0.35f;
    private static final float DIRECTION_CHANGE_MIN_SECONDS = 1.2f;
    private static final float DIRECTION_CHANGE_MAX_SECONDS = 2.4f;
    private static final float SPAWN_INSET = 1.3f;
    private static final Color ENEMY_COLOR = new Color(0.95f, 0.45f, 0.7f, 1f);

    private final Model model;
    private final ModelInstance instance;
    private final Vector3 position = new Vector3();
    private final Vector3 spawnPosition = new Vector3();
    private final Vector3 facingDirection = new Vector3(0f, 0f, -1f);
    private final Vector3 moveDirection = new Vector3(0f, 0f, -1f);
    private final Vector3 nextPosition = new Vector3();
    private final Vector3 centerDirection = new Vector3();

    private float directionChangeTimer;
    private int hp;
    private boolean splatted;
    private float respawnTimer;
    private float invincibleTimer;

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
        updateTimers(delta, floorGrid);
        if (splatted) {
            return;
        }

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
        if (splatted) {
            return;
        }
        modelBatch.render(instance, environment);
    }

    public void reset(FloorGrid3D floorGrid) {
        hp = MAX_HP;
        splatted = false;
        respawnTimer = 0f;
        invincibleTimer = 0f;
        setSpawnPosition(floorGrid);
        position.set(spawnPosition);
        chooseDirectionTowardCenter();
        updateTransform();
    }

    public int getHp() {
        return hp;
    }

    public float getHitRadius() {
        return HIT_RADIUS;
    }

    public boolean isSplatted() {
        return splatted;
    }

    public boolean isInvincible() {
        return invincibleTimer > 0f;
    }

    public boolean takeHit() {
        if (splatted || isInvincible()) {
            return false;
        }

        hp = Math.max(0, hp - 1);
        if (hp <= 0) {
            splatted = true;
            respawnTimer = RESPAWN_SECONDS;
            return true;
        }
        return false;
    }

    public float getRespawnTimer() {
        return respawnTimer;
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

    private void updateTimers(float delta, FloorGrid3D floorGrid) {
        if (invincibleTimer > 0f) {
            invincibleTimer = Math.max(0f, invincibleTimer - delta);
        }

        if (!splatted) {
            return;
        }

        respawnTimer = Math.max(0f, respawnTimer - delta);
        if (respawnTimer <= 0f) {
            respawn(floorGrid);
        }
    }

    private void respawn(FloorGrid3D floorGrid) {
        setSpawnPosition(floorGrid);
        position.set(spawnPosition);
        hp = MAX_HP;
        splatted = false;
        invincibleTimer = INVINCIBLE_SECONDS;
        chooseDirectionTowardCenter();
        updateTransform();
    }

    private void setSpawnPosition(FloorGrid3D floorGrid) {
        float radius = ENEMY_DIAMETER / 2f;
        float minX = floorGrid.getMinX() + radius;
        float maxX = floorGrid.getMaxX() - radius;
        float minZ = floorGrid.getMinZ() + radius;
        float maxZ = floorGrid.getMaxZ() - radius;

        spawnPosition.set(
            MathUtils.clamp(maxX - SPAWN_INSET, minX, maxX),
            radius,
            MathUtils.clamp(maxZ - SPAWN_INSET, minZ, maxZ)
        );
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
