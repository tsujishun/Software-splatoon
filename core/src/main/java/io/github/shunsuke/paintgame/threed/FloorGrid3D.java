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
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;

/**
 * First 3D step: keep the same grid idea from the 2D version,
 * but render each floor cell as a simple 3D tile.
 */
public class FloorGrid3D implements Disposable {
    public static final int CELL_STATE_EMPTY = 0;
    public static final int CELL_STATE_PLAYER = 1;
    public static final int CELL_STATE_ENEMY = 2;

    private static final float TILE_SIZE = 1f;
    private static final float TILE_HEIGHT = 0.15f;
    private static final float TILE_GAP = 0.05f;
    private static final float PAINT_FLASH_DURATION = 0.22f;
    private static final float PAINT_RADIUS_VISUAL_BONUS = 0.08f;
    private static final float PAINT_FLASH_BRIGHTNESS = 0.45f;
    private static final Color EMPTY_TILE_COLOR = new Color(0.16f, 0.18f, 0.23f, 1f);
    private static final Color PLAYER_TILE_COLOR = new Color(0.16f, 0.8f, 1f, 1f);
    private static final Color ENEMY_TILE_COLOR = new Color(1f, 0.36f, 0.66f, 1f);

    private final int columns;
    private final int rows;
    private final int[][] cellStates;
    private final ArrayList<Tile3D> tiles = new ArrayList<>();
    private final Model tileModel;
    private final float minX;
    private final float maxX;
    private final float minZ;
    private final float maxZ;

    private int playerPaintedCellCount;
    private int enemyPaintedCellCount;

    public FloorGrid3D(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        this.cellStates = new int[rows][columns];
        this.minX = -columns * TILE_SIZE / 2f;
        this.maxX = columns * TILE_SIZE / 2f;
        this.minZ = -rows * TILE_SIZE / 2f;
        this.maxZ = rows * TILE_SIZE / 2f;

        ModelBuilder modelBuilder = new ModelBuilder();
        tileModel = modelBuilder.createBox(
            TILE_SIZE - TILE_GAP,
            TILE_HEIGHT,
            TILE_SIZE - TILE_GAP,
            new Material(ColorAttribute.createDiffuse(EMPTY_TILE_COLOR)),
            Usage.Position | Usage.Normal
        );

        buildTiles();
        reset();
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        for (Tile3D tile : tiles) {
            modelBatch.render(tile.instance, environment);
        }
    }

    public void update(float delta) {
        for (Tile3D tile : tiles) {
            if (tile.paintFlashTimer <= 0f) {
                continue;
            }

            tile.paintFlashTimer = Math.max(0f, tile.paintFlashTimer - delta);
            applyTileColor(tile, cellStates[tile.row][tile.column], tile.paintFlashTimer / PAINT_FLASH_DURATION);
        }
    }

    public void reset() {
        playerPaintedCellCount = 0;
        enemyPaintedCellCount = 0;

        for (Tile3D tile : tiles) {
            cellStates[tile.row][tile.column] = CELL_STATE_EMPTY;
            tile.paintFlashTimer = 0f;
            applyTileColor(tile, CELL_STATE_EMPTY, 0f);
        }
    }

    public void setCellState(int row, int column, int cellState) {
        if (row < 0 || row >= rows || column < 0 || column >= columns) {
            return;
        }

        int previousState = cellStates[row][column];
        if (previousState == cellState) {
            return;
        }

        removePaintedCellCount(previousState);
        cellStates[row][column] = cellState;
        addPaintedCellCount(cellState);

        Tile3D tile = tiles.get(row * columns + column);
        tile.paintFlashTimer = PAINT_FLASH_DURATION;
        applyTileColor(tile, cellState, 1f);
    }

    public void paintAtWorldPosition(float worldX, float worldZ, int cellState) {
        paintAtWorldPosition(worldX, worldZ, 0f, cellState);
    }

    public void paintAtWorldPosition(float worldX, float worldZ, float radius, int cellState) {
        if (worldX < minX || worldX >= maxX || worldZ < minZ || worldZ >= maxZ) {
            return;
        }

        float effectiveRadius = radius;
        if (effectiveRadius > 0f) {
            effectiveRadius += PAINT_RADIUS_VISUAL_BONUS;
        }

        int minColumn = MathUtils.clamp((int) ((worldX - effectiveRadius - minX) / TILE_SIZE), 0, columns - 1);
        int maxColumn = MathUtils.clamp((int) ((worldX + effectiveRadius - minX) / TILE_SIZE), 0, columns - 1);
        int minRow = MathUtils.clamp((int) ((worldZ - effectiveRadius - minZ) / TILE_SIZE), 0, rows - 1);
        int maxRow = MathUtils.clamp((int) ((worldZ + effectiveRadius - minZ) / TILE_SIZE), 0, rows - 1);

        for (int row = minRow; row <= maxRow; row++) {
            for (int column = minColumn; column <= maxColumn; column++) {
                if (doesCircleTouchTile(worldX, worldZ, effectiveRadius, row, column)) {
                    setCellState(row, column, cellState);
                }
            }
        }
    }

