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
        0.18f
    );

    private final String name;
    private final float range;
    private final float bulletSpeed;
    private final float paintRadius;
    private final float fireInterval;

    public WeaponConfig3D(String name, float range, float bulletSpeed, float paintRadius, float fireInterval) {
        this.name = name;
        this.range = range;
        this.bulletSpeed = bulletSpeed;
        this.paintRadius = paintRadius;
        this.fireInterval = fireInterval;
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
}
