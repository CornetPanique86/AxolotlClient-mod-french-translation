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

package io.github.axolotlclient.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftClientAccessor {

	@Accessor("fps")
	static int axolotlclient$getCurrentFps() {
		return 0;
	}

	@Accessor("user")
	@Mutable
	void axolotlclient$setSession(User session);

	@Accessor("playerSocialManager")
	@Mutable
	void axolotlclient$setSocialInteractionsManager(PlayerSocialManager manager);

	@Accessor("profileKeyPairManager")
	@Mutable
	void axolotlclient$setPlayerKeyPairManager(ProfileKeyPairManager manager);

	@Accessor("reportingContext")
	@Mutable
	void axolotlclient$setChatReportingContext(ReportingContext context);

	@Accessor("userApiService")
	@Mutable
	void axolotlclient$setUserApiService(UserApiService service);

	@Accessor("authenticationService")
	YggdrasilAuthenticationService getAuthService();
}
