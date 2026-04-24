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
    private static final float SIZE = 0.18f;
    private static final float HEIGHT = 0.25f;
    private static final float SPAWN_FORWARD_OFFSET = 0.55f;
    private static final Color BULLET_COLOR = new Color(1f, 0.82f, 0.25f, 1f);

    private final Model model;
    private final ModelInstance instance;
    private final Vector3 position = new Vector3();
    private final Vector3 direction = new Vector3();
    private final WeaponConfig3D weaponConfig;

    private float traveledDistance;

    public Bullet3D(Vector3 playerPosition, Vector3 facingDirection, WeaponConfig3D weaponConfig) {
        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(
            SIZE,
            SIZE,
            SIZE,
            new Material(ColorAttribute.createDiffuse(BULLET_COLOR)),
            Usage.Position | Usage.Normal
        );
        instance = new ModelInstance(model);
        this.weaponConfig = weaponConfig;

        direction.set(facingDirection).nor();
        position.set(
            playerPosition.x + direction.x * SPAWN_FORWARD_OFFSET,
            HEIGHT,
            playerPosition.z + direction.z * SPAWN_FORWARD_OFFSET
        );
        updateTransform();
    }

    public boolean update(float delta, FloorGrid3D floorGrid) {
        float moveDistance = weaponConfig.getBulletSpeed() * delta;
        float paintStepDistance = Math.max(0.05f, weaponConfig.getPaintRadius() * 0.5f);
        int stepCount = Math.max(1, (int) Math.ceil(moveDistance / paintStepDistance));
        float stepDistance = moveDistance / stepCount;
        boolean isInsideFloor = true;

        paintCurrentTile(floorGrid);

        for (int step = 0; step < stepCount; step++) {
            position.mulAdd(direction, stepDistance);
            traveledDistance += stepDistance;

            if (!isInsideFloorBounds(floorGrid)) {
                isInsideFloor = false;
                break;
            }

            paintCurrentTile(floorGrid);
        }

        updateTransform();

        return traveledDistance < weaponConfig.getRange() && isInsideFloor;
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        modelBatch.render(instance, environment);
    }

    @Override
    public void dispose() {
        model.dispose();
    }

    private boolean isInsideFloorBounds(FloorGrid3D floorGrid) {
        float margin = SIZE / 2f;
        return position.x >= floorGrid.getMinX() - margin
            && position.x <= floorGrid.getMaxX() + margin
            && position.z >= floorGrid.getMinZ() - margin
            && position.z <= floorGrid.getMaxZ() + margin;
    }

    private void paintCurrentTile(FloorGrid3D floorGrid) {
        floorGrid.paintAtWorldPosition(position.x, position.z, weaponConfig.getPaintRadius(), FloorGrid3D.CELL_STATE_PLAYER);
    }

    private void updateTransform() {
        instance.transform.setToTranslation(position);
    }
}
