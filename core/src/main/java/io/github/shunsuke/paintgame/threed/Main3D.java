package io.github.shunsuke.paintgame.threed;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;

/**
 * Separate 3D entry point.
 * This keeps the finished 2D prototype intact while we build the 3D version in small steps.
 */
public class Main3D implements ApplicationListener {
    private static final String TITLE_TEXT = "Paint Battle 3D Prototype";
    private static final String TITLE_PROMPT_TEXT = "Press Enter to Start";
    private static final String STEP_TEXT = "Step 2: 3D Player Movement";
    private static final String PLAY_TEXT = "WASD / Arrows: Move on the floor";
    private static final String RETURN_TEXT = "Press R to return to the title screen";
    private static final float COUNTDOWN_TOTAL_SECONDS = 4f;
    private static final float CAMERA_DISTANCE = 4.5f;
    private static final float CAMERA_HEIGHT = 2.4f;
    private static final float CAMERA_LOOK_HEIGHT = 0.4f;
    private static final float CAMERA_LOOK_AHEAD = 0.8f;
    private static final float CAMERA_FOLLOW_SPEED = 6f;
    private static final Color CLEAR_COLOR = new Color(0.08f, 0.1f, 0.14f, 1f);

    private ModelBatch modelBatch;
    private Environment environment;
    private PerspectiveCamera worldCamera;
    private OrthographicCamera hudCamera;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private FloorGrid3D floorGrid;
    private Player3D player;
    private final Vector3 cameraTarget = new Vector3();
    private final Vector3 desiredCameraPosition = new Vector3();
    private final Vector3 desiredCameraTarget = new Vector3();

