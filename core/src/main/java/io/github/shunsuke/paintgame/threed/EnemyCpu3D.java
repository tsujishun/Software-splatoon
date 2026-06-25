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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

/**
 * Small beginner-friendly CPU enemy for the 3D prototype.
 * The AI stays simple, but the body now uses a few primitive parts so it is easier to identify.
 */
public class EnemyCpu3D implements Disposable {
    public enum EnemyState {
        PAINT,
        ATTACK,
        RESPAWN
    }

    private enum AttackMoveMode {
        APPROACH,
        STRAFE_LEFT,
        STRAFE_RIGHT,
        RETREAT,
        PAINT
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
    private static final float SPAWN_INSET = 1.45f;
    private static final float RETREAT_RANGE = 1.9f;
    private static final float APPROACH_RANGE = 3.4f;
    private static final float ATTACK_DIRECTION_JITTER_DEGREES = 14f;
    private static final float ATTACK_STRAFE_FORWARD_BLEND = 0.28f;
    private static final float ATTACK_STRAFE_BACKWARD_BLEND = 0.14f;
    private static final float ATTACK_PAINT_WANDER_MIN_DEGREES = 45f;
    private static final float ATTACK_PAINT_WANDER_MAX_DEGREES = 95f;
    private static final Color PLAYER_TEAM_BODY_COLOR = new Color(0.2f, 0.82f, 1f, 1f);
    private static final Color PLAYER_TEAM_HEAD_COLOR = new Color(0.78f, 0.95f, 1f, 1f);
    private static final Color PLAYER_TEAM_NOSE_COLOR = new Color(0.05f, 0.26f, 0.38f, 1f);
    private static final Color PLAYER_TEAM_MARKER_COLOR = new Color(0.9f, 1f, 0.35f, 1f);
    private static final Color PLAYER_TEAM_ATTACK_MARKER_COLOR = new Color(0.35f, 0.92f, 1f, 1f);
    private static final Color ENEMY_TEAM_BODY_COLOR = new Color(0.95f, 0.45f, 0.7f, 1f);
    private static final Color ENEMY_TEAM_HEAD_COLOR = new Color(1f, 0.78f, 0.9f, 1f);
    private static final Color ENEMY_TEAM_NOSE_COLOR = new Color(0.42f, 0.08f, 0.26f, 1f);
    private static final Color ENEMY_TEAM_MARKER_COLOR = new Color(1f, 0.9f, 0.35f, 1f);
    private static final Color ENEMY_TEAM_ATTACK_MARKER_COLOR = new Color(1f, 0.45f, 0.28f, 1f);
    private static final float BODY_OFFSET_Y = -0.15f;
    private static final float HEAD_OFFSET_Y = 0.14f;
    private static final float HEAD_OFFSET_Z = -0.02f;
    private static final float NOSE_OFFSET_Y = 0.03f;
    private static final float NOSE_OFFSET_Z = -0.34f;
    private static final float MARKER_OFFSET_Y = 0.48f;
    private static final float MOVE_BOB_SPEED = 9.5f;
    private static final float MOVE_BOB_AMOUNT = 0.03f;
    private static final float RECOIL_DURATION = 0.12f;
    private static final float RECOIL_DISTANCE = 0.07f;
    private static final float HIT_FLASH_DURATION = 0.16f;
    private static final float RESPAWN_SCALE_BONUS = 0.07f;
    private static final float RESPAWN_PULSE_DURATION = 0.42f;
    private static final Color HIT_FLASH_COLOR = new Color(1f, 0.76f, 0.76f, 1f);

