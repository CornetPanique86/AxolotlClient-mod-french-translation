/*
 * Copyright © 2021-2022 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.mixin;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.axolotlclient.util.Hooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.util.collection.IntObjectStorage;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {

    @Shadow
    private boolean pressed;

    @Shadow
    private int code;

    @Shadow
    @Final
    private static IntObjectStorage<KeyBinding> KEY_MAP;

    @Inject(method = "isPressed", at = @At("HEAD"))
    public void axolotlclient$noMovementFixAfterInventory(CallbackInfoReturnable<Boolean> cir) {
        if (this.code == MinecraftClient.getInstance().options.keySneak.getCode()
                || code == MinecraftClient.getInstance().options.keyForward.getCode()
                || code == MinecraftClient.getInstance().options.keyBack.getCode()
                || code == MinecraftClient.getInstance().options.keyRight.getCode()
                || code == MinecraftClient.getInstance().options.keyLeft.getCode()
                || code == MinecraftClient.getInstance().options.keyJump.getCode()
                || code == MinecraftClient.getInstance().options.keySprint.getCode()) {
            this.pressed = Keyboard.isKeyDown(code) && (MinecraftClient.getInstance().currentScreen == null);
        }
    }

    @Inject(method = "setCode", at = @At("RETURN"))
    public void axolotlclient$boundKeySet(int code, CallbackInfo ci) {
        Hooks.KEYBIND_CHANGE.invoker().setBoundKey(code);
    }

    @Inject(method = "setKeyPressed", at = @At(value = "FIELD", target = "Lnet/minecraft/client/options/KeyBinding;pressed:Z"))
    private static void axolotlclient$onPress(int keyCode, boolean pressed, CallbackInfo ci) {
        if (pressed) {
            Hooks.KEYBIND_PRESS.invoker().onPress(KEY_MAP.get(keyCode));
        }
    }
}
