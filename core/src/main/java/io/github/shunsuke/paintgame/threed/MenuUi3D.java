package io.github.shunsuke.paintgame.threed;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;

/**
 * Small helper focused on title, menu, pause, countdown, and result UI drawing.
 * This keeps screen layout rules in one place without moving game state logic out of Main3D.
 */
public class MenuUi3D {
    public static class InfoRow {
        private final String label;
        private final String value;
        private final Color valueColor;

        public InfoRow(String label, String value) {
            this(label, value, Color.WHITE);
        }

        public InfoRow(String label, String value, Color valueColor) {
            this.label = label;
            this.value = value;
            this.valueColor = new Color(valueColor);
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }

        public Color getValueColor() {
            return valueColor;
        }
    }

    private static final float REFERENCE_WIDTH = 1280f;
    private static final float REFERENCE_HEIGHT = 720f;
    private static final float MIN_UI_SCALE = 1f;
    private static final float MAX_UI_SCALE = 1.46f;
    private static final float SCRIM_ALPHA = 0.42f;
    private static final float PANEL_SHADOW_ALPHA = 0.18f;
    private static final float PANEL_ALPHA = 0.82f;
    private static final float PANEL_INNER_ALPHA = 0.94f;
    private static final float CARD_ALPHA = 0.65f;
    private static final float CARD_SELECTED_ALPHA = 0.82f;
    private static final float CARD_INNER_ALPHA = 0.9f;
    private static final float CARD_SELECTED_INNER_ALPHA = 0.96f;
    private static final float TEXT_SHADOW_ALPHA = 0.8f;
    private static final float TITLE_SCALE = 3.02f;
    private static final float SECTION_TITLE_SCALE = 2.18f;
    private static final float BODY_SCALE = 1.48f;
    private static final float SMALL_SCALE = 1.22f;
    private static final float TINY_SCALE = 1.02f;
    private static final float FOOTER_LINE_GAP = 24f;
    private static final int WEAPON_STAT_SEGMENTS = 5;
    private static final Color SCRIM_COLOR = new Color(0.01f, 0.02f, 0.04f, SCRIM_ALPHA);
    private static final Color PANEL_COLOR = new Color(0.04f, 0.06f, 0.1f, PANEL_ALPHA);
    private static final Color PANEL_INNER_COLOR = new Color(0.08f, 0.11f, 0.16f, PANEL_INNER_ALPHA);
    private static final Color PANEL_BORDER_COLOR = new Color(0.9f, 0.96f, 1f, 0.34f);
    private static final Color PLAYER_ACCENT = new Color(0.18f, 0.88f, 1f, 0.96f);
    private static final Color ENEMY_ACCENT = new Color(1f, 0.42f, 0.72f, 0.96f);
    private static final Color TITLE_ACCENT = new Color(1f, 0.92f, 0.36f, 0.98f);
    private static final Color HEADING_COLOR = new Color(0.95f, 0.98f, 1f, 1f);
    private static final Color SUBTEXT_COLOR = new Color(0.78f, 0.84f, 0.91f, 1f);
    private static final Color MUTED_TEXT_COLOR = new Color(0.66f, 0.74f, 0.84f, 1f);
    private static final Color CARD_COLOR = new Color(0.05f, 0.08f, 0.13f, CARD_ALPHA);
    private static final Color CARD_SELECTED_COLOR = new Color(0.08f, 0.16f, 0.24f, CARD_SELECTED_ALPHA);
    private static final Color CARD_INNER_COLOR = new Color(0.09f, 0.12f, 0.18f, CARD_INNER_ALPHA);
    private static final Color CARD_SELECTED_INNER_COLOR = new Color(0.11f, 0.2f, 0.29f, CARD_SELECTED_INNER_ALPHA);
    private static final Color CARD_BORDER_COLOR = new Color(1f, 1f, 1f, 0.16f);
    private static final Color CARD_SELECTED_BORDER_COLOR = new Color(0.62f, 0.92f, 1f, 0.95f);
    private static final Color BAR_BACKGROUND_COLOR = new Color(0f, 0f, 0f, 0.36f);
    private static final Color PAUSE_PANEL_ACCENT = new Color(0.24f, 0.82f, 1f, 1f);
    private static final Color RESULT_PANEL_ACCENT = new Color(1f, 0.55f, 0.78f, 1f);

    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;

    public MenuUi3D(SpriteBatch spriteBatch, ShapeRenderer shapeRenderer, BitmapFont font, GlyphLayout glyphLayout) {
        this.spriteBatch = spriteBatch;
        this.shapeRenderer = shapeRenderer;
        this.font = font;
        this.glyphLayout = glyphLayout;
    }

