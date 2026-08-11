package com.leon.sunstone;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Точка входа мода. Прописана в fabric.mod.json -> entrypoints.main
 * Fabric вызывает onInitialize() один раз при запуске игры (и на клиенте, и на сервере).
 */
public class Sunstone implements ModInitializer {
	/** Идентификатор мода. Должен совпадать с "id" в fabric.mod.json. */
	public static final String MOD_ID = "sunstone";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** Короткий хелпер: Sunstone.id("sunstone_ore") -> sunstone:sunstone_ore */
	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		// Порядок важен: блоки первыми (на грядку ссылается солнечное семя),
		// затем предметы, затем эффекты — на них ссылаются зелья,
		// и только потом вкладка, которая ссылается на всё сразу.
		SunstoneBlocks.registerBlocks();
		SunstoneItems.registerItems();
		SunstoneCriteria.registerCriteria();
		SunstoneEffects.registerEffects();
		SunstonePotions.registerPotions();
		SunstoneItemGroups.registerItemGroups();
		SunstoneWorldGen.registerWorldGen();

		LOGGER.info("Мод Sunstone загружен");
	}
}
