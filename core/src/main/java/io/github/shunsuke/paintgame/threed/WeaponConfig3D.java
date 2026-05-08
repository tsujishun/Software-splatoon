package io.github.shunsuke.paintgame.threed;

/**
 * Small immutable weapon setting object for the early 3D prototype.
 * We start with one weapon so later phases can add more without rewriting bullet logic again.
 */
public class WeaponConfig3D {
    public static final WeaponConfig3D BASIC_SHOOTER = new WeaponConfig3D(
        "Basic Shooter",
        8f,
        9f,
        0.35f,
        0.18f,
        12f
    );
    public static final WeaponConfig3D SHORT_PAINTER = new WeaponConfig3D(
        "Short Painter",
        5.8f,
        8.2f,
        0.48f,
        0.11f,
        15f
    );
    public static final WeaponConfig3D LONG_SHOOTER = new WeaponConfig3D(
        "Long Shooter",
        11.5f,
        11.5f,
        0.24f,
        0.3f,
        16f
    );
    public static final WeaponConfig3D ENEMY_SHOOTER = new WeaponConfig3D(
        "Enemy Shooter",
        7.4f,
        8.6f,
        0.33f,
        0.22f,
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
