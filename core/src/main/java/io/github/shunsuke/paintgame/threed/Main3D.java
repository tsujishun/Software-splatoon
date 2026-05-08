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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Separate 3D entry point.
 * This keeps the finished 2D prototype intact while we build the 3D version in small steps.
 */
public class Main3D implements ApplicationListener {
    private static final boolean DEBUG_MODE = false;
    private static final String TITLE_TEXT = "Paint Battle 3D Prototype";
    private static final String TITLE_PROMPT_TEXT = "Press Enter to Start";
    private static final String STEP_TEXT = "Step 27: Simple Sound Effects";
    private static final String TITLE_CONTROL_MOVE_TEXT = "WASD: Move";
    private static final String TITLE_CONTROL_LOOK_TEXT = "Mouse: Look";
    private static final String TITLE_CONTROL_SHOOT_TEXT = "Space: Shoot";
    private static final String TITLE_CONTROL_WEAPON_TEXT = "1 / 2 / 3: Switch Weapon";
    private static final String TITLE_CONTROL_JUMP_TEXT = "J: Jump / Wall Jump";
    private static final String TITLE_CONTROL_SWIM_TEXT = "Shift: Swim on floor / Climb painted walls";
    private static final String TITLE_CONTROL_RETURN_TEXT = "R: Return to Title";
    private static final String TITLE_CONTROL_PAUSE_TEXT = "Esc: Pause / Release Mouse";
    private static final String PLAY_TEXT = "WASD: Move relative to the camera";
    private static final String CAMERA_CONTROL_TEXT = "Move the mouse to control the camera";
    private static final String SHOOT_TEXT = "Space: Shoot in the camera direction";
    private static final String WEAPON_SWITCH_TEXT = "1 / 2 / 3: Switch weapon";
    private static final String PAUSE_TEXT = "Esc: Pause and release the mouse";
    private static final String DEBUG_PAINT_TEXT = "T: Switch paint color (debug)";
    private static final String RETURN_TEXT = "R: Return to Title";
    private static final String PAUSE_RESUME_TEXT = "Press Esc or Enter to resume";
    private static final float COUNTDOWN_TOTAL_SECONDS = 4f;
    private static final float GAME_DURATION_SECONDS = 60f;
    private static final float INK_EMPTY_SOUND_COOLDOWN = 0.22f;
    private static final float CAMERA_DISTANCE = 4.5f;
    private static final float CAMERA_LOOK_HEIGHT = 0.4f;
    private static final float CAMERA_LOOK_AHEAD = 0.8f;
    private static final float CAMERA_FOLLOW_SPEED = 6f;
    private static final float CAMERA_DEFAULT_YAW = 0f;
    private static final float CAMERA_DEFAULT_PITCH = -26f;
    private static final float CAMERA_MOUSE_SENSITIVITY_X = 0.22f;
    private static final float CAMERA_MOUSE_SENSITIVITY_Y = 0.18f;
    private static final float CAMERA_MIN_PITCH = -60f;
    private static final float CAMERA_MAX_PITCH = -15f;
    private static final float CROSSHAIR_GAP = 5f;
    private static final float CROSSHAIR_ARM_LENGTH = 10f;
    private static final float CROSSHAIR_CENTER_RADIUS = 2f;
    private static final float CROSSHAIR_SHADOW_OFFSET = 1f;
    private static final float CROSSHAIR_DOT_RADIUS = 1.5f;
    private static final float STATUS_BAR_WIDTH = 180f;
    private static final float STATUS_BAR_HEIGHT = 14f;
    private static final float STATUS_BAR_MARGIN = 16f;
    private static final float STATUS_PANEL_HEIGHT = 28f;
    private static final float PLAYER_HIT_FLASH_DURATION = 0.22f;
    private static final float ENEMY_HIT_FLASH_DURATION = 0.2f;
    private static final float HIT_MESSAGE_DURATION = 0.65f;
    private static final float SPLAT_MESSAGE_DURATION = 1.1f;
    private static final Color CROSSHAIR_COLOR = new Color(1f, 1f, 1f, 0.9f);
    private static final Color CROSSHAIR_SHADOW_COLOR = new Color(0f, 0f, 0f, 0.55f);
    private static final Color CROSSHAIR_CENTER_COLOR = new Color(0.16f, 0.8f, 1f, 1f);
    private static final Color CLEAR_COLOR = new Color(0.08f, 0.1f, 0.14f, 1f);
    private static final Color PANEL_COLOR = new Color(0f, 0f, 0f, 0.4f);
    private static final Color PANEL_BORDER_COLOR = new Color(1f, 1f, 1f, 0.15f);
    private static final Color PLAYER_HP_BAR_COLOR = new Color(0.22f, 0.95f, 0.38f, 0.95f);
    private static final Color INK_BAR_COLOR = new Color(0.15f, 0.8f, 1f, 0.95f);
    private static final Color ENEMY_HP_BAR_COLOR = new Color(1f, 0.4f, 0.68f, 0.95f);
    private static final Color BAR_BACKGROUND_COLOR = new Color(0f, 0f, 0f, 0.55f);
    private static final Color PLAYER_DAMAGE_FLASH_COLOR = new Color(1f, 0.15f, 0.15f, 0.22f);
    private static final Color FEEDBACK_HIT_COLOR = new Color(1f, 0.92f, 0.35f, 1f);
    private static final Color FEEDBACK_SPLAT_COLOR = new Color(1f, 0.55f, 0.75f, 1f);

