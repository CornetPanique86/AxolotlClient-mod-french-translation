/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.api;

import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.ui.screen.ConfigScreen;
import io.github.axolotlclient.api.chat.ChatListScreen;
import io.github.axolotlclient.api.requests.UserRequest;
import io.github.axolotlclient.util.keybinds.KeyBinds;
import io.github.axolotlclient.util.options.GenericOption;
import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class APIOptions extends Options {

	@Getter private static final Options Instance = new APIOptions();

	@Override
	public void init() {
		super.init();
		Minecraft client = Minecraft.getInstance();

		openPrivacyNoteScreen = n -> client.execute(() -> client.setScreen(new PrivacyNoticeScreen(client.screen, n)));
		KeyBinds.getInstance().registerWithSimpleAction(
			new KeyMapping("api.friends.sidebar.open", InputConstants.KEY_O, "category.axolotlclient"), () -> {
				if (API.getInstance().isAuthenticated()) {
					client.setScreen(new FriendsSidebar(client.screen));
				}
			});
		category.add(
			new GenericOption("viewFriends", "clickToOpen", () -> client.setScreen(new FriendsScreen(client.screen))));
		category.add(
			new GenericOption("viewChats", "clickToOpen", () -> client.setScreen(new ChatListScreen(client.screen))));
		account.add(new GenericOption("api.account.usernames", "clickToOpen",
									  () -> client.setScreen(new UsernameManagementScreen(client.screen))
		));
		account.add(new GenericOption("api.account.delete", "api.account.delete_account", () -> {
			Screen previous = client.screen;
			client.setScreen(new ConfirmScreen(b -> {
				if (b) {
					UserRequest.delete().thenAccept(r -> {
						if (r) {
							API.getInstance().getNotificationProvider()
								.addStatus("api.account.deletion.success", "api.account.deletion.success.desc");
						} else {
							API.getInstance().getNotificationProvider()
								.addStatus("api.account.deletion.failure", "api.account.deletion.failure.desc");
						}
						enabled.set(false);
					});
				}
				client.setScreen(previous);
			}, Component.translatable("api.account.confirm_deletion"),
											   Component.translatable("api.account.confirm_deletion.desc")
			));
		}));
		Consumer<Boolean> consumer = settingUpdated;
		settingUpdated = b -> {
			if (client.screen instanceof ConfigScreen) {
				consumer.accept(b);
			}
		};
		if (Constants.ENABLED) {
			AxolotlClient.CONFIG.addCategory(category);
			AxolotlClient.config.add(privacyAccepted);
		}
	}
}
