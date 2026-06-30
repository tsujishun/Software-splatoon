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
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Separate 3D entry point.
 * This keeps the finished 2D prototype intact while we build the 3D version in small steps.
 */
public class Main3D implements ApplicationListener {
    private enum TitleMenuScreen {
        MAIN,
        WEAPON_SELECT,
        STAGE_SELECT,
        DIFFICULTY,
        CONTROLS
    }

    private static final boolean DEBUG_MODE = false;
    private static final boolean ENABLE_CHARACTER_SHADOWS = true;
    private static final String[] TITLE_MENU_ITEMS = {
        "Start Game",
        "Weapon Select",
        "Stage Select",
        "Difficulty",
        "Controls"
    };
    private static final WeaponConfig3D[] TITLE_MENU_WEAPONS = {
        WeaponConfig3D.BASIC_SHOOTER,
        WeaponConfig3D.SHORT_PAINTER,
        WeaponConfig3D.LONG_SHOOTER
    };
    private static final String TITLE_TEXT = "Paint Battle 3D Prototype";
    private static final String TITLE_PROMPT_TEXT = "Use W/S or Up/Down, Enter to Select";
    private static final String STEP_TEXT = "Step 37: Team Arena & 2v2 Base";
    private static final String DEBUG_PAINT_TEXT = "T: Switch paint color (debug)";
    private static final String RETURN_TEXT = "R: Return to Title";
    private static final float COUNTDOWN_TOTAL_SECONDS = 4f;
    // Lower this during playtests if you want faster balance checks.
    private static final float GAME_DURATION_SECONDS = 60f;
    private static final float INK_EMPTY_SOUND_COOLDOWN = 0.22f;
    private static final float CAMERA_FOV = 69f;
    private static final float CAMERA_DISTANCE = 4.9f;
    private static final float CAMERA_LOOK_HEIGHT = 0.48f;
    private static final float CAMERA_LOOK_AHEAD = 0.92f;
    private static final float CAMERA_FOLLOW_SPEED = 6f;
    private static final float CAMERA_DEFAULT_YAW = 0f;
    private static final float CAMERA_DEFAULT_PITCH = -24f;
    private static final float CAMERA_MOUSE_SENSITIVITY_X = 0.22f;
    private static final float CAMERA_MOUSE_SENSITIVITY_Y = 0.18f;
    private static final float CAMERA_MIN_PITCH = -60f;
    private static final float CAMERA_MAX_PITCH = -15f;
    private static final float CROSSHAIR_GAP = 5f;
    private static final float CROSSHAIR_ARM_LENGTH = 10f;
    private static final float CROSSHAIR_CENTER_RADIUS = 2f;
    private static final float CROSSHAIR_SHADOW_OFFSET = 1f;
    private static final float CROSSHAIR_DOT_RADIUS = 1.5f;
    private static final float HUD_REFERENCE_WIDTH = 1280f;
    private static final float HUD_REFERENCE_HEIGHT = 720f;
    private static final float HUD_MIN_SCALE = 1f;
    private static final float HUD_MAX_SCALE = 1.45f;
    private static final float TOP_PANEL_MARGIN = 18f;
    private static final float TOP_PANEL_GAP = 16f;
    private static final float TEAM_PANEL_WIDTH = 250f;
    private static final float TEAM_PANEL_HEIGHT = 94f;
    private static final float TEAM_PANEL_INNER_PADDING = 16f;
    private static final float TEAM_PANEL_LINE_GAP = 22f;
    private static final float TEAM_PAINT_BAR_HEIGHT = 8f;
    private static final float TIME_PANEL_WIDTH = 176f;
    private static final float TIME_PANEL_HEIGHT = 90f;
    private static final float INFO_PANEL_MARGIN = 20f;
    private static final float INFO_PANEL_WIDTH = 246f;
    private static final float INFO_PANEL_PADDING = 16f;
    private static final float INFO_PANEL_LINE_GAP = 25f;
    private static final float WEAPON_PANEL_MARGIN = 18f;
    private static final float WEAPON_PANEL_WIDTH = 164f;
    private static final float WEAPON_PANEL_HEIGHT = 64f;
    private static final float WEAPON_PANEL_GAP = 14f;
    private static final float WEAPON_TEXT_LINE_GAP = 18f;
    private static final float WEAPON_PANEL_ACCENT_HEIGHT = 5f;
    private static final float TEAM_MARKER_RADIUS = 9f;
    private static final float TEAM_MARKER_Y_OFFSET = 0.95f;
    private static final float TEAM_MARKER_FORWARD_OFFSET = 0.7f;
    private static final float MINIMAP_DIRECTION_LENGTH = 7f;
    private static final float MINIMAP_TRIANGLE_SIZE = 5f;
    private static final float MINIMAP_MARGIN = 18f;
    private static final float MINIMAP_MAX_SIZE = 180f;
    private static final float MINIMAP_PANEL_PADDING = 8f;
    private static final float MINIMAP_MARKER_RADIUS = 4f;
    private static final float PLAYER_HP_GAUGE_WIDTH = 122f;
    private static final float PLAYER_HP_GAUGE_HEIGHT = 15f;
    private static final float PLAYER_HP_GAUGE_OFFSET_Y = 14f;
    private static final float PLAYER_INK_GAUGE_WIDTH = 18f;
    private static final float PLAYER_INK_GAUGE_HEIGHT = 108f;
    private static final float PLAYER_INK_GAUGE_OFFSET_X = 58f;
    private static final float PLAYER_STATUS_CLAMP_MARGIN = 16f;
    private static final float PLAYER_STATUS_LABEL_GAP = 7f;
    private static final float PLAYER_STATUS_TEXT_SCALE = 1.08f;
    private static final float HUD_SCALE_TITLE = 1.88f;
    private static final float HUD_SCALE_BODY = 1.28f;
    private static final float HUD_SCALE_SMALL = 1.1f;
    private static final float HUD_SCALE_TINY = 0.98f;
    private static final int MAX_INK_PARTICLES = 96;
    private static final int IMPACT_PARTICLE_COUNT = 7;
    private static final float IMPACT_PARTICLE_MIN_LIFETIME = 0.22f;
    private static final float IMPACT_PARTICLE_MAX_LIFETIME = 0.42f;
    private static final float IMPACT_PARTICLE_MIN_SIZE = 0.05f;
    private static final float IMPACT_PARTICLE_MAX_SIZE = 0.11f;
    private static final float IMPACT_PARTICLE_HORIZONTAL_SPEED = 1.2f;
    private static final float IMPACT_PARTICLE_VERTICAL_SPEED_MIN = 0.55f;
    private static final float IMPACT_PARTICLE_VERTICAL_SPEED_MAX = 1.45f;
    private static final float BULLET_TRAIL_WIDTH = 4.2f;
    private static final float BULLET_TRAIL_TIP_RADIUS = 2.6f;
    private static final float PLAYER_SHADOW_WORLD_WIDTH = 0.82f;
    private static final float PLAYER_SHADOW_WORLD_DEPTH = 0.56f;
    private static final float CPU_SHADOW_WORLD_WIDTH = 0.74f;
    private static final float CPU_SHADOW_WORLD_DEPTH = 0.5f;
    private static final float PLAYER_HIT_FLASH_DURATION = 0.22f;
    private static final float ENEMY_HIT_FLASH_DURATION = 0.2f;
    private static final float HIT_MESSAGE_DURATION = 0.65f;
    private static final float SPLAT_MESSAGE_DURATION = 1.1f;
    private static final Color CROSSHAIR_COLOR = new Color(1f, 1f, 1f, 0.9f);
    private static final Color CROSSHAIR_SHADOW_COLOR = new Color(0f, 0f, 0f, 0.55f);
    private static final Color CROSSHAIR_CENTER_COLOR = new Color(0.16f, 0.8f, 1f, 1f);
    private static final Color CLEAR_COLOR = new Color(0.11f, 0.14f, 0.19f, 1f);
    private static final Color PANEL_COLOR = new Color(0.03f, 0.05f, 0.08f, 0.56f);
    private static final Color PANEL_BORDER_COLOR = new Color(0.92f, 0.98f, 1f, 0.2f);
    private static final Color HUD_TIME_ACCENT_COLOR = new Color(1f, 0.9f, 0.36f, 0.98f);
    private static final Color WEAPON_SLOT_COLOR = new Color(0f, 0f, 0f, 0.5f);
    private static final Color WEAPON_SLOT_ACTIVE_COLOR = new Color(0.12f, 0.18f, 0.26f, 0.88f);
    private static final Color WEAPON_SLOT_ACTIVE_BORDER_COLOR = new Color(0.55f, 0.9f, 1f, 0.92f);
    private static final Color PLAYER_TEAM_MARKER_COLOR = new Color(0.18f, 0.88f, 1f, 0.95f);
    private static final Color ENEMY_TEAM_MARKER_COLOR = new Color(1f, 0.42f, 0.72f, 0.95f);
    private static final Color TEAM_MARKER_SHADOW_COLOR = new Color(0f, 0f, 0f, 0.72f);
    private static final Color MINIMAP_PANEL_COLOR = new Color(0f, 0f, 0f, 0.55f);
    private static final Color MINIMAP_BORDER_COLOR = new Color(1f, 1f, 1f, 0.2f);
    private static final Color MINIMAP_EMPTY_COLOR = new Color(0.22f, 0.24f, 0.3f, 0.92f);
    private static final Color MINIMAP_PLAYER_COLOR = new Color(0.2f, 0.86f, 1f, 1f);
    private static final Color MINIMAP_ENEMY_COLOR = new Color(1f, 0.42f, 0.7f, 1f);
    private static final Color PLAYER_HP_BAR_COLOR = new Color(0.22f, 0.95f, 0.38f, 0.95f);
    private static final Color INK_BAR_COLOR = new Color(0.15f, 0.8f, 1f, 0.95f);
    private static final Color BAR_BACKGROUND_COLOR = new Color(0f, 0f, 0f, 0.55f);
    private static final Color PLAYER_DAMAGE_FLASH_COLOR = new Color(1f, 0.15f, 0.15f, 0.22f);
    private static final Color FEEDBACK_HIT_COLOR = new Color(1f, 0.92f, 0.35f, 1f);
    private static final Color FEEDBACK_SPLAT_COLOR = new Color(1f, 0.55f, 0.75f, 1f);