    public void drawTitleMain(
        OrthographicCamera camera,
        String titleText,
        String promptText,
        String stepText,
        String[] menuItems,
        int selectedIndex,
        String selectedWeapon,
        String selectedStage,
        String selectedDifficulty
    ) {
        float uiScale = getUiScale(camera);
        PanelRect panel = createPanel(camera, 0.72f, 0.88f, 800f, 1260f, 600f, 940f, 0f);
        float padding = 32f * uiScale;
        float sectionGap = 22f * uiScale;
        float footerBottom = panel.y + 18f * uiScale;
        float footerHeight = 56f * uiScale;
        float footerTop = footerBottom + footerHeight;
        float summaryCardHeight = 66f * uiScale;
        float summaryCardBottom = footerTop + 28f * uiScale;
        float summaryCardTop = summaryCardBottom + summaryCardHeight;
        float dividerY = summaryCardTop + sectionGap * 0.5f;
        float titleTop = panel.top() - 44f * uiScale;
        float promptY = titleTop - 38f * uiScale;
        boolean showStepText = stepText != null && !stepText.isEmpty();
        float headerHeight = showStepText ? 126f * uiScale : 94f * uiScale;
        float menuItemHeight = 48f * uiScale;
        float menuGap = 12f * uiScale;
        int menuCount = menuItems.length;
        float menuAreaHeight = menuCount * menuItemHeight + (menuCount - 1) * menuGap;
        float menuWidth = panel.width - padding * 2f;
        float menuBottom = summaryCardTop + sectionGap;
        float menuTop = menuBottom + menuAreaHeight;
        float headerBottom = panel.top() - headerHeight;
        if (menuTop > headerBottom) {
            float overflow = menuTop - headerBottom;
            menuBottom -= overflow;
            menuTop -= overflow;
        }
        float summaryLineY = summaryCardBottom;
        float chipHeight = summaryCardHeight;

        float chipWidth = (panel.width - padding * 2f - sectionGap * 2f) / 3f;

        beginShapes(camera);
        drawScreenScrim(camera.viewportWidth, camera.viewportHeight);
        drawPanel(panel, PLAYER_ACCENT);
        for (int i = 0; i < menuCount; i++) {
            float rowY = menuTop - i * (menuItemHeight + menuGap) - menuItemHeight;
            drawMenuRow(panel.x + padding, rowY, menuWidth, menuItemHeight, i == selectedIndex, uiScale);
        }
        drawDivider(panel.x + padding, dividerY, panel.right() - padding);
        drawInfoChipFrame(panel.x + padding, summaryLineY, chipWidth, chipHeight, PLAYER_ACCENT);
        drawInfoChipFrame(panel.x + padding + chipWidth + sectionGap, summaryLineY, chipWidth, chipHeight, ENEMY_ACCENT);
        drawInfoChipFrame(panel.right() - padding - chipWidth, summaryLineY, chipWidth, chipHeight, TITLE_ACCENT);
        endShapes();

        beginText(camera);
        drawCenteredText(titleText, panel.centerX(), titleTop, panel.width - padding * 2f, HEADING_COLOR, TITLE_SCALE * uiScale, 0.9f * uiScale, true);
        drawWrappedText(promptText, panel.x + padding, promptY, panel.width - padding * 2f, Align.center, SUBTEXT_COLOR, SMALL_SCALE * uiScale, true);
        if (showStepText) {
            drawWrappedText(stepText, panel.x + padding, promptY - 28f * uiScale, panel.width - padding * 2f, Align.center, TITLE_ACCENT, TINY_SCALE * uiScale, true);
        }
        for (int i = 0; i < menuCount; i++) {
            float rowY = menuTop - i * (menuItemHeight + menuGap) - menuItemHeight;
            drawCenteredText(
                menuItems[i],
                panel.centerX(),
                rowY + menuItemHeight * 0.67f,
                menuWidth - 68f * uiScale,
                i == selectedIndex ? HEADING_COLOR : (i == 0 ? TITLE_ACCENT : SUBTEXT_COLOR),
                (i == 0 ? BODY_SCALE * 1.08f : BODY_SCALE) * uiScale,
                SMALL_SCALE * uiScale,
                i == selectedIndex
            );
        }

        drawInfoChipText(panel.x + padding, summaryLineY, chipWidth, chipHeight, "Weapon", selectedWeapon, PLAYER_ACCENT, uiScale);
        drawInfoChipText(panel.x + padding + chipWidth + sectionGap, summaryLineY, chipWidth, chipHeight, "Stage", selectedStage, ENEMY_ACCENT, uiScale);
        drawInfoChipText(panel.right() - padding - chipWidth, summaryLineY, chipWidth, chipHeight, "Difficulty", selectedDifficulty, TITLE_ACCENT, uiScale);

        drawFooterText(panel, footerBottom + footerHeight * 0.52f, new String[] {
            "W/S or Arrow Keys: Move   Enter: Select",
            "M: Mute"
        }, uiScale);
        endText();
    }

    public void drawWeaponSelect(
        OrthographicCamera camera,
        WeaponConfig3D[] weapons,
        int selectedIndex,
        WeaponConfig3D currentWeapon
    ) {
        float uiScale = getUiScale(camera);
        PanelRect panel = createPanel(camera, 0.94f, 0.82f, 900f, 1260f, 470f, 820f, 0f);
        float padding = 26f * uiScale;
        float titleY = panel.top() - 42f * uiScale;
        float cardsTop = titleY - 90f * uiScale;
        float footerTop = panel.y + 44f * uiScale;
        float contentWidth = panel.width - padding * 2f;
        float gap = Math.min(22f * uiScale, contentWidth * 0.032f);
        float cardWidth = (contentWidth - gap * (weapons.length - 1)) / weapons.length;
        float cardHeight = Math.min(panel.height * 0.62f, 364f * uiScale);
        float cardY = cardsTop - cardHeight;

        beginShapes(camera);
        drawScreenScrim(camera.viewportWidth, camera.viewportHeight);
        drawPanel(panel, PLAYER_ACCENT);
        for (int i = 0; i < weapons.length; i++) {
            float cardX = panel.x + padding + i * (cardWidth + gap);
            drawSelectionCard(cardX, cardY, cardWidth, cardHeight, i == selectedIndex, PLAYER_ACCENT);
            drawWeaponStats(cardX, cardY, cardWidth, cardHeight, weapons[i], weapons, uiScale);
        }
        endShapes();

        beginText(camera);
        drawCenteredText("Weapon Select", panel.centerX(), titleY, panel.width - padding * 2f, HEADING_COLOR, SECTION_TITLE_SCALE * uiScale, 0.96f * uiScale, true);
        drawCenteredText("Selected Start Weapon: " + currentWeapon.getName(), panel.centerX(), titleY - 28f * uiScale, panel.width - padding * 2f, PLAYER_ACCENT, SMALL_SCALE * uiScale, 0.74f * uiScale, true);
        for (int i = 0; i < weapons.length; i++) {
            float cardX = panel.x + padding + i * (cardWidth + gap);
            drawWeaponCardText(cardX, cardY, cardWidth, cardHeight, weapons[i], i == selectedIndex, uiScale);
        }
        drawFooterText(panel, footerTop, new String[] {
            "W/S or Arrow Keys: Move",
            "Enter: Select",
            "Esc: Back"
        }, uiScale);
        endText();
    }

