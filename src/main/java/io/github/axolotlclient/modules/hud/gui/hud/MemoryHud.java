package io.github.axolotlclient.modules.hud.gui.hud;

import io.github.axolotlclient.AxolotlclientConfig.Color;
import io.github.axolotlclient.AxolotlclientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlclientConfig.options.ColorOption;
import io.github.axolotlclient.AxolotlclientConfig.options.Option;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.List;

public class MemoryHud extends TextHudEntry {

    public static final Identifier ID = new Identifier("axolotlclient", "memoryhud");

    private final Rectangle graph = new Rectangle(0, 0, 0, 0);
    private final ColorOption graphUsedColor = new ColorOption("graphUsedColor", Color.SELECTOR_RED.withAlpha(255));
    private final ColorOption graphFreeColor = new ColorOption("graphFreeColor", Color.SELECTOR_GREEN.withAlpha(255));

    private final BooleanOption showGraph = new BooleanOption("showGraph", true);
    private final BooleanOption showText = new BooleanOption("showText", false);
    private final BooleanOption showAllocated = new BooleanOption("showAllocated", false);

    public MemoryHud() {
        super(150, 27, true);
    }

    @Override
    public void renderComponent(MatrixStack matrices, float delta) {

        DrawPosition pos = getPos();

        if(showGraph.get()){
            graph.setData(pos.x + 5, pos.y + 5, getBounds().width- 10, getBounds().height - 10);

            fill(matrices, graph.x, graph.y,
                    (int) (graph.x + graph.width * (getUsage())),
                    graph.y + graph.height,
                    graphUsedColor.get().getAsInt());
            fill(matrices, (int) (graph.x + graph.width * (getUsage())),
                    graph.y, graph.x + graph.width,
                    graph.y + graph.height,
                    graphFreeColor.get().getAsInt());

            outlineRect(matrices, graph, Color.BLACK);
        }

        if(showText.get()) {
            drawString(matrices, getMemoryLine(),
                    pos.x,
                    pos.y + (Math.round((float) height / 2) - 4) - (showAllocated.get() ? 4 : 0),
                    textColor.get().getAsInt(),
                    shadow.get()
            );

            if (showAllocated.get()) {
                drawString(matrices, getAllocationLine(),
                        pos.x,
                        pos.y + (Math.round((float) height / 2) - 4) + 4,
                        textColor.get().getAsInt(),
                        shadow.get()
                );
            }
        }
    }

    @Override
    public void renderPlaceholderComponent(MatrixStack matrices, float delta) {

        DrawPosition pos = getPos();

        if(showGraph.get()){
            graph.setData(pos.x + 5, pos.y + 5, getBounds().width- 10, getBounds().height - 10);

            fill(matrices, graph.x, graph.y,
                    (int) (graph.x + graph.width * (0.3)),
                    graph.y + graph.height,
                    graphUsedColor.get().getAsInt());
            fill(matrices, (int) (graph.x + graph.width * (0.3)),
                    graph.y, graph.x + graph.width,
                    graph.y + graph.height,
                    graphFreeColor.get().getAsInt());

            outlineRect(matrices, graph, Color.BLACK);
        }

        if(showText.get()) {
            drawString(matrices, "300MiB/1024MiB",
                    pos.x,
                    pos.y + (Math.round((float) height / 2) - 4) - (showAllocated.get() ? 4 : 0),
                    Color.WHITE, shadow.get());
            if (showAllocated.get()) {
                drawString(matrices, I18n.translate("allocated")+": 976MiB",
                        pos.x,
                        pos.y + (Math.round((float) height / 2) - 4) + 4,
                        textColor.get(),
                        shadow.get());
            }
        }

        if(!showGraph.get() && !showText.get()){
            drawString(matrices, I18n.translate(ID.getPath()),
                    pos.x,
                    pos.y + (Math.round((float) height / 2) - 4), Color.WHITE, shadow.get());
        }
    }

    private String getMemoryLine() {
        long max = Runtime.getRuntime().maxMemory();
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        long used = total - free;

        return toMiB(used)+"/"+toMiB(max) + " ("+((int) (getUsage() * 100))+"%)";
    }

    private String getAllocationLine(){
        long total = Runtime.getRuntime().totalMemory();

        return I18n.translate("allocated")+": "+toMiB(total);
    }

    private float getUsage(){
        long max = Runtime.getRuntime().maxMemory();
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        long used = total - free;
        return (float) used / max;
    }

    @Override
    public List<Option<?>> getConfigurationOptions() {
        List<Option<?>> options = super.getConfigurationOptions();
        options.add(showGraph);
        options.add(graphUsedColor);
        options.add(graphFreeColor);
        options.add(showText);
        options.add(showAllocated);
        return options;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public boolean movable() {
        return true;
    }

    private static String toMiB(long bytes) {
        return (bytes / 1024L / 1024L)+"MiB";
    }
}