    private ModelBatch modelBatch;
    private Environment environment;
    private PerspectiveCamera worldCamera;
    private OrthographicCamera hudCamera;
    private ScreenViewport hudViewport;
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private MenuUi3D menuUi;
    private AudioManager3D audioManager;
    private FloorGrid3D floorGrid;
    private StageObstacles3D stageObstacles;
    private Player3D player;
    private EnemyCpu3D allyCpu;
    private EnemyCpu3D enemyCpuOne;
    private EnemyCpu3D enemyCpuTwo;
    private CharacterShadow3D playerShadow;
    private CharacterShadow3D allyCpuShadow;
    private CharacterShadow3D enemyCpuOneShadow;
    private CharacterShadow3D enemyCpuTwoShadow;
    private final ArrayList<EnemyCpu3D> cpuCharacters = new ArrayList<>();
    private final ArrayList<EnemyCpu3D> enemyTeamCpus = new ArrayList<>();
    private final ArrayList<Bullet3D> bullets = new ArrayList<>();
    private final ArrayList<InkParticle3D> inkParticles = new ArrayList<>();
    private final Vector3 cameraTarget = new Vector3();
    private final Vector3 cameraDirection = new Vector3();
    private final Vector3 cameraMoveForward = new Vector3();
    private final Vector3 cameraMoveRight = new Vector3();
    private final Vector3 desiredCameraPosition = new Vector3();
    private final Vector3 desiredCameraTarget = new Vector3();
    private final Vector3 projectedPlayerMarker = new Vector3();
    private final Vector3 projectedCpuMarker = new Vector3();
    private final Vector3 projectedMarkerForward = new Vector3();
    private final Vector3 projectedPlayerHeadGauge = new Vector3();
    private final Vector3 projectedPlayerBodyGauge = new Vector3();
    private final Vector3 minimapFacingDirection = new Vector3();
    private final Vector3 cpuShotDirection = new Vector3();
    private final Vector3 projectedEffectStart = new Vector3();
    private final Vector3 projectedEffectEnd = new Vector3();
    private final Vector3 projectedEffectCenter = new Vector3();
    private final Vector3 projectedEffectRadiusPoint = new Vector3();
    private final Vector3 particleSpawnVelocity = new Vector3();
    private final Vector3 bulletImpactPosition = new Vector3();