    public void drawStageSelect(
        OrthographicCamera camera,
        StageType3D[] stageTypes,
        int selectedIndex,
        StageType3D currentStage
    ) {
        float uiScale = getUiScale(camera);
        PanelRect panel = createPanel(camera, 0.94f, 0.8f, 900f, 1260f, 460f, 800f, 0f);
        float padding = 26f * uiScale;
        float titleY = panel.top() - 42f * uiScale;
        float cardsTop = titleY - 90f * uiScale;
        float footerTop = panel.y + 44f * uiScale;
        float contentWidth = panel.width - padding * 2f;
        float gap = Math.min(22f * uiScale, contentWidth * 0.032f);
        float cardWidth = (contentWidth - gap * (stageTypes.length - 1)) / stageTypes.length;
        float cardHeight = Math.min(panel.height * 0.56f, 330f * uiScale);
        float cardY = cardsTop - cardHeight;

        beginShapes(camera);
        drawScreenScrim(camera.viewportWidth, camera.viewportHeight);
        drawPanel(panel, ENEMY_ACCENT);
        for (int i = 0; i < stageTypes.length; i++) {
            float cardX = panel.x + padding + i * (cardWidth + gap);
            drawSelectionCard(cardX, cardY, cardWidth, cardHeight, i == selectedIndex, ENEMY_ACCENT);
            drawStageCardDecorations(cardX, cardY, cardWidth, cardHeight, stageTypes[i], uiScale);
        }
        endShapes();

        beginText(camera);
        drawCenteredText("Stage Select", panel.centerX(), titleY, panel.width - padding * 2f, HEADING_COLOR, SECTION_TITLE_SCALE * uiScale, 0.96f * uiScale, true);
        drawCenteredText("Current Stage: " + currentStage.getDisplayName(), panel.centerX(), titleY - 28f * uiScale, panel.width - padding * 2f, ENEMY_ACCENT, SMALL_SCALE * uiScale, 0.74f * uiScale, true);
        for (int i = 0; i < stageTypes.length; i++) {
            float cardX = panel.x + padding + i * (cardWidth + gap);
            drawStageCardText(cardX, cardY, cardWidth, cardHeight, stageTypes[i], i == selectedIndex, uiScale);
        }
        drawFooterText(panel, footerTop, new String[] {
            "W/S or Arrow Keys: Move",
            "Enter: Select",
            "Esc: Back"
        }, uiScale);
        endText();
    }

    public void drawDifficultySelect(
        OrthographicCamera camera,
        CpuDifficulty3D[] difficulties,
        int selectedIndex,
        CpuDifficulty3D currentDifficulty
    ) {
        float uiScale = getUiScale(camera);
        PanelRect panel = createPanel(camera, 0.9f, 0.76f, 760f, 1120f, 400f, 720f, 0f);
        float padding = 26f * uiScale;
        float titleY = panel.top() - 42f * uiScale;
        float cardsTop = titleY - 84f * uiScale;
        float footerTop = panel.y + 44f * uiScale;
        float contentWidth = panel.width - padding * 2f;
        float gap = Math.min(22f * uiScale, contentWidth * 0.032f);
        float cardWidth = (contentWidth - gap * (difficulties.length - 1)) / difficulties.length;
        float cardHeight = Math.min(panel.height * 0.52f, 292f * uiScale);
        float cardY = cardsTop - cardHeight;

        beginShapes(camera);
        drawScreenScrim(camera.viewportWidth, camera.viewportHeight);
        drawPanel(panel, TITLE_ACCENT);
        for (int i = 0; i < difficulties.length; i++) {
            float cardX = panel.x + padding + i * (cardWidth + gap);
            drawSelectionCard(cardX, cardY, cardWidth, cardHeight, i == selectedIndex, TITLE_ACCENT);
            drawDifficultyBars(cardX, cardY, cardWidth, cardHeight, difficulties[i], uiScale);
        }
        endShapes();

        beginText(camera);
        drawCenteredText("Difficulty Select", panel.centerX(), titleY, panel.width - padding * 2f, HEADING_COLOR, SECTION_TITLE_SCALE * uiScale, 0.96f * uiScale, true);
        drawCenteredText("Current Difficulty: " + currentDifficulty.getLabel(), panel.centerX(), titleY - 28f * uiScale, panel.width - padding * 2f, TITLE_ACCENT, SMALL_SCALE * uiScale, 0.74f * uiScale, true);
        for (int i = 0; i < difficulties.length; i++) {
            float cardX = panel.x + padding + i * (cardWidth + gap);
            drawDifficultyCardText(cardX, cardY, cardWidth, cardHeight, difficulties[i], i == selectedIndex, uiScale);
        }
        drawFooterText(panel, footerTop, new String[] {
            "W/S or Arrow Keys: Move",
            "Enter: Select",
            "Esc: Back"
        }, uiScale);
        endText();
    }

    public void drawControls(OrthographicCamera camera) {
        float uiScale = getUiScale(camera);
        PanelRect panel = createPanel(camera, 0.9f, 0.88f, 820f, 1160f, 500f, 860f, 0f);
        float padding = 28f * uiScale;
        float titleY = panel.top() - 42f * uiScale;
        float contentTop = titleY - 64f * uiScale;
        float sectionGap = 14f * uiScale;
        float rowGap = 16f * uiScale;
        float keyColumnWidth = Math.min(190f * uiScale, (panel.width - padding * 2f) * 0.34f);
        float valueX = panel.x + padding + keyColumnWidth + 14f * uiScale;
        float currentY = contentTop;

        beginShapes(camera);
        drawScreenScrim(camera.viewportWidth, camera.viewportHeight);
        drawPanel(panel, PLAYER_ACCENT);
        endShapes();

        beginText(camera);
        drawCenteredText("Controls", panel.centerX(), titleY, panel.width - padding * 2f, HEADING_COLOR, SECTION_TITLE_SCALE * uiScale, 0.96f * uiScale, true);

        currentY = drawControlSection(panel.x + padding, currentY, panel.width - padding * 2f, "Movement", uiScale);
        currentY = drawControlRow(panel.x + padding, valueX, currentY, keyColumnWidth, panel.right() - padding - valueX, "W / A / S / D", "Move", uiScale, rowGap);

        currentY = drawControlSection(panel.x + padding, currentY - sectionGap * 0.45f, panel.width - padding * 2f, "Camera", uiScale);
        currentY = drawControlRow(panel.x + padding, valueX, currentY, keyColumnWidth, panel.right() - padding - valueX, "Mouse", "Look", uiScale, rowGap);

        currentY = drawControlSection(panel.x + padding, currentY - sectionGap * 0.45f, panel.width - padding * 2f, "Combat", uiScale);
        currentY = drawControlRow(panel.x + padding, valueX, currentY, keyColumnWidth, panel.right() - padding - valueX, "Space", "Shoot", uiScale, rowGap);
        currentY = drawControlRow(panel.x + padding, valueX, currentY, keyColumnWidth, panel.right() - padding - valueX, "1 / 2 / 3", "Switch Weapon", uiScale, rowGap);

        currentY = drawControlSection(panel.x + padding, currentY - sectionGap * 0.45f, panel.width - padding * 2f, "Special Actions", uiScale);
        currentY = drawControlRow(panel.x + padding, valueX, currentY, keyColumnWidth, panel.right() - padding - valueX, "Shift", "Swim / Climb", uiScale, rowGap);
        currentY = drawControlRow(panel.x + padding, valueX, currentY, keyColumnWidth, panel.right() - padding - valueX, "J", "Jump / Wall Jump", uiScale, rowGap);

        currentY = drawControlSection(panel.x + padding, currentY - sectionGap * 0.45f, panel.width - padding * 2f, "Menu", uiScale);
        currentY = drawControlRow(panel.x + padding, valueX, currentY, keyColumnWidth, panel.right() - padding - valueX, "Enter", "Select / Retry", uiScale, rowGap);
        currentY = drawControlRow(panel.x + padding, valueX, currentY, keyColumnWidth, panel.right() - padding - valueX, "Esc / Backspace", "Pause / Back", uiScale, rowGap);
        currentY = drawControlRow(panel.x + padding, valueX, currentY, keyColumnWidth, panel.right() - padding - valueX, "R", "Return to Title", uiScale, rowGap);
        drawControlRow(panel.x + padding, valueX, currentY, keyColumnWidth, panel.right() - padding - valueX, "M", "Mute", uiScale, rowGap);

        drawFooterText(panel, panel.y + 40f * uiScale, new String[] {
            "Esc or Backspace: Back"
        }, uiScale);
        endText();
    }

