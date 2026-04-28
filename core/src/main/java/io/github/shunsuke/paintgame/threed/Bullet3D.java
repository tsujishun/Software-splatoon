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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

/**
 * Very simple bullet for the early 3D prototype.
 * It moves in a straight line on the floor plane, paints the tiles below,
 * and disappears after a short distance.
 */
public class Bullet3D implements Disposable {
    public static final int OWNER_PLAYER = 1;
    public static final int OWNER_ENEMY = 2;

    private static final float PLAYER_BULLET_WIDTH = 0.16f;
    private static final float PLAYER_BULLET_HEIGHT = 0.16f;
    private static final float PLAYER_BULLET_DEPTH = 0.34f;
    private static final float ENEMY_BULLET_WIDTH = 0.2f;
    private static final float ENEMY_BULLET_HEIGHT = 0.2f;
    private static final float ENEMY_BULLET_DEPTH = 0.42f;
    private static final float SPAWN_HEIGHT_OFFSET = 0.16f;
    private static final float SPAWN_FORWARD_OFFSET = 0.72f;
    private static final Color PLAYER_BULLET_COLOR = new Color(0.12f, 0.88f, 1f, 1f);
    private static final Color ENEMY_BULLET_COLOR = new Color(1f, 0.3f, 0.62f, 1f);
    private static final Color DEFAULT_BULLET_COLOR = new Color(1f, 0.82f, 0.25f, 1f);

    private final Model model;
    private final ModelInstance instance;
    private final Vector3 position = new Vector3();
    private final Vector3 direction = new Vector3();
    private final WeaponConfig3D weaponConfig;
    private final int ownerType;
    private final int paintCellState;
    private final float collisionRadius;

    private float traveledDistance;

    public Bullet3D(
        Vector3 playerPosition,
        Vector3 facingDirection,
        WeaponConfig3D weaponConfig,
        int ownerType,
        int paintCellState
    ) {
        ModelBuilder modelBuilder = new ModelBuilder();
        float bulletWidth = getBulletWidth(paintCellState);
        float bulletHeight = getBulletHeight(paintCellState);
        float bulletDepth = getBulletDepth(paintCellState);
        model = modelBuilder.createBox(
            bulletWidth,
            bulletHeight,
            bulletDepth,
            new Material(ColorAttribute.createDiffuse(getBulletColor(paintCellState))),
            Usage.Position | Usage.Normal
        );
        instance = new ModelInstance(model);
        this.weaponConfig = weaponConfig;
        this.ownerType = ownerType;
        this.paintCellState = paintCellState;
        this.collisionRadius = Math.max(bulletWidth, bulletDepth) / 2f;

        // Only use the horizontal part of the aim so the bullet stays on the floor plane for now.
        direction.set(facingDirection.x, 0f, facingDirection.z);
        if (direction.isZero(0.0001f)) {
            direction.set(0f, 0f, -1f);
        } else {
            direction.nor();
        }
        position.set(
            playerPosition.x + direction.x * SPAWN_FORWARD_OFFSET,
            playerPosition.y + SPAWN_HEIGHT_OFFSET,
            playerPosition.z + direction.z * SPAWN_FORWARD_OFFSET
        );
        updateTransform();
    }

    public boolean update(float delta, FloorGrid3D floorGrid, StageObstacles3D stageObstacles) {
        float moveDistance = weaponConfig.getBulletSpeed() * delta;
        float paintStepDistance = Math.max(0.05f, weaponConfig.getPaintRadius() * 0.5f);
        int stepCount = Math.max(1, (int) Math.ceil(moveDistance / paintStepDistance));
        float stepDistance = moveDistance / stepCount;
        boolean isInsideFloor = true;
        boolean hitObstacle = false;

        paintCurrentTile(floorGrid);

        for (int step = 0; step < stepCount; step++) {
            float nextX = position.x + direction.x * stepDistance;
            float nextZ = position.z + direction.z * stepDistance;

            if (stageObstacles.collidesCircle(nextX, nextZ, collisionRadius)) {
                hitObstacle = true;
                break;
            }

            position.set(nextX, position.y, nextZ);
            traveledDistance += stepDistance;

            if (!isInsideFloorBounds(floorGrid)) {
                isInsideFloor = false;
                break;
            }

            paintCurrentTile(floorGrid);
        }

        updateTransform();

        return traveledDistance < weaponConfig.getRange() && isInsideFloor && !hitObstacle;
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        modelBatch.render(instance, environment);
    }

    public Vector3 getPosition() {
        return position;
    }

    public int getOwnerType() {
        return ownerType;
    }

    public int getPaintCellState() {
        return paintCellState;
    }

    @Override
    public void dispose() {
        model.dispose();
    }

    private boolean isInsideFloorBounds(FloorGrid3D floorGrid) {
        float margin = Math.max(getBulletWidth(paintCellState), getBulletDepth(paintCellState)) / 2f;
        return position.x >= floorGrid.getMinX() - margin
            && position.x <= floorGrid.getMaxX() + margin
            && position.z >= floorGrid.getMinZ() - margin
            && position.z <= floorGrid.getMaxZ() + margin;
    }

    private void paintCurrentTile(FloorGrid3D floorGrid) {
        floorGrid.paintAtWorldPosition(position.x, position.z, weaponConfig.getPaintRadius(), paintCellState);
    }

    private void updateTransform() {
        float facingAngleDegrees = (float) Math.toDegrees(Math.atan2(direction.x, -direction.z));
        instance.transform.idt();
        instance.transform.translate(position);
        instance.transform.rotate(Vector3.Y, facingAngleDegrees);
    }

    private Color getBulletColor(int cellState) {
        if (cellState == FloorGrid3D.CELL_STATE_PLAYER) {
            return PLAYER_BULLET_COLOR;
        }
        if (cellState == FloorGrid3D.CELL_STATE_ENEMY) {
            return ENEMY_BULLET_COLOR;
        }
        return DEFAULT_BULLET_COLOR;
    }

    private float getBulletWidth(int cellState) {
        if (cellState == FloorGrid3D.CELL_STATE_ENEMY) {
            return ENEMY_BULLET_WIDTH;
        }
        return PLAYER_BULLET_WIDTH;
    }

    private float getBulletHeight(int cellState) {
        if (cellState == FloorGrid3D.CELL_STATE_ENEMY) {
            return ENEMY_BULLET_HEIGHT;
        }
        return PLAYER_BULLET_HEIGHT;
    }

    private float getBulletDepth(int cellState) {
        if (cellState == FloorGrid3D.CELL_STATE_ENEMY) {
            return ENEMY_BULLET_DEPTH;
        }
        return PLAYER_BULLET_DEPTH;
    }
}
