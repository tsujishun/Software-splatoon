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
 * Beginner-friendly player object for the 3D prototype.
 * The look stays simple, but it now uses a few primitive parts so the facing direction is easier to read.
 */
public class Player3D implements Disposable {
    public static final float MOVE_SPEED = 4.5f;
    public static final float OWN_PAINT_SPEED_MULTIPLIER = 1.15f;
    public static final float ENEMY_PAINT_SPEED_MULTIPLIER = 0.78f;
    public static final float NEUTRAL_SPEED_MULTIPLIER = 1f;
    public static final float JUMP_VELOCITY = 5.6f;
    public static final float GRAVITY = 15f;
    public static final float MAX_INK_AMOUNT = 100f;
    public static final float OWN_PAINT_INK_RECOVERY_PER_SECOND = 28f;
    public static final float SWIM_OWN_PAINT_INK_RECOVERY_PER_SECOND = 54f;
    public static final float NEUTRAL_INK_RECOVERY_PER_SECOND = 0f;
    public static final float ENEMY_PAINT_INK_RECOVERY_PER_SECOND = 0f;
    public static final float SWIM_SPEED_MULTIPLIER = 1.4f;
    public static final int MAX_HP = 3;
    public static final float HIT_RADIUS = 0.42f;
    public static final float RESPAWN_SECONDS = 2.2f;
    public static final float INVINCIBLE_SECONDS = 1.2f;

    private static final float PLAYER_WIDTH = 0.52f;
    private static final float PLAYER_HEIGHT = 0.9f;
    private static final float PLAYER_DEPTH = 0.78f;
    private static final float COLLISION_RADIUS = 0.34f;
    private static final Color PLAYER_COLOR = new Color(0.25f, 0.7f, 0.95f, 1f);
    private static final Color PLAYER_ACCENT_COLOR = new Color(0.84f, 0.95f, 1f, 1f);
    private static final Color PLAYER_NOSE_COLOR = new Color(0.08f, 0.18f, 0.32f, 1f);
    private static final Color PLAYER_PACK_COLOR = new Color(0.13f, 0.47f, 0.76f, 1f);
    private static final Color SWIM_COLOR = new Color(0.1f, 0.48f, 0.75f, 1f);
    private static final Color SWIM_ACCENT_COLOR = new Color(0.55f, 0.82f, 0.98f, 1f);
    private static final Color SWIM_NOSE_COLOR = new Color(0.05f, 0.12f, 0.2f, 1f);
    private static final Color SWIM_PACK_COLOR = new Color(0.07f, 0.32f, 0.52f, 1f);
    private static final float SWIM_HEIGHT_SCALE = 0.35f;
    private static final float SWIM_WIDTH_SCALE = 0.92f;
    private static final float SWIM_DEPTH_SCALE = 0.92f;
    private static final float GROUND_Y = PLAYER_HEIGHT / 2f;
    private static final float BODY_OFFSET_Y = -0.16f;
    private static final float CANOPY_OFFSET_Y = 0.18f;
    private static final float CANOPY_OFFSET_Z = -0.05f;
    private static final float NOSE_OFFSET_Y = -0.02f;
    private static final float NOSE_OFFSET_Z = -0.47f;
    private static final float PACK_OFFSET_Y = 0.03f;
    private static final float PACK_OFFSET_Z = 0.29f;

    private final Model bodyModel;
    private final Model canopyModel;
    private final Model noseModel;
    private final Model packModel;
    private final ModelInstance bodyInstance;
    private final ModelInstance canopyInstance;
    private final ModelInstance noseInstance;
    private final ModelInstance packInstance;
    private final Vector3 position = new Vector3();
    private final Vector3 spawnPosition = new Vector3();
    private final Vector3 facingDirection = new Vector3(0f, 0f, -1f);
    private final Vector3 moveDirection = new Vector3();
    private final Vector3 sideDirection = new Vector3();
    private final Vector3 actualMoveDirection = new Vector3();
    private final Matrix4 actorTransform = new Matrix4();
    private final Color bodyTint = new Color();
    private final Color canopyTint = new Color();
    private final Color noseTint = new Color();
    private final Color packTint = new Color();

    private int hp;
    private boolean splatted;
    private float respawnTimer;
    private float invincibleTimer;
    private float inkAmount;
    private boolean swimming;
    private float verticalVelocity;
    private boolean grounded;

