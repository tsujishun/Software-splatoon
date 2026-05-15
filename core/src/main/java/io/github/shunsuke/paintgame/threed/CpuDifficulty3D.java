package io.github.shunsuke.paintgame.threed;

/**
 * Small difficulty preset for the beginner-friendly 3D prototype.
 * The values only tweak the existing CPU behavior a little, so the match still feels fair.
 */
public enum CpuDifficulty3D {
    EASY("Easy", 4.4f, 4.2f, 1.35f, 0.82f),
    NORMAL("Normal", 5.2f, 6f, 1.15f, 0.92f),
    HARD("Hard", 6.0f, 8.2f, 0.95f, 1.02f);

    private final String label;
    private final float attackRange;
    private final float aimAccuracy;
    private final float fireIntervalMultiplier;
    private final float moveSpeedMultiplier;

    CpuDifficulty3D(
        String label,
        float attackRange,
        float aimAccuracy,
        float fireIntervalMultiplier,
        float moveSpeedMultiplier
    ) {
        this.label = label;
        this.attackRange = attackRange;
        this.aimAccuracy = aimAccuracy;
        this.fireIntervalMultiplier = fireIntervalMultiplier;
        this.moveSpeedMultiplier = moveSpeedMultiplier;
    }

    public String getLabel() {
        return label;
    }

    public float getAttackRange() {
        return attackRange;
    }

    public float getAimAccuracy() {
        return aimAccuracy;
    }

    public float getFireIntervalMultiplier() {
        return fireIntervalMultiplier;
    }

    public float getMoveSpeedMultiplier() {
        return moveSpeedMultiplier;
    }
}
