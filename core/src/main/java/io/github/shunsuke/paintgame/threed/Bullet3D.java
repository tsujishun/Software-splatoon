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
 * It moves in a straight line on the floor plane and disappears after a short distance.
 */
public class Bullet3D implements Disposable {
    public static final float SPEED = 9f;
    public static final float MAX_TRAVEL_DISTANCE = 8f;
    public static final float SIZE = 0.18f;

    private static final float HEIGHT = 0.25f;
    private static final float SPAWN_FORWARD_OFFSET = 0.55f;
    private static final Color BULLET_COLOR = new Color(1f, 0.82f, 0.25f, 1f);

    private final Model model;
    private final ModelInstance instance;
    private final Vector3 position = new Vector3();
    private final Vector3 direction = new Vector3();

    private float traveledDistance;

    public Bullet3D(Vector3 playerPosition, Vector3 facingDirection) {
        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(
            SIZE,
            SIZE,
            SIZE,
            new Material(ColorAttribute.createDiffuse(BULLET_COLOR)),
            Usage.Position | Usage.Normal
        );
        instance = new ModelInstance(model);

        direction.set(facingDirection).nor();
        position.set(
            playerPosition.x + direction.x * SPAWN_FORWARD_OFFSET,
            HEIGHT,
            playerPosition.z + direction.z * SPAWN_FORWARD_OFFSET
        );
        updateTransform();
    }

    public boolean update(float delta, FloorGrid3D floorGrid) {
        float moveDistance = SPEED * delta;
        position.mulAdd(direction, moveDistance);
        traveledDistance += moveDistance;
        updateTransform();

        return traveledDistance < MAX_TRAVEL_DISTANCE && isInsideFloorBounds(floorGrid);
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

    private void updateTransform() {
        instance.transform.setToTranslation(position);
    }
}