    private ModelBatch modelBatch;
    private Environment environment;
    private PerspectiveCamera worldCamera;
    private OrthographicCamera hudCamera;
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private AudioManager3D audioManager;
    private FloorGrid3D floorGrid;
    private StageObstacles3D stageObstacles;
    private Player3D player;
    private EnemyCpu3D enemyCpu;
    private final ArrayList<Bullet3D> bullets = new ArrayList<>();
    private final Vector3 cameraTarget = new Vector3();
    private final Vector3 cameraDirection = new Vector3();
    private final Vector3 cameraMoveForward = new Vector3();
    private final Vector3 cameraMoveRight = new Vector3();
    private final Vector3 desiredCameraPosition = new Vector3();
    private final Vector3 desiredCameraTarget = new Vector3();

    private GameFlowState flowState;
    private float countdownTimer;
    private float remainingTime;
    private WeaponConfig3D playerWeapon;
    private float fireCooldownRemaining;
    private float enemyFireCooldownRemaining;
    private float cameraYaw;
    private float cameraPitch;
    private boolean mouseCaptured;
    private int currentPaintCellState;
    private String resultText;
    private int finalPlayerScore;
    private int finalEnemyScore;
    private int finalTotalTiles;
    private float finalPlayerPaintRate;
    private float finalEnemyPaintRate;
    private int playerSplatCount;
    private int enemySplatCount;
    private float playerHitFlashTimer;
    private float enemyHitFlashTimer;
    private float feedbackMessageTimer;
    private String feedbackMessageText;
    private final Color feedbackMessageColor = new Color(Color.WHITE);
    private int lastCountdownCue;
    private float inkEmptySoundCooldownRemaining;

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        glyphLayout = new GlyphLayout();
        audioManager = new AudioManager3D();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.85f, 1f));
        environment.add(new DirectionalLight().set(0.7f, 0.7f, 0.75f, -1f, -0.8f, -0.3f));

        floorGrid = new FloorGrid3D(12, 12);
        stageObstacles = new StageObstacles3D();
        player = new Player3D();
        enemyCpu = new EnemyCpu3D();
        enemyCpu.reset(floorGrid);
        playerWeapon = WeaponConfig3D.BASIC_SHOOTER;
        flowState = GameFlowState.TITLE;
        countdownTimer = COUNTDOWN_TOTAL_SECONDS;
        remainingTime = GAME_DURATION_SECONDS;
        fireCooldownRemaining = 0f;
        enemyFireCooldownRemaining = getEnemyFireInterval();
        mouseCaptured = false;
        currentPaintCellState = FloorGrid3D.CELL_STATE_PLAYER;
        resetMatchResult();
        playerSplatCount = 0;
        enemySplatCount = 0;
        resetCombatFeedback();
        resetCameraAngles();
        lastCountdownCue = Integer.MIN_VALUE;
        inkEmptySoundCooldownRemaining = 0f;
        setMouseCapture(false);

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
        stageObstacles.render(modelBatch, environment);
        renderBullets();
        if (flowState != GameFlowState.TITLE) {
            player.render(modelBatch, environment);
            enemyCpu.render(modelBatch, environment);
        }
        modelBatch.end();

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        hudCamera.update();
        drawCombatShapes();
        spriteBatch.setProjectionMatrix(hudCamera.combined);
        drawOverlay();
        drawCrosshair();
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
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (audioManager != null) {
            audioManager.dispose();
        }
        if (floorGrid != null) {
            floorGrid.dispose();
        }
        if (stageObstacles != null) {
            stageObstacles.dispose();
        }
        if (player != null) {
            player.dispose();
        }
        if (enemyCpu != null) {
            enemyCpu.dispose();
        }
        clearBullets();
    }

    private void handleGlobalInput() {
        if (flowState != GameFlowState.TITLE && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            goToTitleScreen();
            return;
        }

        if (flowState == GameFlowState.PLAYING && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pauseGame();
            return;
        }

        if (flowState == GameFlowState.PAUSED
            && (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER))) {
            resumeGame();
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
            updateCountdownAudio();
            if (countdownTimer <= 0f) {
                countdownTimer = 0f;
                flowState = GameFlowState.PLAYING;
                setMouseCapture(true);
            }
            return;
        }

        if (flowState == GameFlowState.PAUSED) {
            return;
        }

        if (flowState == GameFlowState.PLAYING) {
            remainingTime = Math.max(0f, remainingTime - delta);
            if (remainingTime <= 0f) {
                finishGame();
                return;
            }

            updateCameraControl();
            updateCameraMovementBasis();
            boolean shootHeld = isShootInputHeld();
            player.update(
                delta,
                floorGrid,
                getMoveForwardInput(),
                getMoveSideInput(),
                cameraMoveForward,
                cameraMoveRight,
                getSwimInput(),
                getJumpInput(),
                shootHeld,
                stageObstacles
            );
            enemyCpu.update(delta, floorGrid, player.getPosition(), !player.isSplatted(), stageObstacles);
            fireCooldownRemaining = Math.max(0f, fireCooldownRemaining - delta);
            enemyFireCooldownRemaining = Math.max(0f, enemyFireCooldownRemaining - delta);
            inkEmptySoundCooldownRemaining = Math.max(0f, inkEmptySoundCooldownRemaining - delta);
            handlePaintColorToggle();
            handleWeaponSwitchInput();
            handleShootingInput();
            handleEnemyShooting();
            updateBullets(delta);
            floorGrid.update(delta);
            updateCombatFeedback(delta);
        }
    }

    private void drawOverlay() {
        spriteBatch.begin();

        if (flowState == GameFlowState.TITLE) {
            drawCenteredText(TITLE_TEXT, hudCamera.viewportHeight / 2f + 40f);
            drawCenteredText(TITLE_PROMPT_TEXT, hudCamera.viewportHeight / 2f + 8f);
            drawCenteredText(STEP_TEXT, hudCamera.viewportHeight / 2f - 24f);
            drawCenteredText(TITLE_CONTROL_MOVE_TEXT, hudCamera.viewportHeight / 2f - 64f);
            drawCenteredText(TITLE_CONTROL_LOOK_TEXT, hudCamera.viewportHeight / 2f - 88f);
            drawCenteredText(TITLE_CONTROL_SHOOT_TEXT, hudCamera.viewportHeight / 2f - 112f);
            drawCenteredText(TITLE_CONTROL_WEAPON_TEXT, hudCamera.viewportHeight / 2f - 136f);
            drawCenteredText(TITLE_CONTROL_JUMP_TEXT, hudCamera.viewportHeight / 2f - 160f);
            drawCenteredText(TITLE_CONTROL_SWIM_TEXT, hudCamera.viewportHeight / 2f - 184f);
            drawCenteredText(TITLE_CONTROL_RETURN_TEXT, hudCamera.viewportHeight / 2f - 208f);
            drawCenteredText(TITLE_CONTROL_PAUSE_TEXT, hudCamera.viewportHeight / 2f - 232f);
        } else if (flowState == GameFlowState.COUNTDOWN) {
            drawTopLeftText(STEP_TEXT, 12f, hudCamera.viewportHeight - 12f);
            drawCenteredText(getCountdownText(), hudCamera.viewportHeight / 2f + 12f);
            drawCenteredText("Controls are locked during the countdown.", hudCamera.viewportHeight / 2f - 18f);
            drawCenteredText(RETURN_TEXT, hudCamera.viewportHeight / 2f - 48f);
        } else if (flowState == GameFlowState.PLAYING) {
            drawTopLeftText(STEP_TEXT, 12f, hudCamera.viewportHeight - 12f);
            drawTopLeftText(String.format("HP %d / %d", player.getHp(), Player3D.MAX_HP), 24f, hudCamera.viewportHeight - 42f);
            drawTopLeftText(String.format("Ink %.0f / %.0f", player.getInkAmount(), Player3D.MAX_INK_AMOUNT), 24f, hudCamera.viewportHeight - 68f);
            drawTopLeftText(String.format("Enemy HP %d / %d", enemyCpu.getHp(), EnemyCpu3D.MAX_HP), hudCamera.viewportWidth - 190f, hudCamera.viewportHeight - 42f);
            drawCenteredText(String.format("Time %d", (int) Math.ceil(remainingTime)), hudCamera.viewportHeight - 18f);
            drawTopLeftText("Weapon: " + playerWeapon.getName(), 12f, hudCamera.viewportHeight - 90f);
            drawTopLeftText("Mode: " + getPlayerModeLabel(), 12f, hudCamera.viewportHeight - 112f);
            drawTopLeftText("Ground: " + getGroundStateLabel(player.getPosition()), 12f, hudCamera.viewportHeight - 134f);
            drawTopLeftText(
                String.format("Player Paint: %d (%.1f%%)", floorGrid.getPlayerPaintedCellCount(), floorGrid.getPlayerPaintRatePercent()),
                12f,
                hudCamera.viewportHeight - 156f
            );
            drawTopLeftText(
                String.format("Enemy Paint: %d (%.1f%%)", floorGrid.getEnemyPaintedCellCount(), floorGrid.getEnemyPaintRatePercent()),
                12f,
                hudCamera.viewportHeight - 178f
            );
            drawTopLeftText(String.format("Splats P / E: %d / %d", playerSplatCount, enemySplatCount), 12f, hudCamera.viewportHeight - 200f);
            drawTopLeftText("J: Jump / Wall Jump   Shift: Swim / Climb on your paint", 12f, hudCamera.viewportHeight - 222f);
            drawTopLeftText(WEAPON_SWITCH_TEXT, 12f, hudCamera.viewportHeight - 244f);
            drawTopLeftText("Esc: Pause   R: Title", 12f, hudCamera.viewportHeight - 266f);
            if (DEBUG_MODE) {
                drawTopLeftText(DEBUG_PAINT_TEXT, 12f, hudCamera.viewportHeight - 288f);
                drawTopLeftText("Current Color: " + getCurrentPaintColorLabel(), 12f, hudCamera.viewportHeight - 310f);
                drawTopLeftText("Enemy AI: " + enemyCpu.getCurrentState(), 12f, hudCamera.viewportHeight - 332f);
                drawTopLeftText("Enemy Weapon: " + enemyCpu.getWeaponConfig().getName(), 12f, hudCamera.viewportHeight - 354f);
            }

            if (feedbackMessageTimer > 0f && feedbackMessageText != null && !feedbackMessageText.isEmpty()) {
                drawCenteredTextWithShadow(feedbackMessageText, hudCamera.viewportHeight / 2f + 76f, feedbackMessageColor);
            }
            if (player.isSplatted()) {
                drawCenteredTextWithShadow("Splatted!", hudCamera.viewportHeight / 2f + 26f, FEEDBACK_SPLAT_COLOR);
                drawCenteredTextWithShadow(
                    String.format("Respawning... %.1f", player.getRespawnTimer()),
                    hudCamera.viewportHeight / 2f - 10f,
                    Color.WHITE
                );
            }
        } else if (flowState == GameFlowState.PAUSED) {
            drawTopLeftText(STEP_TEXT, 12f, hudCamera.viewportHeight - 12f);
            drawTopLeftText(String.format("Player Paint: %d (%.1f%%)", floorGrid.getPlayerPaintedCellCount(), floorGrid.getPlayerPaintRatePercent()), 12f, hudCamera.viewportHeight - 40f);
            drawTopLeftText(String.format("Enemy Paint: %d (%.1f%%)", floorGrid.getEnemyPaintedCellCount(), floorGrid.getEnemyPaintRatePercent()), 12f, hudCamera.viewportHeight - 62f);
            drawTopLeftText(String.format("Player HP: %d / %d", player.getHp(), Player3D.MAX_HP), 12f, hudCamera.viewportHeight - 84f);
            drawTopLeftText(String.format("Ink: %.0f / %.0f", player.getInkAmount(), Player3D.MAX_INK_AMOUNT), 12f, hudCamera.viewportHeight - 106f);
            drawTopLeftText(String.format("Enemy HP: %d / %d", enemyCpu.getHp(), EnemyCpu3D.MAX_HP), 12f, hudCamera.viewportHeight - 128f);
            drawTopLeftText("Weapon: " + playerWeapon.getName(), 12f, hudCamera.viewportHeight - 150f);
            drawTopLeftText(String.format("Splats P / E: %d / %d", playerSplatCount, enemySplatCount), 12f, hudCamera.viewportHeight - 172f);
            drawTopLeftText(String.format("Time: %d", (int) Math.ceil(remainingTime)), hudCamera.viewportWidth - 120f, hudCamera.viewportHeight - 12f);
            drawCenteredText("Paused", hudCamera.viewportHeight / 2f + 24f);
            drawCenteredText(PAUSE_RESUME_TEXT, hudCamera.viewportHeight / 2f - 8f);
            drawCenteredText(RETURN_TEXT, hudCamera.viewportHeight / 2f - 40f);
        } else if (flowState == GameFlowState.GAME_OVER) {
            drawTopLeftText(STEP_TEXT, 12f, hudCamera.viewportHeight - 12f);
            drawTopLeftText("Player: " + finalPlayerScore, 12f, hudCamera.viewportHeight - 34f);
            drawTopLeftText("Enemy: " + finalEnemyScore, 12f, hudCamera.viewportHeight - 56f);
            drawTopLeftText("Player Splats: " + playerSplatCount, 12f, hudCamera.viewportHeight - 78f);
            drawTopLeftText("Enemy Splats: " + enemySplatCount, 12f, hudCamera.viewportHeight - 100f);
            drawTopLeftText("Total: " + finalTotalTiles, 12f, hudCamera.viewportHeight - 122f);
            drawTopLeftText(String.format("Player Paint Rate: %.1f%%", finalPlayerPaintRate), 12f, hudCamera.viewportHeight - 144f);
            drawTopLeftText(String.format("Enemy Paint Rate: %.1f%%", finalEnemyPaintRate), 12f, hudCamera.viewportHeight - 166f);
            drawTopLeftText("Time: 0", hudCamera.viewportWidth - 120f, hudCamera.viewportHeight - 12f);
            drawCenteredText("Game Over", hudCamera.viewportHeight / 2f + 48f);
            drawCenteredText(resultText, hudCamera.viewportHeight / 2f + 16f);
            drawCenteredText(
                String.format("Player %d / Enemy %d", finalPlayerScore, finalEnemyScore),
                hudCamera.viewportHeight / 2f - 16f
            );
            drawCenteredText(
                String.format("Player %.1f%% / Enemy %.1f%%", finalPlayerPaintRate, finalEnemyPaintRate),
                hudCamera.viewportHeight / 2f - 48f
            );
            drawCenteredText(RETURN_TEXT, hudCamera.viewportHeight / 2f - 80f);
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

    private void drawCenteredTextWithShadow(String text, float y, Color color) {
        glyphLayout.setText(font, text);
        float x = (hudCamera.viewportWidth - glyphLayout.width) / 2f;
        float oldR = font.getColor().r;
        float oldG = font.getColor().g;
        float oldB = font.getColor().b;
        float oldA = font.getColor().a;

        font.setColor(0f, 0f, 0f, 0.75f);
        font.draw(spriteBatch, glyphLayout, x + 2f, y - 2f);
        font.setColor(color);
        font.draw(spriteBatch, glyphLayout, x, y);
        font.setColor(oldR, oldG, oldB, oldA);
    }

    private void drawCombatShapes() {
        if (flowState != GameFlowState.PLAYING) {
            return;
        }

        float playerHpX = STATUS_BAR_MARGIN;
        float playerHpY = hudCamera.viewportHeight - 48f;
        float inkX = STATUS_BAR_MARGIN;
        float inkY = hudCamera.viewportHeight - 74f;
        float enemyHpX = hudCamera.viewportWidth - STATUS_BAR_MARGIN - STATUS_BAR_WIDTH;
        float enemyHpY = hudCamera.viewportHeight - 48f;
        float timePanelWidth = 110f;
        float timePanelX = (hudCamera.viewportWidth - timePanelWidth) / 2f;
        float timePanelY = hudCamera.viewportHeight - 44f;

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (playerHitFlashTimer > 0f) {
            float flashAlpha = (playerHitFlashTimer / PLAYER_HIT_FLASH_DURATION) * PLAYER_DAMAGE_FLASH_COLOR.a;
            shapeRenderer.setColor(PLAYER_DAMAGE_FLASH_COLOR.r, PLAYER_DAMAGE_FLASH_COLOR.g, PLAYER_DAMAGE_FLASH_COLOR.b, flashAlpha);
            shapeRenderer.rect(0f, 0f, hudCamera.viewportWidth, hudCamera.viewportHeight);
        }

        shapeRenderer.setColor(PANEL_COLOR);
        shapeRenderer.rect(playerHpX - 8f, playerHpY - 6f, STATUS_BAR_WIDTH + 16f, STATUS_PANEL_HEIGHT);
        shapeRenderer.rect(inkX - 8f, inkY - 6f, STATUS_BAR_WIDTH + 16f, STATUS_PANEL_HEIGHT);
        shapeRenderer.rect(enemyHpX - 8f, enemyHpY - 6f, STATUS_BAR_WIDTH + 16f, STATUS_PANEL_HEIGHT);
        shapeRenderer.rect(timePanelX, timePanelY, timePanelWidth, STATUS_PANEL_HEIGHT);

        if (feedbackMessageTimer > 0f && feedbackMessageText != null && !feedbackMessageText.isEmpty()) {
            shapeRenderer.rect(hudCamera.viewportWidth / 2f - 112f, hudCamera.viewportHeight / 2f + 52f, 224f, 34f);
        }
        if (player.isSplatted()) {
            shapeRenderer.rect(hudCamera.viewportWidth / 2f - 140f, hudCamera.viewportHeight / 2f - 32f, 280f, 88f);
        }

        drawBarFill(playerHpX, playerHpY, STATUS_BAR_WIDTH, STATUS_BAR_HEIGHT, player.getHp() / (float) Player3D.MAX_HP, PLAYER_HP_BAR_COLOR);
        drawBarFill(inkX, inkY, STATUS_BAR_WIDTH, STATUS_BAR_HEIGHT, player.getInkAmount() / Player3D.MAX_INK_AMOUNT, INK_BAR_COLOR);

        Color enemyBarColor = new Color(ENEMY_HP_BAR_COLOR);
        if (enemyHitFlashTimer > 0f) {
            enemyBarColor.lerp(Color.WHITE, Math.min(1f, enemyHitFlashTimer / ENEMY_HIT_FLASH_DURATION));
        }
        drawBarFill(enemyHpX, enemyHpY, STATUS_BAR_WIDTH, STATUS_BAR_HEIGHT, enemyCpu.getHp() / (float) EnemyCpu3D.MAX_HP, enemyBarColor);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(PANEL_BORDER_COLOR);
        shapeRenderer.rect(playerHpX - 8f, playerHpY - 6f, STATUS_BAR_WIDTH + 16f, STATUS_PANEL_HEIGHT);
        shapeRenderer.rect(inkX - 8f, inkY - 6f, STATUS_BAR_WIDTH + 16f, STATUS_PANEL_HEIGHT);
        shapeRenderer.rect(enemyHpX - 8f, enemyHpY - 6f, STATUS_BAR_WIDTH + 16f, STATUS_PANEL_HEIGHT);
        shapeRenderer.rect(timePanelX, timePanelY, timePanelWidth, STATUS_PANEL_HEIGHT);
        if (feedbackMessageTimer > 0f && feedbackMessageText != null && !feedbackMessageText.isEmpty()) {
            shapeRenderer.rect(hudCamera.viewportWidth / 2f - 112f, hudCamera.viewportHeight / 2f + 52f, 224f, 34f);
        }
        if (player.isSplatted()) {
            shapeRenderer.rect(hudCamera.viewportWidth / 2f - 140f, hudCamera.viewportHeight / 2f - 32f, 280f, 88f);
        }
        shapeRenderer.end();
    }

    private void drawBarFill(float x, float y, float width, float height, float ratio, Color fillColor) {
        float clampedRatio = MathUtils.clamp(ratio, 0f, 1f);
        shapeRenderer.setColor(BAR_BACKGROUND_COLOR);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(x, y, width * clampedRatio, height);
    }

    private void drawCrosshair() {
        if (flowState != GameFlowState.PLAYING) {
            return;
        }

        float centerX = hudCamera.viewportWidth / 2f;
        float centerY = hudCamera.viewportHeight / 2f;

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(CROSSHAIR_SHADOW_COLOR);
        shapeRenderer.line(
            centerX - CROSSHAIR_GAP - CROSSHAIR_ARM_LENGTH + CROSSHAIR_SHADOW_OFFSET,
            centerY - CROSSHAIR_SHADOW_OFFSET,
            centerX - CROSSHAIR_GAP + CROSSHAIR_SHADOW_OFFSET,
            centerY - CROSSHAIR_SHADOW_OFFSET
        );
        shapeRenderer.line(
            centerX + CROSSHAIR_GAP + CROSSHAIR_SHADOW_OFFSET,
            centerY - CROSSHAIR_SHADOW_OFFSET,
            centerX + CROSSHAIR_GAP + CROSSHAIR_ARM_LENGTH + CROSSHAIR_SHADOW_OFFSET,
            centerY - CROSSHAIR_SHADOW_OFFSET
        );
        shapeRenderer.line(
            centerX + CROSSHAIR_SHADOW_OFFSET,
            centerY - CROSSHAIR_GAP - CROSSHAIR_ARM_LENGTH - CROSSHAIR_SHADOW_OFFSET,
            centerX + CROSSHAIR_SHADOW_OFFSET,
            centerY - CROSSHAIR_GAP - CROSSHAIR_SHADOW_OFFSET
        );
        shapeRenderer.line(
            centerX + CROSSHAIR_SHADOW_OFFSET,
            centerY + CROSSHAIR_GAP - CROSSHAIR_SHADOW_OFFSET,
            centerX + CROSSHAIR_SHADOW_OFFSET,
            centerY + CROSSHAIR_GAP + CROSSHAIR_ARM_LENGTH - CROSSHAIR_SHADOW_OFFSET
        );
        shapeRenderer.circle(centerX + CROSSHAIR_SHADOW_OFFSET, centerY - CROSSHAIR_SHADOW_OFFSET, CROSSHAIR_CENTER_RADIUS);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(CROSSHAIR_COLOR);
        shapeRenderer.line(centerX - CROSSHAIR_GAP - CROSSHAIR_ARM_LENGTH, centerY, centerX - CROSSHAIR_GAP, centerY);
        shapeRenderer.line(centerX + CROSSHAIR_GAP, centerY, centerX + CROSSHAIR_GAP + CROSSHAIR_ARM_LENGTH, centerY);
        shapeRenderer.line(centerX, centerY - CROSSHAIR_GAP - CROSSHAIR_ARM_LENGTH, centerX, centerY - CROSSHAIR_GAP);
        shapeRenderer.line(centerX, centerY + CROSSHAIR_GAP, centerX, centerY + CROSSHAIR_GAP + CROSSHAIR_ARM_LENGTH);
        shapeRenderer.circle(centerX, centerY, CROSSHAIR_CENTER_RADIUS);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(CROSSHAIR_CENTER_COLOR);
        shapeRenderer.circle(centerX, centerY, CROSSHAIR_DOT_RADIUS);
        shapeRenderer.end();
    }

    private void goToTitleScreen() {
        floorGrid.reset();
        stageObstacles.resetPaint();
        player.reset();
        enemyCpu.reset(floorGrid);
        clearBullets();
        flowState = GameFlowState.TITLE;
        countdownTimer = COUNTDOWN_TOTAL_SECONDS;
        remainingTime = GAME_DURATION_SECONDS;
        playerWeapon = WeaponConfig3D.BASIC_SHOOTER;
        fireCooldownRemaining = 0f;
        enemyFireCooldownRemaining = getEnemyFireInterval();
        currentPaintCellState = FloorGrid3D.CELL_STATE_PLAYER;
        resetMatchResult();
        playerSplatCount = 0;
        enemySplatCount = 0;
        resetCombatFeedback();
        resetCameraAngles();
        lastCountdownCue = Integer.MIN_VALUE;
        inkEmptySoundCooldownRemaining = 0f;
        setMouseCapture(false);
        snapCameraToPlayer();
    }

    private void startCountdown() {
        floorGrid.reset();
        stageObstacles.resetPaint();
        player.reset();
        enemyCpu.reset(floorGrid);
        clearBullets();
        flowState = GameFlowState.COUNTDOWN;
        countdownTimer = COUNTDOWN_TOTAL_SECONDS;
        remainingTime = GAME_DURATION_SECONDS;
        playerWeapon = WeaponConfig3D.BASIC_SHOOTER;
        fireCooldownRemaining = 0f;
        enemyFireCooldownRemaining = getEnemyFireInterval();
        currentPaintCellState = FloorGrid3D.CELL_STATE_PLAYER;
        resetMatchResult();
        playerSplatCount = 0;
        enemySplatCount = 0;
        resetCombatFeedback();
        resetCameraAngles();
        lastCountdownCue = Integer.MIN_VALUE;
        inkEmptySoundCooldownRemaining = 0f;
        setMouseCapture(false);
        snapCameraToPlayer();
    }

    private void pauseGame() {
        player.setSwimming(false);
        flowState = GameFlowState.PAUSED;
        setMouseCapture(false);
    }

    private void resumeGame() {
        flowState = GameFlowState.PLAYING;
        setMouseCapture(true);
    }

    private void finishGame() {
        // Freeze the match result at time-up so the Game Over screen never changes afterward.
        finalPlayerScore = floorGrid.getPlayerPaintedCellCount();
        finalEnemyScore = floorGrid.getEnemyPaintedCellCount();
        finalTotalTiles = floorGrid.getTotalCellCount();
        finalPlayerPaintRate = floorGrid.getPlayerPaintRatePercent();
        finalEnemyPaintRate = floorGrid.getEnemyPaintRatePercent();
        resultText = getResultText(finalPlayerScore, finalEnemyScore);
        remainingTime = 0f;
        player.setSwimming(false);
        flowState = GameFlowState.GAME_OVER;
        clearBullets();
        resetCombatFeedback();
        audioManager.playGameOver();
        setMouseCapture(false);
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

    private void updateCountdownAudio() {
        int countdownCue = getCountdownCueIndex();
        if (countdownCue == lastCountdownCue) {
            return;
        }

        lastCountdownCue = countdownCue;
        if (countdownCue == 0) {
            audioManager.playCountdownGo();
        } else {
            audioManager.playCountdownBeep();
        }
    }

    private int getCountdownCueIndex() {
        if (countdownTimer > 3f) {
            return 3;
        }
        if (countdownTimer > 2f) {
            return 2;
        }
        if (countdownTimer > 1f) {
            return 1;
        }
        return 0;
    }

    private void updateCameraPosition(float delta) {
        calculateDesiredCamera();

        float followAlpha = Math.min(1f, CAMERA_FOLLOW_SPEED * delta);
        worldCamera.position.lerp(desiredCameraPosition, followAlpha);
        cameraTarget.lerp(desiredCameraTarget, followAlpha);
        worldCamera.up.set(Vector3.Y);
        worldCamera.lookAt(cameraTarget);
    }

    private void updateCameraControl() {
        cameraYaw += Gdx.input.getDeltaX() * CAMERA_MOUSE_SENSITIVITY_X;
        cameraPitch -= Gdx.input.getDeltaY() * CAMERA_MOUSE_SENSITIVITY_Y;
        cameraPitch = MathUtils.clamp(cameraPitch, CAMERA_MIN_PITCH, CAMERA_MAX_PITCH);
    }

    private void setMouseCapture(boolean shouldCapture) {
        if (mouseCaptured == shouldCapture) {
            return;
        }

        mouseCaptured = shouldCapture;
        Gdx.input.setCursorCatched(shouldCapture);

        if (shouldCapture) {
            Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
            Gdx.input.getDeltaX();
            Gdx.input.getDeltaY();
        }
    }

    private void handlePaintColorToggle() {
        if (DEBUG_MODE && Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            if (currentPaintCellState == FloorGrid3D.CELL_STATE_PLAYER) {
                currentPaintCellState = FloorGrid3D.CELL_STATE_ENEMY;
            } else {
                currentPaintCellState = FloorGrid3D.CELL_STATE_PLAYER;
            }
        }
    }

    private void handleWeaponSwitchInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            setPlayerWeapon(WeaponConfig3D.BASIC_SHOOTER);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            setPlayerWeapon(WeaponConfig3D.SHORT_PAINTER);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            setPlayerWeapon(WeaponConfig3D.LONG_SHOOTER);
        }
    }

    private void setPlayerWeapon(WeaponConfig3D newWeapon) {
        if (playerWeapon == newWeapon) {
            return;
        }

        playerWeapon = newWeapon;
        fireCooldownRemaining = Math.min(fireCooldownRemaining, playerWeapon.getFireInterval());
        audioManager.playWeaponSwitch();
    }

    private void handleShootingInput() {
        if (player.isSplatted()) {
            return;
        }

        if (player.isSwimming() && Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            player.setSwimming(false);
        }

        // Holding Space should keep firing at the weapon's configured interval.
        while (Gdx.input.isKeyPressed(Input.Keys.SPACE) && fireCooldownRemaining <= 0f) {
            if (!player.consumeInk(playerWeapon.getInkCost())) {
                playInkEmptySoundIfNeeded();
                break;
            }

            // Shooting should make the player visually face the same horizontal direction as the shot.
            player.setFacingDirection(cameraMoveForward);
            audioManager.playPlayerShoot();
            bullets.add(new Bullet3D(
                player.getPosition(),
                cameraMoveForward,
                playerWeapon,
                Bullet3D.OWNER_PLAYER,
                getPlayerPaintCellState()
            ));
            fireCooldownRemaining += playerWeapon.getFireInterval();
        }
    }

    private void handleEnemyShooting() {
        if (enemyCpu.isSplatted()) {
            return;
        }

        while (enemyFireCooldownRemaining <= 0f) {
            WeaponConfig3D enemyWeapon = enemyCpu.getWeaponConfig();
            audioManager.playEnemyShoot();
            bullets.add(new Bullet3D(
                enemyCpu.getPosition(),
                enemyCpu.getFacingDirection(),
                enemyWeapon,
                Bullet3D.OWNER_ENEMY,
                FloorGrid3D.CELL_STATE_ENEMY
            ));
            enemyFireCooldownRemaining += getEnemyFireInterval();
        }
    }

    private void updateBullets(float delta) {
        Iterator<Bullet3D> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet3D bullet = iterator.next();
            boolean isStillActive = bullet.update(delta, floorGrid, stageObstacles);
            boolean hitCharacter = handleBulletCharacterHit(bullet);
            if (!isStillActive || hitCharacter) {
                bullet.dispose();
                iterator.remove();
            }
        }
    }

    private boolean handleBulletCharacterHit(Bullet3D bullet) {
        if (bullet.getOwnerType() == Bullet3D.OWNER_PLAYER) {
            if (!enemyCpu.isSplatted()
                && !enemyCpu.isInvincible()
                && isHitOnFloorPlane(bullet.getPosition(), enemyCpu.getPosition(), enemyCpu.getHitRadius())) {
                enemyHitFlashTimer = ENEMY_HIT_FLASH_DURATION;
                audioManager.playHit();
                if (enemyCpu.takeHit()) {
                    playerSplatCount++;
                    audioManager.playEnemySplatted();
                    showFeedbackMessage("Enemy Splatted!", FEEDBACK_SPLAT_COLOR, SPLAT_MESSAGE_DURATION);
                } else {
                    showFeedbackMessage("Hit!", FEEDBACK_HIT_COLOR, HIT_MESSAGE_DURATION);
                }
                return true;
            }
            return false;
        }

        if (bullet.getOwnerType() == Bullet3D.OWNER_ENEMY) {
            if (!player.isSplatted()
                && !player.isInvincible()
                && isHitOnFloorPlane(bullet.getPosition(), player.getPosition(), player.getHitRadius())) {
                playerHitFlashTimer = PLAYER_HIT_FLASH_DURATION;
                audioManager.playHit();
                if (player.takeHit()) {
                    enemySplatCount++;
                    audioManager.playPlayerSplatted();
                }
                return true;
            }
        }
        return false;
    }

    private void renderBullets() {
        for (Bullet3D bullet : bullets) {
            bullet.render(modelBatch, environment);
        }
    }

    private void clearBullets() {
        for (Bullet3D bullet : bullets) {
            bullet.dispose();
        }
        bullets.clear();
    }

    private boolean isHitOnFloorPlane(Vector3 bulletPosition, Vector3 targetPosition, float hitRadius) {
        float deltaX = bulletPosition.x - targetPosition.x;
        float deltaZ = bulletPosition.z - targetPosition.z;
        return deltaX * deltaX + deltaZ * deltaZ <= hitRadius * hitRadius;
    }

    private void updateCombatFeedback(float delta) {
        playerHitFlashTimer = Math.max(0f, playerHitFlashTimer - delta);
        enemyHitFlashTimer = Math.max(0f, enemyHitFlashTimer - delta);
        feedbackMessageTimer = Math.max(0f, feedbackMessageTimer - delta);
        if (feedbackMessageTimer <= 0f) {
            feedbackMessageText = "";
        }
    }

    private void resetCombatFeedback() {
        playerHitFlashTimer = 0f;
        enemyHitFlashTimer = 0f;
        feedbackMessageTimer = 0f;
        feedbackMessageText = "";
        feedbackMessageColor.set(Color.WHITE);
    }

    private void showFeedbackMessage(String text, Color color, float duration) {
        feedbackMessageText = text;
        feedbackMessageColor.set(color);
        feedbackMessageTimer = duration;
    }

    private void updateCameraMovementBasis() {
        float cosPitch = MathUtils.cosDeg(cameraPitch);
        cameraDirection.set(
            MathUtils.sinDeg(cameraYaw) * cosPitch,
            MathUtils.sinDeg(cameraPitch),
            -MathUtils.cosDeg(cameraYaw) * cosPitch
        ).nor();

        // Ignore the camera's vertical tilt so movement stays on the floor plane.
        cameraMoveForward.set(cameraDirection.x, 0f, cameraDirection.z);
        if (cameraMoveForward.isZero(0.0001f)) {
            cameraMoveForward.set(0f, 0f, -1f);
        } else {
            cameraMoveForward.nor();
        }
        cameraMoveRight.set(cameraMoveForward).crs(Vector3.Y).nor();
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
        desiredCameraPosition.set(
            playerPosition.x + cameraMoveForward.x * CAMERA_LOOK_AHEAD,
            playerPosition.y + CAMERA_LOOK_HEIGHT,
            playerPosition.z + cameraMoveForward.z * CAMERA_LOOK_AHEAD
        );
        desiredCameraTarget.set(desiredCameraPosition);
        desiredCameraPosition.mulAdd(cameraDirection, -CAMERA_DISTANCE);
    }

    private void resetCameraAngles() {
        cameraYaw = CAMERA_DEFAULT_YAW;
        cameraPitch = CAMERA_DEFAULT_PITCH;
        updateCameraMovementBasis();
    }

    private float getMoveForwardInput() {
        float moveForward = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            moveForward += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            moveForward -= 1f;
        }
        return moveForward;
    }

    private float getMoveSideInput() {
        float moveSide = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveSide -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveSide += 1f;
        }
        return moveSide;
    }

    private boolean getSwimInput() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }

    private boolean getJumpInput() {
        return Gdx.input.isKeyJustPressed(Input.Keys.J);
    }

    private boolean isShootInputHeld() {
        return Gdx.input.isKeyPressed(Input.Keys.SPACE);
    }

    private String getCurrentPaintColorLabel() {
        if (currentPaintCellState == FloorGrid3D.CELL_STATE_ENEMY) {
            return "Enemy";
        }
        return "Player";
    }

    private int getPlayerPaintCellState() {
        if (DEBUG_MODE) {
            return currentPaintCellState;
        }
        return FloorGrid3D.CELL_STATE_PLAYER;
    }

    private void playInkEmptySoundIfNeeded() {
        if (inkEmptySoundCooldownRemaining > 0f) {
            return;
        }

        audioManager.playInkEmpty();
        inkEmptySoundCooldownRemaining = INK_EMPTY_SOUND_COOLDOWN;
    }

    private float getEnemyFireInterval() {
        return enemyCpu.getWeaponConfig().getFireInterval() * enemyCpu.getFireIntervalMultiplier();
    }

    private String getGroundStateLabel(Vector3 actorPosition) {
        if (actorPosition == player.getPosition() && player.isOnRaisedPlatform(stageObstacles)) {
            return "Platform";
        }
        int groundState = floorGrid.getCellStateAtWorldPosition(actorPosition.x, actorPosition.z);
        if (groundState == FloorGrid3D.CELL_STATE_PLAYER) {
            return "Player";
        }
        if (groundState == FloorGrid3D.CELL_STATE_ENEMY) {
            return "Enemy";
        }
        return "Neutral";
    }

    private String getPlayerModeLabel() {
        if (player.isClimbing()) {
            return "Climb";
        }
        if (!player.isGrounded()) {
            return "Air";
        }
        if (player.isSwimming()) {
            return "Swim";
        }
        return "Human";
    }

    private void resetMatchResult() {
        resultText = "";
        finalPlayerScore = 0;
        finalEnemyScore = 0;
        finalTotalTiles = floorGrid != null ? floorGrid.getTotalCellCount() : 0;
        finalPlayerPaintRate = 0f;
        finalEnemyPaintRate = 0f;
    }

    private String getResultText(int playerScore, int enemyScore) {
        if (playerScore > enemyScore) {
            return "Player Wins";
        }
        if (enemyScore > playerScore) {
            return "Enemy Wins";
        }
        return "Draw";
    }
}
