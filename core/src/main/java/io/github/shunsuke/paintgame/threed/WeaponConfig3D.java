package io.github.shunsuke.paintgame.threed;

/**
 * Small immutable weapon setting object for the early 3D prototype.
 * We start with one weapon so later phases can add more without rewriting bullet logic again.
 */
public class WeaponConfig3D {
    // Standard all-rounder for learning the prototype.
    public static final WeaponConfig3D BASIC_SHOOTER = new WeaponConfig3D(
        "Basic Shooter",
        8.2f,
        9.2f,
        0.34f,
        0.16f,
        8.5f
    );
    // Short range, wide paint, and fast output. It spends ink quickly if you mash it.
    public static final WeaponConfig3D SHORT_PAINTER = new WeaponConfig3D(
        "Short Painter",
        5.2f,
        7.6f,
        0.56f,
        0.095f,
        10.5f
    );
    // Longer reach and faster bullets, but slower firing and a tighter paint spread.
    public static final WeaponConfig3D LONG_SHOOTER = new WeaponConfig3D(
        "Long Shooter",
        12.4f,
        12.0f,
        0.22f,
        0.34f,
        12.5f
    );
    public static final WeaponConfig3D ENEMY_SHOOTER = new WeaponConfig3D(
        "Enemy Shooter",
        7.1f,
        8.4f,
        0.32f,
        0.26f,
        10f
    );

    private final String name;
    private final float range;
    private final float bulletSpeed;
    private final float paintRadius;
    private final float fireInterval;
    private final float inkCost;

    public WeaponConfig3D(String name, float range, float bulletSpeed, float paintRadius, float fireInterval, float inkCost) {
        this.name = name;
        this.range = range;
        this.bulletSpeed = bulletSpeed;
        this.paintRadius = paintRadius;
        this.fireInterval = fireInterval;
        this.inkCost = inkCost;
    }

    public String getName() {
        return name;
    }

    public float getRange() {
        return range;
    }

    public float getBulletSpeed() {
        return bulletSpeed;
    }

    public float getPaintRadius() {
        return paintRadius;
    }

    public float getFireInterval() {
        return fireInterval;
    }

    public float getInkCost() {
        return inkCost;
    }
}
