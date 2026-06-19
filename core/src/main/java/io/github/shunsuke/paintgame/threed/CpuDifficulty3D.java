package io.github.shunsuke.paintgame.threed;

/**
 * Small difficulty preset for the beginner-friendly 3D prototype.
 * These values keep the CPU readable and fair:
 * - Easy misses more and wanders more
 * - Hard reacts faster, but never becomes perfect aimbot
 */
public enum CpuDifficulty3D {
    EASY("Easy", 4.2f, 2.8f, 12f, 1.55f, 0.76f, 0.42f, 0.55f, 1.1f),
    NORMAL("Normal", 5.2f, 4.8f, 6.5f, 1.22f, 0.88f, 0.24f, 0.8f, 1.35f),
    HARD("Hard", 6.2f, 7.2f, 3.0f, 1.0f, 0.98f, 0.1f, 0.95f, 1.45f);

    private final String label;
    private final float attackRange;
    private final float aimTurnSpeed;
    private final float aimSpreadDegrees;
    private final float fireIntervalMultiplier;
    private final float moveSpeedMultiplier;
    private final float paintBehaviorChance;
    private final float attackDecisionMinSeconds;
    private final float attackDecisionMaxSeconds;

    CpuDifficulty3D(
        String label,
        float attackRange,
        float aimTurnSpeed,
        float aimSpreadDegrees,
        float fireIntervalMultiplier,
        float moveSpeedMultiplier,
        float paintBehaviorChance,
        float attackDecisionMinSeconds,
        float attackDecisionMaxSeconds
    ) {
        this.label = label;
        this.attackRange = attackRange;
        this.aimTurnSpeed = aimTurnSpeed;
        this.aimSpreadDegrees = aimSpreadDegrees;
        this.fireIntervalMultiplier = fireIntervalMultiplier;
        this.moveSpeedMultiplier = moveSpeedMultiplier;
        this.paintBehaviorChance = paintBehaviorChance;
        this.attackDecisionMinSeconds = attackDecisionMinSeconds;
        this.attackDecisionMaxSeconds = attackDecisionMaxSeconds;
    }

    public String getLabel() {
        return label;
    }

    public float getAttackRange() {
        return attackRange;
    }

    public float getAimTurnSpeed() {
        return aimTurnSpeed;
    }

    public float getAimSpreadDegrees() {
        return aimSpreadDegrees;
    }

    public float getFireIntervalMultiplier() {
        return fireIntervalMultiplier;
    }

    public float getMoveSpeedMultiplier() {
        return moveSpeedMultiplier;
    }

    public float getPaintBehaviorChance() {
        return paintBehaviorChance;
    }

    public float getAttackDecisionMinSeconds() {
        return attackDecisionMinSeconds;
    }

    public float getAttackDecisionMaxSeconds() {
        return attackDecisionMaxSeconds;
    }
}
