package com.leon.sunstone;

import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;

/**
 * Ступени солнечного камня.
 *
 * Класс существует по одной причине: у ванильного StairsBlock конструктор
 * объявлен protected, и снаружи его пакета вызвать нельзя. Наследник открывает
 * доступ, ничего не меняя в поведении.
 */
public class SunstoneStairsBlock extends StairsBlock {

	public SunstoneStairsBlock(BlockState baseState, Settings settings) {
		super(baseState, settings);
	}
}
