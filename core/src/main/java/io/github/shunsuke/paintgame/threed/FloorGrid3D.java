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
    private static final Color EMPTY_TILE_COLOR = new Color(0.2f, 0.22f, 0.27f, 1f);
    private static final Color PLAYER_TILE_COLOR = new Color(0.25f, 0.7f, 0.95f, 1f);
    private static final Color ENEMY_TILE_COLOR = new Color(0.95f, 0.45f, 0.7f, 1f);

    private final int columns;
    private final int rows;
    private final int[][] cellStates;
    private final ArrayList<Tile3D> tiles = new ArrayList<>();
    private final Model tileModel;

    private int playerPaintedCellCount;
    private int enemyPaintedCellCount;

    public FloorGrid3D(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        this.cellStates = new int[rows][columns];

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

    public void reset() {
        playerPaintedCellCount = 0;
        enemyPaintedCellCount = 0;

        for (Tile3D tile : tiles) {
            cellStates[tile.row][tile.column] = CELL_STATE_EMPTY;
            setInstanceColor(tile.instance, EMPTY_TILE_COLOR);
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
        setInstanceColor(tile.instance, getTileColor(cellState));
    }

    public int getPlayerPaintedCellCount() {
        return playerPaintedCellCount;
    }

    public int getEnemyPaintedCellCount() {
        return enemyPaintedCellCount;
    }

    public float getWorldWidth() {
        return columns * TILE_SIZE;
    }

    public float getWorldDepth() {
        return rows * TILE_SIZE;
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

    private void setInstanceColor(ModelInstance instance, Color color) {
        instance.materials.get(0).set(ColorAttribute.createDiffuse(color));
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

    private static class Tile3D {
        private final int row;
        private final int column;
        private final ModelInstance instance;

        private Tile3D(int row, int column, ModelInstance instance) {
            this.row = row;
            this.column = column;
            this.instance = instance;
        }
    }
}
