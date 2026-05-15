package io.github.shunsuke.paintgame.threed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

/**
 * Very small sound helper for the 3D prototype.
 *
 * Place sound files under these asset paths when they become available:
 * - assets/audio/player_shoot.wav
 * - assets/audio/enemy_shoot.wav
 * - assets/audio/hit.wav
 * - assets/audio/player_splatted.wav
 * - assets/audio/enemy_splatted.wav
 * - assets/audio/countdown_beep.wav
 * - assets/audio/countdown_go.wav
 * - assets/audio/game_over.wav
 * - assets/audio/weapon_switch.wav
 * - assets/audio/ink_empty.wav
 * - assets/audio/title_bgm.wav
 * - assets/audio/battle_bgm.wav
 * - assets/audio/result_bgm.wav
 *
 * Missing files are allowed. The game should keep running silently instead of crashing.
 */
public class AudioManager3D implements Disposable {
    private static final float MASTER_SFX_VOLUME = 1.0f;
    private static final float MASTER_BGM_VOLUME = 0.34f;
    private static final float PAUSED_BGM_VOLUME_MULTIPLIER = 0.55f;
    private static final float SHOOT_VOLUME = 0.42f;
    private static final float HIT_VOLUME = 0.55f;
    private static final float SPLAT_VOLUME = 0.7f;
    private static final float COUNTDOWN_VOLUME = 0.55f;
    private static final float GAME_OVER_VOLUME = 0.7f;
    private static final float UI_VOLUME = 0.45f;
    private static final float WARNING_VOLUME = 0.5f;

    private final Sound playerShootSound;
    private final Sound enemyShootSound;
    private final Sound hitSound;
    private final Sound playerSplattedSound;
    private final Sound enemySplattedSound;
    private final Sound countdownBeepSound;
    private final Sound countdownGoSound;
    private final Sound gameOverSound;
    private final Sound weaponSwitchSound;
    private final Sound inkEmptySound;
    private final Music titleBgm;
    private final Music battleBgm;
    private final Music resultBgm;

    private Music currentBgm;
    private boolean muted;
    private boolean pausedDucked;

    public AudioManager3D() {
        playerShootSound = loadSound("audio/player_shoot.wav");
        enemyShootSound = loadSound("audio/enemy_shoot.wav");
        hitSound = loadSound("audio/hit.wav");
        playerSplattedSound = loadSound("audio/player_splatted.wav");
        enemySplattedSound = loadSound("audio/enemy_splatted.wav");
        countdownBeepSound = loadSound("audio/countdown_beep.wav");
        countdownGoSound = loadSound("audio/countdown_go.wav");
        gameOverSound = loadSound("audio/game_over.wav");
        weaponSwitchSound = loadSound("audio/weapon_switch.wav");
        inkEmptySound = loadSound("audio/ink_empty.wav");
        titleBgm = loadMusic("audio/title_bgm.wav");
        battleBgm = loadMusic("audio/battle_bgm.wav");
        resultBgm = loadMusic("audio/result_bgm.wav");
    }

    public void playPlayerShoot() {
        play(playerShootSound, SHOOT_VOLUME);
    }

    public void playEnemyShoot() {
        play(enemyShootSound, SHOOT_VOLUME);
    }

    public void playHit() {
        play(hitSound, HIT_VOLUME);
    }

    public void playPlayerSplatted() {
        play(playerSplattedSound, SPLAT_VOLUME);
    }

    public void playEnemySplatted() {
        play(enemySplattedSound, SPLAT_VOLUME);
    }

    public void playCountdownBeep() {
        play(countdownBeepSound, COUNTDOWN_VOLUME);
    }

    public void playCountdownGo() {
        play(countdownGoSound, COUNTDOWN_VOLUME);
    }

    public void playGameOver() {
        play(gameOverSound, GAME_OVER_VOLUME);
    }

    public void playWeaponSwitch() {
        play(weaponSwitchSound, UI_VOLUME);
    }

    public void playInkEmpty() {
        play(inkEmptySound, WARNING_VOLUME);
    }

    public void playTitleBgm() {
        playBgm(titleBgm, true);
    }

    public void playBattleBgm() {
        playBgm(battleBgm, true);
    }

    public void playResultBgm() {
        playBgm(resultBgm, true);
    }

    public void setPausedDucked(boolean shouldDuck) {
        pausedDucked = shouldDuck;
        applyBgmVolume();
    }

    public void toggleMute() {
        muted = !muted;
        applyBgmVolume();
    }

    public boolean isMuted() {
        return muted;
    }

    @Override
    public void dispose() {
        disposeSound(playerShootSound);
        disposeSound(enemyShootSound);
        disposeSound(hitSound);
        disposeSound(playerSplattedSound);
        disposeSound(enemySplattedSound);
        disposeSound(countdownBeepSound);
        disposeSound(countdownGoSound);
        disposeSound(gameOverSound);
        disposeSound(weaponSwitchSound);
        disposeSound(inkEmptySound);
        disposeMusic(titleBgm);
        disposeMusic(battleBgm);
        disposeMusic(resultBgm);
    }

    private Sound loadSound(String path) {
        try {
            FileHandle fileHandle = Gdx.files.internal(path);
            if (!fileHandle.exists()) {
                return null;
            }
            return Gdx.audio.newSound(fileHandle);
        } catch (Exception exception) {
            return null;
        }
    }

    private void play(Sound sound, float volume) {
        if (sound == null || muted) {
            return;
        }

        sound.play(volume * MASTER_SFX_VOLUME);
    }

    private void disposeSound(Sound sound) {
        if (sound != null) {
            sound.dispose();
        }
    }

    private Music loadMusic(String path) {
        try {
            FileHandle fileHandle = Gdx.files.internal(path);
            if (!fileHandle.exists()) {
                return null;
            }
            return Gdx.audio.newMusic(fileHandle);
        } catch (Exception exception) {
            return null;
        }
    }

    private void playBgm(Music music, boolean looping) {
        if (currentBgm == music) {
            if (currentBgm != null) {
                currentBgm.setLooping(looping);
                applyBgmVolume();
                if (!currentBgm.isPlaying()) {
                    currentBgm.play();
                }
            }
            return;
        }

        if (currentBgm != null) {
            currentBgm.stop();
        }

        currentBgm = music;
        if (currentBgm == null) {
            return;
        }

        currentBgm.setLooping(looping);
        applyBgmVolume();
        currentBgm.play();
    }

    private void applyBgmVolume() {
        if (currentBgm == null) {
            return;
        }

        float volume = muted ? 0f : MASTER_BGM_VOLUME;
        if (pausedDucked) {
            volume *= PAUSED_BGM_VOLUME_MULTIPLIER;
        }
        currentBgm.setVolume(volume);
    }

    private void disposeMusic(Music music) {
        if (music != null) {
            music.dispose();
        }
    }
}
