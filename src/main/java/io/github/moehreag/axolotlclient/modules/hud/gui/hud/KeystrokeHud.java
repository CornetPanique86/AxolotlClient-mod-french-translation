package io.github.moehreag.axolotlclient.modules.hud.gui.hud;

import com.mojang.blaze3d.platform.InputUtil;
import io.github.moehreag.axolotlclient.config.Color;
import io.github.moehreag.axolotlclient.config.options.BooleanOption;
import io.github.moehreag.axolotlclient.config.options.ColorOption;
import io.github.moehreag.axolotlclient.config.options.Option;
import io.github.moehreag.axolotlclient.modules.hud.gui.AbstractHudEntry;
import io.github.moehreag.axolotlclient.modules.hud.util.DrawPosition;
import io.github.moehreag.axolotlclient.modules.hud.util.Rectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBind;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This implementation of Hud modules is based on KronHUD.
 * https://github.com/DarkKronicle/KronHUD
 * Licensed under GPL-3.0
 */

public class KeystrokeHud extends AbstractHudEntry {
    public static final Identifier ID = new Identifier("kronhud", "keystrokehud");

	public KeystrokeHud() {
		super(53, 61);
	}

    private final ColorOption pressedTextColor = new ColorOption("heldtextcolor", Color.parse("#FF000000"));
    private final ColorOption pressedBackgroundColor = new ColorOption( "heldbackgroundcolor", Color.parse("#64FFFFFF"));
    private final BooleanOption showW = new BooleanOption("showW", true);
    private final BooleanOption showA = new BooleanOption("showA", true);
    private final BooleanOption showS = new BooleanOption("showS", true);
    private final BooleanOption showD = new BooleanOption("showD", true);
    private final BooleanOption showSpace = new BooleanOption("showSpace", true);
    private final BooleanOption showLMB = new BooleanOption("showLMB", true);
    private final BooleanOption showRMB = new BooleanOption("showRMB", true);

    private Keystroke w;
    private Keystroke a;
    private Keystroke s;
    private Keystroke d;
    private Keystroke lmb;
    private Keystroke rmb;
    private Keystroke space;

    private ArrayList<Keystroke> keystrokes;
    private static final MinecraftClient client = MinecraftClient.getInstance();

	public static Optional<String> getMouseKeyBindName(KeyBind keyBinding) {
		if (keyBinding.getKeyTranslationKey().equalsIgnoreCase(InputUtil.Type.MOUSE.createFromKeyCode(GLFW.GLFW_MOUSE_BUTTON_1).getTranslationKey())) {
			return Optional.of("LMB");
		} else if (keyBinding.getKeyTranslationKey().equalsIgnoreCase(InputUtil.Type.MOUSE.createFromKeyCode(GLFW.GLFW_MOUSE_BUTTON_2).getTranslationKey())) {
			return Optional.of("RMB");
		} else if (keyBinding.getKeyTranslationKey().equalsIgnoreCase(InputUtil.Type.MOUSE.createFromKeyCode(GLFW.GLFW_MOUSE_BUTTON_3).getTranslationKey())) {
			return Optional.of("MMB");
		}
		return Optional.empty();
	}

    public void setKeystrokes() {
        keystrokes = new ArrayList<>();
        DrawPosition pos = getPos();
        // LMB
        if(showLMB.get()) {
            lmb = createFromKey(new Rectangle(0, 36, 26, 17), pos, client.options.attackKey);
            keystrokes.add(lmb);
        }
        // RMB
        if(showRMB.get()) {
            rmb = createFromKey(new Rectangle(27, 36, 26, 17), pos, client.options.useKey);
            keystrokes.add(rmb);
        }
        // W
        if(showW.get()) {
            w = createFromKey(new Rectangle(18, 0, 17, 17), pos, client.options.forwardKey);
            keystrokes.add(w);
        }
        // A
        if(showA.get()) {
            a = createFromKey(new Rectangle(0, 18, 17, 17), pos, client.options.leftKey);
            keystrokes.add(a);
        }
        // S
        if(showS.get()) {
            s = createFromKey(new Rectangle(18, 18, 17, 17), pos, client.options.backKey);
            keystrokes.add(s);
        }
        // D
        if(showD.get()) {
            d = createFromKey(new Rectangle(36, 18, 17, 17), pos, client.options.rightKey);
            keystrokes.add(d);
        }

        // Space
        if(showSpace.get()) {
            space = new Keystroke(new Rectangle(0, 54, 53, 7), pos, client.options.jumpKey, (matrixStack, stroke) -> {
                Rectangle bounds = stroke.bounds;
                Rectangle spaceBounds = new Rectangle(bounds.x + stroke.offset.x + 4,
                        bounds.y + stroke.offset.y + 2,
                        bounds.width - 8, 1);
                fillRect(matrixStack, spaceBounds, stroke.getFGColor());
                if (shadow.get()) {
                    fillRect(matrixStack, spaceBounds.offset(1, 1),
                            new Color((stroke.getFGColor().getAsInt() & 16579836) >> 2 | stroke.getFGColor().getAsInt() & -16777216));
                }
            });
            keystrokes.add(space);
        }
        KeyBind.unpressAll();
        KeyBind.updateBoundKeys();
    }

    @Override
    public void render(MatrixStack matrixStack) {
        scale(matrixStack);
        if (keystrokes == null) {
            setKeystrokes();
        }
        for (Keystroke stroke : keystrokes) {
            stroke.render(matrixStack);
        }
        matrixStack.pop();
    }

    @Override
    public boolean tickable() {
        return true;
    }

