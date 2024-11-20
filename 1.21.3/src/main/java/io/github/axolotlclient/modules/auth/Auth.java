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

package io.github.axolotlclient.modules.auth;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.util.UndashedUuid;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.mixin.MinecraftClientAccessor;
import io.github.axolotlclient.modules.Module;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.ThreadExecuter;
import io.github.axolotlclient.util.notifications.Notifications;
import io.github.axolotlclient.util.options.GenericOption;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class Auth extends Accounts implements Module {

	@Getter
	private final static Auth Instance = new Auth();
	public final BooleanOption showButton = new BooleanOption("auth.showButton", false);
	private final Minecraft mc = Minecraft.getInstance();
	private final GenericOption viewAccounts = new GenericOption("viewAccounts", "clickToOpen", () -> mc.setScreen(new AccountsScreen(mc.screen)));
	private final Set<String> loadingTexture = new HashSet<>();
	private final Map<String, ResourceLocation> textures = new WeakHashMap<>();

	@Override
	public void init() {
		load();
		this.auth = new MSAuth(AxolotlClient.LOGGER, this, () -> mc.options.languageCode);
		if (isContained(mc.getUser().getSessionId())) {
			current = getAccounts().stream().filter(account -> account.getUuid().equals(UndashedUuid.toString(mc.getUser().getProfileId()))).toList().getFirst();
			if (current.needsRefresh()) {
				current.refresh(auth, () -> {
				});
			}
		} else {
			current = new Account(mc.getUser().getName(), UndashedUuid.toString(mc.getUser().getProfileId()), mc.getUser().getAccessToken());
		}

		OptionCategory category = OptionCategory.create("auth");
		category.add(showButton, viewAccounts);
		AxolotlClient.CONFIG.general.add(category);
	}

	@Override
	protected Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}

	@Override
	protected void login(Account account) {
		if (mc.level != null) {
			return;
		}

		Runnable runnable = () -> {
			try {
				API.getInstance().shutdown();
				((MinecraftClientAccessor) mc).axolotlclient$setSession(new User(account.getName(), UndashedUuid.fromString(account.getUuid()), account.getAuthToken(), Optional.empty(), Optional.empty(), User.Type.MSA));
				UserApiService service;
				if (account.isOffline()) {
					service = UserApiService.OFFLINE;
				} else {

					service = ((MinecraftClientAccessor) mc).getAuthService().createUserApiService(mc.getUser().getAccessToken());
				}
				((MinecraftClientAccessor) mc).axolotlclient$setUserApiService(service);
				((MinecraftClientAccessor) mc).axolotlclient$setSocialInteractionsManager(new PlayerSocialManager(mc, service));
				((MinecraftClientAccessor) mc).axolotlclient$setPlayerKeyPairManager(ProfileKeyPairManager.create(service, mc.getUser(), mc.gameDirectory.toPath()));
				((MinecraftClientAccessor) mc).axolotlclient$setChatReportingContext(ReportingContext.create(ReportEnvironment.local(), service));
				save();
				current = account;
				Notifications.getInstance().addStatus(Component.translatable("auth.notif.title"), Component.translatable("auth.notif.login.successful", current.getName()));
				ThreadExecuter.scheduleTask(() -> API.getInstance().startup(account), 50, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				Notifications.getInstance().addStatus(Component.translatable("auth.notif.title"), Component.translatable("auth.notif.login.failed"));
			}
		};

		if (account.needsRefresh() && !account.isOffline()) {
			if (account.isExpired()) {
				Notifications.getInstance().addStatus(Component.translatable("auth.notif.title"), Component.translatable("auth.notif.refreshing", account.getName()));
			}
			account.refresh(auth, runnable);
		} else {
			new Thread(runnable).start();
		}
	}

	@Override
	protected Logger getLogger() {
		return AxolotlClient.LOGGER;
	}

	@Override
	void showAccountsExpiredScreen(Account account) {
		Screen current = mc.screen;
		mc.execute(() -> mc.setScreen(new ConfirmScreen((bl) -> {
			mc.setScreen(current);
			if (bl) {
				auth.startDeviceAuth(() -> {
				});
			}
		}, Component.translatable("auth"), Component.translatable("auth.accountExpiredNotice", account.getName()))));
	}

	@Override
	void displayDeviceCode(DeviceFlowData data) {
		Screen display = new DeviceCodeDisplayScreen(mc.screen, data);
		mc.setScreen(display);
	}

	private void loadTexture(String uuid) {
		if (!loadingTexture.contains(uuid)) {
			loadingTexture.add(uuid);
			ThreadExecuter.scheduleTask(() -> {
				UUID uUID = UndashedUuid.fromString(uuid);
				ProfileResult profileResult = mc.getMinecraftSessionService().fetchProfile(uUID, false);
				if (profileResult != null) {
					mc.getSkinManager().getOrLoad(profileResult.profile()).thenAccept(playerSkin -> textures.put(uuid, playerSkin.texture()));
				}
				loadingTexture.remove(uuid);
			});
		}
	}

	public ResourceLocation getSkinTexture(Account account) {
		return getSkinTexture(account.getUuid(), account.getName());
	}

	public ResourceLocation getSkinTexture(String uuid, String name) {
		if (!textures.containsKey(uuid)) {
			loadTexture(uuid);
			return Objects.requireNonNullElseGet(textures.get(uuid), () -> DefaultPlayerSkin.get(UndashedUuid.fromString(uuid)).texture());
		}
		return textures.get(uuid);
	}
}
