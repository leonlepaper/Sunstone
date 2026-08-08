package com.leon.sunstone;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.List;

/**
 * Солнечный дистиллят трёх порядков.
 *
 * Варится из солнечного цветка. Порошок блейза при этом не ингредиент,
 * а топливо варочной стойки — он нужен всегда и в рецепте не указывается.
 * Каждый следующий порядок получается перегонкой предыдущего тем же цветком.
 */
public class SunstonePotions {

	// Чем сильнее порядок, тем короче действие: слабый эффект носишь долго,
	// сильный — вспышкой, а третий вообще пережить надо.
	private static final int DURATION_1 = 3600;   // 3 минуты, как у ванильных утилитарных зелий
	private static final int DURATION_2 = 1800;   // 1.5 минуты
	private static final int DURATION_3 = 600;    // 30 секунд

	/**
	 * Первый порядок: ускоряет добычу.
	 *
	 * «Свечение» тут не годится — его уже раздают спектральные стрелы.
	 * Спешка же зельем не выдаётся нигде: в ванили её даёт только маяк.
	 */
	public static final RegistryEntry<Potion> SOLAR_DISTILLATE = register("solar_distillate",
			new Potion("solar_distillate",
					new StatusEffectInstance(StatusEffects.HASTE, DURATION_1, 0)));

	/**
	 * Второй порядок: свет вокруг носителя плюс всё та же спешка.
	 * Порядки складываются, а не заменяют друг друга — иначе второй был бы
	 * не усилением первого, а просто другим зельем.
	 */
	public static final RegistryEntry<Potion> SOLAR_DISTILLATE_2 = register("solar_distillate_2",
			new Potion("solar_distillate_2",
					new StatusEffectInstance(SunstoneEffects.SUNLIT, DURATION_2, 0),
					new StatusEffectInstance(StatusEffects.HASTE, DURATION_2, 0)));

	/**
	 * Третий порядок: вершина ветки — свет и удвоенная спешка, но носитель горит.
	 *
	 * Огнестойкость сюда намеренно не входит: она обнулила бы всю затею.
	 * Подготовиться игрок должен сам — своим зельем, дождём или водой.
	 * Во взрывном варианте это же зелье работает как оружие: поджигает тех, в кого прилетело.
	 */
	public static final RegistryEntry<Potion> SOLAR_DISTILLATE_3 = register("solar_distillate_3",
			new Potion("solar_distillate_3",
					new StatusEffectInstance(SunstoneEffects.SOLAR_OVERLOAD, DURATION_3, 0),
					new StatusEffectInstance(SunstoneEffects.SUNLIT, DURATION_3, 0),
					new StatusEffectInstance(StatusEffects.HASTE, DURATION_3, 1)));

	private static RegistryEntry<Potion> register(String name, Potion potion) {
		return Registry.registerReference(Registries.POTION, Sunstone.id(name), potion);
	}

	/** Все три порядка в порядке усиления — используется и вкладкой креатива. */
	public static final List<RegistryEntry<Potion>> ALL =
			List.of(SOLAR_DISTILLATE, SOLAR_DISTILLATE_2, SOLAR_DISTILLATE_3);

	public static void registerPotions() {
		FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
			Ingredient flower = Ingredient.ofItems(SunstoneItems.SUNSTONE_FLOWER);

			// Из мутного зелья, а не из воды: в Minecraft вода + незеровый нарост
			// даёт мутное зелье, и уже оно служит основой для всех остальных.
			builder.registerPotionRecipe(Potions.AWKWARD, flower, SOLAR_DISTILLATE);
			builder.registerPotionRecipe(SOLAR_DISTILLATE, flower, SOLAR_DISTILLATE_2);
			builder.registerPotionRecipe(SOLAR_DISTILLATE_2, flower, SOLAR_DISTILLATE_3);
		});

		Sunstone.LOGGER.info("Регистрирую зелья Sunstone");
	}
}