    public Player3D() {
        ModelBuilder modelBuilder = new ModelBuilder();
        bodyModel = modelBuilder.createBox(
            0.58f,
            0.56f,
            0.78f,
            new Material(ColorAttribute.createDiffuse(PLAYER_COLOR)),
            Usage.Position | Usage.Normal
        );
        canopyModel = modelBuilder.createSphere(
            0.34f,
            0.32f,
            0.34f,
            16,
            16,
            new Material(ColorAttribute.createDiffuse(PLAYER_ACCENT_COLOR)),
            Usage.Position | Usage.Normal
        );
        noseModel = modelBuilder.createBox(
            0.16f,
            0.18f,
            0.26f,
            new Material(ColorAttribute.createDiffuse(PLAYER_NOSE_COLOR)),
            Usage.Position | Usage.Normal
        );
        packModel = modelBuilder.createBox(
            0.26f,
            0.34f,
            0.18f,
            new Material(ColorAttribute.createDiffuse(PLAYER_PACK_COLOR)),
            Usage.Position | Usage.Normal
        );
        bodyInstance = new ModelInstance(bodyModel);
        canopyInstance = new ModelInstance(canopyModel);
        noseInstance = new ModelInstance(noseModel);
        packInstance = new ModelInstance(packModel);
        reset();
    }

    public void update(
        float delta,
        FloorGrid3D floorGrid,
        float moveForward,
        float moveSide,
        Vector3 cameraForward,
        Vector3 cameraRight,
        boolean swimInput,
        boolean jumpJustPressed,
        boolean shootHeld,
        StageObstacles3D stageObstacles
    ) {
        updateTimers(delta);
        if (splatted) {
            return;
        }

        int currentGroundState = getCurrentGroundCellState(floorGrid, stageObstacles);
        updateSwimmingState(swimInput, shootHeld, currentGroundState);
        if (jumpJustPressed) {
            // Jumping is only allowed from the floor, so airborne double-jumps never happen.
            tryJump();
        }

        float previousX = position.x;
        float previousZ = position.z;
        if (moveForward != 0f || moveSide != 0f) {
            // Convert keyboard input into a direction based on the camera's horizontal view.
            moveDirection.set(cameraForward).scl(moveForward);
            sideDirection.set(cameraRight).scl(moveSide);
            moveDirection.add(sideDirection).nor();

            // Moving on your own paint should feel better, while enemy paint should slow you down.
            float speedMultiplier = getGroundSpeedMultiplier(currentGroundState);
            if (swimming) {
                speedMultiplier *= SWIM_SPEED_MULTIPLIER;
            }
            float moveSpeed = MOVE_SPEED * speedMultiplier;
            moveWithObstacleCollision(moveSpeed * delta, floorGrid, stageObstacles);
        }

        // Keep the player inside the floor so the early 3D prototype is easy to control.
        float halfWidth = PLAYER_WIDTH / 2f;
        float halfDepth = PLAYER_DEPTH / 2f;
        position.x = MathUtils.clamp(position.x, floorGrid.getMinX() + halfWidth, floorGrid.getMaxX() - halfWidth);
        position.z = MathUtils.clamp(position.z, floorGrid.getMinZ() + halfDepth, floorGrid.getMaxZ() - halfDepth);
        updateFacingDirection(previousX, previousZ, cameraForward, shootHeld);

        updateVerticalMotion(delta, stageObstacles);

        int groundStateAfterMove = getCurrentGroundCellState(floorGrid, stageObstacles);
        updateSwimmingState(swimInput, shootHeld, groundStateAfterMove);
        recoverInk(delta, groundStateAfterMove);

        updateTransform();
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        if (splatted) {
            return;
        }
        modelBatch.render(bodyInstance, environment);
        modelBatch.render(canopyInstance, environment);
        modelBatch.render(noseInstance, environment);
        modelBatch.render(packInstance, environment);
    }