    public int getPlayerPaintedCellCount() {
        return playerPaintedCellCount;
    }

    public int getEnemyPaintedCellCount() {
        return enemyPaintedCellCount;
    }

    public int getPaintedCellCount() {
        return playerPaintedCellCount + enemyPaintedCellCount;
    }

    public int getTotalCellCount() {
        return rows * columns;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public float getPaintRatePercent() {
        int totalCellCount = getTotalCellCount();
        if (totalCellCount <= 0) {
            return 0f;
        }

        return getPaintedCellCount() * 100f / totalCellCount;
    }

    public float getPlayerPaintRatePercent() {
        int totalCellCount = getTotalCellCount();
        if (totalCellCount <= 0) {
            return 0f;
        }

        return playerPaintedCellCount * 100f / totalCellCount;
    }

    public float getEnemyPaintRatePercent() {
        int totalCellCount = getTotalCellCount();
        if (totalCellCount <= 0) {
            return 0f;
        }

        return enemyPaintedCellCount * 100f / totalCellCount;
    }

    public int getCellStateAtWorldPosition(float worldX, float worldZ) {
        if (worldX < minX || worldX >= maxX || worldZ < minZ || worldZ >= maxZ) {
            return CELL_STATE_EMPTY;
        }

        int column = MathUtils.clamp((int) ((worldX - minX) / TILE_SIZE), 0, columns - 1);
        int row = MathUtils.clamp((int) ((worldZ - minZ) / TILE_SIZE), 0, rows - 1);
        return cellStates[row][column];
    }

    public int getCellState(int row, int column) {
        if (row < 0 || row >= rows || column < 0 || column >= columns) {
            return CELL_STATE_EMPTY;
        }

        return cellStates[row][column];
    }

    public float getWorldWidth() {
        return columns * TILE_SIZE;
    }

    public float getWorldDepth() {
        return rows * TILE_SIZE;
    }

    public float getMinX() {
        return minX;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMinZ() {
        return minZ;
    }

    public float getMaxZ() {
        return maxZ;
    }

    @Override
    public void dispose() {
        tileModel.dispose();
    }

    private void buildTiles() {
        float startX = -((columns - 1) * TILE_SIZE) / 2f;
        float startZ = -((rows - 1) * TILE_SIZE) / 2f;

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                ModelInstance instance = new ModelInstance(tileModel);
                instance.transform.setToTranslation(
                    startX + column * TILE_SIZE,
                    -TILE_HEIGHT / 2f,
                    startZ + row * TILE_SIZE
                );
                tiles.add(new Tile3D(row, column, instance));
            }
        }
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

    private boolean doesCircleTouchTile(float worldX, float worldZ, float radius, int row, int column) {
        float tileMinX = minX + column * TILE_SIZE;
        float tileMaxX = tileMinX + TILE_SIZE;
        float tileMinZ = minZ + row * TILE_SIZE;
        float tileMaxZ = tileMinZ + TILE_SIZE;

        float nearestX = MathUtils.clamp(worldX, tileMinX, tileMaxX);
        float nearestZ = MathUtils.clamp(worldZ, tileMinZ, tileMaxZ);
        float deltaX = worldX - nearestX;
        float deltaZ = worldZ - nearestZ;
        return deltaX * deltaX + deltaZ * deltaZ <= radius * radius;
    }

    private Color getTileColor(int cellState) {
        if (cellState == CELL_STATE_PLAYER) {
            return PLAYER_TILE_COLOR;
        }
        if (cellState == CELL_STATE_ENEMY) {
            return ENEMY_TILE_COLOR;
        }
        return EMPTY_TILE_COLOR;
    }

    private void applyTileColor(Tile3D tile, int cellState, float flashRatio) {
        tile.displayColor.set(getTileColor(cellState));
        if (flashRatio > 0f) {
            tile.displayColor.lerp(Color.WHITE, flashRatio * PAINT_FLASH_BRIGHTNESS);
        }
        tile.instance.materials.get(0).set(ColorAttribute.createDiffuse(tile.displayColor));
    }

    private static class Tile3D {
        private final int row;
        private final int column;
        private final ModelInstance instance;
        private final Color displayColor = new Color();
        private float paintFlashTimer;

        private Tile3D(int row, int column, ModelInstance instance) {
            this.row = row;
            this.column = column;
            this.instance = instance;
        }
    }
}