    private GameFlowState flowState;
    private float countdownTimer;

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        glyphLayout = new GlyphLayout();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.85f, 1f));
        environment.add(new DirectionalLight().set(0.7f, 0.7f, 0.75f, -1f, -0.8f, -0.3f));

        floorGrid = new FloorGrid3D(12, 12);
        player = new Player3D();
        flowState = GameFlowState.TITLE;
        countdownTimer = COUNTDOWN_TOTAL_SECONDS;

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        if (worldCamera == null) {
            worldCamera = new PerspectiveCamera(67f, width, height);
        }
        worldCamera.viewportWidth = width;
        worldCamera.viewportHeight = height;

        snapCameraToPlayer();
        worldCamera.near = 0.1f;
        worldCamera.far = 80f;
        worldCamera.update();

        if (hudCamera == null) {
            hudCamera = new OrthographicCamera();
        }
        hudCamera.setToOrtho(false, width, height);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        handleGlobalInput();
        updateFlow(delta);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glClearColor(CLEAR_COLOR.r, CLEAR_COLOR.g, CLEAR_COLOR.b, CLEAR_COLOR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        updateCameraPosition(delta);
        worldCamera.update();
        modelBatch.begin(worldCamera);
        floorGrid.render(modelBatch, environment);
        if (flowState != GameFlowState.TITLE) {
            player.render(modelBatch, environment);
        }
        modelBatch.end();

        hudCamera.update();
        spriteBatch.setProjectionMatrix(hudCamera.combined);
        drawOverlay();
    }

    @Override
    public void pause() {
        // No special pause behavior yet for the 3D prototype.
    }

    @Override
    public void resume() {
        // No special resume behavior yet for the 3D prototype.
    }

    @Override
    public void dispose() {
        if (modelBatch != null) {
            modelBatch.dispose();
        }
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (floorGrid != null) {
            floorGrid.dispose();
        }
        if (player != null) {
            player.dispose();
        }
    }

    private void handleGlobalInput() {
        if (flowState != GameFlowState.TITLE && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            goToTitleScreen();
        }
    }

    private void updateFlow(float delta) {
        if (flowState == GameFlowState.TITLE) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                startCountdown();
            }
            return;
        }

        if (flowState == GameFlowState.COUNTDOWN) {
            countdownTimer -= delta;
            if (countdownTimer <= 0f) {
                countdownTimer = 0f;
                flowState = GameFlowState.PLAYING;
            }
            return;
        }

        if (flowState == GameFlowState.PLAYING) {
            player.update(delta, floorGrid);
        }
    }

    private void drawOverlay() {
        spriteBatch.begin();

        if (flowState == GameFlowState.TITLE) {
            drawCenteredText(TITLE_TEXT, hudCamera.viewportHeight / 2f + 40f);
            drawCenteredText(TITLE_PROMPT_TEXT, hudCamera.viewportHeight / 2f + 8f);
            drawCenteredText(STEP_TEXT, hudCamera.viewportHeight / 2f - 24f);
        } else if (flowState == GameFlowState.COUNTDOWN) {
            drawTopLeftText(STEP_TEXT, 12f, hudCamera.viewportHeight - 12f);
            drawCenteredText(getCountdownText(), hudCamera.viewportHeight / 2f + 12f);
            drawCenteredText("Controls are locked during the countdown.", hudCamera.viewportHeight / 2f - 18f);
            drawCenteredText(RETURN_TEXT, hudCamera.viewportHeight / 2f - 48f);
        } else if (flowState == GameFlowState.PLAYING) {
            drawTopLeftText(STEP_TEXT, 12f, hudCamera.viewportHeight - 12f);
            drawTopLeftText(PLAY_TEXT, 12f, hudCamera.viewportHeight - 34f);
            drawTopLeftText("Move Speed: " + Player3D.MOVE_SPEED, 12f, hudCamera.viewportHeight - 56f);
            drawTopLeftText("Camera: third-person follow", 12f, hudCamera.viewportHeight - 78f);
            drawTopLeftText(RETURN_TEXT, 12f, hudCamera.viewportHeight - 100f);
            drawTopLeftText("Tiles: " + (int) floorGrid.getWorldWidth() + " x " + (int) floorGrid.getWorldDepth(), 12f, hudCamera.viewportHeight - 122f);
            drawTopLeftText(
                String.format("Player Position: %.1f, %.1f", player.getPosition().x, player.getPosition().z),
                12f,
                hudCamera.viewportHeight - 144f
            );
        }

        spriteBatch.end();
    }

    private void drawCenteredText(String text, float y) {
        glyphLayout.setText(font, text);
        float x = (hudCamera.viewportWidth - glyphLayout.width) / 2f;
        font.draw(spriteBatch, glyphLayout, x, y);
    }

    private void drawTopLeftText(String text, float x, float y) {
        font.draw(spriteBatch, text, x, y);
    }

    private void goToTitleScreen() {
        floorGrid.reset();
        player.reset();
        flowState = GameFlowState.TITLE;
        countdownTimer = COUNTDOWN_TOTAL_SECONDS;
        snapCameraToPlayer();
    }

    private void startCountdown() {
        floorGrid.reset();
        player.reset();
        flowState = GameFlowState.COUNTDOWN;
        countdownTimer = COUNTDOWN_TOTAL_SECONDS;
        snapCameraToPlayer();
    }

    private String getCountdownText() {
        if (countdownTimer > 3f) {
            return "3";
        }
        if (countdownTimer > 2f) {
            return "2";
        }
        if (countdownTimer > 1f) {
            return "1";
        }
        return "GO!";
    }

    private void updateCameraPosition(float delta) {
        calculateDesiredCamera();

        float followAlpha = Math.min(1f, CAMERA_FOLLOW_SPEED * delta);
        worldCamera.position.lerp(desiredCameraPosition, followAlpha);
        cameraTarget.lerp(desiredCameraTarget, followAlpha);
        worldCamera.up.set(Vector3.Y);
        worldCamera.lookAt(cameraTarget);
    }

    private void snapCameraToPlayer() {
        calculateDesiredCamera();
        worldCamera.position.set(desiredCameraPosition);
        cameraTarget.set(desiredCameraTarget);
        worldCamera.up.set(Vector3.Y);
        worldCamera.lookAt(cameraTarget);
    }

    private void calculateDesiredCamera() {
        Vector3 playerPosition = player.getPosition();
        Vector3 facingDirection = player.getFacingDirection();

        desiredCameraPosition.set(
            playerPosition.x - facingDirection.x * CAMERA_DISTANCE,
            playerPosition.y + CAMERA_HEIGHT,
            playerPosition.z - facingDirection.z * CAMERA_DISTANCE
        );

        desiredCameraTarget.set(
            playerPosition.x + facingDirection.x * CAMERA_LOOK_AHEAD,
            playerPosition.y + CAMERA_LOOK_HEIGHT,
            playerPosition.z + facingDirection.z * CAMERA_LOOK_AHEAD
        );
    }
}
