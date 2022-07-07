package io.github.axolotlclient.modules.hud;

import com.mojang.blaze3d.platform.InputUtil;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.config.Color;
import io.github.axolotlclient.config.options.OptionCategory;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.modules.hud.gui.AbstractHudEntry;
import io.github.axolotlclient.modules.hud.gui.hud.ActionBarHud;
import io.github.axolotlclient.modules.hud.gui.hud.ArmorHud;
import io.github.axolotlclient.modules.hud.gui.hud.ArrowHud;
import io.github.axolotlclient.modules.hud.gui.hud.BossBarHud;
import io.github.axolotlclient.modules.hud.gui.hud.CPSHud;
import io.github.axolotlclient.modules.hud.gui.hud.CoordsHud;
import io.github.axolotlclient.modules.hud.gui.hud.CrosshairHud;
import io.github.axolotlclient.modules.hud.gui.hud.FPSHud;
import io.github.axolotlclient.modules.hud.gui.hud.IPHud;
import io.github.axolotlclient.modules.hud.gui.hud.ItemUpdateHud;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import io.github.axolotlclient.modules.hud.gui.hud.PackDisplayHud;
import io.github.axolotlclient.modules.hud.gui.hud.PingHud;
import io.github.axolotlclient.modules.hud.gui.hud.PotionsHud;
import io.github.axolotlclient.modules.hud.gui.hud.RealTimeHud;
import io.github.axolotlclient.modules.hud.gui.hud.ScoreboardHud;
import io.github.axolotlclient.modules.hud.gui.hud.SpeedHud;
import io.github.axolotlclient.modules.hud.gui.hud.ToggleSprintHud;
import io.github.axolotlclient.modules.hud.gui.hud.iconHud;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBind;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This implementation of Hud modules is based on KronHUD.
 * https://github.com/DarkKronicle/KronHUD
 * Licensed under GPL-3.0
 */


public class HudManager extends AbstractModule {

    public static Identifier ID = new Identifier("hud");

    private final Map<Identifier, AbstractHudEntry> entries = new HashMap<>();

    private final OptionCategory hudCategory = new OptionCategory(new Identifier("hud"), "hud");

    private final MinecraftClient client = MinecraftClient.getInstance();
    private static final HudManager INSTANCE = new HudManager();

    static KeyBind key = new KeyBind("key.openHud", InputUtil.KEY_RIGHT_SHIFT_CODE, "category.axolotlclient");

    public void save(){

    }

    public static HudManager getINSTANCE(){
        return INSTANCE;
    }

    public void init(){

        Color.setupChroma();

        KeyBindingHelper.registerKeyBinding(key);

        AxolotlClient.CONFIG.addCategory(hudCategory);

        HudRenderCallback.EVENT.register((matrices, v)->render(matrices));

        add(new PingHud());
        add(new FPSHud());
        add(new CPSHud());
        add(new ArmorHud());
        add(new PotionsHud());
        add(new KeystrokeHud());
        add(new ToggleSprintHud());
        add(new IPHud());
        add(new iconHud());
        add(new SpeedHud());
        add(new ScoreboardHud());
        add(new CrosshairHud());
        add(new CoordsHud());
        add(new ActionBarHud());
        add(new BossBarHud());
        //add(new ChatHud()); // really just use AdvancedChat
        add(new ArrowHud());
        add(new ItemUpdateHud());
        add(new PackDisplayHud());
        add(new RealTimeHud());

        entries.forEach((identifier, abstractHudEntry) -> abstractHudEntry.init());
    }

    public void tick(){
        if(key.isPressed()) MinecraftClient.getInstance().setScreen(new HudEditScreen());
        INSTANCE.entries.forEach((identifier, abstractHudEntry) -> {
            if(abstractHudEntry.tickable())abstractHudEntry.tick();
        });
    }

    public HudManager add(AbstractHudEntry entry) {
        entries.put(entry.getId(), entry);
        hudCategory.addSubCategory(entry.getAllOptions());
        return this;
    }

    public List<AbstractHudEntry> getEntries() {
        if (entries.size() > 0) {
            return new ArrayList<>(entries.values());
        }
        return new ArrayList<>();
    }

    public List<AbstractHudEntry> getMovableEntries() {
        if (entries.size() > 0) {
            return entries.values().stream().filter((entry) -> entry.isEnabled() && entry.movable()).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public AbstractHudEntry get(Identifier identifier) {
        return entries.get(identifier);
    }

    public void render(MatrixStack matrices) {
        if (!(client.currentScreen instanceof HudEditScreen) && !client.options.debugEnabled) {
            for (AbstractHudEntry hud : getEntries()) {
                if (hud.isEnabled()) {
                    hud.renderHud(matrices);
                }
            }
        }
    }

    public Optional<AbstractHudEntry> getEntryXY(int x, int y) {
        for (AbstractHudEntry entry : getMovableEntries()) {
            Rectangle bounds = entry.getScaledBounds();
            if (bounds.x <= x && bounds.x + bounds.width >= x && bounds.y <= y && bounds.y + bounds.height >= y) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public void renderPlaceholder(MatrixStack matrices) {
        for (AbstractHudEntry hud : getEntries()) {
            if (hud.isEnabled()) {
                hud.renderPlaceholder(matrices);
            }
        }
    }

    public List<Rectangle> getAllBounds() {
        ArrayList<Rectangle> bounds = new ArrayList<>();
        for (AbstractHudEntry entry : getMovableEntries()) {
            bounds.add(entry.getScaledBounds());
        }
        return bounds;
    }
}
