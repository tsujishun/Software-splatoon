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
 * Simple player object for the early 3D phases.
 * The player only moves on the floor plane for now.
 */
public class Player3D implements Disposable {
    public static final float MOVE_SPEED = 4.5f;
    public static final float OWN_PAINT_SPEED_MULTIPLIER = 1.15f;
    public static final float ENEMY_PAINT_SPEED_MULTIPLIER = 0.78f;
    public static final float NEUTRAL_SPEED_MULTIPLIER = 1f;
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
    private static final Color PLAYER_COLOR = new Color(0.25f, 0.7f, 0.95f, 1f);
    private static final Color SWIM_COLOR = new Color(0.1f, 0.48f, 0.75f, 1f);
    private static final float SWIM_HEIGHT_SCALE = 0.35f;
    private static final float SWIM_WIDTH_SCALE = 0.92f;
    private static final float SWIM_DEPTH_SCALE = 0.92f;

    private final Model model;
    private final ModelInstance instance;
    private final Vector3 position = new Vector3();
    private final Vector3 spawnPosition = new Vector3();
    private final Vector3 facingDirection = new Vector3(0f, 0f, -1f);
    private final Vector3 moveDirection = new Vector3();
    private final Vector3 sideDirection = new Vector3();

    private int hp;
    private boolean splatted;
    private float respawnTimer;
    private float invincibleTimer;
    private float inkAmount;
    private boolean swimming;

    public Player3D() {
        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(
            PLAYER_WIDTH,
            PLAYER_HEIGHT,
            PLAYER_DEPTH,
            new Material(ColorAttribute.createDiffuse(PLAYER_COLOR)),
            Usage.Position | Usage.Normal
        );
        instance = new ModelInstance(model);
        reset();
    }

    public void update(
        float delta,
        FloorGrid3D floorGrid,
        float moveForward,
        float moveSide,
        Vector3 cameraForward,
        Vector3 cameraRight,
        boolean swimInput
    ) {
        updateTimers(delta);
        if (splatted) {
            return;
        }

        int currentGroundState = floorGrid.getCellStateAtWorldPosition(position.x, position.z);
        updateSwimmingState(swimInput, currentGroundState);

        if (moveForward != 0f || moveSide != 0f) {
            // Convert keyboard input into a direction based on the camera's horizontal view.
            moveDirection.set(cameraForward).scl(moveForward);
            sideDirection.set(cameraRight).scl(moveSide);
            moveDirection.add(sideDirection).nor();

            // Moving on your own paint should feel better, while enemy paint should slow you down.
            float speedMultiplier = getGroundSpeedMultiplier(floorGrid.getCellStateAtWorldPosition(position.x, position.z));
            if (swimming) {
                speedMultiplier *= SWIM_SPEED_MULTIPLIER;
            }
            float moveSpeed = MOVE_SPEED * speedMultiplier;

            position.x += moveDirection.x * moveSpeed * delta;
            position.z += moveDirection.z * moveSpeed * delta;
        }

        // Keep the player inside the floor so the early 3D prototype is easy to control.
        float halfWidth = PLAYER_WIDTH / 2f;
        float halfDepth = PLAYER_DEPTH / 2f;
        position.x = MathUtils.clamp(position.x, floorGrid.getMinX() + halfWidth, floorGrid.getMaxX() - halfWidth);
        position.z = MathUtils.clamp(position.z, floorGrid.getMinZ() + halfDepth, floorGrid.getMaxZ() - halfDepth);

        int groundStateAfterMove = floorGrid.getCellStateAtWorldPosition(position.x, position.z);
        updateSwimmingState(swimInput, groundStateAfterMove);
        recoverInk(delta, groundStateAfterMove);

        updateTransform();
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        if (splatted) {
            return;
        }
        modelBatch.render(instance, environment);
    }

    public void reset() {
        spawnPosition.set(0f, PLAYER_HEIGHT / 2f, 0f);
        position.set(spawnPosition);
        facingDirection.set(0f, 0f, -1f);
        hp = MAX_HP;
        splatted = false;
        respawnTimer = 0f;
        invincibleTimer = 0f;
        inkAmount = MAX_INK_AMOUNT;
        swimming = false;
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

    public float getRespawnTimer() {
        return respawnTimer;
    }

    public boolean takeHit() {
        if (splatted || isInvincible()) {
            return false;
        }

        hp = Math.max(0, hp - 1);
        if (hp <= 0) {
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
        if (this.swimming == swimming) {
            return;
        }

        this.swimming = swimming;
        updateTransform();
    }

    @Override
    public void dispose() {
        model.dispose();
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
        updateTransform();
    }

    private void updateTransform() {
        float facingAngleDegrees = MathUtils.atan2(facingDirection.x, -facingDirection.z) * MathUtils.radiansToDegrees;
        float heightScale = swimming ? SWIM_HEIGHT_SCALE : 1f;
        float widthScale = swimming ? SWIM_WIDTH_SCALE : 1f;
        float depthScale = swimming ? SWIM_DEPTH_SCALE : 1f;
        float visualY = position.y - PLAYER_HEIGHT * (1f - heightScale) * 0.32f;
        instance.transform.idt();
        instance.transform.translate(position.x, visualY, position.z);
        instance.transform.rotate(Vector3.Y, facingAngleDegrees);
        instance.transform.scale(widthScale, heightScale, depthScale);
        instance.materials.get(0).set(ColorAttribute.createDiffuse(swimming ? SWIM_COLOR : PLAYER_COLOR));
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

    private void updateSwimmingState(boolean swimInput, int groundCellState) {
        boolean canSwim = swimInput && groundCellState == FloorGrid3D.CELL_STATE_PLAYER;
        if (swimming != canSwim) {
            setSwimming(canSwim);
        }
    }
}