    public void reset() {
        spawnPosition.set(0f, GROUND_Y, 0f);
        position.set(spawnPosition);
        facingDirection.set(0f, 0f, -1f);
        hp = MAX_HP;
        splatted = false;
        respawnTimer = 0f;
        invincibleTimer = 0f;
        inkAmount = MAX_INK_AMOUNT;
        swimming = false;
        verticalVelocity = 0f;
        grounded = true;
        updateTransform();
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(Vector3 direction) {
        if (!direction.isZero(0.0001f)) {
            facingDirection.set(direction).nor();
        }
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

    public boolean isGrounded() {
        return grounded;
    }

    public boolean isOnRaisedPlatform(StageObstacles3D stageObstacles) {
        return getSupportHeight(stageObstacles) > 0.001f;
    }

    public float getRespawnTimer() {
        return respawnTimer;
    }

    public boolean takeHit() {
        if (splatted || isInvincible()) {
            return false;
        }

        hp = Math.max(0, hp - 1);
        if (hp <= 0) {
            position.y = GROUND_Y;
            verticalVelocity = 0f;
            grounded = true;
            swimming = false;
            splatted = true;
            respawnTimer = RESPAWN_SECONDS;
            return true;
        }
        return false;
    }

    public float getInkAmount() {
        return inkAmount;
    }

    public boolean hasEnoughInk(float inkCost) {
        return inkAmount >= inkCost;
    }

    public boolean consumeInk(float inkCost) {
        if (!hasEnoughInk(inkCost)) {
            return false;
        }

        inkAmount = Math.max(0f, inkAmount - inkCost);
        return true;
    }

    public boolean isSwimming() {
        return swimming;
    }

    public void setSwimming(boolean swimming) {
        if (swimming && !grounded) {
            return;
        }

        if (this.swimming == swimming) {
            return;
        }

        this.swimming = swimming;
        updateTransform();
    }

    public void tryJump() {
        if (splatted || swimming || !grounded) {
            return;
        }

        grounded = false;
        verticalVelocity = JUMP_VELOCITY;
    }

    @Override
    public void dispose() {
        bodyModel.dispose();
        canopyModel.dispose();
        noseModel.dispose();
        packModel.dispose();
    }

    private void updateTimers(float delta) {
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
        inkAmount = MAX_INK_AMOUNT;
        swimming = false;
        verticalVelocity = 0f;
        grounded = true;
        updateTransform();
    }

    private void updateTransform() {
        // The player model is built facing local -Z, so the Y rotation needs the opposite sign
        // to keep the nose on the true front side for left and right movement as well.
        float facingAngleDegrees = -MathUtils.atan2(facingDirection.x, -facingDirection.z) * MathUtils.radiansToDegrees;
        float heightScale = swimming ? SWIM_HEIGHT_SCALE : 1f;
        float widthScale = swimming ? SWIM_WIDTH_SCALE : 1f;
        float depthScale = swimming ? SWIM_DEPTH_SCALE : 1f;
        float visualY = position.y - PLAYER_HEIGHT * (1f - heightScale) * 0.32f;
        actorTransform.idt();
        actorTransform.translate(position.x, visualY, position.z);
        actorTransform.rotate(Vector3.Y, facingAngleDegrees);
        actorTransform.scale(widthScale, heightScale, depthScale);

        setPartTransform(bodyInstance, 0f, BODY_OFFSET_Y, 0f);
        setPartTransform(canopyInstance, 0f, CANOPY_OFFSET_Y, CANOPY_OFFSET_Z);
        setPartTransform(noseInstance, 0f, NOSE_OFFSET_Y, NOSE_OFFSET_Z);
        setPartTransform(packInstance, 0f, PACK_OFFSET_Y, PACK_OFFSET_Z);

        float invincibleFlash = getInvincibleFlash();
        setInstanceColor(bodyInstance, bodyTint.set(swimming ? SWIM_COLOR : PLAYER_COLOR).lerp(Color.WHITE, invincibleFlash));
        setInstanceColor(canopyInstance, canopyTint.set(swimming ? SWIM_ACCENT_COLOR : PLAYER_ACCENT_COLOR).lerp(Color.WHITE, invincibleFlash));
        setInstanceColor(noseInstance, noseTint.set(swimming ? SWIM_NOSE_COLOR : PLAYER_NOSE_COLOR).lerp(Color.WHITE, invincibleFlash * 0.6f));
        setInstanceColor(packInstance, packTint.set(swimming ? SWIM_PACK_COLOR : PLAYER_PACK_COLOR).lerp(Color.WHITE, invincibleFlash));
    }

    private void setPartTransform(ModelInstance partInstance, float offsetX, float offsetY, float offsetZ) {
        partInstance.transform.set(actorTransform).translate(offsetX, offsetY, offsetZ);
    }

    private void setInstanceColor(ModelInstance partInstance, Color color) {
        partInstance.materials.get(0).set(ColorAttribute.createDiffuse(color));
    }

    private void updateFacingDirection(float previousX, float previousZ, Vector3 cameraForward, boolean shootHeld) {
        if (shootHeld) {
            setFacingDirection(cameraForward);
            return;
        }

        actualMoveDirection.set(position.x - previousX, 0f, position.z - previousZ);
        if (!actualMoveDirection.isZero(0.0001f)) {
            facingDirection.set(actualMoveDirection).nor();
        }
    }

    private float getInvincibleFlash() {
        if (invincibleTimer <= 0f) {
            return 0f;
        }
        return 0.28f + 0.3f * (0.5f + 0.5f * MathUtils.sin(invincibleTimer * 24f));
    }

    private void moveWithObstacleCollision(float moveDistance, FloorGrid3D floorGrid, StageObstacles3D stageObstacles) {
        float candidateX = MathUtils.clamp(
            position.x + moveDirection.x * moveDistance,
            floorGrid.getMinX() + PLAYER_WIDTH / 2f,
            floorGrid.getMaxX() - PLAYER_WIDTH / 2f
        );
        if (!stageObstacles.collidesCircleForPlayer(candidateX, position.z, COLLISION_RADIUS, getBaseY())) {
            position.x = candidateX;
        }

        float candidateZ = MathUtils.clamp(
            position.z + moveDirection.z * moveDistance,
            floorGrid.getMinZ() + PLAYER_DEPTH / 2f,
            floorGrid.getMaxZ() - PLAYER_DEPTH / 2f
        );
        if (!stageObstacles.collidesCircleForPlayer(position.x, candidateZ, COLLISION_RADIUS, getBaseY())) {
            position.z = candidateZ;
        }
    }

    private int getCurrentGroundCellState(FloorGrid3D floorGrid, StageObstacles3D stageObstacles) {
        if (isOnRaisedPlatform(stageObstacles)) {
            return FloorGrid3D.CELL_STATE_EMPTY;
        }
        return floorGrid.getCellStateAtWorldPosition(position.x, position.z);
    }

    private float getGroundSpeedMultiplier(int groundCellState) {
        if (groundCellState == FloorGrid3D.CELL_STATE_PLAYER) {
            return OWN_PAINT_SPEED_MULTIPLIER;
        }
        if (groundCellState == FloorGrid3D.CELL_STATE_ENEMY) {
            return ENEMY_PAINT_SPEED_MULTIPLIER;
        }
        return NEUTRAL_SPEED_MULTIPLIER;
    }

    private void recoverInk(float delta, int groundCellState) {
        float recoveryPerSecond = getInkRecoveryPerSecond(groundCellState);
        if (recoveryPerSecond <= 0f) {
            return;
        }

        inkAmount = Math.min(MAX_INK_AMOUNT, inkAmount + recoveryPerSecond * delta);
    }

    private float getInkRecoveryPerSecond(int groundCellState) {
        if (swimming && groundCellState == FloorGrid3D.CELL_STATE_PLAYER) {
            return SWIM_OWN_PAINT_INK_RECOVERY_PER_SECOND;
        }
        if (groundCellState == FloorGrid3D.CELL_STATE_PLAYER) {
            return OWN_PAINT_INK_RECOVERY_PER_SECOND;
        }
        if (groundCellState == FloorGrid3D.CELL_STATE_ENEMY) {
            return ENEMY_PAINT_INK_RECOVERY_PER_SECOND;
        }
        return NEUTRAL_INK_RECOVERY_PER_SECOND;
    }

    private void updateSwimmingState(boolean swimInput, boolean shootHeld, int groundCellState) {
        boolean canSwim = !shootHeld && grounded && swimInput && groundCellState == FloorGrid3D.CELL_STATE_PLAYER;
        if (swimming != canSwim) {
            setSwimming(canSwim);
        }
    }

    private void updateVerticalMotion(float delta, StageObstacles3D stageObstacles) {
        if (grounded) {
            float supportHeight = getSupportHeight(stageObstacles);
            float currentBaseY = getBaseY();
            if (Math.abs(currentBaseY - supportHeight) <= 0.06f) {
                position.y = supportHeight + GROUND_Y;
                verticalVelocity = 0f;
                return;
            }

            grounded = false;
        }

        float previousBaseY = getBaseY();
        // A simple gravity model is enough for this early 3D prototype.
        verticalVelocity -= GRAVITY * delta;
        position.y += verticalVelocity * delta;
        float nextBaseY = getBaseY();
        float landingHeight = stageObstacles.getLandingHeightAt(position.x, position.z, COLLISION_RADIUS, previousBaseY, nextBaseY);
        if (landingHeight > Float.NEGATIVE_INFINITY) {
            position.y = landingHeight + GROUND_Y;
            verticalVelocity = 0f;
            grounded = true;
            return;
        }

        if (nextBaseY <= 0f) {
            position.y = GROUND_Y;
            verticalVelocity = 0f;
            grounded = true;
        }
    }

    private float getSupportHeight(StageObstacles3D stageObstacles) {
        return stageObstacles.getSupportHeightAt(position.x, position.z, COLLISION_RADIUS, getBaseY());
    }

    private float getBaseY() {
        return position.y - GROUND_Y;
    }
}
