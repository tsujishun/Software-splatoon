package io.github.shunsuke.paintgame.threed;

/**
 * Simple state list for the 3D prototype flow.
 * We keep it separate so future phases can add more 3D screens without mixing booleans.
 */
public enum GameFlowState {
    TITLE,
    COUNTDOWN,
    PLAYING,
    PAUSED,
    GAME_OVER
}
