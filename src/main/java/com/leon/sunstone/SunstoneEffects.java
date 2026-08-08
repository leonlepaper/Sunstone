package com.leon.sunstone;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

/** Эффекты мода и их регистрация. */
public class SunstoneEffects {

	/** Реальный свет вокруг носителя. Второй порядок дистиллята. */
	public static final RegistryEntry<StatusEffect> SUNLIT = register("sunlit",
			new SunlitStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD84F));

	/** Носитель горит. Третий порядок дистиллята. */
	public static final RegistryEntry<StatusEffect> SOLAR_OVERLOAD = register("solar_overload",
			new SolarOverloadStatusEffect(StatusEffectCategory.HARMFUL, 0xFF6A00));

	private static RegistryEntry<StatusEffect> register(String name, StatusEffect effect) {
		return Registry.registerReference(Registries.STATUS_EFFECT, Sunstone.id(name), effect);
	}

	public static void registerEffects() {
		// Сборщик мусора для блоков света: эффект может кончиться, а блок остаться.
		ServerTickEvents.END_SERVER_TICK.register(SunlitStatusEffect::sweep);

		Sunstone.LOGGER.info("Регистрирую эффекты Sunstone");
	}
}
