/*
 * Copyright © 2021-2023 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hypixel.autoboop;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.modules.hypixel.AbstractHypixelMod;
import io.github.axolotlclient.util.ThreadExecuter;
import io.github.axolotlclient.util.Util;
import lombok.Getter;
import net.minecraft.text.Text;

// Based on https://github.com/VeryHolyCheeeese/AutoBoop/blob/main/src/main/java/autoboop/AutoBoop.java
public class AutoBoop implements AbstractHypixelMod {

	@Getter
	private final static AutoBoop Instance = new AutoBoop();

	protected final OptionCategory cat = OptionCategory.create("autoBoop");
	protected final BooleanOption enabled = new BooleanOption("enabled", false);

	@Override
	public void init() {
		cat.add(enabled);
	}

	@Override
	public OptionCategory getCategory() {
		return cat;
	}

	public void onMessage(Text message) {
		if (enabled.get() && message.getString().contains("Friend >") && message.getString().contains("joined.")) {
			String player = message.getString().substring(message.getString().indexOf(">"),
				message.getString().lastIndexOf(" "));
			ThreadExecuter.scheduleTask(() -> {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignored) {
				}
				Util.sendChatMessage("/boop " + player);
				AxolotlClient.LOGGER.info("Booped " + player);
			});
		}
	}
}
