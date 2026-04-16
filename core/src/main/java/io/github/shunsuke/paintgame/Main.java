package io.github.shunsuke.paintgame;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {
    private static final float PLAYER_SIZE = 32f;
    private static final float PLAYER_SPEED = 220f;
    private static final float BULLET_SIZE = 10f;
    private static final float BULLET_SPEED = 360f;
    private static final float FACING_MARKER_SIZE = 8f;

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private final ArrayList<Bullet> bullets = new ArrayList<>();

    private float worldWidth;
    private float worldHeight;
    private float playerX;
    private float playerY;
    private float facingX = 0f;
    private float facingY = 1f;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        playerX = (worldWidth - PLAYER_SIZE) / 2f;
        playerY = (worldHeight - PLAYER_SIZE) / 2f;
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
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        updatePlayerPosition(delta);
        updateBullets(delta);
        handleShooting();

        Gdx.gl.glClearColor(0.12f, 0.12f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.18f, 0.18f, 0.22f, 1f);
        shapeRenderer.rect(0f, 0f, worldWidth, worldHeight);

        shapeRenderer.setColor(1f, 0.85f, 0.2f, 1f);
        for (Bullet bullet : bullets) {
            shapeRenderer.circle(bullet.x, bullet.y, BULLET_SIZE / 2f);
        }

        shapeRenderer.setColor(0.2f, 0.8f, 0.9f, 1f);
        shapeRenderer.rect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);

        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.circle(getFacingMarkerX(), getFacingMarkerY(), FACING_MARKER_SIZE / 2f);
        shapeRenderer.end();
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
        bullets.add(new Bullet(bulletX, bulletY, facingX, facingY));
    }

    private void updateBullets(float delta) {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.x += bullet.directionX * BULLET_SPEED * delta;
            bullet.y += bullet.directionY * BULLET_SPEED * delta;

            // Remove bullets once they leave the visible play area.
            if (bullet.x + BULLET_SIZE / 2f < 0f
                || bullet.x - BULLET_SIZE / 2f > worldWidth
                || bullet.y + BULLET_SIZE / 2f < 0f
                || bullet.y - BULLET_SIZE / 2f > worldHeight) {
                bullets.remove(i);
            }
        }
    }

    private float getFacingMarkerX() {
        return playerX + PLAYER_SIZE / 2f + facingX * (PLAYER_SIZE / 2f);
    }

    private float getFacingMarkerY() {
        return playerY + PLAYER_SIZE / 2f + facingY * (PLAYER_SIZE / 2f);
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
    }

    private static class Bullet {
        private float x;
        private float y;
        private final float directionX;
        private final float directionY;

        private Bullet(float x, float y, float directionX, float directionY) {
            this.x = x;
            this.y = y;
            this.directionX = directionX;
            this.directionY = directionY;
        }
    }
}