    private final Model bodyModel;
    private final Model headModel;
    private final Model noseModel;
    private final Model markerModel;
    private final ModelInstance bodyInstance;
    private final ModelInstance headInstance;
    private final ModelInstance noseInstance;
    private final ModelInstance markerInstance;
    private final Team3D team;
    private final String displayName;
    private final int spawnIndex;
    private final Vector3 position = new Vector3();
    private final Vector3 spawnPosition = new Vector3();
    private final Vector3 facingDirection = new Vector3(0f, 0f, -1f);
    private final Vector3 moveDirection = new Vector3(0f, 0f, -1f);
    private final Vector3 nextPosition = new Vector3();
    private final Vector3 centerDirection = new Vector3();
    private final Vector3 targetDirection = new Vector3();
    private final Vector3 retreatDirection = new Vector3();
    private final Vector3 sidestepDirection = new Vector3();
    private final Vector3 attackMoveDirection = new Vector3();
    private final Vector3 shotDirection = new Vector3();
    private final Matrix4 actorTransform = new Matrix4();
    private final Color bodyTint = new Color();
    private final Color headTint = new Color();
    private final Color noseTint = new Color();
    private final Color markerTint = new Color();
    private final WeaponConfig3D weaponConfig = WeaponConfig3D.ENEMY_SHOOTER;
    private final Color bodyBaseColor;
    private final Color headBaseColor;
    private final Color noseBaseColor;
    private final Color markerBaseColor;
    private final Color attackMarkerColor;

    private float directionChangeTimer;
    private float fireCooldownRemaining;
    private float attackMoveTimer;
    private int hp;
    private boolean splatted;
    private float respawnTimer;
    private float invincibleTimer;
    private float moveBobTimer;
    private float moveBobStrength;
    private float recoilTimer;
    private float hitFlashTimer;
    private EnemyState currentState = EnemyState.PAINT;
    private AttackMoveMode currentAttackMoveMode = AttackMoveMode.APPROACH;
    private CpuDifficulty3D difficulty = CpuDifficulty3D.NORMAL;

    public EnemyCpu3D(Team3D team, String displayName, int spawnIndex) {
        this.team = team;
        this.displayName = displayName;
        this.spawnIndex = spawnIndex;
        this.bodyBaseColor = team == Team3D.PLAYER ? PLAYER_TEAM_BODY_COLOR : ENEMY_TEAM_BODY_COLOR;
        this.headBaseColor = team == Team3D.PLAYER ? PLAYER_TEAM_HEAD_COLOR : ENEMY_TEAM_HEAD_COLOR;
        this.noseBaseColor = team == Team3D.PLAYER ? PLAYER_TEAM_NOSE_COLOR : ENEMY_TEAM_NOSE_COLOR;
        this.markerBaseColor = team == Team3D.PLAYER ? PLAYER_TEAM_MARKER_COLOR : ENEMY_TEAM_MARKER_COLOR;
        this.attackMarkerColor = team == Team3D.PLAYER ? PLAYER_TEAM_ATTACK_MARKER_COLOR : ENEMY_TEAM_ATTACK_MARKER_COLOR;
        ModelBuilder modelBuilder = new ModelBuilder();
        bodyModel = modelBuilder.createSphere(
            0.82f,
            0.42f,
            0.82f,
            16,
            16,
            new Material(ColorAttribute.createDiffuse(bodyBaseColor)),
            Usage.Position | Usage.Normal
        );
        headModel = modelBuilder.createSphere(
            0.48f,
            0.44f,
            0.48f,
            16,
            16,
            new Material(ColorAttribute.createDiffuse(headBaseColor)),
            Usage.Position | Usage.Normal
        );
        noseModel = modelBuilder.createBox(
            0.14f,
            0.16f,
            0.28f,
            new Material(ColorAttribute.createDiffuse(noseBaseColor)),
            Usage.Position | Usage.Normal
        );
        markerModel = modelBuilder.createBox(
            0.62f,
            0.05f,
            0.62f,
            new Material(ColorAttribute.createDiffuse(markerBaseColor)),
            Usage.Position | Usage.Normal
        );
        bodyInstance = new ModelInstance(bodyModel);
        headInstance = new ModelInstance(headModel);
        noseInstance = new ModelInstance(noseModel);
        markerInstance = new ModelInstance(markerModel);
    }

