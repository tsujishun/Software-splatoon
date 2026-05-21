package io.github.shunsuke.paintgame.threed;

/**
 * Small team definition for the beginner-friendly 3D prototype.
 * This keeps paint color and bullet ownership in one place.
 */
public enum Team3D {
    PLAYER("Player Team", FloorGrid3D.CELL_STATE_PLAYER, Bullet3D.OWNER_PLAYER),
    ENEMY("Enemy Team", FloorGrid3D.CELL_STATE_ENEMY, Bullet3D.OWNER_ENEMY);

    private final String displayName;
    private final int paintCellState;
    private final int bulletOwnerType;

    Team3D(String displayName, int paintCellState, int bulletOwnerType) {
        this.displayName = displayName;
        this.paintCellState = paintCellState;
        this.bulletOwnerType = bulletOwnerType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPaintCellState() {
        return paintCellState;
    }

    public int getBulletOwnerType() {
        return bulletOwnerType;
    }

    public boolean isOwnPaint(int cellState) {
        return cellState == paintCellState;
    }

    public boolean isEnemyPaint(int cellState) {
        return cellState != FloorGrid3D.CELL_STATE_EMPTY && cellState != paintCellState;
    }
}
