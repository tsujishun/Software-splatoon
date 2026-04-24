package io.github.shunsuke.paintgame.threed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

    private static final float PLAYER_WIDTH = 0.6f;
    private static final float PLAYER_HEIGHT = 0.9f;
    private static final float PLAYER_DEPTH = 0.6f;
    private static final Color PLAYER_COLOR = new Color(0.25f, 0.7f, 0.95f, 1f);

    private final Model model;
    private final ModelInstance instance;
    private final Vector3 position = new Vector3();
    private final Vector3 facingDirection = new Vector3(0f, 0f, -1f);
    private final Vector3 moveDirection = new Vector3();
    private final Vector3 sideDirection = new Vector3();

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

    public void update(float delta, FloorGrid3D floorGrid, Vector3 cameraForward, Vector3 cameraRight) {
        float moveForward = 0f;
        float moveSide = 0f;

        if (isLeftPressed()) {
            moveSide -= 1f;
        }
        if (isRightPressed()) {
            moveSide += 1f;
        }
        if (isForwardPressed()) {
            moveForward += 1f;
        }
        if (isBackwardPressed()) {
            moveForward -= 1f;
        }

        if (moveForward != 0f || moveSide != 0f) {
            // Convert keyboard input into a direction based on the camera's horizontal view.
            moveDirection.set(cameraForward).scl(moveForward);
            sideDirection.set(cameraRight).scl(moveSide);
            moveDirection.add(sideDirection).nor();

            facingDirection.set(moveDirection);
            position.x += moveDirection.x * MOVE_SPEED * delta;
            position.z += moveDirection.z * MOVE_SPEED * delta;
        }

        // Keep the player inside the floor so the early 3D prototype is easy to control.
        float halfWidth = PLAYER_WIDTH / 2f;
        float halfDepth = PLAYER_DEPTH / 2f;
        position.x = MathUtils.clamp(position.x, floorGrid.getMinX() + halfWidth, floorGrid.getMaxX() - halfWidth);
        position.z = MathUtils.clamp(position.z, floorGrid.getMinZ() + halfDepth, floorGrid.getMaxZ() - halfDepth);

        updateTransform();
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        modelBatch.render(instance, environment);
    }

    public void reset() {
        position.set(0f, PLAYER_HEIGHT / 2f, 0f);
        facingDirection.set(0f, 0f, -1f);
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

    private void updateTransform() {
        instance.transform.setToTranslation(position);
    }

    private boolean isLeftPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
    }

    private boolean isRightPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
    }

    private boolean isForwardPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
    }

    private boolean isBackwardPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
    }
}
