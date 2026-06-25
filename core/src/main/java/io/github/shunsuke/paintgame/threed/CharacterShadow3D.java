package io.github.shunsuke.paintgame.threed;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

/**
 * Thin ellipse shadow that stays on the ground as a 3D object.
 * This avoids the HUD-layer black-circle issue from the projected 2D shadow.
 */
public class CharacterShadow3D implements Disposable {
    private static final float SHADOW_HEIGHT = 0.02f;
    private static final float SHADOW_Y_OFFSET = 0.02f;
    private static final float HEIGHT_TO_SCALE = 0.18f;
    private static final float MIN_SCALE = 0.55f;
    private static final float HEIGHT_TO_ALPHA = 0.08f;
    private static final float MIN_ALPHA_SCALE = 0.72f;

    private final Model model;
    private final ModelInstance instance;
    private final float baseWidth;
    private final float baseDepth;
    private final float baseAlpha;
    private final Color diffuseColor = new Color();

    private boolean visible;

    public CharacterShadow3D(float baseWidth, float baseDepth, float baseAlpha) {
        this.baseWidth = baseWidth;
        this.baseDepth = baseDepth;
        this.baseAlpha = baseAlpha;

        ModelBuilder modelBuilder = new ModelBuilder();
        Material material = new Material(
            ColorAttribute.createDiffuse(new Color(0f, 0f, 0f, baseAlpha)),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, baseAlpha)
        );
        model = modelBuilder.createCylinder(
            1f,
            SHADOW_HEIGHT,
            1f,
            24,
            material,
            Usage.Position | Usage.Normal
        );
        instance = new ModelInstance(model);
        visible = false;
    }

    public void update(Vector3 actorPosition, float groundY, float actorBaseY) {
        float heightAboveGround = Math.max(0f, actorBaseY - groundY);
        float scaleMultiplier = MathUtils.clamp(1f - heightAboveGround * HEIGHT_TO_SCALE, MIN_SCALE, 1f);
        float alphaMultiplier = MathUtils.clamp(1f - heightAboveGround * HEIGHT_TO_ALPHA, MIN_ALPHA_SCALE, 1f);

        instance.transform.idt();
        instance.transform.translate(actorPosition.x, groundY + SHADOW_Y_OFFSET + SHADOW_HEIGHT / 2f, actorPosition.z);
        instance.transform.scale(baseWidth * scaleMultiplier, 1f, baseDepth * scaleMultiplier);
        updateOpacity(baseAlpha * alphaMultiplier);
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        if (!visible) {
            return;
        }
        modelBatch.render(instance, environment);
    }

    @Override
    public void dispose() {
        model.dispose();
    }

    private void updateOpacity(float opacity) {
        diffuseColor.set(0f, 0f, 0f, opacity);
        Material material = instance.materials.first();
        ((ColorAttribute) material.get(ColorAttribute.Diffuse)).color.set(diffuseColor);
        ((BlendingAttribute) material.get(BlendingAttribute.Type)).opacity = opacity;
    }
}
