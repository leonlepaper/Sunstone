package com.leon.sunstone;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.intprovider.UniformIntProvider;

/**
 * Все блоки мода.
 *
 * Каждый блок регистрируется дважды: как Block (то, что стоит в мире)
 * и как BlockItem (то, что лежит в инвентаре). Это разные реестры.
 */
public class SunstoneBlocks {

	/** Руда в обычном камне. Даёт 2-5 опыта при добыче без «шёлкового касания». */
	public static final Block SUNSTONE_ORE = register("sunstone_ore",
			new ExperienceDroppingBlock(
					UniformIntProvider.create(2, 5),
					AbstractBlock.Settings.copy(Blocks.IRON_ORE)));

	/** Та же руда, но в глубинном сланце — она прочнее, копается дольше. */
	public static final Block DEEPSLATE_SUNSTONE_ORE = register("deepslate_sunstone_ore",
			new ExperienceDroppingBlock(
					UniformIntProvider.create(2, 5),
					AbstractBlock.Settings.copy(Blocks.DEEPSLATE_IRON_ORE)));

	/** Блок хранения 9 солнечных камней. Светит как факел (14), можно ставить вместо него. */
	public static final Block SUNSTONE_BLOCK = register("sunstone_block",
			new Block(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)
					.luminance(state -> 14)));

	/**
	 * Грядка солнечного камня. Регистрируется без BlockItem: в инвентаре её
	 * представляет солнечное семя (см. SunstoneItems.SUNSTONE_SEED).
	 * Разгорается по мере роста — от 0 до 10 единиц света.
	 */
	public static final Block SUNSTONE_CROP = Registry.register(
			Registries.BLOCK, Sunstone.id("sunstone_crop"),
			new SunstoneCropBlock(AbstractBlock.Settings.copy(Blocks.WHEAT)
					.luminance(state -> state.get(SunstoneCropBlock.AGE) * 2)));

	/**
	 * Срезанный цветок, который можно поставить на землю.
	 * BlockItem для него заводится в SunstoneItems — там же, где остальные предметы.
	 * Второй аргумент — эффект, который цветок даёт подозрительному супу.
	 */
	public static final Block SUNSTONE_FLOWER = Registry.register(
			Registries.BLOCK, Sunstone.id("sunstone_flower"),
			new FlowerBlock(StatusEffects.GLOWING, 6.0f,
					AbstractBlock.Settings.copy(Blocks.POPPY).luminance(state -> 7)));

	/**
	 * Тот же цветок в горшке. Отдельно связывать его с пустым горшком не нужно:
	 * конструктор FlowerPotBlock сам вписывает себя в ванильную карту
	 * «что во что сажается», и клик цветком по горшку начинает работать.
	 */
	public static final Block POTTED_SUNSTONE_FLOWER = Registry.register(
			Registries.BLOCK, Sunstone.id("potted_sunstone_flower"),
			new FlowerPotBlock(SUNSTONE_FLOWER, AbstractBlock.Settings.copy(Blocks.POTTED_POPPY)));

	private static Block register(String name, Block block) {
		Block registered = Registry.register(Registries.BLOCK, Sunstone.id(name), block);
		Registry.register(Registries.ITEM, Sunstone.id(name),
				new BlockItem(registered, new Item.Settings()));
		return registered;
	}

	public static void registerBlocks() {
		Sunstone.LOGGER.info("Регистрирую блоки Sunstone");
	}
}