    public void drawCountdown(OrthographicCamera camera, String countdownText, String subtitleText, String backText) {
        float uiScale = getUiScale(camera);
        PanelRect panel = createPanel(camera, 0.46f, 0.34f, 360f, 560f, 240f, 360f, 0f);

        beginShapes(camera);
        drawScreenScrim(camera.viewportWidth, camera.viewportHeight);
        drawPanel(panel, TITLE_ACCENT);
        endShapes();

        beginText(camera);
        drawCenteredText(countdownText, panel.centerX(), panel.centerY() + 34f * uiScale, panel.width - 40f * uiScale, HEADING_COLOR, 1.92f * uiScale, 1.2f * uiScale, true);
        drawWrappedText(subtitleText, panel.x + 20f * uiScale, panel.centerY() - 4f * uiScale, panel.width - 40f * uiScale, Align.center, SUBTEXT_COLOR, SMALL_SCALE * uiScale, true);
        drawFooterText(panel, panel.y + 34f * uiScale, new String[] { backText }, uiScale);
        endText();
    }

    public void drawPause(OrthographicCamera camera, InfoRow[] rows) {
        float uiScale = getUiScale(camera);
        PanelRect panel = createPanel(camera, 0.58f, 0.62f, 520f, 820f, 380f, 620f, 0f);

        beginShapes(camera);
        drawScreenScrim(camera.viewportWidth, camera.viewportHeight);
        drawPanel(panel, PAUSE_PANEL_ACCENT);
        endShapes();

        beginText(camera);
        float padding = 24f * uiScale;
        float titleY = panel.top() - 34f * uiScale;
        drawCenteredText("PAUSED", panel.centerX(), titleY, panel.width - padding * 2f, HEADING_COLOR, 1.36f * uiScale, 1f * uiScale, true);
        drawWrappedText("Match progress is frozen until you resume.", panel.x + padding, titleY - 28f * uiScale, panel.width - padding * 2f, Align.center, SUBTEXT_COLOR, SMALL_SCALE * uiScale, true);

        float rowsTop = titleY - 70f * uiScale;
        drawInfoRows(rows, panel.x + padding, rowsTop, panel.width - padding * 2f, 21f * uiScale, uiScale);
        drawFooterText(panel, panel.y + 42f * uiScale, new String[] {
            "Esc / Enter: Resume",
            "R: Return to Title",
            "M: Mute"
        }, uiScale);
        endText();
    }

    public void drawResult(
        OrthographicCamera camera,
        String resultText,
        InfoRow[] rows,
        String retryText,
        String returnText
    ) {
        float uiScale = getUiScale(camera);
        PanelRect panel = createPanel(camera, 0.6f, 0.72f, 520f, 860f, 420f, 700f, 0f);

        beginShapes(camera);
        drawScreenScrim(camera.viewportWidth, camera.viewportHeight);
        drawPanel(panel, RESULT_PANEL_ACCENT);
        endShapes();

        beginText(camera);
        float padding = 24f * uiScale;
        float titleY = panel.top() - 34f * uiScale;
        drawCenteredText("GAME OVER", panel.centerX(), titleY, panel.width - padding * 2f, HEADING_COLOR, 1.28f * uiScale, 0.96f * uiScale, true);
        drawCenteredText(resultText, panel.centerX(), titleY - 34f * uiScale, panel.width - padding * 2f, RESULT_PANEL_ACCENT, 1.34f * uiScale, 0.92f * uiScale, true);
        drawInfoRows(rows, panel.x + padding, titleY - 82f * uiScale, panel.width - padding * 2f, 21f * uiScale, uiScale);
        drawFooterText(panel, panel.y + 42f * uiScale, new String[] {
            retryText,
            returnText
        }, uiScale);
        endText();
    }

    private void beginShapes(OrthographicCamera camera) {
        shapeRenderer.setProjectionMatrix(camera.combined);
    }

    private void endShapes() {
        // Helper for symmetry in the draw methods.
    }

    private void beginText(OrthographicCamera camera) {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
    }

    private void endText() {
        spriteBatch.end();
    }

    private float getUiScale(OrthographicCamera camera) {
        float widthScale = camera.viewportWidth / REFERENCE_WIDTH;
        float heightScale = camera.viewportHeight / REFERENCE_HEIGHT;
        return MathUtils.clamp(Math.min(widthScale, heightScale), MIN_UI_SCALE, MAX_UI_SCALE);
    }

    private PanelRect createPanel(
        OrthographicCamera camera,
        float widthRatio,
        float heightRatio,
        float minWidth,
        float maxWidth,
        float minHeight,
        float maxHeight,
        float offsetY
    ) {
        float width = MathUtils.clamp(camera.viewportWidth * widthRatio, minWidth, maxWidth);
        float height = MathUtils.clamp(camera.viewportHeight * heightRatio, minHeight, maxHeight);
        float x = (camera.viewportWidth - width) / 2f;
        float y = (camera.viewportHeight - height) / 2f + offsetY;
        return new PanelRect(x, y, width, height);
    }

