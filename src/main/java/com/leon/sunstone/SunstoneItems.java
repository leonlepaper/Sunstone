package com.leon.sunstone;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * Все предметы мода.
 *
 * Цепочка: рудный блок -> сырая руда -> (печь) -> солнечный камень.
 * Дальше камень расходится на две ветки: блок хранения и зельеварение.
 */
public class SunstoneItems {

	/** Выпадает из рудного блока. Сырьё для всего остального. */
	public static final Item RAW_SUNSTONE = register("raw_sunstone",
			new Item(new Item.Settings()));

	/** Переплавленная руда. Идёт в блок и в грядку через семя. */
	public static final Item SUNSTONE = register("sunstone",
			new Item(new Item.Settings()));

	/**
	 * Урожай с грядки. Ингредиент всех трёх порядков дистиллята.
	 * BlockItem, а не простой Item: цветок можно поставить на землю и в горшок.
	 * Название при этом берётся из блока — ключ перевода block.sunstone.sunstone_flower.
	 */
	public static final Item SUNSTONE_FLOWER = register("sunstone_flower",
			new BlockItem(SunstoneBlocks.SUNSTONE_FLOWER, new Item.Settings()));

	/**
	 * Семя грядки. AliasedBlockItem ставит блок, но название берёт из предмета,
	 * а не из блока — иначе семя называлось бы «грядка солнечного камня».
	 */
	public static final Item SUNSTONE_SEED = register("sunstone_seed",
			new AliasedBlockItem(SunstoneBlocks.SUNSTONE_CROP, new Item.Settings()));

	/**
	 * Насыщает ровно как обычное яблоко, но откатывает время суток.
	 * alwaysEdible — можно есть с полной шкалой голода, как золотые яблоки:
	 * иначе сытый игрок не смог бы воспользоваться откатом времени.
	 */
	public static final Item SUNSTONE_APPLE = register("sunstone_apple",
			new SunstoneAppleItem(new Item.Settings().food(
					new FoodComponent.Builder()
							.nutrition(4)
							.saturationModifier(0.3f)
							.alwaysEdible()
							.build())));

	private static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, Sunstone.id(name), item);
	}

	public static void registerItems() {
		Sunstone.LOGGER.info("Регистрирую предметы Sunstone");
	}
}
