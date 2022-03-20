package io.github.moehreag.axolotlclient.config.screen;

import io.github.moehreag.axolotlclient.Axolotlclient;
import io.github.moehreag.axolotlclient.config.widgets.BooleanButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class RPCConfScreen extends ConfScreen {
    protected Screen parent;

    public RPCConfScreen(Screen parent){
        super("rpcConf.title");
        this.parent = parent;
    }

    @Override
    public void init() {
        super.init();
        this.buttons.add(new BooleanButtonWidget(1, this.width / 2 - 155, this.height / 6 + 72 - 6, "enableRPC", Axolotlclient.CONFIG.RPCConfig.enableRPC));
        this.buttons.add(new BooleanButtonWidget(2, this.width / 2 + 5, this.height / 6 + 72 - 6, "showActivity", Axolotlclient.CONFIG.RPCConfig.showActivity));
    }

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        super.render(mouseX, mouseY, tickDelta);
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        super.buttonClicked(button);
        if(button.id>0){
            if(button.id==1)Axolotlclient.CONFIG.RPCConfig.enableRPC=!Axolotlclient.CONFIG.RPCConfig.enableRPC;
            if(button.id==2)Axolotlclient.CONFIG.RPCConfig.showActivity=!Axolotlclient.CONFIG.RPCConfig.showActivity;
            MinecraftClient.getInstance().openScreen(this);
        }
    }
}