    private void drawScreenScrim(float viewportWidth, float viewportHeight) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(SCRIM_COLOR);
        shapeRenderer.rect(0f, 0f, viewportWidth, viewportHeight);
        shapeRenderer.end();
    }

    private void drawPanel(PanelRect panel, Color accentColor) {
        float inset = Math.max(8f, panel.width * 0.016f);
        float topStripeHeight = Math.max(6f, panel.height * 0.016f);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, PANEL_SHADOW_ALPHA);
        shapeRenderer.rect(panel.x + 10f, panel.y - 10f, panel.width, panel.height);
        shapeRenderer.setColor(PANEL_COLOR);
        shapeRenderer.rect(panel.x, panel.y, panel.width, panel.height);
        shapeRenderer.setColor(PANEL_INNER_COLOR);
        shapeRenderer.rect(panel.x + inset, panel.y + inset, panel.width - inset * 2f, panel.height - inset * 2f);
        shapeRenderer.setColor(accentColor.r, accentColor.g, accentColor.b, 0.85f);
        shapeRenderer.rect(panel.x + inset, panel.top() - inset - topStripeHeight, panel.width - inset * 2f, topStripeHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(PANEL_BORDER_COLOR);
        shapeRenderer.rect(panel.x, panel.y, panel.width, panel.height);
        shapeRenderer.setColor(accentColor.r, accentColor.g, accentColor.b, 0.78f);
        shapeRenderer.rect(panel.x + inset, panel.y + inset, panel.width - inset * 2f, panel.height - inset * 2f);
        shapeRenderer.end();
    }

    private void drawMenuRow(float x, float y, float width, float height, boolean selected, float uiScale) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (selected) {
            shapeRenderer.setColor(PLAYER_ACCENT.r, PLAYER_ACCENT.g, PLAYER_ACCENT.b, 0.18f);
            shapeRenderer.rect(x, y, width, height);
            shapeRenderer.setColor(PLAYER_ACCENT.r, PLAYER_ACCENT.g, PLAYER_ACCENT.b, 0.95f);
            float markerWidth = 5f * uiScale;
            shapeRenderer.rect(x, y, markerWidth, height);
            shapeRenderer.rect(x + width - markerWidth, y, markerWidth, height);
            float arrowInset = 16f * uiScale;
            float arrowHalf = 7f * uiScale;
            float midY = y + height / 2f;
            shapeRenderer.triangle(x + arrowInset, midY, x + arrowInset + 10f * uiScale, midY + arrowHalf, x + arrowInset + 10f * uiScale, midY - arrowHalf);
            shapeRenderer.triangle(x + width - arrowInset, midY, x + width - arrowInset - 10f * uiScale, midY + arrowHalf, x + width - arrowInset - 10f * uiScale, midY - arrowHalf);
        } else {
            shapeRenderer.setColor(1f, 1f, 1f, 0.09f);
            shapeRenderer.rect(x, y, width, height);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(selected ? CARD_SELECTED_BORDER_COLOR : PANEL_BORDER_COLOR);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
    }

    private void drawSelectionCard(float x, float y, float width, float height, boolean selected, Color accentColor) {
        float inset = Math.max(7f, width * 0.04f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(selected ? CARD_SELECTED_COLOR : CARD_COLOR);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(selected ? CARD_SELECTED_INNER_COLOR : CARD_INNER_COLOR);
        shapeRenderer.rect(x + inset, y + inset, width - inset * 2f, height - inset * 2f);
        if (selected) {
            shapeRenderer.setColor(accentColor.r, accentColor.g, accentColor.b, 0.92f);
            shapeRenderer.rect(x + inset, y + height - inset - 6f, width - inset * 2f, 6f);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(selected ? CARD_SELECTED_BORDER_COLOR : CARD_BORDER_COLOR);
        shapeRenderer.rect(x, y, width, height);
        if (selected) {
            shapeRenderer.setColor(accentColor);
            shapeRenderer.rect(x + inset, y + inset, width - inset * 2f, height - inset * 2f);
        }
        shapeRenderer.end();
    }

    private void drawWeaponStats(float x, float y, float width, float height, WeaponConfig3D weapon, WeaponConfig3D[] allWeapons, float uiScale) {
        float padding = 18f * uiScale;
        float rowWidth = width - padding * 2f;
        float labelWidth = rowWidth * 0.29f;
        float valueWidth = 56f * uiScale;
        float barWidth = rowWidth - labelWidth - valueWidth - 14f * uiScale;
        float barX = x + padding + labelWidth + 8f * uiScale;
        float rowHeight = 28f * uiScale;
        float startY = y + height - 154f * uiScale;
        float barHeight = 10f * uiScale;

        drawStatRowBackground(x + padding, startY, rowWidth, rowHeight, uiScale);
        drawSegmentBar(barX, startY + 9f * uiScale, barWidth, barHeight, getWeaponRangeRatio(weapon, allWeapons), PLAYER_ACCENT);
        drawStatRowBackground(x + padding, startY - 38f * uiScale, rowWidth, rowHeight, uiScale);
        drawSegmentBar(barX, startY - 29f * uiScale, barWidth, barHeight, getWeaponFireRatio(weapon, allWeapons), PLAYER_ACCENT);
        drawStatRowBackground(x + padding, startY - 76f * uiScale, rowWidth, rowHeight, uiScale);
        drawSegmentBar(barX, startY - 67f * uiScale, barWidth, barHeight, getWeaponPaintRatio(weapon, allWeapons), PLAYER_ACCENT);
        drawStatRowBackground(x + padding, startY - 114f * uiScale, rowWidth, rowHeight, uiScale);
        drawSegmentBar(barX, startY - 105f * uiScale, barWidth, barHeight, getWeaponInkCostRatio(weapon, allWeapons), ENEMY_ACCENT);
    }

    private void drawStageCardDecorations(float x, float y, float width, float height, StageType3D stageType, float uiScale) {
        float left = x + 16f * uiScale;
        float bottom = y + 18f * uiScale;
        float iconHeight = 30f * uiScale;
        float iconWidth = Math.min(width - 32f * uiScale, 90f * uiScale);
        Color accent = stageType == StageType3D.TRAINING_STAGE ? TITLE_ACCENT : (stageType == StageType3D.WIDE_ARENA ? PLAYER_ACCENT : ENEMY_ACCENT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(accent.r, accent.g, accent.b, 0.18f);
        shapeRenderer.rect(left, bottom, iconWidth, iconHeight);
        shapeRenderer.setColor(accent.r, accent.g, accent.b, 0.78f);
        float columnWidth = (iconWidth - 8f * uiScale) / 3f;
        float[] heights = getStagePreviewHeights(stageType);
        for (int i = 0; i < heights.length; i++) {
            float barHeight = 8f * uiScale + heights[i] * 16f * uiScale;
            shapeRenderer.rect(left + i * (columnWidth + 4f * uiScale), bottom + 4f * uiScale, columnWidth, barHeight);
        }
        shapeRenderer.end();
    }

    private void drawDifficultyBars(float x, float y, float width, float height, CpuDifficulty3D difficulty, float uiScale) {
        float padding = 18f * uiScale;
        float rowWidth = width - padding * 2f;
        float labelWidth = rowWidth * 0.34f;
        float barWidth = rowWidth - labelWidth - 12f * uiScale;
        float barX = x + padding + labelWidth + 8f * uiScale;
        float startY = y + height - 138f * uiScale;
        float barHeight = 10f * uiScale;

        drawStatRowBackground(x + padding, startY, rowWidth, 28f * uiScale, uiScale);
        drawSegmentBar(barX, startY + 9f * uiScale, barWidth, barHeight, getDifficultySpeedRatio(difficulty), TITLE_ACCENT);
        drawStatRowBackground(x + padding, startY - 38f * uiScale, rowWidth, 28f * uiScale, uiScale);
        drawSegmentBar(barX, startY - 29f * uiScale, barWidth, barHeight, getDifficultyAccuracyRatio(difficulty), TITLE_ACCENT);
        drawStatRowBackground(x + padding, startY - 76f * uiScale, rowWidth, 28f * uiScale, uiScale);
        drawSegmentBar(barX, startY - 67f * uiScale, barWidth, barHeight, getDifficultyReactionRatio(difficulty), TITLE_ACCENT);
    }

    private void drawStatRowBackground(float x, float y, float width, float height, float uiScale) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 1f, 1f, 0.05f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
    }

    private void drawSegmentBar(float x, float y, float width, float height, float ratio, Color fillColor) {
        float gap = 4f;
        float segmentWidth = (width - gap * (WEAPON_STAT_SEGMENTS - 1)) / WEAPON_STAT_SEGMENTS;
        int activeSegments = MathUtils.clamp(MathUtils.ceil(ratio * WEAPON_STAT_SEGMENTS), 1, WEAPON_STAT_SEGMENTS);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < WEAPON_STAT_SEGMENTS; i++) {
            float segmentX = x + i * (segmentWidth + gap);
            shapeRenderer.setColor(BAR_BACKGROUND_COLOR);
            shapeRenderer.rect(segmentX, y, segmentWidth, height);
            if (i < activeSegments) {
                shapeRenderer.setColor(fillColor);
                shapeRenderer.rect(segmentX, y, segmentWidth, height);
            }
        }
        shapeRenderer.end();
    }

    private void drawWeaponCardText(float x, float y, float width, float height, WeaponConfig3D weapon, boolean selected, float uiScale) {
        float padding = 18f * uiScale;
        float titleY = y + height - 34f * uiScale;
        float roleY = titleY - 28f * uiScale;
        float rowWidth = width - padding * 2f;
        float rowLabelWidth = rowWidth * 0.29f;
        float rowValueWidth = 56f * uiScale;
        float rowStartY = y + height - 146f * uiScale;

        drawCenteredText(weapon.getName(), x + width / 2f, titleY, width - padding * 2f, selected ? HEADING_COLOR : Color.WHITE, SMALL_SCALE * uiScale, TINY_SCALE * uiScale, true);
        drawCenteredText(weapon.getRoleLabel(), x + width / 2f, roleY, width - padding * 2f, SUBTEXT_COLOR, SMALL_SCALE * 0.86f * uiScale, TINY_SCALE * uiScale, false);

        drawLeftText("Range", x + padding, rowStartY, rowLabelWidth, SUBTEXT_COLOR, TINY_SCALE * uiScale, TINY_SCALE * uiScale, false);
        drawLeftText("Fire", x + padding, rowStartY - 38f * uiScale, rowLabelWidth, SUBTEXT_COLOR, TINY_SCALE * uiScale, TINY_SCALE * uiScale, false);
        drawLeftText("Paint", x + padding, rowStartY - 76f * uiScale, rowLabelWidth, SUBTEXT_COLOR, TINY_SCALE * uiScale, TINY_SCALE * uiScale, false);
        drawLeftText("Ink Cost", x + padding, rowStartY - 114f * uiScale, rowLabelWidth, SUBTEXT_COLOR, TINY_SCALE * uiScale, TINY_SCALE * uiScale, false);

        drawRightText(String.format("%.1f", weapon.getRange()), x + width - padding, rowStartY, rowValueWidth, MUTED_TEXT_COLOR, TINY_SCALE * uiScale, TINY_SCALE * uiScale);
        drawRightText(String.format("%.1f/s", 1f / weapon.getFireInterval()), x + width - padding, rowStartY - 38f * uiScale, rowValueWidth, MUTED_TEXT_COLOR, TINY_SCALE * uiScale, TINY_SCALE * uiScale);
        drawRightText(String.format("%.2f", weapon.getPaintRadius()), x + width - padding, rowStartY - 76f * uiScale, rowValueWidth, MUTED_TEXT_COLOR, TINY_SCALE * uiScale, TINY_SCALE * uiScale);
        drawRightText(String.format("%.0f", weapon.getInkCost()), x + width - padding, rowStartY - 114f * uiScale, rowValueWidth, MUTED_TEXT_COLOR, TINY_SCALE * uiScale, TINY_SCALE * uiScale);
    }

    private void drawStageCardText(float x, float y, float width, float height, StageType3D stageType, boolean selected, float uiScale) {
        float padding = 16f * uiScale;
        StageConfig3D stageConfig = StageConfig3D.forType(stageType);
        String summary = getStageSummary(stageType);
        String sizeText = String.format("%d x %d Floor", stageConfig.getColumns(), stageConfig.getRows());
        String playerText = stageType == StageType3D.TEAM_ARENA ? "Recommended: 2 vs 2" : "Recommended: 1 vs 1";

        drawCenteredText(stageType.getDisplayName(), x + width / 2f, y + height - 36f * uiScale, width - padding * 2f, selected ? HEADING_COLOR : Color.WHITE, SMALL_SCALE * uiScale, TINY_SCALE * uiScale, true);
        drawWrappedText(summary, x + padding, y + height - 74f * uiScale, width - padding * 2f, Align.center, SUBTEXT_COLOR, SMALL_SCALE * 0.86f * uiScale, false);
        drawWrappedText(sizeText, x + padding, y + 112f * uiScale, width - padding * 2f, Align.center, MUTED_TEXT_COLOR, TINY_SCALE * uiScale, false);
        drawWrappedText(playerText, x + padding, y + 82f * uiScale, width - padding * 2f, Align.center, MUTED_TEXT_COLOR, TINY_SCALE * uiScale, false);
    }

    private void drawDifficultyCardText(float x, float y, float width, float height, CpuDifficulty3D difficulty, boolean selected, float uiScale) {
        float padding = 16f * uiScale;
        float rowStartY = y + height - 138f * uiScale;

        drawCenteredText(difficulty.getLabel(), x + width / 2f, y + height - 36f * uiScale, width - padding * 2f, selected ? HEADING_COLOR : Color.WHITE, SMALL_SCALE * uiScale, TINY_SCALE * uiScale, true);
        drawWrappedText(getDifficultySummary(difficulty), x + padding, y + height - 74f * uiScale, width - padding * 2f, Align.center, SUBTEXT_COLOR, SMALL_SCALE * 0.86f * uiScale, false);
        drawLeftText("Speed", x + padding, rowStartY, width * 0.34f, SUBTEXT_COLOR, TINY_SCALE * uiScale, TINY_SCALE * uiScale, false);
        drawLeftText("Accuracy", x + padding, rowStartY - 38f * uiScale, width * 0.34f, SUBTEXT_COLOR, TINY_SCALE * uiScale, TINY_SCALE * uiScale, false);
        drawLeftText("Reaction", x + padding, rowStartY - 76f * uiScale, width * 0.34f, SUBTEXT_COLOR, TINY_SCALE * uiScale, TINY_SCALE * uiScale, false);
    }

    private float drawInfoRows(InfoRow[] rows, float x, float topY, float width, float rowGap, float uiScale) {
        float labelWidth = width * 0.42f;
        float currentY = topY;
        for (InfoRow row : rows) {
            drawLeftText(row.getLabel(), x, currentY, labelWidth, SUBTEXT_COLOR, SMALL_SCALE * uiScale, 0.72f * uiScale, false);
            drawRightText(row.getValue(), x + width, currentY, width - labelWidth - 10f * uiScale, row.getValueColor(), SMALL_SCALE * uiScale, 0.72f * uiScale);
            currentY -= rowGap;
        }
        return currentY;
    }

    private void drawInfoChipFrame(float x, float y, float width, float height, Color accentColor) {
        float inset = 6f;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(accentColor.r, accentColor.g, accentColor.b, 0.12f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(0f, 0f, 0f, 0.18f);
        shapeRenderer.rect(x + inset, y + inset, width - inset * 2f, height - inset * 2f);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(accentColor.r, accentColor.g, accentColor.b, 0.85f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
    }

    private void drawInfoChipText(float x, float y, float width, float height, String label, String value, Color accentColor, float uiScale) {
        drawCenteredText(label, x + width / 2f, y + height - 16f * uiScale, width - 20f * uiScale, accentColor, TINY_SCALE * uiScale, TINY_SCALE * uiScale, false);
        drawCenteredText(value, x + width / 2f, y + 24f * uiScale, width - 20f * uiScale, Color.WHITE, SMALL_SCALE * uiScale, TINY_SCALE * uiScale, false);
    }

    private void drawDivider(float startX, float y, float endX) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(PANEL_BORDER_COLOR);
        shapeRenderer.line(startX, y, endX, y);
        shapeRenderer.end();
    }

    private float drawControlSection(float x, float topY, float width, String title, float uiScale) {
        drawLeftText(title, x, topY, width, TITLE_ACCENT, SMALL_SCALE * uiScale, 0.76f * uiScale, true);
        return topY - 20f * uiScale;
    }

    private float drawControlRow(
        float keyX,
        float valueX,
        float baselineY,
        float keyWidth,
        float valueWidth,
        String keyText,
        String valueText,
        float uiScale,
        float rowGap
    ) {
        drawLeftText(keyText, keyX, baselineY, keyWidth, HEADING_COLOR, SMALL_SCALE * uiScale, 0.72f * uiScale, false);
        drawLeftText(valueText, valueX, baselineY, valueWidth, SUBTEXT_COLOR, SMALL_SCALE * uiScale, 0.72f * uiScale, false);
        return baselineY - rowGap;
    }

    private void drawFooterText(PanelRect panel, float startY, String[] lines, float uiScale) {
        float currentY = startY + (lines.length - 1) * FOOTER_LINE_GAP * uiScale * 0.5f;
        for (String line : lines) {
            drawCenteredText(line, panel.centerX(), currentY, panel.width - 44f * uiScale, MUTED_TEXT_COLOR, TINY_SCALE * uiScale, 0.7f * uiScale, false);
            currentY -= FOOTER_LINE_GAP * uiScale;
        }
    }

    private void drawCenteredText(String text, float centerX, float y, float maxWidth, Color color, float preferredScale, float minScale, boolean shadow) {
        float scale = fitSingleLineScale(text, maxWidth, preferredScale, minScale);
        drawSingleLine(text, centerX, y, maxWidth, color, scale, Align.center, shadow);
    }

    private void drawLeftText(String text, float x, float y, float maxWidth, Color color, float preferredScale, float minScale, boolean shadow) {
        float scale = fitSingleLineScale(text, maxWidth, preferredScale, minScale);
        drawSingleLine(text, x, y, maxWidth, color, scale, Align.left, shadow);
    }

    private void drawRightText(String text, float rightX, float y, float maxWidth, Color color, float preferredScale, float minScale) {
        float scale = fitSingleLineScale(text, maxWidth, preferredScale, minScale);
        withFontScale(scale);
        glyphLayout.setText(font, text);
        float drawX = rightX - glyphLayout.width;
        drawTextInternal(text, drawX, y, color, shadowOffset(scale), false, scale);
        restoreFontScale();
    }

    private void drawWrappedText(String text, float x, float y, float width, int align, Color color, float scale, boolean shadow) {
        withFontScale(scale);
        Color previousColor = copyColor(font.getColor());
        font.setColor(color);
        glyphLayout.setText(font, text, color, width, align, true);
        if (shadow) {
            font.setColor(0f, 0f, 0f, TEXT_SHADOW_ALPHA);
            font.draw(spriteBatch, glyphLayout, x + shadowOffset(scale), y - shadowOffset(scale));
            font.setColor(color);
        }
        font.draw(spriteBatch, glyphLayout, x, y);
        font.setColor(previousColor);
        restoreFontScale();
    }

    private void drawSingleLine(String text, float anchorX, float y, float maxWidth, Color color, float scale, int align, boolean shadow) {
        withFontScale(scale);
        glyphLayout.setText(font, text);
        float drawX = anchorX;
        if (align == Align.center) {
            drawX = anchorX - glyphLayout.width / 2f;
        }
        drawTextInternal(text, drawX, y, color, shadowOffset(scale), shadow, scale);
        restoreFontScale();
    }

    private void drawTextInternal(String text, float x, float y, Color color, float shadowOffset, boolean shadow, float scale) {
        Color previousColor = copyColor(font.getColor());
        font.setColor(color);
        if (shadow) {
            font.setColor(0f, 0f, 0f, TEXT_SHADOW_ALPHA);
            font.draw(spriteBatch, text, x + shadowOffset, y - shadowOffset);
            font.setColor(color);
        }
        font.draw(spriteBatch, text, x, y);
        font.setColor(previousColor);
    }

    private float fitSingleLineScale(String text, float maxWidth, float preferredScale, float minScale) {
        withFontScale(preferredScale);
        glyphLayout.setText(font, text);
        float measuredWidth = glyphLayout.width;
        restoreFontScale();
        if (measuredWidth <= maxWidth || measuredWidth <= 0f) {
            return preferredScale;
        }
        return Math.max(minScale, preferredScale * (maxWidth / measuredWidth));
    }

    private float shadowOffset(float scale) {
        return Math.max(1f, 1.6f * scale);
    }

    private float getWeaponRangeRatio(WeaponConfig3D weapon, WeaponConfig3D[] allWeapons) {
        return normalizeMetric(weapon.getRange(), getWeaponMinRange(allWeapons), getWeaponMaxRange(allWeapons));
    }

    private float getWeaponFireRatio(WeaponConfig3D weapon, WeaponConfig3D[] allWeapons) {
        float shotsPerSecond = 1f / weapon.getFireInterval();
        return normalizeMetric(shotsPerSecond, getWeaponMinShotsPerSecond(allWeapons), getWeaponMaxShotsPerSecond(allWeapons));
    }

    private float getWeaponPaintRatio(WeaponConfig3D weapon, WeaponConfig3D[] allWeapons) {
        return normalizeMetric(weapon.getPaintRadius(), getWeaponMinPaint(allWeapons), getWeaponMaxPaint(allWeapons));
    }

    private float getWeaponInkCostRatio(WeaponConfig3D weapon, WeaponConfig3D[] allWeapons) {
        return normalizeMetric(weapon.getInkCost(), getWeaponMinInkCost(allWeapons), getWeaponMaxInkCost(allWeapons));
    }

    private float getDifficultySpeedRatio(CpuDifficulty3D difficulty) {
        return normalizeMetric(difficulty.getMoveSpeedMultiplier(), 0.76f, 0.98f);
    }

    private float getDifficultyAccuracyRatio(CpuDifficulty3D difficulty) {
        return normalizeMetric(14f - difficulty.getAimSpreadDegrees(), 2f, 11f);
    }

    private float getDifficultyReactionRatio(CpuDifficulty3D difficulty) {
        return normalizeMetric(difficulty.getAimTurnSpeed(), 2.8f, 7.2f);
    }

    private float normalizeMetric(float value, float min, float max) {
        if (MathUtils.isEqual(min, max)) {
            return 1f;
        }
        return MathUtils.clamp((value - min) / (max - min), 0.18f, 1f);
    }

    private float getWeaponMinRange(WeaponConfig3D[] weapons) {
        float min = Float.MAX_VALUE;
        for (WeaponConfig3D weapon : weapons) {
            min = Math.min(min, weapon.getRange());
        }
        return min;
    }

    private float getWeaponMaxRange(WeaponConfig3D[] weapons) {
        float max = Float.MIN_VALUE;
        for (WeaponConfig3D weapon : weapons) {
            max = Math.max(max, weapon.getRange());
        }
        return max;
    }

    private float getWeaponMinShotsPerSecond(WeaponConfig3D[] weapons) {
        float min = Float.MAX_VALUE;
        for (WeaponConfig3D weapon : weapons) {
            min = Math.min(min, 1f / weapon.getFireInterval());
        }
        return min;
    }

    private float getWeaponMaxShotsPerSecond(WeaponConfig3D[] weapons) {
        float max = Float.MIN_VALUE;
        for (WeaponConfig3D weapon : weapons) {
            max = Math.max(max, 1f / weapon.getFireInterval());
        }
        return max;
    }

    private float getWeaponMinPaint(WeaponConfig3D[] weapons) {
        float min = Float.MAX_VALUE;
        for (WeaponConfig3D weapon : weapons) {
            min = Math.min(min, weapon.getPaintRadius());
        }
        return min;
    }

    private float getWeaponMaxPaint(WeaponConfig3D[] weapons) {
        float max = Float.MIN_VALUE;
        for (WeaponConfig3D weapon : weapons) {
            max = Math.max(max, weapon.getPaintRadius());
        }
        return max;
    }

    private float getWeaponMinInkCost(WeaponConfig3D[] weapons) {
        float min = Float.MAX_VALUE;
        for (WeaponConfig3D weapon : weapons) {
            min = Math.min(min, weapon.getInkCost());
        }
        return min;
    }

    private float getWeaponMaxInkCost(WeaponConfig3D[] weapons) {
        float max = Float.MIN_VALUE;
        for (WeaponConfig3D weapon : weapons) {
            max = Math.max(max, weapon.getInkCost());
        }
        return max;
    }

    private float[] getStagePreviewHeights(StageType3D stageType) {
        if (stageType == StageType3D.TEAM_ARENA) {
            return new float[] {0.85f, 1f, 0.85f};
        }
        if (stageType == StageType3D.WIDE_ARENA) {
            return new float[] {0.55f, 0.85f, 0.55f};
        }
        return new float[] {0.35f, 0.5f, 0.35f};
    }

    private String getStageSummary(StageType3D stageType) {
        if (stageType == StageType3D.TEAM_ARENA) {
            return "Large / 2 vs 2";
        }
        if (stageType == StageType3D.WIDE_ARENA) {
            return "Medium / Open";
        }
        return "Small / Practice";
    }

    private String getDifficultySummary(CpuDifficulty3D difficulty) {
        if (difficulty == CpuDifficulty3D.EASY) {
            return "Slow reactions / Low accuracy";
        }
        if (difficulty == CpuDifficulty3D.HARD) {
            return "Fast reactions / High accuracy";
        }
        return "Standard";
    }

    private Color copyColor(Color source) {
        return new Color(source);
    }

    private float previousScaleX;
    private float previousScaleY;

    private void withFontScale(float scale) {
        previousScaleX = font.getData().scaleX;
        previousScaleY = font.getData().scaleY;
        font.getData().setScale(scale);
    }

    private void restoreFontScale() {
        font.getData().setScale(previousScaleX, previousScaleY);
    }

    private static class PanelRect {
        private final float x;
        private final float y;
        private final float width;
        private final float height;

        private PanelRect(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        private float right() {
            return x + width;
        }

        private float top() {
            return y + height;
        }

        private float centerX() {
            return x + width / 2f;
        }

        private float centerY() {
            return y + height / 2f;
        }
    }
}