    public void update(
        float delta,
        FloorGrid3D floorGrid,
        Vector3 targetPosition,
        boolean targetable,
        StageObstacles3D stageObstacles
    ) {
        updateTimers(delta, floorGrid);
        fireCooldownRemaining = Math.max(0f, fireCooldownRemaining - delta);
        if (splatted) {
            currentState = EnemyState.RESPAWN;
            return;
        }

        updateBehavior(delta, targetPosition, targetable);

        float speedMultiplier = getGroundSpeedMultiplier(floorGrid.getCellStateAtWorldPosition(position.x, position.z));
        float moveSpeed = MOVE_SPEED * difficulty.getMoveSpeedMultiplier() * speedMultiplier;
        float previousX = position.x;
        float previousZ = position.z;
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
        updateVisualTimers(delta, previousX, previousZ);
        updateTransform();
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        if (splatted) {
            return;
        }
        modelBatch.render(bodyInstance, environment);
        modelBatch.render(headInstance, environment);
        modelBatch.render(noseInstance, environment);
        modelBatch.render(markerInstance, environment);
    }

    public void reset(FloorGrid3D floorGrid) {
        hp = MAX_HP;
        splatted = false;
        respawnTimer = 0f;
        invincibleTimer = 0f;
        fireCooldownRemaining = getFireInterval();
        attackMoveTimer = 0f;
        moveBobStrength = 0f;
        recoilTimer = 0f;
        hitFlashTimer = 0f;
        currentState = EnemyState.PAINT;
        setSpawnPosition(floorGrid);
        position.set(spawnPosition);
        chooseDirectionTowardCenter();
        updateTransform();
    }

