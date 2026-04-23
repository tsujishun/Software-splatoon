package io.github.shunsuke.paintgame;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {
    private static final int CELL_STATE_EMPTY = 0;
    private static final int CELL_STATE_PLAYER = 1;
    private static final int CELL_STATE_ENEMY = 2;
    private static final String GAME_TITLE = "Paint Battle Prototype";
    private static final Color EMPTY_CELL_COLOR = new Color(0.18f, 0.18f, 0.22f, 1f);
    private static final Color PLAYER_CELL_COLOR = new Color(0.25f, 0.7f, 0.95f, 1f);
    private static final Color ENEMY_CELL_COLOR = new Color(0.95f, 0.45f, 0.7f, 1f);
    private static final float GAME_DURATION_SECONDS = 60f;
    private static final float COUNTDOWN_TOTAL_SECONDS = 4f;
    private static final float PLAYER_SIZE = 32f;
    private static final float PLAYER_SPEED = 220f;
    private static final float ENEMY_SIZE = 30f;
    private static final float ENEMY_SPEED = 150f;
    private static final float ENEMY_DIRECTION_CHANGE_MIN = 1.0f;
    private static final float ENEMY_DIRECTION_CHANGE_MAX = 2.2f;
    private static final float ENEMY_SHOT_INTERVAL = 1.1f;
    private static final float BULLET_SIZE = 10f;
    private static final float BULLET_SPEED = 360f;
    private static final float FACING_MARKER_SIZE = 8f;
    private static final float GRID_CELL_SIZE = 32f;
    private static final float HUD_PADDING = 12f;
    private static final float GAME_OVER_TEXT_X = 230f;
    private static final float GAME_OVER_TEXT_Y = 260f;
    private static final float RESULT_TEXT_OFFSET_X = 50f;
    private static final float RESULT_TEXT_OFFSET_Y = 0f;
    private static final float SCORE_TEXT_OFFSET_X = 95f;
    private static final float SCORE_TEXT_OFFSET_Y = -25f;
    private static final float RESTART_TEXT_X = 190f;
    private static final float RESTART_TEXT_Y = 180f;
    private static final float TITLE_TEXT_OFFSET_X = 90f;
    private static final float TITLE_TEXT_OFFSET_Y = 40f;
    private static final float TITLE_PROMPT_OFFSET_X = 85f;
    private static final float TITLE_PROMPT_OFFSET_Y = 5f;
    private static final float COUNTDOWN_TEXT_OFFSET_X = 12f;
    private static final float COUNTDOWN_TEXT_OFFSET_Y = 0f;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private final ArrayList<Bullet> bullets = new ArrayList<>();

    private float worldWidth;
    private float worldHeight;
    private float playerX;
    private float playerY;
    private float facingX = 0f;
    private float facingY = 1f;
    private float enemyX;
    private float enemyY;
    private float enemyDirectionX = -1f;
    private float enemyDirectionY = 0f;
    private float enemyDirectionChangeTimer;
    private float enemyShotTimer;
    private int gridColumns;
    private int gridRows;
    private int[][] cellStates;
    private int playerPaintedCellCount;
    private int enemyPaintedCellCount;
    private int currentPaintState;
    private float remainingTime;
    private boolean gameOver;
    private String gameResultText = "";
    private int finalPlayerPaintedCellCount;
    private int finalEnemyPaintedCellCount;
    private boolean titleScreen = true;
    private boolean countdownActive;
    private float countdownTimer;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        goToTitleScreen();
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if (width <= 0 || height <= 0) {
            return;
        }

        worldWidth = width;
        worldHeight = height;

        if (camera == null) {
            camera = new OrthographicCamera();
        }
        camera.setToOrtho(false, worldWidth, worldHeight);

        initializeFloorGrid();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        handleRestart();
        updateGame(delta);

        Gdx.gl.glClearColor(0.12f, 0.12f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        spriteBatch.setProjectionMatrix(camera.combined);

        if (titleScreen) {
            drawTitleScreen();
            return;
        }

        drawFloorGrid();
        drawActors();
        drawHud();
    }

    private void updateGame(float delta) {
        if (titleScreen) {
            handleStartFromTitle();
            return;
        }

        if (countdownActive) {
            updateCountdown(delta);
            return;
        }

        if (gameOver) {
            return;
        }

        remainingTime -= delta;
        if (remainingTime <= 0f) {
            remainingTime = 0f;
            finishGame();
            return;
        }

        handleColorToggle();
        updatePlayerPosition(delta);
        updateEnemy(delta);
        updateBullets(delta);
        handleShooting();
        handleEnemyShooting(delta);
    }

    private void handleStartFromTitle() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            startCountdown();
        }
    }

    private void updateCountdown(float delta) {
        countdownTimer -= delta;
        if (countdownTimer <= 0f) {
            countdownTimer = 0f;
            countdownActive = false;
        }
    }

    private void updatePlayerPosition(float delta) {
        float moveX = 0f;
        float moveY = 0f;

        if (isLeftPressed()) {
            moveX -= 1f;
        }
        if (isRightPressed()) {
            moveX += 1f;
        }
        if (isDownPressed()) {
            moveY -= 1f;
        }
        if (isUpPressed()) {
            moveY += 1f;
        }

        // Normalize diagonal movement so it is not faster than horizontal or vertical movement.
        if (moveX != 0f || moveY != 0f) {
            float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);
            moveX /= length;
            moveY /= length;

            facingX = moveX;
            facingY = moveY;

            playerX += moveX * PLAYER_SPEED * delta;
            playerY += moveY * PLAYER_SPEED * delta;
        }

        playerX = MathUtils.clamp(playerX, 0f, worldWidth - PLAYER_SIZE);
        playerY = MathUtils.clamp(playerY, 0f, worldHeight - PLAYER_SIZE);
    }

    private void handleShooting() {
        if (!Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            return;
        }

        float bulletX = playerX + PLAYER_SIZE / 2f;
        float bulletY = playerY + PLAYER_SIZE / 2f;
        bullets.add(new Bullet(bulletX, bulletY, facingX, facingY, currentPaintState, false));
    }

    private void updateBullets(float delta) {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.x += bullet.directionX * BULLET_SPEED * delta;
            bullet.y += bullet.directionY * BULLET_SPEED * delta;

            paintCellAt(bullet.x, bullet.y, bullet.paintState);

            // Remove bullets once they leave the visible play area.
            if (bullet.x + BULLET_SIZE / 2f < 0f
                || bullet.x - BULLET_SIZE / 2f > worldWidth
                || bullet.y + BULLET_SIZE / 2f < 0f
                || bullet.y - BULLET_SIZE / 2f > worldHeight) {
                bullets.remove(i);
            }
        }
    }

    private void updateEnemy(float delta) {
        enemyDirectionChangeTimer -= delta;
        if (enemyDirectionChangeTimer <= 0f) {
            chooseEnemyRandomDirection();
        }

        // The CPU enemy just wanders around and changes direction when it gets near a wall.
        float nextEnemyX = enemyX + enemyDirectionX * ENEMY_SPEED * delta;
        float nextEnemyY = enemyY + enemyDirectionY * ENEMY_SPEED * delta;
        if (!isInsideActorBounds(nextEnemyX, nextEnemyY, ENEMY_SIZE)) {
            chooseEnemyRandomDirection();
            nextEnemyX = enemyX + enemyDirectionX * ENEMY_SPEED * delta;
            nextEnemyY = enemyY + enemyDirectionY * ENEMY_SPEED * delta;
        }

        enemyX = MathUtils.clamp(nextEnemyX, 0f, worldWidth - ENEMY_SIZE);
        enemyY = MathUtils.clamp(nextEnemyY, 0f, worldHeight - ENEMY_SIZE);
    }

    private void handleEnemyShooting(float delta) {
        enemyShotTimer -= delta;
        if (enemyShotTimer > 0f) {
            return;
        }

        float bulletX = enemyX + ENEMY_SIZE / 2f;
        float bulletY = enemyY + ENEMY_SIZE / 2f;
        bullets.add(new Bullet(bulletX, bulletY, enemyDirectionX, enemyDirectionY, CELL_STATE_ENEMY, true));
        enemyShotTimer = ENEMY_SHOT_INTERVAL;
    }

    private void chooseEnemyRandomDirection() {
        for (int attempt = 0; attempt < 8; attempt++) {
            float randomAngle = MathUtils.random(0f, MathUtils.PI2);
            float directionX = MathUtils.cos(randomAngle);
            float directionY = MathUtils.sin(randomAngle);
            float testEnemyX = enemyX + directionX * ENEMY_SPEED * 0.2f;
            float testEnemyY = enemyY + directionY * ENEMY_SPEED * 0.2f;
            if (isInsideActorBounds(testEnemyX, testEnemyY, ENEMY_SIZE)) {
                enemyDirectionX = directionX;
                enemyDirectionY = directionY;
                break;
            }
        }

        enemyDirectionChangeTimer = MathUtils.random(ENEMY_DIRECTION_CHANGE_MIN, ENEMY_DIRECTION_CHANGE_MAX);
    }

    private float getFacingMarkerX() {
        return playerX + PLAYER_SIZE / 2f + facingX * (PLAYER_SIZE / 2f);
    }

    private float getFacingMarkerY() {
        return playerY + PLAYER_SIZE / 2f + facingY * (PLAYER_SIZE / 2f);
    }

    private void initializeFloorGrid() {
        gridColumns = Math.max(1, MathUtils.ceil(worldWidth / GRID_CELL_SIZE));
        gridRows = Math.max(1, MathUtils.ceil(worldHeight / GRID_CELL_SIZE));
        cellStates = new int[gridRows][gridColumns];
        playerPaintedCellCount = 0;
        enemyPaintedCellCount = 0;
    }

    private void drawFloorGrid() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int row = 0; row < gridRows; row++) {
            for (int column = 0; column < gridColumns; column++) {
                float cellX = column * GRID_CELL_SIZE;
                float cellY = row * GRID_CELL_SIZE;
                float cellWidth = Math.min(GRID_CELL_SIZE, worldWidth - cellX);
                float cellHeight = Math.min(GRID_CELL_SIZE, worldHeight - cellY);

                if (cellWidth <= 0f || cellHeight <= 0f) {
                    continue;
                }

                shapeRenderer.setColor(getCellColor(cellStates[row][column]));
                shapeRenderer.rect(cellX, cellY, cellWidth, cellHeight);
            }
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.1f, 0.1f, 0.14f, 1f);
        for (int row = 0; row < gridRows; row++) {
            for (int column = 0; column < gridColumns; column++) {
                float cellX = column * GRID_CELL_SIZE;
                float cellY = row * GRID_CELL_SIZE;
                float cellWidth = Math.min(GRID_CELL_SIZE, worldWidth - cellX);
                float cellHeight = Math.min(GRID_CELL_SIZE, worldHeight - cellY);

                if (cellWidth <= 0f || cellHeight <= 0f) {
                    continue;
                }

                shapeRenderer.rect(cellX, cellY, cellWidth, cellHeight);
            }
        }
        shapeRenderer.end();
    }

    private void drawActors() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Bullet bullet : bullets) {
            shapeRenderer.setColor(getCellColor(bullet.paintState));
            if (bullet.fromEnemy) {
                shapeRenderer.rect(
                    bullet.x - BULLET_SIZE / 2f,
                    bullet.y - BULLET_SIZE / 2f,
                    BULLET_SIZE,
                    BULLET_SIZE
                );
            } else {
                shapeRenderer.circle(bullet.x, bullet.y, BULLET_SIZE / 2f);
            }
        }

        shapeRenderer.setColor(0.2f, 0.8f, 0.9f, 1f);
        shapeRenderer.rect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);

        shapeRenderer.setColor(ENEMY_CELL_COLOR);
        shapeRenderer.circle(enemyX + ENEMY_SIZE / 2f, enemyY + ENEMY_SIZE / 2f, ENEMY_SIZE / 2f);

        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.circle(getFacingMarkerX(), getFacingMarkerY(), FACING_MARKER_SIZE / 2f);
        shapeRenderer.circle(
            enemyX + ENEMY_SIZE / 2f + enemyDirectionX * (ENEMY_SIZE / 2f),
            enemyY + ENEMY_SIZE / 2f + enemyDirectionY * (ENEMY_SIZE / 2f),
            FACING_MARKER_SIZE / 2f
        );
        shapeRenderer.end();
    }

    private void paintCellAt(float worldX, float worldY, int paintState) {
        if (cellStates == null || !isInsideWorld(worldX, worldY)) {
            return;
        }

        // Convert the bullet position to a row and column in the floor grid.
        int column = Math.min(gridColumns - 1, (int) (worldX / GRID_CELL_SIZE));
        int row = Math.min(gridRows - 1, (int) (worldY / GRID_CELL_SIZE));
        int previousState = cellStates[row][column];
        if (previousState == paintState) {
            return;
        }

        // When a cell changes color, move its count from the old color to the new color.
        removePaintedCellCount(previousState);
        cellStates[row][column] = paintState;
        addPaintedCellCount(paintState);
    }

    private boolean isInsideWorld(float worldX, float worldY) {
        return worldX >= 0f && worldX < worldWidth && worldY >= 0f && worldY < worldHeight;
    }

    private boolean isInsideActorBounds(float actorX, float actorY, float actorSize) {
        return actorX >= 0f
            && actorX <= worldWidth - actorSize
            && actorY >= 0f
            && actorY <= worldHeight - actorSize;
    }

    private void drawHud() {
        spriteBatch.begin();
        font.draw(spriteBatch, "Painted: " + getTotalPaintedCellCount(), HUD_PADDING, worldHeight - HUD_PADDING);
        font.draw(spriteBatch, "Player: " + playerPaintedCellCount, HUD_PADDING, worldHeight - HUD_PADDING - 20f);
        font.draw(spriteBatch, "Enemy: " + enemyPaintedCellCount, HUD_PADDING, worldHeight - HUD_PADDING - 40f);
        font.draw(spriteBatch, "Color: " + getPaintStateName(currentPaintState), HUD_PADDING, worldHeight - HUD_PADDING - 60f);
        font.draw(spriteBatch, "Time: " + MathUtils.ceil(remainingTime), worldWidth - 90f, worldHeight - HUD_PADDING);
        if (countdownActive) {
            font.draw(
                spriteBatch,
                getCountdownText(),
                worldWidth / 2f - COUNTDOWN_TEXT_OFFSET_X,
                worldHeight / 2f + COUNTDOWN_TEXT_OFFSET_Y
            );
        }
        if (gameOver) {
            font.draw(spriteBatch, "Game Over", GAME_OVER_TEXT_X, GAME_OVER_TEXT_Y);
            font.draw(spriteBatch, gameResultText, worldWidth / 2f - RESULT_TEXT_OFFSET_X, worldHeight / 2f + RESULT_TEXT_OFFSET_Y);
            font.draw(
                spriteBatch,
                "Player " + finalPlayerPaintedCellCount + " / Enemy " + finalEnemyPaintedCellCount,
                worldWidth / 2f - SCORE_TEXT_OFFSET_X,
                worldHeight / 2f + SCORE_TEXT_OFFSET_Y
            );
            font.draw(spriteBatch, "Press R to Restart", RESTART_TEXT_X, RESTART_TEXT_Y);
        }
        spriteBatch.end();
    }

    private void drawTitleScreen() {
        spriteBatch.begin();
        font.draw(spriteBatch, GAME_TITLE, worldWidth / 2f - TITLE_TEXT_OFFSET_X, worldHeight / 2f + TITLE_TEXT_OFFSET_Y);
        font.draw(
            spriteBatch,
            "Press Enter to Start",
            worldWidth / 2f - TITLE_PROMPT_OFFSET_X,
            worldHeight / 2f + TITLE_PROMPT_OFFSET_Y
        );
        spriteBatch.end();
    }

    private void handleColorToggle() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            if (currentPaintState == CELL_STATE_PLAYER) {
                currentPaintState = CELL_STATE_ENEMY;
            } else {
                currentPaintState = CELL_STATE_PLAYER;
            }
        }
    }

    private void handleRestart() {
        if (!Gdx.input.isKeyJustPressed(Input.Keys.R) || titleScreen) {
            return;
        }

        if (gameOver) {
            goToTitleScreen();
        } else {
            startCountdown();
        }
    }

    private void resetMatchState() {
        playerX = (worldWidth - PLAYER_SIZE) / 2f;
        playerY = (worldHeight - PLAYER_SIZE) / 2f;
        facingX = 0f;
        facingY = 1f;
        enemyX = MathUtils.clamp(worldWidth - ENEMY_SIZE - 80f, 0f, worldWidth - ENEMY_SIZE);
        enemyY = MathUtils.clamp(worldHeight - ENEMY_SIZE - 80f, 0f, worldHeight - ENEMY_SIZE);
        chooseEnemyRandomDirection();
        enemyShotTimer = ENEMY_SHOT_INTERVAL;
        bullets.clear();
        initializeFloorGrid();
        currentPaintState = CELL_STATE_PLAYER;
        remainingTime = GAME_DURATION_SECONDS;
        gameOver = false;
        gameResultText = "";
        finalPlayerPaintedCellCount = 0;
        finalEnemyPaintedCellCount = 0;
    }

    private void goToTitleScreen() {
        resetMatchState();
        titleScreen = true;
        countdownActive = false;
        countdownTimer = COUNTDOWN_TOTAL_SECONDS;
    }

    private void startCountdown() {
        resetMatchState();
        titleScreen = false;
        countdownActive = true;
        countdownTimer = COUNTDOWN_TOTAL_SECONDS;
    }

    private void finishGame() {
        gameOver = true;
        countdownActive = false;
        bullets.clear();
        finalPlayerPaintedCellCount = playerPaintedCellCount;
        finalEnemyPaintedCellCount = enemyPaintedCellCount;

        // Decide the result once at time-up and keep it fixed until restart.
        if (finalPlayerPaintedCellCount > finalEnemyPaintedCellCount) {
            gameResultText = "Player Wins";
        } else if (finalEnemyPaintedCellCount > finalPlayerPaintedCellCount) {
            gameResultText = "Enemy Wins";
        } else {
            gameResultText = "Draw";
        }
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

    private int getTotalPaintedCellCount() {
        return playerPaintedCellCount + enemyPaintedCellCount;
    }

    private void addPaintedCellCount(int cellState) {
        if (cellState == CELL_STATE_PLAYER) {
            playerPaintedCellCount++;
        } else if (cellState == CELL_STATE_ENEMY) {
            enemyPaintedCellCount++;
        }
    }

    private void removePaintedCellCount(int cellState) {
        if (cellState == CELL_STATE_PLAYER) {
            playerPaintedCellCount--;
        } else if (cellState == CELL_STATE_ENEMY) {
            enemyPaintedCellCount--;
        }
    }

    private Color getCellColor(int cellState) {
        if (cellState == CELL_STATE_PLAYER) {
            return PLAYER_CELL_COLOR;
        }
        if (cellState == CELL_STATE_ENEMY) {
            return ENEMY_CELL_COLOR;
        }
        return EMPTY_CELL_COLOR;
    }

    private String getPaintStateName(int cellState) {
        if (cellState == CELL_STATE_PLAYER) {
            return "Player";
        }
        if (cellState == CELL_STATE_ENEMY) {
            return "Enemy";
        }
        return "Empty";
    }

    private boolean isLeftPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
    }

    private boolean isRightPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
    }

    private boolean isUpPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
    }

    private boolean isDownPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }
        if (font != null) {
            font.dispose();
        }
    }

    private static class Bullet {
        private float x;
        private float y;
        private final float directionX;
        private final float directionY;
        private final int paintState;
        private final boolean fromEnemy;

        private Bullet(float x, float y, float directionX, float directionY, int paintState, boolean fromEnemy) {
            this.x = x;
            this.y = y;
            this.directionX = directionX;
            this.directionY = directionY;
            this.paintState = paintState;
            this.fromEnemy = fromEnemy;
        }
    }
}
