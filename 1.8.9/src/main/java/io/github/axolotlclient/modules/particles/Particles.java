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

package io.github.axolotlclient.modules.particles;

import java.util.*;
import java.util.stream.Collectors;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.mixin.ParticleAccessor;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.Util;
import lombok.Getter;
import net.minecraft.client.entity.particle.Particle;
import net.minecraft.entity.particle.ParticleType;
import org.apache.commons.lang3.StringUtils;

public class Particles extends AbstractModule {

	@Getter
	private static final Particles Instance = new Particles();

	public final HashMap<ParticleType, HashMap<String, Option<?>>> particleOptions = new HashMap<>();
	public final HashMap<Particle, ParticleType> particleMap = new HashMap<>();

	private final OptionCategory cat = OptionCategory.create("particles");
	private final BooleanOption enabled = new BooleanOption("enabled", false);

	@Override
	public void init() {
		cat.add(enabled);

		addParticleOptions();
		AxolotlClient.CONFIG.rendering.add(cat);
	}

	private void addParticleOptions() {
		for (ParticleType type : Arrays.stream(ParticleType.values()).sorted(new AlphabeticalComparator())
			.collect(Collectors.toList())) {
			OptionCategory category = OptionCategory.create(
				StringUtils.capitalize(Util.splitAtCapitalLetters(type.getKey().replace("_", ""))));
			HashMap<String, Option<?>> optionsByKey = new LinkedHashMap<>();

			populateMap(optionsByKey, new BooleanOption("showParticle", true),
				new IntegerOption("count", 1, 1, 20),
				new BooleanOption("customColor", false),
				new ColorOption("color", "particles.color.tooltip", ClientColors.WHITE));

			if (type == ParticleType.CRIT || type == ParticleType.CRIT_MAGIC) {
				populateMap(optionsByKey, new BooleanOption("alwaysCrit", false));
			}

			optionsByKey.values().forEach(category::add);
			particleOptions.put(type, optionsByKey);

			cat.add(category);
		}
	}

	private void populateMap(HashMap<String, Option<?>> map, Option<?>... options) {
		for (Option<?> option : options) {
			map.put(option.getName(), option);
		}
	}

	public void applyOptions(Particle particle) {
		if (enabled.get() && particleMap.containsKey(particle)) {
			ParticleType type = particleMap.get(particle);
			if (particleOptions.containsKey(type)) {
				HashMap<String, Option<?>> options = particleOptions.get(type);

				if (((BooleanOption) options.get("customColor")).get()) {
					Color color = ((ColorOption) options.get("color")).get();
					particle.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
					((ParticleAccessor) particle).setAlpha(color.getAlpha() / 255F);
				}
			}
		}
	}

	public int getMultiplier(ParticleType type) {
		if (enabled.get() && particleOptions.containsKey(type)) {
			HashMap<String, Option<?>> options = particleOptions.get(type);

			return ((IntegerOption) options.get("count")).get();
		}
		return 1;
	}

	public boolean getAlwaysOn(ParticleType type) {
		return enabled.get() && particleOptions.containsKey(type)
			&& ((BooleanOption) Particles.getInstance().particleOptions.get(type).get("alwaysCrit"))
			.get();
	}

	public boolean getShowParticle(ParticleType type) {
		return enabled.get() && particleOptions.containsKey(type)
			? ((BooleanOption) Particles.getInstance().particleOptions.get(type).get("showParticle")).get()
			: true;
	}

	protected static class AlphabeticalComparator implements Comparator<ParticleType> {

		// Function to compare
		public int compare(ParticleType s1, ParticleType s2) {
			if (s1.getKey().equals(s2.getKey()))
				return 0;
			String[] strings = {s1.getKey(), s2.getKey()};
			Arrays.sort(strings, Collections.reverseOrder());

			if (strings[0].equals(s1.getKey()))
				return 1;
			else
				return -1;
		}
	}
}
