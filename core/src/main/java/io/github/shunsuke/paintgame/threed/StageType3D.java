package io.github.shunsuke.paintgame.threed;

/**
 * Simple stage list for the 3D prototype.
 */
public enum StageType3D {
    TRAINING_STAGE("Training Stage"),
    WIDE_ARENA("Wide Arena"),
    TEAM_ARENA("Team Arena");

    private final String displayName;

    StageType3D(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
