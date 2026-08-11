package com.leon.sunstone;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

/** Эффекты мода и их регистрация. */
public class SunstoneEffects {

	// Цвет эффекта — это ещё и цвет зелья: ваниль смешивает цвета всех эффектов,
	// взвешивая по уровню. Золотой (0xFFD84F) сливался с ванильной «Спешкой»
	// (0xD9C043), и первые два порядка выглядели одинаково. Белый и красный
	// разводят смеси: жёлтый -> бледно-жёлтый -> оранжевый.

	/** Реальный свет вокруг носителя. Второй порядок дистиллята. */
	public static final RegistryEntry<StatusEffect> SUNLIT = register("sunlit",
			new SunlitStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFFFFF));

	/** Носитель горит. Третий порядок дистиллята. */
	public static final RegistryEntry<StatusEffect> SOLAR_OVERLOAD = register("solar_overload",
			new SolarOverloadStatusEffect(StatusEffectCategory.HARMFUL, 0xFF3010));

	private static RegistryEntry<StatusEffect> register(String name, StatusEffect effect) {
		return Registry.registerReference(Registries.STATUS_EFFECT, Sunstone.id(name), effect);
	}

	public static void registerEffects() {
		// Сборщик мусора для блоков света: эффект может кончиться, а блок остаться.
		ServerTickEvents.END_SERVER_TICK.register(SunlitStatusEffect::sweep);

		// Достижения перегрева: эффект кончается без всякого хука, поэтому
		// досматриваем попытки «пережить» тем же способом — обходом на тике.
		ServerTickEvents.END_SERVER_TICK.register(SolarOverloadStatusEffect::sweep);

		// И запоминаем брошенные взрывные зелья, чтобы отличить «поджёг себя сам».
		UseItemCallback.EVENT.register(SolarOverloadStatusEffect::onItemUsed);

		Sunstone.LOGGER.info("Регистрирую эффекты Sunstone");
	}
}