    @Override
    public void tick() {
        DrawPosition pos = getPos();
        if (keystrokes == null) {
            setKeystrokes();
        }
        for (Keystroke stroke : keystrokes) {
            stroke.offset = pos;
        }

        if(keystrokes.contains(lmb) && !showLMB.get()){
            keystrokes.remove(lmb);
        }
        if(keystrokes.contains(rmb) && !showRMB.get()){
            keystrokes.remove(rmb);
        }
        if(keystrokes.contains(w) && !showW.get()){
            keystrokes.remove(w);
        }
        if(keystrokes.contains(a) && !showA.get()){
            keystrokes.remove(a);
        }
        if(keystrokes.contains(s) && !showS.get()){
            keystrokes.remove(s);
        }
        if(keystrokes.contains(d) && !showD.get()){
            keystrokes.remove(d);
        }
        if(keystrokes.contains(space) && !showSpace.get()){
            keystrokes.remove(space);
        }

        if(!keystrokes.contains(lmb) && showLMB.get() ||
                !keystrokes.contains(rmb) && showRMB.get() ||
                !keystrokes.contains(w) && showW.get() ||
                !keystrokes.contains(a) && showA.get() ||
                !keystrokes.contains(s) && showS.get() ||
                !keystrokes.contains(d) && showD.get() ||
                !keystrokes.contains(space) && showSpace.get()){
            setKeystrokes();
        }

    }

    @Override
    protected boolean getShadowDefault() {
        return false;
    }

    @Override
    public void renderPlaceholder(MatrixStack matrixStack) {
        renderPlaceholderBackground(matrixStack);
        scale(matrixStack);
        if (keystrokes == null) {
            setKeystrokes();
        }
        for (Keystroke stroke : keystrokes) {
            stroke.render(matrixStack);
        }
        matrixStack.pop();
        hovered = false;
    }

    public Keystroke createFromKey(Rectangle bounds, DrawPosition offset, KeyBind key) {
        String name = getMouseKeyBindName(key).orElse(key.getKeyName().getString().toUpperCase());
        if (name.length() > 4) {
            name = name.substring(0, 2);
        }
        return createFromString(bounds, offset, key, name);
    }

    public Keystroke createFromString(Rectangle bounds, DrawPosition offset, KeyBind key, String word) {
        return new Keystroke(bounds, offset, key, (matrixStack, stroke) -> {
            Rectangle strokeBounds = stroke.bounds;
            float x = (strokeBounds.x + stroke.offset.x + ((float) strokeBounds.width / 2)) -
                    ((float) client.textRenderer.getWidth(word) / 2 -1);
            float y = strokeBounds.y + stroke.offset.y + ((float) strokeBounds.height / 2) - 4;

            drawString(matrixStack, client.textRenderer, word, (int) x, (int) y, stroke.getFGColor().getAsInt(), shadow.get());
        });
    }

	@Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public boolean movable() {
        return true;
    }

    @Override
    public void addConfigOptions(List<Option> options) {
        super.addConfigOptions(options);
        options.add(textColor);
        options.add(chroma);
        options.add(pressedTextColor);
        options.add(shadow);
        options.add(background);
        options.add(backgroundColor);
        options.add(pressedBackgroundColor);
        options.add(outline);
        options.add(outlineColor);
        options.add(showLMB);
        options.add(showRMB);
        options.add(showSpace);
        options.add(showW);
        options.add(showA);
        options.add(showS);
        options.add(showD);
    }

    public class Keystroke {
        public final KeyBind key;
        public final KeystrokeRenderer render;
        public Rectangle bounds;
        public DrawPosition offset;
        private float start = -1;
        private final int animTime = 100;
        private boolean wasPressed = true;

        public Keystroke(Rectangle bounds, DrawPosition offset, KeyBind key, KeystrokeRenderer render) {
            this.bounds = bounds;
            this.offset = offset;
            this.key = key;
            this.render = render;
        }

        public void setPos(int x, int y) {
            bounds = new Rectangle(x, y, bounds.width, bounds.height);
        }

        public void setDimensions(int width, int height) {
            bounds = new Rectangle(bounds.x, bounds.y, width, height);
        }

        public void setBounds(int x, int y, int width, int height) {
            bounds = new Rectangle(x, y, width, height);
        }

        public void renderStroke(MatrixStack matrixStack) {
            if (key.isPressed() != wasPressed) {
                start = System.nanoTime() / 1000000F;

            }
            if (background.get()) {
                fillRect(matrixStack, bounds.offset(offset),
                        getColor());
            }
            if(outline.get()) outlineRect(matrixStack, bounds.offset(offset), outlineColor.get());
            if ((System.nanoTime() / 1000000F - start) / animTime >= 1) {
                start = -1;
            }
            wasPressed = key.isPressed();
        }

        private float getPercentPressed() {
            return start == -1 ? 1 : MathHelper.clamp((System.nanoTime() / 1000000F - start) / animTime, 0, 1);
        }

        public Color getColor() {
            return key.isPressed() ? Color.blend(backgroundColor.get(), pressedBackgroundColor.get(),
                    getPercentPressed()) :
                    Color.blend(pressedBackgroundColor.get(),
                    backgroundColor.get(),
                    getPercentPressed());
        }

        public Color getFGColor() {
            return key.isPressed() ? Color.blend(chroma.get()? textColor.getChroma() : textColor.get(), chroma.get()? textColor.getChroma() : pressedTextColor.get(), getPercentPressed()) :
                    Color.blend(chroma.get()? textColor.getChroma() : pressedTextColor.get(),
                            chroma.get()? textColor.getChroma() : textColor.get(),
                            getPercentPressed());
        }

        public void render(MatrixStack matrixStack) {
            renderStroke(matrixStack);
            render.render(matrixStack, this);
        }

    }

    public interface KeystrokeRenderer {
        void render(MatrixStack matrixStack, Keystroke stroke);
    }

}
