package io.github.shunsuke.paintgame.threed;

/**
 * Small difficulty preset for the beginner-friendly 3D prototype.
 * The values only tweak the existing CPU behavior a little, so the match still feels fair.
 */
public enum CpuDifficulty3D {
    EASY("Easy", 4.2f, 3.6f, 1.55f, 0.76f),
    NORMAL("Normal", 5.2f, 5.4f, 1.22f, 0.88f),
    HARD("Hard", 6.2f, 7.2f, 1.0f, 0.98f);

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
