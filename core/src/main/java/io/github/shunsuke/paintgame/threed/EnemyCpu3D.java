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
    public enum EnemyState {
        PAINT,
        ATTACK,
        RESPAWN
    }

    public static final float MOVE_SPEED = 2.8f;
    public static final float OWN_PAINT_SPEED_MULTIPLIER = 1.15f;
    public static final float ENEMY_PAINT_SPEED_MULTIPLIER = 0.78f;
    public static final float NEUTRAL_SPEED_MULTIPLIER = 1f;
    public static final int MAX_HP = 3;
    public static final float HIT_RADIUS = 0.42f;
    public static final float RESPAWN_SECONDS = 2.6f;
    public static final float INVINCIBLE_SECONDS = 1.1f;

    private static final float ENEMY_DIAMETER = 0.7f;
    private static final float EDGE_MARGIN = 0.35f;
    private static final float DIRECTION_CHANGE_MIN_SECONDS = 1.2f;
    private static final float DIRECTION_CHANGE_MAX_SECONDS = 2.4f;
    private static final float SPAWN_INSET = 1.3f;
    private static final float ATTACK_RANGE = 5.2f;
    private static final float RETREAT_RANGE = 1.9f;
    private static final float APPROACH_RANGE = 3.4f;
    private static final Color ENEMY_COLOR = new Color(0.95f, 0.45f, 0.7f, 1f);

    private final Model model;
    private final ModelInstance instance;
    private final Vector3 position = new Vector3();
    private final Vector3 spawnPosition = new Vector3();
    private final Vector3 facingDirection = new Vector3(0f, 0f, -1f);
    private final Vector3 moveDirection = new Vector3(0f, 0f, -1f);
    private final Vector3 nextPosition = new Vector3();
    private final Vector3 centerDirection = new Vector3();
    private final Vector3 targetDirection = new Vector3();
    private final Vector3 retreatDirection = new Vector3();
    private final Vector3 sidestepDirection = new Vector3();

    private float directionChangeTimer;
    private int hp;
    private boolean splatted;
    private float respawnTimer;
    private float invincibleTimer;
    private EnemyState currentState = EnemyState.PAINT;

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

    public void update(
        float delta,
        FloorGrid3D floorGrid,
        Vector3 playerPosition,
        boolean playerTargetable,
        StageObstacles3D stageObstacles
    ) {
        updateTimers(delta, floorGrid);
        if (splatted) {
            currentState = EnemyState.RESPAWN;
            return;
        }

        updateBehavior(delta, playerPosition, playerTargetable);

        float speedMultiplier = getGroundSpeedMultiplier(floorGrid.getCellStateAtWorldPosition(position.x, position.z));
        float moveSpeed = MOVE_SPEED * speedMultiplier;
        nextPosition.set(position).mulAdd(moveDirection, moveSpeed * delta);

        float radius = ENEMY_DIAMETER / 2f;
        float minX = floorGrid.getMinX() + radius;
        float maxX = floorGrid.getMaxX() - radius;
        float minZ = floorGrid.getMinZ() + radius;
        float maxZ = floorGrid.getMaxZ() - radius;
        boolean hitWall = nextPosition.x < minX || nextPosition.x > maxX || nextPosition.z < minZ || nextPosition.z > maxZ;
        boolean hitObstacle = moveWithObstacleCollision(stageObstacles, minX, maxX, minZ, maxZ, radius);

        // If the enemy reaches an edge, point it back toward the middle so it keeps moving around the arena.
        if (hitWall || hitObstacle || isNearFloorEdge(floorGrid, radius)) {
            chooseDirectionTowardCenter();
        }

        if (currentState == EnemyState.PAINT) {
            facingDirection.set(moveDirection).nor();
        }
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
        currentState = EnemyState.PAINT;
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
            currentState = EnemyState.RESPAWN;
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

    public EnemyState getCurrentState() {
        return currentState;
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
        currentState = EnemyState.PAINT;
        chooseDirectionTowardCenter();
        updateTransform();
    }

    private void updateBehavior(float delta, Vector3 playerPosition, boolean playerTargetable) {
        if (playerTargetable && isPlayerInsideAttackRange(playerPosition)) {
            updateAttackBehavior(playerPosition);
            return;
        }

        currentState = EnemyState.PAINT;
        directionChangeTimer -= delta;
        if (directionChangeTimer <= 0f) {
            chooseRandomDirection();
        }
    }

    private boolean isPlayerInsideAttackRange(Vector3 playerPosition) {
        float deltaX = playerPosition.x - position.x;
        float deltaZ = playerPosition.z - position.z;
        return deltaX * deltaX + deltaZ * deltaZ <= ATTACK_RANGE * ATTACK_RANGE;
    }

    private void updateAttackBehavior(Vector3 playerPosition) {
        currentState = EnemyState.ATTACK;
        targetDirection.set(playerPosition.x - position.x, 0f, playerPosition.z - position.z);
        if (targetDirection.isZero(0.0001f)) {
            targetDirection.set(facingDirection);
        } else {
            targetDirection.nor();
        }

        facingDirection.set(targetDirection);

        float deltaX = playerPosition.x - position.x;
        float deltaZ = playerPosition.z - position.z;
        float distanceToPlayer = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (distanceToPlayer < RETREAT_RANGE) {
            retreatDirection.set(targetDirection).scl(-1f);
            moveDirection.set(retreatDirection).nor();
            return;
        }

        if (distanceToPlayer > APPROACH_RANGE) {
            moveDirection.set(targetDirection).nor();
            return;
        }

        // In the middle range, drift sideways a little so the enemy still looks alive while aiming.
        sidestepDirection.set(-targetDirection.z, 0f, targetDirection.x);
        if (sidestepDirection.isZero(0.0001f)) {
            moveDirection.set(targetDirection).nor();
        } else {
            moveDirection.set(sidestepDirection).nor();
        }
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

    private boolean moveWithObstacleCollision(
        StageObstacles3D stageObstacles,
        float minX,
        float maxX,
        float minZ,
        float maxZ,
        float radius
    ) {
        boolean hitObstacle = false;

        float candidateX = MathUtils.clamp(nextPosition.x, minX, maxX);
        if (!stageObstacles.collidesCircle(candidateX, position.z, radius)) {
            position.x = candidateX;
        } else {
            hitObstacle = true;
        }

        float candidateZ = MathUtils.clamp(nextPosition.z, minZ, maxZ);
        if (!stageObstacles.collidesCircle(position.x, candidateZ, radius)) {
            position.z = candidateZ;
        } else {
            hitObstacle = true;
        }

        return hitObstacle;
    }

    private float getGroundSpeedMultiplier(int groundCellState) {
        if (groundCellState == FloorGrid3D.CELL_STATE_ENEMY) {
            return OWN_PAINT_SPEED_MULTIPLIER;
        }
        if (groundCellState == FloorGrid3D.CELL_STATE_PLAYER) {
            return ENEMY_PAINT_SPEED_MULTIPLIER;
        }
        return NEUTRAL_SPEED_MULTIPLIER;
    }
}