    private GameFlowState flowState;
    private TitleMenuScreen titleMenuScreen;
    private int titleMenuIndex;
    private int weaponMenuIndex;
    private int stageMenuIndex;
    private int difficultyMenuIndex;
    private float countdownTimer;
    private float remainingTime;
    private WeaponConfig3D playerWeapon;
    private WeaponConfig3D selectedTitleWeapon;
    private StageType3D selectedStageType;
    private CpuDifficulty3D selectedCpuDifficulty;
    private StageConfig3D currentStageConfig;
    private float fireCooldownRemaining;
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
    private int finalPlayerSplats;
    private int finalEnemySplats;
    private String finalWeaponName;
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
        menuUi = new MenuUi3D(spriteBatch, shapeRenderer, font, glyphLayout);
        audioManager = new AudioManager3D();
        audioManager.playTitleBgm();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.64f, 0.67f, 0.72f, 1f));
        environment.add(new DirectionalLight().set(0.92f, 0.9f, 0.84f, -0.8f, -1f, -0.35f));
        environment.add(new DirectionalLight().set(0.26f, 0.3f, 0.38f, 0.7f, -0.4f, 0.55f));

        player = new Player3D();
        allyCpu = new EnemyCpu3D(Team3D.PLAYER, "Ally CPU", 0);
        enemyCpuOne = new EnemyCpu3D(Team3D.ENEMY, "Enemy CPU 1", 1);
        enemyCpuTwo = new EnemyCpu3D(Team3D.ENEMY, "Enemy CPU 2", 2);
        if (ENABLE_CHARACTER_SHADOWS) {
            playerShadow = new CharacterShadow3D(PLAYER_SHADOW_WORLD_WIDTH, PLAYER_SHADOW_WORLD_DEPTH, 0.2f);
            allyCpuShadow = new CharacterShadow3D(CPU_SHADOW_WORLD_WIDTH, CPU_SHADOW_WORLD_DEPTH, 0.2f);
            enemyCpuOneShadow = new CharacterShadow3D(CPU_SHADOW_WORLD_WIDTH, CPU_SHADOW_WORLD_DEPTH, 0.2f);
            enemyCpuTwoShadow = new CharacterShadow3D(CPU_SHADOW_WORLD_WIDTH, CPU_SHADOW_WORLD_DEPTH, 0.2f);
        }
        cpuCharacters.clear();
        cpuCharacters.add(allyCpu);
        cpuCharacters.add(enemyCpuOne);
        cpuCharacters.add(enemyCpuTwo);
        enemyTeamCpus.clear();
        enemyTeamCpus.add(enemyCpuOne);
        enemyTeamCpus.add(enemyCpuTwo);
        selectedStageType = StageType3D.TRAINING_STAGE;
        selectedTitleWeapon = WeaponConfig3D.BASIC_SHOOTER;
        selectedCpuDifficulty = CpuDifficulty3D.NORMAL;
        applyCpuDifficulty();
        rebuildStage(StageConfig3D.forType(selectedStageType));
        playerWeapon = selectedTitleWeapon;
        flowState = GameFlowState.TITLE;
        resetTitleMenuState();
        countdownTimer = COUNTDOWN_TOTAL_SECONDS;
        remainingTime = GAME_DURATION_SECONDS;
        fireCooldownRemaining = 0f;
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
            worldCamera = new PerspectiveCamera(CAMERA_FOV, width, height);
        }
        worldCamera.fieldOfView = CAMERA_FOV;
        worldCamera.viewportWidth = width;
        worldCamera.viewportHeight = height;

        snapCameraToPlayer();
        worldCamera.near = 0.1f;
        worldCamera.far = 80f;
        worldCamera.update();

        if (hudCamera == null) {
            hudCamera = new OrthographicCamera();
            hudViewport = new ScreenViewport(hudCamera);
        }
        if (hudViewport == null) {
            hudViewport = new ScreenViewport(hudCamera);
        }
        hudViewport.update(width, height, true);
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
        updateCharacterShadows();
        renderCharacterShadows();
        renderBullets();
        if (flowState != GameFlowState.TITLE) {
            player.render(modelBatch, environment);
            for (EnemyCpu3D cpuCharacter : cpuCharacters) {
                cpuCharacter.render(modelBatch, environment);
            }
        }
        modelBatch.end();

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        if (hudViewport != null) {
            hudViewport.apply();
        }
        hudCamera.update();
        drawWorldEffects();
        drawCombatShapes();
        drawPlayerStatusGauges();
        drawTeamMarkers();
        drawMinimap();
        drawOverlay();
        drawCrosshair();
        drawStateMenus();
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
        if (playerShadow != null) {
            playerShadow.dispose();
        }
        if (allyCpuShadow != null) {
            allyCpuShadow.dispose();
        }
        if (enemyCpuOneShadow != null) {
            enemyCpuOneShadow.dispose();
        }
        if (enemyCpuTwoShadow != null) {
            enemyCpuTwoShadow.dispose();
        }
        if (allyCpu != null) {
            allyCpu.dispose();
        }
        if (enemyCpuOne != null) {
            enemyCpuOne.dispose();
        }
        if (enemyCpuTwo != null) {
            enemyCpuTwo.dispose();
        }
        clearBullets();
    }

    private void handleGlobalInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            audioManager.toggleMute();
        }

        if (flowState == GameFlowState.GAME_OVER && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            startCountdown();
            return;
        }

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
            handleTitleMenuInput();
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
            updateCpuCharacters(delta);
            fireCooldownRemaining = Math.max(0f, fireCooldownRemaining - delta);
            inkEmptySoundCooldownRemaining = Math.max(0f, inkEmptySoundCooldownRemaining - delta);
            handlePaintColorToggle();
            handleWeaponSwitchInput();
            handleShootingInput();
            handleCpuShooting();
            updateBullets(delta);
            updateInkParticles(delta);
            floorGrid.update(delta);
            updateCombatFeedback(delta);
        }
    }

    private void handleTitleMenuInput() {
        if (isTitleBackPressed()) {
            if (titleMenuScreen != TitleMenuScreen.MAIN) {
                titleMenuScreen = TitleMenuScreen.MAIN;
                audioManager.playWeaponSwitch();
            }
            return;
        }

        if (titleMenuScreen == TitleMenuScreen.CONTROLS) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                titleMenuScreen = TitleMenuScreen.MAIN;
                audioManager.playWeaponSwitch();
            }
            return;
        }

        if (titleMenuScreen == TitleMenuScreen.MAIN) {
            if (isTitleUpPressed()) {
                titleMenuIndex = wrapMenuIndex(titleMenuIndex - 1, 5);
                audioManager.playWeaponSwitch();
            } else if (isTitleDownPressed()) {
                titleMenuIndex = wrapMenuIndex(titleMenuIndex + 1, 5);
                audioManager.playWeaponSwitch();
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                openSelectedTitleMenu();
            }
            return;
        }

        if (titleMenuScreen == TitleMenuScreen.WEAPON_SELECT) {
            if (isTitleUpPressed()) {
                weaponMenuIndex = wrapMenuIndex(weaponMenuIndex - 1, 3);
                audioManager.playWeaponSwitch();
            } else if (isTitleDownPressed()) {
                weaponMenuIndex = wrapMenuIndex(weaponMenuIndex + 1, 3);
                audioManager.playWeaponSwitch();
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                selectedTitleWeapon = getWeaponByMenuIndex(weaponMenuIndex);
                titleMenuScreen = TitleMenuScreen.MAIN;
                audioManager.playWeaponSwitch();
            }
            return;
        }

        if (titleMenuScreen == TitleMenuScreen.STAGE_SELECT) {
            if (isTitleUpPressed()) {
                stageMenuIndex = wrapMenuIndex(stageMenuIndex - 1, StageType3D.values().length);
                audioManager.playWeaponSwitch();
            } else if (isTitleDownPressed()) {
                stageMenuIndex = wrapMenuIndex(stageMenuIndex + 1, StageType3D.values().length);
                audioManager.playWeaponSwitch();
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                selectedStageType = StageType3D.values()[stageMenuIndex];
                rebuildStage(StageConfig3D.forType(selectedStageType));
                titleMenuScreen = TitleMenuScreen.MAIN;
                audioManager.playWeaponSwitch();
            }
            return;
        }

        if (titleMenuScreen == TitleMenuScreen.DIFFICULTY) {
            if (isTitleUpPressed()) {
                difficultyMenuIndex = wrapMenuIndex(difficultyMenuIndex - 1, CpuDifficulty3D.values().length);
                audioManager.playWeaponSwitch();
            } else if (isTitleDownPressed()) {
                difficultyMenuIndex = wrapMenuIndex(difficultyMenuIndex + 1, CpuDifficulty3D.values().length);
                audioManager.playWeaponSwitch();
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                selectedCpuDifficulty = CpuDifficulty3D.values()[difficultyMenuIndex];
                applyCpuDifficulty();
                titleMenuScreen = TitleMenuScreen.MAIN;
                audioManager.playWeaponSwitch();
            }
        }
    }

    private void openSelectedTitleMenu() {
        switch (titleMenuIndex) {
            case 0:
                startCountdown();
                break;
            case 1:
                titleMenuScreen = TitleMenuScreen.WEAPON_SELECT;
                weaponMenuIndex = getWeaponMenuIndex(selectedTitleWeapon);
                audioManager.playWeaponSwitch();
                break;
            case 2:
                titleMenuScreen = TitleMenuScreen.STAGE_SELECT;
                stageMenuIndex = selectedStageType.ordinal();
                audioManager.playWeaponSwitch();
                break;
            case 3:
                titleMenuScreen = TitleMenuScreen.DIFFICULTY;
                difficultyMenuIndex = selectedCpuDifficulty.ordinal();
                audioManager.playWeaponSwitch();
                break;
            case 4:
                titleMenuScreen = TitleMenuScreen.CONTROLS;
                audioManager.playWeaponSwitch();
                break;
            default:
                break;
        }
    }

    private void drawOverlay() {
        if (flowState != GameFlowState.PLAYING) {
            return;
        }

        float hudScale = getHudScale();
        float teamPanelX = TOP_PANEL_MARGIN * hudScale;
        float teamPanelY = hudCamera.viewportHeight - TEAM_PANEL_HEIGHT * hudScale - TOP_PANEL_MARGIN * hudScale;
        float enemyPanelX = hudCamera.viewportWidth - TEAM_PANEL_WIDTH * hudScale - TOP_PANEL_MARGIN * hudScale;
        float enemyPanelY = teamPanelY;
        float timePanelX = (hudCamera.viewportWidth - TIME_PANEL_WIDTH * hudScale) / 2f;
        float timePanelY = hudCamera.viewportHeight - TIME_PANEL_HEIGHT * hudScale - TOP_PANEL_MARGIN * hudScale;
        float infoPanelX = INFO_PANEL_MARGIN * hudScale;
        float weaponPanelY = WEAPON_PANEL_MARGIN * hudScale;
        float infoPanelY = weaponPanelY + WEAPON_PANEL_HEIGHT * hudScale + 18f * hudScale;
        float infoTopY = infoPanelY + getBottomInfoPanelHeight(hudScale) - INFO_PANEL_PADDING * hudScale - 2f * hudScale;
        float slotStartX = getWeaponSlotStartX(hudScale);

        spriteBatch.setProjectionMatrix(hudCamera.combined);
        spriteBatch.begin();

        drawHudLeftTextFit(
            "PLAYER TEAM",
            teamPanelX + TEAM_PANEL_INNER_PADDING * hudScale,
            teamPanelY + TEAM_PANEL_HEIGHT * hudScale - 16f * hudScale,
            TEAM_PANEL_WIDTH * hudScale - TEAM_PANEL_INNER_PADDING * 2f * hudScale,
            PLAYER_TEAM_MARKER_COLOR,
            HUD_SCALE_SMALL * hudScale,
            HUD_SCALE_TINY * hudScale,
            true
        );
        drawHudLeftTextFit(
            floorGrid.getPlayerPaintedCellCount() + " Tiles",
            teamPanelX + TEAM_PANEL_INNER_PADDING * hudScale,
            teamPanelY + TEAM_PANEL_HEIGHT * hudScale - 40f * hudScale,
            TEAM_PANEL_WIDTH * hudScale - TEAM_PANEL_INNER_PADDING * 2f * hudScale,
            Color.WHITE,
            HUD_SCALE_BODY * hudScale,
            HUD_SCALE_SMALL * hudScale,
            false
        );
        drawHudLeftTextFit(
            String.format("%.1f%% Paint", floorGrid.getPlayerPaintRatePercent()),
            teamPanelX + TEAM_PANEL_INNER_PADDING * hudScale,
            teamPanelY + TEAM_PANEL_HEIGHT * hudScale - 64f * hudScale,
            TEAM_PANEL_WIDTH * hudScale - TEAM_PANEL_INNER_PADDING * 2f * hudScale,
            PLAYER_TEAM_MARKER_COLOR,
            HUD_SCALE_SMALL * hudScale,
            HUD_SCALE_TINY * hudScale,
            false
        );

        drawHudLeftTextFit(
            "ENEMY TEAM",
            enemyPanelX + TEAM_PANEL_INNER_PADDING * hudScale,
            enemyPanelY + TEAM_PANEL_HEIGHT * hudScale - 16f * hudScale,
            TEAM_PANEL_WIDTH * hudScale - TEAM_PANEL_INNER_PADDING * 2f * hudScale,
            ENEMY_TEAM_MARKER_COLOR,
            HUD_SCALE_SMALL * hudScale,
            HUD_SCALE_TINY * hudScale,
            true
        );
        drawHudLeftTextFit(
            floorGrid.getEnemyPaintedCellCount() + " Tiles",
            enemyPanelX + TEAM_PANEL_INNER_PADDING * hudScale,
            enemyPanelY + TEAM_PANEL_HEIGHT * hudScale - 40f * hudScale,
            TEAM_PANEL_WIDTH * hudScale - TEAM_PANEL_INNER_PADDING * 2f * hudScale,
            Color.WHITE,
            HUD_SCALE_BODY * hudScale,
            HUD_SCALE_SMALL * hudScale,
            false
        );
        drawHudLeftTextFit(
            String.format("%.1f%% Paint", floorGrid.getEnemyPaintRatePercent()),
            enemyPanelX + TEAM_PANEL_INNER_PADDING * hudScale,
            enemyPanelY + TEAM_PANEL_HEIGHT * hudScale - 64f * hudScale,
            TEAM_PANEL_WIDTH * hudScale - TEAM_PANEL_INNER_PADDING * 2f * hudScale,
            ENEMY_TEAM_MARKER_COLOR,
            HUD_SCALE_SMALL * hudScale,
            HUD_SCALE_TINY * hudScale,
            false
        );

        drawHudCenteredTextFit(
            "TIME",
            timePanelX + TIME_PANEL_WIDTH * hudScale / 2f,
            timePanelY + TIME_PANEL_HEIGHT * hudScale - 18f * hudScale,
            TIME_PANEL_WIDTH * hudScale - 28f * hudScale,
            HUD_TIME_ACCENT_COLOR,
            HUD_SCALE_SMALL * hudScale,
            HUD_SCALE_TINY * hudScale,
            true
        );
        drawHudCenteredTextFit(
            String.format("%d", (int) Math.ceil(remainingTime)),
            timePanelX + TIME_PANEL_WIDTH * hudScale / 2f,
            timePanelY + 38f * hudScale,
            TIME_PANEL_WIDTH * hudScale - 28f * hudScale,
            Color.WHITE,
            HUD_SCALE_TITLE * hudScale,
            HUD_SCALE_BODY * hudScale,
            true
        );

        float infoTextWidth = INFO_PANEL_WIDTH * hudScale - INFO_PANEL_PADDING * 2f * hudScale;
        drawHudLeftTextFit("Mode: " + getPlayerModeLabel(), infoPanelX + INFO_PANEL_PADDING * hudScale, infoTopY, infoTextWidth, Color.WHITE, HUD_SCALE_BODY * hudScale, HUD_SCALE_SMALL * hudScale, false);
        drawHudLeftTextFit("Ground: " + getGroundStateLabel(player.getPosition()), infoPanelX + INFO_PANEL_PADDING * hudScale, infoTopY - INFO_PANEL_LINE_GAP * hudScale, infoTextWidth, Color.WHITE, HUD_SCALE_BODY * hudScale, HUD_SCALE_SMALL * hudScale, false);
        drawHudLeftTextFit(String.format("Splats: %d - %d", playerSplatCount, enemySplatCount), infoPanelX + INFO_PANEL_PADDING * hudScale, infoTopY - INFO_PANEL_LINE_GAP * 2f * hudScale, infoTextWidth, Color.WHITE, HUD_SCALE_BODY * hudScale, HUD_SCALE_SMALL * hudScale, false);
        drawHudLeftTextFit("Ally HP: " + allyCpu.getHp(), infoPanelX + INFO_PANEL_PADDING * hudScale, infoTopY - INFO_PANEL_LINE_GAP * 3f * hudScale, infoTextWidth, Color.WHITE, HUD_SCALE_BODY * hudScale, HUD_SCALE_SMALL * hudScale, false);
        drawHudLeftTextFit(
            String.format("Enemy HP: %d / %d", enemyCpuOne.getHp(), enemyCpuTwo.getHp()),
            infoPanelX + INFO_PANEL_PADDING * hudScale,
            infoTopY - INFO_PANEL_LINE_GAP * 4f * hudScale,
            infoTextWidth,
            ENEMY_TEAM_MARKER_COLOR,
            HUD_SCALE_BODY * hudScale,
            HUD_SCALE_SMALL * hudScale,
            false
        );
        if (DEBUG_MODE) {
            drawHudLeftTextFit(DEBUG_PAINT_TEXT, infoPanelX + INFO_PANEL_PADDING * hudScale, infoTopY - INFO_PANEL_LINE_GAP * 5f * hudScale, infoTextWidth, HUD_TIME_ACCENT_COLOR, HUD_SCALE_SMALL * hudScale, HUD_SCALE_TINY * hudScale, false);
            drawHudLeftTextFit("Current Color: " + getCurrentPaintColorLabel(), infoPanelX + INFO_PANEL_PADDING * hudScale, infoTopY - INFO_PANEL_LINE_GAP * 6f * hudScale, infoTextWidth, HUD_TIME_ACCENT_COLOR, HUD_SCALE_SMALL * hudScale, HUD_SCALE_TINY * hudScale, false);
        }

        drawPlayerStatusText(hudScale);
        drawWeaponHudText(slotStartX, weaponPanelY, hudScale);

        if (feedbackMessageTimer > 0f && feedbackMessageText != null && !feedbackMessageText.isEmpty()) {
            drawHudCenteredTextFit(
                feedbackMessageText,
                hudCamera.viewportWidth / 2f,
                hudCamera.viewportHeight / 2f + 84f * hudScale,
                320f * hudScale,
                feedbackMessageColor,
                HUD_SCALE_BODY * hudScale,
                HUD_SCALE_SMALL * hudScale,
                true
            );
        }
        if (player.isSplatted()) {
            drawHudCenteredTextFit("Splatted!", hudCamera.viewportWidth / 2f, hudCamera.viewportHeight / 2f + 36f * hudScale, 320f * hudScale, FEEDBACK_SPLAT_COLOR, HUD_SCALE_TITLE * hudScale, HUD_SCALE_BODY * hudScale, true);
            drawHudCenteredTextFit(
                String.format("Respawning... %.1f", player.getRespawnTimer()),
                hudCamera.viewportWidth / 2f,
                hudCamera.viewportHeight / 2f + 2f * hudScale,
                320f * hudScale,
                Color.WHITE,
                HUD_SCALE_BODY * hudScale,
                HUD_SCALE_SMALL * hudScale,
                true
            );
        }

        spriteBatch.end();
    }

    private void drawStateMenus() {
        if (flowState == GameFlowState.TITLE) {
            if (titleMenuScreen == TitleMenuScreen.MAIN) {
                menuUi.drawTitleMain(
                    hudCamera,
                    TITLE_TEXT,
                    TITLE_PROMPT_TEXT,
                    DEBUG_MODE ? STEP_TEXT : "",
                    TITLE_MENU_ITEMS,
                    titleMenuIndex,
                    selectedTitleWeapon.getName(),
                    selectedStageType.getDisplayName(),
                    selectedCpuDifficulty.getLabel()
                );
                return;
            }

            if (titleMenuScreen == TitleMenuScreen.WEAPON_SELECT) {
                menuUi.drawWeaponSelect(hudCamera, TITLE_MENU_WEAPONS, weaponMenuIndex, selectedTitleWeapon);
                return;
            }

            if (titleMenuScreen == TitleMenuScreen.STAGE_SELECT) {
                menuUi.drawStageSelect(hudCamera, StageType3D.values(), stageMenuIndex, selectedStageType);
                return;
            }

            if (titleMenuScreen == TitleMenuScreen.DIFFICULTY) {
                menuUi.drawDifficultySelect(hudCamera, CpuDifficulty3D.values(), difficultyMenuIndex, selectedCpuDifficulty);
                return;
            }

            menuUi.drawControls(hudCamera);
            return;
        }

        if (flowState == GameFlowState.COUNTDOWN) {
            menuUi.drawCountdown(
                hudCamera,
                getCountdownText(),
                "Controls are locked during the countdown.",
                RETURN_TEXT
            );
            return;
        }

        if (flowState == GameFlowState.PAUSED) {
            menuUi.drawPause(hudCamera, new MenuUi3D.InfoRow[] {
                new MenuUi3D.InfoRow("Player Paint", String.format("%d (%.1f%%)", floorGrid.getPlayerPaintedCellCount(), floorGrid.getPlayerPaintRatePercent()), PLAYER_TEAM_MARKER_COLOR),
                new MenuUi3D.InfoRow("Enemy Paint", String.format("%d (%.1f%%)", floorGrid.getEnemyPaintedCellCount(), floorGrid.getEnemyPaintRatePercent()), ENEMY_TEAM_MARKER_COLOR),
                new MenuUi3D.InfoRow("Player HP", String.format("%d / %d", player.getHp(), Player3D.MAX_HP)),
                new MenuUi3D.InfoRow("Ink", String.format("%.0f / %.0f", player.getInkAmount(), Player3D.MAX_INK_AMOUNT)),
                new MenuUi3D.InfoRow("Ally / Enemy HP", String.format("%d / %d / %d", allyCpu.getHp(), enemyCpuOne.getHp(), enemyCpuTwo.getHp())),
                new MenuUi3D.InfoRow("Weapon", playerWeapon.getName(), PLAYER_TEAM_MARKER_COLOR),
                new MenuUi3D.InfoRow("Team Splats", String.format("%d / %d", playerSplatCount, enemySplatCount)),
                new MenuUi3D.InfoRow("Time", String.format("%d", (int) Math.ceil(remainingTime)))
            });
            return;
        }

        if (flowState == GameFlowState.GAME_OVER) {
            menuUi.drawResult(hudCamera, resultText, new MenuUi3D.InfoRow[] {
                new MenuUi3D.InfoRow("Player Team Tiles", String.valueOf(finalPlayerScore), PLAYER_TEAM_MARKER_COLOR),
                new MenuUi3D.InfoRow("Enemy Team Tiles", String.valueOf(finalEnemyScore), ENEMY_TEAM_MARKER_COLOR),
                new MenuUi3D.InfoRow("Player Paint Rate", String.format("%.1f%%", finalPlayerPaintRate), PLAYER_TEAM_MARKER_COLOR),
                new MenuUi3D.InfoRow("Enemy Paint Rate", String.format("%.1f%%", finalEnemyPaintRate), ENEMY_TEAM_MARKER_COLOR),
                new MenuUi3D.InfoRow("Player Team Splats", String.valueOf(finalPlayerSplats), PLAYER_TEAM_MARKER_COLOR),
                new MenuUi3D.InfoRow("Enemy Team Splats", String.valueOf(finalEnemySplats), ENEMY_TEAM_MARKER_COLOR),
                new MenuUi3D.InfoRow("Weapon", finalWeaponName)
            }, "Enter: Retry", "R: Return to Title");
        }
    }

    private float getHudScale() {
        float widthScale = Gdx.graphics.getWidth() / HUD_REFERENCE_WIDTH;
        float heightScale = Gdx.graphics.getHeight() / HUD_REFERENCE_HEIGHT;
        return MathUtils.clamp(Math.min(widthScale, heightScale), HUD_MIN_SCALE, HUD_MAX_SCALE);
    }

    private float getBottomInfoPanelHeight(float hudScale) {
        int lineCount = DEBUG_MODE ? 7 : 5;
        return INFO_PANEL_PADDING * hudScale * 2f + (lineCount - 1) * INFO_PANEL_LINE_GAP * hudScale + 18f * hudScale;
    }

    private float getWeaponSlotStartX(float hudScale) {
        int slotCount = getWeaponSlots().length;
        float totalWidth = slotCount * WEAPON_PANEL_WIDTH * hudScale + (slotCount - 1) * WEAPON_PANEL_GAP * hudScale;
        return (hudCamera.viewportWidth - totalWidth) / 2f;
    }

    private void drawHudCenteredTextFit(
        String text,
        float centerX,
        float y,
        float maxWidth,
        Color color,
        float preferredScale,
        float minScale,
        boolean shadow
    ) {
        float scale = fitHudSingleLineScale(text, maxWidth, preferredScale, minScale);
        drawHudTextInternal(text, centerX, y, maxWidth, color, scale, true, shadow);
    }

    private void drawHudLeftTextFit(
        String text,
        float x,
        float y,
        float maxWidth,
        Color color,
        float preferredScale,
        float minScale,
        boolean shadow
    ) {
        float scale = fitHudSingleLineScale(text, maxWidth, preferredScale, minScale);
        drawHudTextInternal(text, x, y, maxWidth, color, scale, false, shadow);
    }

    private float fitHudSingleLineScale(String text, float maxWidth, float preferredScale, float minScale) {
        float previousScaleX = font.getData().scaleX;
        float previousScaleY = font.getData().scaleY;
        font.getData().setScale(preferredScale);
        glyphLayout.setText(font, text);
        float measuredWidth = glyphLayout.width;
        font.getData().setScale(previousScaleX, previousScaleY);
        if (measuredWidth <= maxWidth || measuredWidth <= 0f) {
            return preferredScale;
        }
        return Math.max(minScale, preferredScale * (maxWidth / measuredWidth));
    }

    private void drawHudTextInternal(String text, float anchorX, float y, float maxWidth, Color color, float scale, boolean centerAligned, boolean shadow) {
        float previousScaleX = font.getData().scaleX;
        float previousScaleY = font.getData().scaleY;
        Color previousColor = new Color(font.getColor());
        font.getData().setScale(scale);
        glyphLayout.setText(font, text);
        float drawX = centerAligned ? anchorX - glyphLayout.width / 2f : anchorX;
        if (!centerAligned && glyphLayout.width > maxWidth) {
            drawX = anchorX;
        }
        if (shadow) {
            font.setColor(0f, 0f, 0f, 0.78f);
            font.draw(spriteBatch, text, drawX + Math.max(1f, scale * 1.2f), y - Math.max(1f, scale * 1.2f));
        }
        font.setColor(color);
        font.draw(spriteBatch, text, drawX, y);
        font.setColor(previousColor);
        font.getData().setScale(previousScaleX, previousScaleY);
    }

    private void drawCombatShapes() {
        if (flowState != GameFlowState.PLAYING) {
            return;
        }

        float hudScale = getHudScale();
        float playerTeamPanelX = TOP_PANEL_MARGIN * hudScale;
        float playerTeamPanelY = hudCamera.viewportHeight - TEAM_PANEL_HEIGHT * hudScale - TOP_PANEL_MARGIN * hudScale;
        float enemyTeamPanelX = hudCamera.viewportWidth - TEAM_PANEL_WIDTH * hudScale - TOP_PANEL_MARGIN * hudScale;
        float enemyTeamPanelY = playerTeamPanelY;
        float timePanelX = (hudCamera.viewportWidth - TIME_PANEL_WIDTH * hudScale) / 2f;
        float timePanelY = hudCamera.viewportHeight - TIME_PANEL_HEIGHT * hudScale - TOP_PANEL_MARGIN * hudScale;
        float infoPanelX = INFO_PANEL_MARGIN * hudScale;
        float weaponPanelY = WEAPON_PANEL_MARGIN * hudScale;
        float infoPanelY = weaponPanelY + WEAPON_PANEL_HEIGHT * hudScale + 18f * hudScale;
        float infoPanelHeight = getBottomInfoPanelHeight(hudScale);
        float slotStartX = getWeaponSlotStartX(hudScale);
        float teamGaugeWidth = TEAM_PANEL_WIDTH * hudScale - TEAM_PANEL_INNER_PADDING * 2f * hudScale;
        float teamGaugeY = playerTeamPanelY + 12f * hudScale;

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (playerHitFlashTimer > 0f) {
            float flashAlpha = (playerHitFlashTimer / PLAYER_HIT_FLASH_DURATION) * PLAYER_DAMAGE_FLASH_COLOR.a;
            shapeRenderer.setColor(PLAYER_DAMAGE_FLASH_COLOR.r, PLAYER_DAMAGE_FLASH_COLOR.g, PLAYER_DAMAGE_FLASH_COLOR.b, flashAlpha);
            shapeRenderer.rect(0f, 0f, hudCamera.viewportWidth, hudCamera.viewportHeight);
        }

        shapeRenderer.setColor(PANEL_COLOR);
        shapeRenderer.rect(playerTeamPanelX, playerTeamPanelY, TEAM_PANEL_WIDTH * hudScale, TEAM_PANEL_HEIGHT * hudScale);
        shapeRenderer.rect(enemyTeamPanelX, enemyTeamPanelY, TEAM_PANEL_WIDTH * hudScale, TEAM_PANEL_HEIGHT * hudScale);
        shapeRenderer.rect(timePanelX, timePanelY, TIME_PANEL_WIDTH * hudScale, TIME_PANEL_HEIGHT * hudScale);
        shapeRenderer.rect(infoPanelX, infoPanelY, INFO_PANEL_WIDTH * hudScale, infoPanelHeight);
        drawWeaponSlotPanels(slotStartX, weaponPanelY, hudScale);
        drawBarFill(
            playerTeamPanelX + TEAM_PANEL_INNER_PADDING * hudScale,
            teamGaugeY,
            teamGaugeWidth,
            TEAM_PAINT_BAR_HEIGHT * hudScale,
            floorGrid.getPlayerPaintRatePercent() / 100f,
            PLAYER_TEAM_MARKER_COLOR
        );
        drawBarFill(
            enemyTeamPanelX + TEAM_PANEL_INNER_PADDING * hudScale,
            enemyTeamPanelY + 12f * hudScale,
            teamGaugeWidth,
            TEAM_PAINT_BAR_HEIGHT * hudScale,
            floorGrid.getEnemyPaintRatePercent() / 100f,
            ENEMY_TEAM_MARKER_COLOR
        );

        if (feedbackMessageTimer > 0f && feedbackMessageText != null && !feedbackMessageText.isEmpty()) {
            shapeRenderer.rect(hudCamera.viewportWidth / 2f - 152f * hudScale, hudCamera.viewportHeight / 2f + 58f * hudScale, 304f * hudScale, 42f * hudScale);
        }
        if (player.isSplatted()) {
            shapeRenderer.rect(hudCamera.viewportWidth / 2f - 166f * hudScale, hudCamera.viewportHeight / 2f - 42f * hudScale, 332f * hudScale, 102f * hudScale);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(PANEL_BORDER_COLOR);
        shapeRenderer.rect(playerTeamPanelX, playerTeamPanelY, TEAM_PANEL_WIDTH * hudScale, TEAM_PANEL_HEIGHT * hudScale);
        shapeRenderer.rect(enemyTeamPanelX, enemyTeamPanelY, TEAM_PANEL_WIDTH * hudScale, TEAM_PANEL_HEIGHT * hudScale);
        shapeRenderer.rect(timePanelX, timePanelY, TIME_PANEL_WIDTH * hudScale, TIME_PANEL_HEIGHT * hudScale);
        shapeRenderer.rect(infoPanelX, infoPanelY, INFO_PANEL_WIDTH * hudScale, infoPanelHeight);
        drawWeaponSlotBorders(slotStartX, weaponPanelY, hudScale);
        if (feedbackMessageTimer > 0f && feedbackMessageText != null && !feedbackMessageText.isEmpty()) {
            shapeRenderer.rect(hudCamera.viewportWidth / 2f - 152f * hudScale, hudCamera.viewportHeight / 2f + 58f * hudScale, 304f * hudScale, 42f * hudScale);
        }
        if (player.isSplatted()) {
            shapeRenderer.rect(hudCamera.viewportWidth / 2f - 166f * hudScale, hudCamera.viewportHeight / 2f - 42f * hudScale, 332f * hudScale, 102f * hudScale);
        }
        shapeRenderer.end();
    }

    private void drawWorldEffects() {
        if (flowState == GameFlowState.TITLE) {
            return;
        }

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Bullet3D bullet : bullets) {
            drawBulletTrail(bullet);
        }
        for (InkParticle3D inkParticle : inkParticles) {
            drawProjectedParticle(inkParticle);
        }

        shapeRenderer.end();
    }

    private void updateCharacterShadows() {
        if (!ENABLE_CHARACTER_SHADOWS) {
            return;
        }
        if (flowState == GameFlowState.TITLE) {
            hideAllCharacterShadows();
            return;
        }

        updatePlayerShadow();
        updateCpuShadow(allyCpu, allyCpuShadow);
        updateCpuShadow(enemyCpuOne, enemyCpuOneShadow);
        updateCpuShadow(enemyCpuTwo, enemyCpuTwoShadow);
    }

    private void renderCharacterShadows() {
        if (!ENABLE_CHARACTER_SHADOWS) {
            return;
        }

        if (playerShadow != null) {
            playerShadow.render(modelBatch, environment);
        }
        if (allyCpuShadow != null) {
            allyCpuShadow.render(modelBatch, environment);
        }
        if (enemyCpuOneShadow != null) {
            enemyCpuOneShadow.render(modelBatch, environment);
        }
        if (enemyCpuTwoShadow != null) {
            enemyCpuTwoShadow.render(modelBatch, environment);
        }
    }

    private void updatePlayerShadow() {
        if (playerShadow == null) {
            return;
        }
        if (player.isSplatted()) {
            playerShadow.hide();
            return;
        }

        float playerGroundY = player.isClimbing()
            ? 0f
            : stageObstacles.getSupportHeightAt(
                player.getPosition().x,
                player.getPosition().z,
                player.getCollisionRadius(),
                player.getBaseY()
            );
        playerShadow.update(player.getPosition(), playerGroundY, player.getBaseY());
    }

    private void updateCpuShadow(EnemyCpu3D cpuCharacter, CharacterShadow3D cpuShadow) {
        if (cpuShadow == null) {
            return;
        }
        if (cpuCharacter == null || cpuCharacter.isSplatted()) {
            cpuShadow.hide();
            return;
        }
        cpuShadow.update(cpuCharacter.getPosition(), 0f, cpuCharacter.getBaseY());
    }

    private void hideAllCharacterShadows() {
        if (playerShadow != null) {
            playerShadow.hide();
        }
        if (allyCpuShadow != null) {
            allyCpuShadow.hide();
        }
        if (enemyCpuOneShadow != null) {
            enemyCpuOneShadow.hide();
        }
        if (enemyCpuTwoShadow != null) {
            enemyCpuTwoShadow.hide();
        }
    }

    private void drawTeamMarkers() {
        if (flowState != GameFlowState.PLAYING) {
            return;
        }

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (!player.isSplatted()) {
            drawProjectedTeamMarker(projectedPlayerMarker, player.getPosition(), player.getFacingDirection(), PLAYER_TEAM_MARKER_COLOR, player.isInvincible());
        }
        for (EnemyCpu3D cpuCharacter : cpuCharacters) {
            if (cpuCharacter.isSplatted()) {
                continue;
            }
            Color markerColor = cpuCharacter.getTeam() == Team3D.PLAYER ? PLAYER_TEAM_MARKER_COLOR : ENEMY_TEAM_MARKER_COLOR;
            drawProjectedTeamMarker(projectedCpuMarker, cpuCharacter.getPosition(), cpuCharacter.getFacingDirection(), markerColor, cpuCharacter.isInvincible());
        }

        shapeRenderer.end();
    }

    private void drawMinimap() {
        if (flowState != GameFlowState.PLAYING) {
            return;
        }
        if (floorGrid == null || hudCamera == null) {
            return;
        }

        int columns = floorGrid.getColumns();
        int rows = floorGrid.getRows();
        if (columns <= 0 || rows <= 0) {
            return;
        }

        float availableSize = Math.min(
            MINIMAP_MAX_SIZE,
            Math.min(hudCamera.viewportWidth * 0.24f, hudCamera.viewportHeight * 0.28f)
        );
        float cellSize = availableSize / Math.max(columns, rows);
        float mapWidth = columns * cellSize;
        float mapHeight = rows * cellSize;
        float panelWidth = availableSize + MINIMAP_PANEL_PADDING * 2f;
        float panelHeight = availableSize + MINIMAP_PANEL_PADDING * 2f;
        float panelX = hudCamera.viewportWidth - panelWidth - MINIMAP_MARGIN;
        float panelY = MINIMAP_MARGIN;
        float mapX = panelX + MINIMAP_PANEL_PADDING + (availableSize - mapWidth) / 2f;
        float mapY = panelY + MINIMAP_PANEL_PADDING + (availableSize - mapHeight) / 2f;

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(MINIMAP_PANEL_COLOR);
        shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                shapeRenderer.setColor(getMinimapCellColor(floorGrid.getCellState(row, column)));
                shapeRenderer.rect(
                    mapX + column * cellSize,
                    mapY + row * cellSize,
                    cellSize + 0.5f,
                    cellSize + 0.5f
                );
            }
        }

        if (!player.isSplatted()) {
            drawMinimapMarker(player.getPosition(), player.getFacingDirection(), mapX, mapY, mapWidth, mapHeight, MINIMAP_PLAYER_COLOR);
        }
        for (EnemyCpu3D cpuCharacter : cpuCharacters) {
            if (cpuCharacter.isSplatted()) {
                continue;
            }
            Color markerColor = cpuCharacter.getTeam() == Team3D.PLAYER ? MINIMAP_PLAYER_COLOR : MINIMAP_ENEMY_COLOR;
            drawMinimapMarker(cpuCharacter.getPosition(), cpuCharacter.getFacingDirection(), mapX, mapY, mapWidth, mapHeight, markerColor);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(MINIMAP_BORDER_COLOR);
        shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
        shapeRenderer.rect(mapX, mapY, mapWidth, mapHeight);
        shapeRenderer.end();
    }

    private void drawBarFill(float x, float y, float width, float height, float ratio, Color fillColor) {
        float clampedRatio = MathUtils.clamp(ratio, 0f, 1f);
        shapeRenderer.setColor(BAR_BACKGROUND_COLOR);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(x, y, width * clampedRatio, height);
    }

    private void drawBulletTrail(Bullet3D bullet) {
        projectedEffectStart.set(bullet.getPreviousPosition());
        projectedEffectEnd.set(bullet.getPosition());
        worldCamera.project(projectedEffectStart);
        worldCamera.project(projectedEffectEnd);
        if (!isProjectedPointVisible(projectedEffectStart) || !isProjectedPointVisible(projectedEffectEnd)) {
            return;
        }

        Color trailColor = bullet.getVisualColor();
        shapeRenderer.setColor(trailColor.r, trailColor.g, trailColor.b, 0.48f);
        shapeRenderer.rectLine(
            projectedEffectStart.x,
            projectedEffectStart.y,
            projectedEffectEnd.x,
            projectedEffectEnd.y,
            BULLET_TRAIL_WIDTH
        );
        shapeRenderer.setColor(trailColor.r, trailColor.g, trailColor.b, 0.75f);
        shapeRenderer.circle(projectedEffectEnd.x, projectedEffectEnd.y, BULLET_TRAIL_TIP_RADIUS);
    }

    private void drawProjectedParticle(InkParticle3D inkParticle) {
        projectedEffectCenter.set(inkParticle.getPosition());
        projectedEffectRadiusPoint.set(
            inkParticle.getPosition().x + inkParticle.getCurrentSize(),
            inkParticle.getPosition().y,
            inkParticle.getPosition().z
        );
        worldCamera.project(projectedEffectCenter);
        worldCamera.project(projectedEffectRadiusPoint);
        if (!isProjectedPointVisible(projectedEffectCenter) || !isProjectedPointVisible(projectedEffectRadiusPoint)) {
            return;
        }

        float radius = projectedEffectCenter.dst(projectedEffectRadiusPoint);
        if (radius <= 0.5f) {
            radius = 0.5f;
        }
        Color particleColor = inkParticle.getColor();
        float alpha = inkParticle.getAlpha() * 0.9f;
        shapeRenderer.setColor(particleColor.r, particleColor.g, particleColor.b, alpha);
        shapeRenderer.circle(projectedEffectCenter.x, projectedEffectCenter.y, radius);
    }

    private boolean isProjectedPointVisible(Vector3 projectedPoint) {
        return projectedPoint.z >= 0f && projectedPoint.z <= 1f;
    }

    private void drawPlayerStatusGauges() {
        if (flowState != GameFlowState.PLAYING || player.isSplatted()) {
            return;
        }
        if (!projectPlayerStatusAnchors()) {
            return;
        }

        float hudScale = getHudScale();
        float clampMargin = PLAYER_STATUS_CLAMP_MARGIN * hudScale;
        float minStatusY = WEAPON_PANEL_MARGIN * hudScale + WEAPON_PANEL_HEIGHT * hudScale + 28f * hudScale;
        float hpWidth = PLAYER_HP_GAUGE_WIDTH * hudScale;
        float hpHeight = PLAYER_HP_GAUGE_HEIGHT * hudScale;
        float hpX = MathUtils.clamp(
            projectedPlayerHeadGauge.x - hpWidth / 2f,
            clampMargin,
            hudCamera.viewportWidth - hpWidth - clampMargin
        );
        float hpY = MathUtils.clamp(
            projectedPlayerHeadGauge.y + PLAYER_HP_GAUGE_OFFSET_Y * hudScale,
            minStatusY,
            hudCamera.viewportHeight - TEAM_PANEL_HEIGHT * hudScale - TOP_PANEL_MARGIN * hudScale - hpHeight - 14f * hudScale
        );
        float inkWidth = PLAYER_INK_GAUGE_WIDTH * hudScale;
        float inkHeight = PLAYER_INK_GAUGE_HEIGHT * hudScale;
        float inkX = MathUtils.clamp(
            projectedPlayerBodyGauge.x - PLAYER_INK_GAUGE_OFFSET_X * hudScale - inkWidth,
            INFO_PANEL_MARGIN * hudScale + INFO_PANEL_WIDTH * hudScale + 14f * hudScale,
            hudCamera.viewportWidth - inkWidth - clampMargin
        );
        float inkY = MathUtils.clamp(
            projectedPlayerBodyGauge.y - inkHeight / 2f,
            minStatusY,
            hudCamera.viewportHeight - TEAM_PANEL_HEIGHT * hudScale - TOP_PANEL_MARGIN * hudScale - inkHeight - 18f * hudScale
        );

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(BAR_BACKGROUND_COLOR);
        shapeRenderer.rect(hpX, hpY, hpWidth, hpHeight);
        shapeRenderer.rect(inkX, inkY, inkWidth, inkHeight);
        shapeRenderer.setColor(PLAYER_HP_BAR_COLOR);
        shapeRenderer.rect(hpX, hpY, hpWidth * (player.getHp() / (float) Player3D.MAX_HP), hpHeight);
        float inkRatio = player.getInkAmount() / Player3D.MAX_INK_AMOUNT;
        shapeRenderer.setColor(INK_BAR_COLOR);
        shapeRenderer.rect(inkX, inkY, inkWidth, inkHeight * inkRatio);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(PANEL_BORDER_COLOR);
        shapeRenderer.rect(hpX, hpY, hpWidth, hpHeight);
        shapeRenderer.rect(inkX, inkY, inkWidth, inkHeight);
        shapeRenderer.end();
    }

    private void drawPlayerStatusText(float hudScale) {
        if (!projectPlayerStatusAnchors()) {
            return;
        }

        float clampMargin = PLAYER_STATUS_CLAMP_MARGIN * hudScale;
        float minStatusY = WEAPON_PANEL_MARGIN * hudScale + WEAPON_PANEL_HEIGHT * hudScale + 28f * hudScale;
        float hpWidth = PLAYER_HP_GAUGE_WIDTH * hudScale;
        float hpHeight = PLAYER_HP_GAUGE_HEIGHT * hudScale;
        float hpX = MathUtils.clamp(
            projectedPlayerHeadGauge.x - hpWidth / 2f,
            clampMargin,
            hudCamera.viewportWidth - hpWidth - clampMargin
        );
        float hpY = MathUtils.clamp(
            projectedPlayerHeadGauge.y + PLAYER_HP_GAUGE_OFFSET_Y * hudScale,
            minStatusY,
            hudCamera.viewportHeight - TEAM_PANEL_HEIGHT * hudScale - TOP_PANEL_MARGIN * hudScale - hpHeight - 14f * hudScale
        );
        float inkWidth = PLAYER_INK_GAUGE_WIDTH * hudScale;
        float inkHeight = PLAYER_INK_GAUGE_HEIGHT * hudScale;
        float inkX = MathUtils.clamp(
            projectedPlayerBodyGauge.x - PLAYER_INK_GAUGE_OFFSET_X * hudScale - inkWidth,
            INFO_PANEL_MARGIN * hudScale + INFO_PANEL_WIDTH * hudScale + 14f * hudScale,
            hudCamera.viewportWidth - inkWidth - clampMargin
        );
        float inkY = MathUtils.clamp(
            projectedPlayerBodyGauge.y - inkHeight / 2f,
            minStatusY,
            hudCamera.viewportHeight - TEAM_PANEL_HEIGHT * hudScale - TOP_PANEL_MARGIN * hudScale - inkHeight - 18f * hudScale
        );

        drawHudCenteredTextFit(
            player.getHp() + " / " + Player3D.MAX_HP,
            hpX + hpWidth / 2f,
            hpY + hpHeight + PLAYER_STATUS_LABEL_GAP * hudScale + 12f * hudScale,
            hpWidth + 34f * hudScale,
            Color.WHITE,
            PLAYER_STATUS_TEXT_SCALE * hudScale,
            HUD_SCALE_TINY * hudScale,
            true
        );
        drawHudCenteredTextFit(
            "INK",
            inkX + inkWidth / 2f,
            inkY + inkHeight + PLAYER_STATUS_LABEL_GAP * hudScale + 12f * hudScale,
            62f * hudScale,
            INK_BAR_COLOR,
            HUD_SCALE_SMALL * hudScale,
            HUD_SCALE_TINY * hudScale,
            true
        );
    }

    private boolean projectPlayerStatusAnchors() {
        if (player.isSplatted()) {
            return false;
        }
        projectedPlayerHeadGauge.set(player.getPosition().x, player.getUiHeadAnchorY(), player.getPosition().z);
        worldCamera.project(projectedPlayerHeadGauge);
        if (!isProjectedPointVisible(projectedPlayerHeadGauge)) {
            return false;
        }

        projectedPlayerBodyGauge.set(player.getPosition().x, player.getUiBodyAnchorY(), player.getPosition().z);
        worldCamera.project(projectedPlayerBodyGauge);
        return isProjectedPointVisible(projectedPlayerBodyGauge);
    }

    private void drawWeaponHudText(float slotStartX, float slotY, float hudScale) {
        WeaponConfig3D[] weapons = getWeaponSlots();
        for (int i = 0; i < weapons.length; i++) {
            float slotWidth = WEAPON_PANEL_WIDTH * hudScale;
            float slotHeight = WEAPON_PANEL_HEIGHT * hudScale;
            float slotX = slotStartX + i * (slotWidth + WEAPON_PANEL_GAP * hudScale);
            WeaponConfig3D weapon = weapons[i];
            Color titleColor = playerWeapon == weapon ? Color.WHITE : PANEL_BORDER_COLOR;
            Color roleColor = playerWeapon == weapon ? WEAPON_SLOT_ACTIVE_BORDER_COLOR : new Color(0.82f, 0.86f, 0.92f, 0.9f);
            drawHudLeftTextFit("[" + (i + 1) + "]", slotX + 10f * hudScale, slotY + slotHeight - 12f * hudScale, 34f * hudScale, Color.WHITE, HUD_SCALE_TINY * hudScale, HUD_SCALE_TINY * hudScale, false);
            drawHudCenteredTextFit(weapon.getName(), slotX + slotWidth / 2f, slotY + slotHeight - 16f * hudScale, slotWidth - 44f * hudScale, titleColor, HUD_SCALE_SMALL * hudScale, HUD_SCALE_TINY * hudScale, false);
            drawHudCenteredTextFit(weapon.getRoleLabel(), slotX + slotWidth / 2f, slotY + 18f * hudScale, slotWidth - 24f * hudScale, roleColor, HUD_SCALE_TINY * hudScale, HUD_SCALE_TINY * hudScale, false);
        }
    }

    private void drawWeaponSlotPanels(float slotStartX, float slotY, float hudScale) {
        WeaponConfig3D[] weapons = getWeaponSlots();
        for (int i = 0; i < weapons.length; i++) {
            float slotWidth = WEAPON_PANEL_WIDTH * hudScale;
            float slotHeight = WEAPON_PANEL_HEIGHT * hudScale;
            float slotX = slotStartX + i * (slotWidth + WEAPON_PANEL_GAP * hudScale);
            WeaponConfig3D weapon = weapons[i];
            shapeRenderer.setColor(playerWeapon == weapon ? WEAPON_SLOT_ACTIVE_COLOR : WEAPON_SLOT_COLOR);
            shapeRenderer.rect(slotX, slotY, slotWidth, slotHeight);
            if (playerWeapon == weapon) {
                shapeRenderer.setColor(WEAPON_SLOT_ACTIVE_BORDER_COLOR.r, WEAPON_SLOT_ACTIVE_BORDER_COLOR.g, WEAPON_SLOT_ACTIVE_BORDER_COLOR.b, 0.28f);
                shapeRenderer.rect(slotX, slotY + slotHeight - WEAPON_PANEL_ACCENT_HEIGHT * hudScale, slotWidth, WEAPON_PANEL_ACCENT_HEIGHT * hudScale);
            }
        }
    }

    private void drawWeaponSlotBorders(float slotStartX, float slotY, float hudScale) {
        WeaponConfig3D[] weapons = getWeaponSlots();
        for (int i = 0; i < weapons.length; i++) {
            float slotWidth = WEAPON_PANEL_WIDTH * hudScale;
            float slotHeight = WEAPON_PANEL_HEIGHT * hudScale;
            float slotX = slotStartX + i * (slotWidth + WEAPON_PANEL_GAP * hudScale);
            WeaponConfig3D weapon = weapons[i];
            shapeRenderer.setColor(playerWeapon == weapon ? WEAPON_SLOT_ACTIVE_BORDER_COLOR : PANEL_BORDER_COLOR);
            shapeRenderer.rect(slotX, slotY, slotWidth, slotHeight);
        }
    }

    private void drawProjectedTeamMarker(
        Vector3 projectionTarget,
        Vector3 actorPosition,
        Vector3 actorFacingDirection,
        Color baseColor,
        boolean invincible
    ) {
        projectionTarget.set(actorPosition.x, actorPosition.y + TEAM_MARKER_Y_OFFSET, actorPosition.z);
        worldCamera.project(projectionTarget);
        if (projectionTarget.z < 0f || projectionTarget.z > 1f) {
            return;
        }

        float centerX = projectionTarget.x;
        float centerY = projectionTarget.y;
        Color markerColor = invincible ? new Color(baseColor).lerp(Color.WHITE, 0.45f) : baseColor;
        shapeRenderer.setColor(TEAM_MARKER_SHADOW_COLOR);
        shapeRenderer.circle(centerX, centerY, TEAM_MARKER_RADIUS + 2f);
        shapeRenderer.setColor(markerColor);
        shapeRenderer.circle(centerX, centerY, TEAM_MARKER_RADIUS);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(centerX, centerY, 3f);

        projectedMarkerForward.set(actorFacingDirection.x, 0f, actorFacingDirection.z);
        if (!projectedMarkerForward.isZero(0.0001f)) {
            // Project a point slightly in front of the actor so the 2D marker
            // arrow always matches the visible 3D facing direction.
            projectedMarkerForward.nor().scl(TEAM_MARKER_FORWARD_OFFSET)
                .add(actorPosition.x, actorPosition.y + TEAM_MARKER_Y_OFFSET, actorPosition.z);
            worldCamera.project(projectedMarkerForward);

            float facingScreenX = projectedMarkerForward.x - centerX;
            float facingScreenY = projectedMarkerForward.y - centerY;
            float facingLength = (float) Math.sqrt(facingScreenX * facingScreenX + facingScreenY * facingScreenY);
            if (facingLength <= 0.0001f) {
                return;
            }

            facingScreenX /= facingLength;
            facingScreenY /= facingLength;
            float tipX = centerX + facingScreenX * (TEAM_MARKER_RADIUS + 10f);
            float tipY = centerY + facingScreenY * (TEAM_MARKER_RADIUS + 10f);
            float sideX = -facingScreenY * 4f;
            float sideY = facingScreenX * 4f;
            shapeRenderer.setColor(markerColor);
            shapeRenderer.triangle(
                tipX,
                tipY,
                tipX - facingScreenX * 8f + sideX,
                tipY - facingScreenY * 8f + sideY,
                tipX - facingScreenX * 8f - sideX,
                tipY - facingScreenY * 8f - sideY
            );
        }
    }

    private void drawMinimapMarker(
        Vector3 actorPosition,
        Vector3 facingDirection,
        float mapX,
        float mapY,
        float mapWidth,
        float mapHeight,
        Color markerColor
    ) {
        float worldWidth = floorGrid.getWorldWidth();
        float worldDepth = floorGrid.getWorldDepth();
        if (worldWidth <= 0f || worldDepth <= 0f) {
            return;
        }

        float normalizedX = MathUtils.clamp((actorPosition.x - floorGrid.getMinX()) / worldWidth, 0f, 1f);
        float normalizedZ = MathUtils.clamp((actorPosition.z - floorGrid.getMinZ()) / worldDepth, 0f, 1f);
        float markerX = mapX + normalizedX * mapWidth;
        float markerY = mapY + normalizedZ * mapHeight;

        shapeRenderer.setColor(0f, 0f, 0f, 0.85f);
        shapeRenderer.circle(markerX, markerY, MINIMAP_MARKER_RADIUS + 1.5f);
        shapeRenderer.setColor(markerColor);
        shapeRenderer.circle(markerX, markerY, MINIMAP_MARKER_RADIUS);

        minimapFacingDirection.set(facingDirection.x, 0f, facingDirection.z);
        if (!minimapFacingDirection.isZero(0.0001f)) {
            minimapFacingDirection.nor();
            float tipX = markerX + minimapFacingDirection.x * MINIMAP_DIRECTION_LENGTH;
            float tipY = markerY + minimapFacingDirection.z * MINIMAP_DIRECTION_LENGTH;
            float sideX = -minimapFacingDirection.z * MINIMAP_TRIANGLE_SIZE;
            float sideY = minimapFacingDirection.x * MINIMAP_TRIANGLE_SIZE;
            shapeRenderer.triangle(
                tipX,
                tipY,
                markerX - minimapFacingDirection.x * 2.5f + sideX,
                markerY - minimapFacingDirection.z * 2.5f + sideY,
                markerX - minimapFacingDirection.x * 2.5f - sideX,
                markerY - minimapFacingDirection.z * 2.5f - sideY
            );
        }
    }

    private WeaponConfig3D[] getWeaponSlots() {
        return new WeaponConfig3D[] {
            WeaponConfig3D.BASIC_SHOOTER,
            WeaponConfig3D.SHORT_PAINTER,
            WeaponConfig3D.LONG_SHOOTER
        };
    }

    private Color getMinimapCellColor(int cellState) {
        if (cellState == FloorGrid3D.CELL_STATE_PLAYER) {
            return MINIMAP_PLAYER_COLOR;
        }
        if (cellState == FloorGrid3D.CELL_STATE_ENEMY) {
            return MINIMAP_ENEMY_COLOR;
        }

        return MINIMAP_EMPTY_COLOR;
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
        rebuildStage(StageConfig3D.forType(selectedStageType));
        applyCpuDifficulty();
        flowState = GameFlowState.TITLE;
        resetRoundState();
        resetTitleMenuState();
        audioManager.playTitleBgm();
        snapCameraToPlayer();
    }

    private void startCountdown() {
        rebuildStage(StageConfig3D.forType(selectedStageType));
        applyCpuDifficulty();
        flowState = GameFlowState.COUNTDOWN;
        resetRoundState();
        audioManager.playBattleBgm();
        snapCameraToPlayer();
    }

    private void resetRoundState() {
        clearBullets();
        clearInkParticles();
        countdownTimer = COUNTDOWN_TOTAL_SECONDS;
        remainingTime = GAME_DURATION_SECONDS;
        playerWeapon = selectedTitleWeapon;
        fireCooldownRemaining = 0f;
        currentPaintCellState = FloorGrid3D.CELL_STATE_PLAYER;
        resetMatchResult();
        playerSplatCount = 0;
        enemySplatCount = 0;
        resetCombatFeedback();
        resetCameraAngles();
        lastCountdownCue = Integer.MIN_VALUE;
        inkEmptySoundCooldownRemaining = 0f;
        setMouseCapture(false);
        audioManager.setPausedDucked(false);
    }

    private void pauseGame() {
        player.setSwimming(false);
        flowState = GameFlowState.PAUSED;
        setMouseCapture(false);
        audioManager.setPausedDucked(true);
    }

    private void resumeGame() {
        flowState = GameFlowState.PLAYING;
        setMouseCapture(true);
        audioManager.setPausedDucked(false);
    }

    private void finishGame() {
        // Freeze the match result at time-up so the Game Over screen never changes afterward.
        finalPlayerScore = floorGrid.getPlayerPaintedCellCount();
        finalEnemyScore = floorGrid.getEnemyPaintedCellCount();
        finalTotalTiles = floorGrid.getTotalCellCount();
        finalPlayerPaintRate = floorGrid.getPlayerPaintRatePercent();
        finalEnemyPaintRate = floorGrid.getEnemyPaintRatePercent();
        finalPlayerSplats = playerSplatCount;
        finalEnemySplats = enemySplatCount;
        finalWeaponName = playerWeapon.getName();
        resultText = getResultText(finalPlayerScore, finalEnemyScore);
        remainingTime = 0f;
        player.setSwimming(false);
        flowState = GameFlowState.GAME_OVER;
        clearBullets();
        clearInkParticles();
        resetCombatFeedback();
        audioManager.setPausedDucked(false);
        audioManager.playGameOver();
        audioManager.playResultBgm();
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

        // Treat the currently equipped weapon as the next retry/title default too.
        playerWeapon = newWeapon;
        selectedTitleWeapon = newWeapon;
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
            player.triggerRecoil();
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

    private void handleCpuShooting() {
        for (EnemyCpu3D cpuCharacter : cpuCharacters) {
            while (cpuCharacter.canShoot()) {
                cpuCharacter.triggerRecoil();
                audioManager.playEnemyShoot();
                bullets.add(new Bullet3D(
                    cpuCharacter.getPosition(),
                    cpuCharacter.buildShotDirection(cpuShotDirection),
                    cpuCharacter.getWeaponConfig(),
                    cpuCharacter.getTeam().getBulletOwnerType(),
                    cpuCharacter.getTeam().getPaintCellState()
                ));
                cpuCharacter.consumeShotCooldown();
            }
        }
    }

    private void updateBullets(float delta) {
        Iterator<Bullet3D> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet3D bullet = iterator.next();
            boolean isStillActive = bullet.update(delta, floorGrid, stageObstacles);
            boolean hitCharacter = handleBulletCharacterHit(bullet);
            boolean hitSurface = !hitCharacter && bullet.consumeSurfaceImpact(bulletImpactPosition);
            if (hitSurface) {
                spawnImpactParticles(bulletImpactPosition, bullet.getPaintCellState());
            }
            if (!isStillActive || hitCharacter) {
                bullet.dispose();
                iterator.remove();
            }
        }
    }

    private boolean handleBulletCharacterHit(Bullet3D bullet) {
        if (bullet.getOwnerType() == Bullet3D.OWNER_PLAYER) {
            for (EnemyCpu3D enemyTarget : enemyTeamCpus) {
                if (enemyTarget.isSplatted() || enemyTarget.isInvincible()) {
                    continue;
                }
                if (!isHitOnFloorPlane(bullet.getPosition(), enemyTarget.getPosition(), enemyTarget.getHitRadius())) {
                    continue;
                }
                enemyHitFlashTimer = ENEMY_HIT_FLASH_DURATION;
                enemyTarget.triggerHitFlash();
                audioManager.playHit();
                if (enemyTarget.takeHit()) {
                    playerSplatCount++;
                    audioManager.playEnemySplatted();
                    showFeedbackMessage(enemyTarget.getDisplayName() + " Splatted!", FEEDBACK_SPLAT_COLOR, SPLAT_MESSAGE_DURATION);
                } else {
                    showFeedbackMessage("Hit!", FEEDBACK_HIT_COLOR, HIT_MESSAGE_DURATION);
                }
                return true;
            }
            return false;
        }

        if (bullet.getOwnerType() == Bullet3D.OWNER_ENEMY) {
            if (isPlayerCharacterHit(bullet.getPosition(), player)) {
                playerHitFlashTimer = PLAYER_HIT_FLASH_DURATION;
                player.triggerHitFlash();
                audioManager.playHit();
                if (player.takeHit()) {
                    enemySplatCount++;
                    audioManager.playPlayerSplatted();
                }
                return true;
            }
            if (!allyCpu.isSplatted()
                && !allyCpu.isInvincible()
                && isHitOnFloorPlane(bullet.getPosition(), allyCpu.getPosition(), allyCpu.getHitRadius())) {
                allyCpu.triggerHitFlash();
                audioManager.playHit();
                if (allyCpu.takeHit()) {
                    enemySplatCount++;
                    audioManager.playEnemySplatted();
                    showFeedbackMessage("Ally Splatted!", FEEDBACK_SPLAT_COLOR, SPLAT_MESSAGE_DURATION);
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

    private void clearInkParticles() {
        inkParticles.clear();
    }

    private void updateInkParticles(float delta) {
        Iterator<InkParticle3D> iterator = inkParticles.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().update(delta)) {
                iterator.remove();
            }
        }
    }

    private void spawnImpactParticles(Vector3 impactPosition, int paintCellState) {
        Color particleColor = getPaintEffectColor(paintCellState);
        for (int i = 0; i < IMPACT_PARTICLE_COUNT; i++) {
            if (inkParticles.size() >= MAX_INK_PARTICLES) {
                inkParticles.remove(0);
            }
            particleSpawnVelocity.set(
                MathUtils.random(-IMPACT_PARTICLE_HORIZONTAL_SPEED, IMPACT_PARTICLE_HORIZONTAL_SPEED),
                MathUtils.random(IMPACT_PARTICLE_VERTICAL_SPEED_MIN, IMPACT_PARTICLE_VERTICAL_SPEED_MAX),
                MathUtils.random(-IMPACT_PARTICLE_HORIZONTAL_SPEED, IMPACT_PARTICLE_HORIZONTAL_SPEED)
            );
            inkParticles.add(new InkParticle3D(
                impactPosition,
                particleSpawnVelocity,
                particleColor,
                MathUtils.random(IMPACT_PARTICLE_MIN_LIFETIME, IMPACT_PARTICLE_MAX_LIFETIME),
                MathUtils.random(IMPACT_PARTICLE_MIN_SIZE, IMPACT_PARTICLE_MAX_SIZE)
            ));
        }
    }

    private Color getPaintEffectColor(int paintCellState) {
        if (paintCellState == FloorGrid3D.CELL_STATE_ENEMY) {
            return MINIMAP_ENEMY_COLOR;
        }
        return MINIMAP_PLAYER_COLOR;
    }

    private boolean isHitOnFloorPlane(Vector3 bulletPosition, Vector3 targetPosition, float hitRadius) {
        float deltaX = bulletPosition.x - targetPosition.x;
        float deltaZ = bulletPosition.z - targetPosition.z;
        return deltaX * deltaX + deltaZ * deltaZ <= hitRadius * hitRadius;
    }

    private boolean isPlayerCharacterHit(Vector3 bulletPosition, Player3D targetPlayer) {
        return !targetPlayer.isSplatted()
            && !targetPlayer.isInvincible()
            && isHitOnFloorPlane(bulletPosition, targetPlayer.getPosition(), targetPlayer.getHitRadius());
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

    private void updateCpuCharacters(float delta) {
        for (EnemyCpu3D cpuCharacter : cpuCharacters) {
            Vector3 targetPosition = findTargetPositionFor(cpuCharacter);
            boolean targetable = targetPosition != null;
            cpuCharacter.update(
                delta,
                floorGrid,
                targetable ? targetPosition : cpuCharacter.getPosition(),
                targetable,
                stageObstacles
            );
        }
    }

    private Vector3 findTargetPositionFor(EnemyCpu3D cpuCharacter) {
        if (cpuCharacter.getTeam() == Team3D.PLAYER) {
            return findNearestCpuTarget(cpuCharacter.getPosition(), enemyTeamCpus);
        }

        Vector3 nearestTarget = null;
        float nearestDistanceSquared = Float.MAX_VALUE;

        if (!player.isSplatted()) {
            float distanceSquared = getDistanceSquared(cpuCharacter.getPosition(), player.getPosition());
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearestTarget = player.getPosition();
            }
        }

        if (!allyCpu.isSplatted()) {
            float distanceSquared = getDistanceSquared(cpuCharacter.getPosition(), allyCpu.getPosition());
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearestTarget = allyCpu.getPosition();
            }
        }

        return nearestTarget;
    }

    private Vector3 findNearestCpuTarget(Vector3 fromPosition, ArrayList<EnemyCpu3D> candidates) {
        Vector3 nearestTarget = null;
        float nearestDistanceSquared = Float.MAX_VALUE;
        for (EnemyCpu3D candidate : candidates) {
            if (candidate.isSplatted()) {
                continue;
            }
            float distanceSquared = getDistanceSquared(fromPosition, candidate.getPosition());
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearestTarget = candidate.getPosition();
            }
        }
        return nearestTarget;
    }

    private float getDistanceSquared(Vector3 fromPosition, Vector3 toPosition) {
        float deltaX = toPosition.x - fromPosition.x;
        float deltaZ = toPosition.z - fromPosition.z;
        return deltaX * deltaX + deltaZ * deltaZ;
    }

    private void applyCpuDifficulty() {
        for (EnemyCpu3D cpuCharacter : cpuCharacters) {
            cpuCharacter.setDifficulty(selectedCpuDifficulty);
        }
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

    private void resetTitleMenuState() {
        titleMenuScreen = TitleMenuScreen.MAIN;
        titleMenuIndex = 0;
        weaponMenuIndex = getWeaponMenuIndex(selectedTitleWeapon);
        stageMenuIndex = selectedStageType.ordinal();
        difficultyMenuIndex = selectedCpuDifficulty.ordinal();
    }

    private void rebuildStage(StageConfig3D stageConfig) {
        if (floorGrid != null) {
            floorGrid.dispose();
        }
        if (stageObstacles != null) {
            stageObstacles.dispose();
        }

        currentStageConfig = stageConfig;
        floorGrid = new FloorGrid3D(stageConfig.getColumns(), stageConfig.getRows());
        stageObstacles = new StageObstacles3D(stageConfig);
        player.reset(stageConfig);
        for (EnemyCpu3D cpuCharacter : cpuCharacters) {
            cpuCharacter.reset(floorGrid, stageConfig);
        }
    }

    private boolean isTitleUpPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W);
    }

    private boolean isTitleDownPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S);
    }

    private boolean isTitleBackPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE);
    }

    private int wrapMenuIndex(int index, int itemCount) {
        if (itemCount <= 0) {
            return 0;
        }
        return (index % itemCount + itemCount) % itemCount;
    }

    private WeaponConfig3D getWeaponByMenuIndex(int index) {
        switch (index) {
            case 1:
                return WeaponConfig3D.SHORT_PAINTER;
            case 2:
                return WeaponConfig3D.LONG_SHOOTER;
            default:
                return WeaponConfig3D.BASIC_SHOOTER;
        }
    }

    private int getWeaponMenuIndex(WeaponConfig3D weapon) {
        if (weapon == WeaponConfig3D.SHORT_PAINTER) {
            return 1;
        }
        if (weapon == WeaponConfig3D.LONG_SHOOTER) {
            return 2;
        }
        return 0;
    }

    private void resetMatchResult() {
        resultText = "";
        finalPlayerScore = 0;
        finalEnemyScore = 0;
        finalTotalTiles = floorGrid != null ? floorGrid.getTotalCellCount() : 0;
        finalPlayerPaintRate = 0f;
        finalEnemyPaintRate = 0f;
        finalPlayerSplats = 0;
        finalEnemySplats = 0;
        finalWeaponName = WeaponConfig3D.BASIC_SHOOTER.getName();
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
