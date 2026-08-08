package com.leon.sunstone;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

/**
 * Подключает генерацию руды в мире.
 *
 * Сама «форма» жилы описана в JSON (data/sunstone/worldgen/...), а здесь мы
 * только говорим игре: «добавь эту фичу во все биомы Верхнего мира».
 */
public class SunstoneWorldGen {

	/** Ссылка на data/sunstone/worldgen/placed_feature/sunstone_ore_placed.json */
	public static final RegistryKey<PlacedFeature> SUNSTONE_ORE_PLACED =
			RegistryKey.of(RegistryKeys.PLACED_FEATURE, Sunstone.id("sunstone_ore_placed"));

	public static void registerWorldGen() {
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES,
				SUNSTONE_ORE_PLACED);

		Sunstone.LOGGER.info("Генерация руды Sunstone подключена");
	}
}