    public void reset(FloorGrid3D floorGrid, StageConfig3D stageConfig) {
        hp = MAX_HP;
        splatted = false;
        respawnTimer = 0f;
        invincibleTimer = 0f;
        fireCooldownRemaining = getFireInterval();
        attackMoveTimer = 0f;
        moveBobStrength = 0f;
        recoilTimer = 0f;
        hitFlashTimer = 0f;
        currentState = EnemyState.PAINT;
        setSpawnPosition(floorGrid, stageConfig);
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

    public Team3D getTeam() {
        return team;
    }

    public float getBaseY() {
        return position.y - ENEMY_DIAMETER / 2f;
    }

    public String getDisplayName() {
        return displayName;
    }

    public EnemyState getCurrentState() {
        return currentState;
    }

    public WeaponConfig3D getWeaponConfig() {
        return weaponConfig;
    }

    public boolean canShoot() {
        return !splatted && fireCooldownRemaining <= 0f;
    }

    public void consumeShotCooldown() {
        fireCooldownRemaining += getFireInterval();
    }

    public void triggerRecoil() {
        if (!splatted) {
            recoilTimer = RECOIL_DURATION;
        }
    }

    public void triggerHitFlash() {
        if (!splatted) {
            hitFlashTimer = HIT_FLASH_DURATION;
        }
    }

    public Vector3 buildShotDirection(Vector3 outputDirection) {
        outputDirection.set(facingDirection.x, 0f, facingDirection.z);
        if (outputDirection.isZero(0.0001f)) {
            outputDirection.set(moveDirection.x, 0f, moveDirection.z);
        }
        if (outputDirection.isZero(0.0001f)) {
            outputDirection.set(0f, 0f, -1f);
        }

        outputDirection.nor();
        float spreadDegrees = difficulty.getAimSpreadDegrees();
        if (spreadDegrees > 0.001f) {
            outputDirection.rotate(Vector3.Y, MathUtils.random(-spreadDegrees, spreadDegrees));
        }
        return outputDirection.nor();
    }

    public CpuDifficulty3D getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(CpuDifficulty3D newDifficulty) {
        if (newDifficulty != null) {
            difficulty = newDifficulty;
        }
    }

    @Override
    public void dispose() {
        bodyModel.dispose();
        headModel.dispose();
        noseModel.dispose();
        markerModel.dispose();
    }

    private void updateTimers(float delta, FloorGrid3D floorGrid) {
        recoilTimer = Math.max(0f, recoilTimer - delta);
        hitFlashTimer = Math.max(0f, hitFlashTimer - delta);
        if (invincibleTimer > 0f) {
            invincibleTimer = Math.max(0f, invincibleTimer - delta);
        }

        if (!splatted) {
            return;
        }

        respawnTimer = Math.max(0f, respawnTimer - delta);
        if (respawnTimer <= 0f) {
            respawn();
        }
    }

    private void respawn() {
        position.set(spawnPosition);
        hp = MAX_HP;
        splatted = false;
        invincibleTimer = INVINCIBLE_SECONDS;
        fireCooldownRemaining = getFireInterval();
        attackMoveTimer = 0f;
        moveBobStrength = 0f;
        recoilTimer = 0f;
        hitFlashTimer = 0f;
        currentState = EnemyState.PAINT;
        chooseDirectionTowardCenter();
        updateTransform();
    }

    private void updateBehavior(float delta, Vector3 targetPosition, boolean targetable) {
        if (targetable && isTargetInsideAttackRange(targetPosition)) {
            updateAttackBehavior(targetPosition, delta);
            return;
        }

        currentState = EnemyState.PAINT;
        directionChangeTimer -= delta;
        if (directionChangeTimer <= 0f) {
            chooseRandomDirection();
        }
    }

    private boolean isTargetInsideAttackRange(Vector3 targetPosition) {
        float deltaX = targetPosition.x - position.x;
        float deltaZ = targetPosition.z - position.z;
        float attackRange = difficulty.getAttackRange();
        return deltaX * deltaX + deltaZ * deltaZ <= attackRange * attackRange;
    }

    private void updateAttackBehavior(Vector3 targetPosition, float delta) {
        currentState = EnemyState.ATTACK;
        targetDirection.set(targetPosition.x - position.x, 0f, targetPosition.z - position.z);
        if (targetDirection.isZero(0.0001f)) {
            targetDirection.set(facingDirection);
        } else {
            targetDirection.nor();
        }

        float aimAlpha = Math.min(1f, difficulty.getAimTurnSpeed() * delta);
        facingDirection.lerp(targetDirection, aimAlpha).nor();

        float deltaX = targetPosition.x - position.x;
        float deltaZ = targetPosition.z - position.z;
        float distanceToTarget = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        attackMoveTimer -= delta;
        if (attackMoveTimer <= 0f) {
            chooseAttackMoveMode(distanceToTarget);
        }

        updateAttackMoveDirection(distanceToTarget);
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

    private void setSpawnPosition(FloorGrid3D floorGrid, StageConfig3D stageConfig) {
        float radius = ENEMY_DIAMETER / 2f;
        float minX = floorGrid.getMinX() + radius;
        float maxX = floorGrid.getMaxX() - radius;
        float minZ = floorGrid.getMinZ() + radius;
        float maxZ = floorGrid.getMaxZ() - radius;

        spawnPosition.set(
            MathUtils.clamp(stageConfig.getCpuSpawnX(spawnIndex), minX, maxX),
            radius,
            MathUtils.clamp(stageConfig.getCpuSpawnZ(spawnIndex), minZ, maxZ)
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

    private void chooseAttackMoveMode(float distanceToTarget) {
        float random = MathUtils.random();
        float paintChance = difficulty.getPaintBehaviorChance();

        if (distanceToTarget < RETREAT_RANGE) {
            currentAttackMoveMode = random < 0.55f ? AttackMoveMode.RETREAT : getRandomStrafeMode();
        } else if (distanceToTarget > APPROACH_RANGE) {
            if (random < 0.62f) {
                currentAttackMoveMode = AttackMoveMode.APPROACH;
            } else if (random < 0.62f + paintChance) {
                currentAttackMoveMode = AttackMoveMode.PAINT;
            } else {
                currentAttackMoveMode = getRandomStrafeMode();
            }
        } else {
            if (random < paintChance) {
                currentAttackMoveMode = AttackMoveMode.PAINT;
            } else if (random < 0.58f) {
                currentAttackMoveMode = getRandomStrafeMode();
            } else if (random < 0.8f) {
                currentAttackMoveMode = AttackMoveMode.APPROACH;
            } else {
                currentAttackMoveMode = AttackMoveMode.RETREAT;
            }
        }

        attackMoveTimer = MathUtils.random(
            difficulty.getAttackDecisionMinSeconds(),
            difficulty.getAttackDecisionMaxSeconds()
        );
    }

    private AttackMoveMode getRandomStrafeMode() {
        return MathUtils.randomBoolean() ? AttackMoveMode.STRAFE_LEFT : AttackMoveMode.STRAFE_RIGHT;
    }

    private void updateAttackMoveDirection(float distanceToTarget) {
        switch (currentAttackMoveMode) {
            case RETREAT:
                retreatDirection.set(targetDirection).scl(-1f);
                applyAttackDirection(retreatDirection, ATTACK_DIRECTION_JITTER_DEGREES);
                break;
            case APPROACH:
                applyAttackDirection(targetDirection, ATTACK_DIRECTION_JITTER_DEGREES);
                break;
            case STRAFE_LEFT:
                sidestepDirection.set(-targetDirection.z, 0f, targetDirection.x);
                if (distanceToTarget < (RETREAT_RANGE + APPROACH_RANGE) * 0.5f) {
                    sidestepDirection.mulAdd(targetDirection, -ATTACK_STRAFE_BACKWARD_BLEND);
                } else {
                    sidestepDirection.mulAdd(targetDirection, ATTACK_STRAFE_FORWARD_BLEND);
                }
                applyAttackDirection(sidestepDirection, ATTACK_DIRECTION_JITTER_DEGREES * 0.6f);
                break;
            case STRAFE_RIGHT:
                sidestepDirection.set(targetDirection.z, 0f, -targetDirection.x);
                if (distanceToTarget < (RETREAT_RANGE + APPROACH_RANGE) * 0.5f) {
                    sidestepDirection.mulAdd(targetDirection, -ATTACK_STRAFE_BACKWARD_BLEND);
                } else {
                    sidestepDirection.mulAdd(targetDirection, ATTACK_STRAFE_FORWARD_BLEND);
                }
                applyAttackDirection(sidestepDirection, ATTACK_DIRECTION_JITTER_DEGREES * 0.6f);
                break;
            case PAINT:
                attackMoveDirection.set(targetDirection);
                float paintAngle = MathUtils.randomBoolean()
                    ? MathUtils.random(ATTACK_PAINT_WANDER_MIN_DEGREES, ATTACK_PAINT_WANDER_MAX_DEGREES)
                    : -MathUtils.random(ATTACK_PAINT_WANDER_MIN_DEGREES, ATTACK_PAINT_WANDER_MAX_DEGREES);
                attackMoveDirection.rotate(Vector3.Y, paintAngle);
                applyAttackDirection(attackMoveDirection, ATTACK_DIRECTION_JITTER_DEGREES);
                break;
            default:
                applyAttackDirection(targetDirection, ATTACK_DIRECTION_JITTER_DEGREES);
                break;
        }
    }

    private void applyAttackDirection(Vector3 baseDirection, float extraJitterDegrees) {
        attackMoveDirection.set(baseDirection.x, 0f, baseDirection.z);
        if (attackMoveDirection.isZero(0.0001f)) {
            attackMoveDirection.set(targetDirection.x, 0f, targetDirection.z);
        }
        if (attackMoveDirection.isZero(0.0001f)) {
            attackMoveDirection.set(0f, 0f, -1f);
        }
        attackMoveDirection.nor();
        if (extraJitterDegrees > 0.001f) {
            attackMoveDirection.rotate(Vector3.Y, MathUtils.random(-extraJitterDegrees, extraJitterDegrees));
        }
        moveDirection.set(attackMoveDirection).nor();
    }

    private void updateTransform() {
        float facingAngleDegrees = MathUtils.atan2(facingDirection.x, -facingDirection.z) * MathUtils.radiansToDegrees;
        float bobOffset = getMovementBobOffset();
        float recoilOffsetZ = getRecoilOffsetZ();
        float respawnScale = 1f + getRespawnScaleBonus();
        actorTransform.idt();
        actorTransform.translate(position.x, position.y + bobOffset, position.z);
        actorTransform.rotate(Vector3.Y, facingAngleDegrees);
        actorTransform.scale(respawnScale, respawnScale, respawnScale);

        setPartTransform(bodyInstance, 0f, BODY_OFFSET_Y, 0f);
        setPartTransform(headInstance, 0f, HEAD_OFFSET_Y, HEAD_OFFSET_Z);
        setPartTransform(noseInstance, 0f, NOSE_OFFSET_Y, NOSE_OFFSET_Z + recoilOffsetZ);
        setPartTransform(markerInstance, 0f, MARKER_OFFSET_Y, 0f);

        float invincibleFlash = getInvincibleFlash();
        float hitFlash = getHitFlashAmount();
        setInstanceColor(bodyInstance, applyVisualTint(bodyTint, bodyBaseColor, invincibleFlash, hitFlash));
        setInstanceColor(headInstance, applyVisualTint(headTint, headBaseColor, invincibleFlash, hitFlash * 0.8f));
        setInstanceColor(noseInstance, applyVisualTint(noseTint, noseBaseColor, invincibleFlash * 0.5f, hitFlash));
        Color currentMarkerColor = currentState == EnemyState.ATTACK ? attackMarkerColor : markerBaseColor;
        setInstanceColor(markerInstance, applyVisualTint(markerTint, currentMarkerColor, invincibleFlash, hitFlash * 0.6f));
    }

    private void setPartTransform(ModelInstance partInstance, float offsetX, float offsetY, float offsetZ) {
        partInstance.transform.set(actorTransform).translate(offsetX, offsetY, offsetZ);
    }

    private void setInstanceColor(ModelInstance partInstance, Color color) {
        partInstance.materials.get(0).set(ColorAttribute.createDiffuse(color));
    }

    private float getInvincibleFlash() {
        if (invincibleTimer <= 0f) {
            return 0f;
        }
        return 0.25f + 0.28f * (0.5f + 0.5f * MathUtils.sin(invincibleTimer * 22f));
    }

    private Color applyVisualTint(Color tintTarget, Color baseColor, float invincibleFlash, float hitFlash) {
        tintTarget.set(baseColor).lerp(Color.WHITE, invincibleFlash);
        if (hitFlash > 0f) {
            tintTarget.lerp(HIT_FLASH_COLOR, hitFlash);
        }
        return tintTarget;
    }

    private float getMovementBobOffset() {
        if (moveBobStrength <= 0.001f || currentState == EnemyState.RESPAWN) {
            return 0f;
        }
        return MathUtils.sin(moveBobTimer) * MOVE_BOB_AMOUNT * moveBobStrength;
    }

    private float getRecoilOffsetZ() {
        if (recoilTimer <= 0f) {
            return 0f;
        }
        return (recoilTimer / RECOIL_DURATION) * RECOIL_DISTANCE;
    }

    private float getHitFlashAmount() {
        if (hitFlashTimer <= 0f) {
            return 0f;
        }
        return hitFlashTimer / HIT_FLASH_DURATION;
    }

    private float getRespawnScaleBonus() {
        float elapsedSinceRespawn = INVINCIBLE_SECONDS - invincibleTimer;
        if (elapsedSinceRespawn < 0f || elapsedSinceRespawn > RESPAWN_PULSE_DURATION) {
            return 0f;
        }
        float progress = elapsedSinceRespawn / RESPAWN_PULSE_DURATION;
        return (1f - progress) * RESPAWN_SCALE_BONUS;
    }

    private void updateVisualTimers(float delta, float previousX, float previousZ) {
        float movedDistanceSquared = (position.x - previousX) * (position.x - previousX)
            + (position.z - previousZ) * (position.z - previousZ);
        boolean movedThisFrame = movedDistanceSquared > 0.00004f;
        if (movedThisFrame) {
            moveBobTimer += delta * MOVE_BOB_SPEED;
            moveBobStrength = Math.min(1f, moveBobStrength + delta * 6f);
        } else {
            moveBobStrength = Math.max(0f, moveBobStrength - delta * 7f);
        }
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
        if (team.isOwnPaint(groundCellState)) {
            return OWN_PAINT_SPEED_MULTIPLIER;
        }
        if (team.isEnemyPaint(groundCellState)) {
            return ENEMY_PAINT_SPEED_MULTIPLIER;
        }
        return NEUTRAL_SPEED_MULTIPLIER;
    }

    private float getFireInterval() {
        return weaponConfig.getFireInterval() * difficulty.getFireIntervalMultiplier();
    }
}
