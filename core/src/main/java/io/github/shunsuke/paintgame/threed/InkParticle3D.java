package io.github.shunsuke.paintgame.threed;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Tiny visual-only particle used for simple ink splashes.
 * It has no collision and only exists for a very short time.
 */
public class InkParticle3D {
    private static final float GRAVITY = 10f;

    private final Vector3 position = new Vector3();
    private final Vector3 velocity = new Vector3();
    private final Color color = new Color();
    private final float totalLifetime;
    private final float baseSize;

    private float lifetimeRemaining;

    public InkParticle3D(Vector3 startPosition, Vector3 startVelocity, Color color, float lifetimeSeconds, float baseSize) {
        this.position.set(startPosition);
        this.velocity.set(startVelocity);
        this.color.set(color);
        this.totalLifetime = Math.max(0.01f, lifetimeSeconds);
        this.baseSize = Math.max(0.01f, baseSize);
        this.lifetimeRemaining = this.totalLifetime;
    }

    public boolean update(float delta) {
        lifetimeRemaining -= delta;
        if (lifetimeRemaining <= 0f) {
            lifetimeRemaining = 0f;
            return false;
        }

        velocity.y -= GRAVITY * delta;
        position.mulAdd(velocity, delta);
        return true;
    }

    public Vector3 getPosition() {
        return position;
    }

    public Color getColor() {
        return color;
    }

    public float getAlpha() {
        return MathUtils.clamp(lifetimeRemaining / totalLifetime, 0f, 1f);
    }

    public float getCurrentSize() {
        return baseSize * (0.45f + getAlpha() * 0.55f);
    }
}
