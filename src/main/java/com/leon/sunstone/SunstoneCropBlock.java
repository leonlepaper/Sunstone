package com.leon.sunstone;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.item.ItemConvertible;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;

/**
 * Грядка солнечного камня: шесть стадий роста, на последней распускается цветок.
 *
 * CropBlock по умолчанию рассчитан на восемь стадий (AGE_7), поэтому мы
 * подменяем свойство возраста и его максимум на AGE_5 (значения 0..5).
 */
public class SunstoneCropBlock extends CropBlock {

	public static final MapCodec<SunstoneCropBlock> CODEC = createCodec(SunstoneCropBlock::new);

	public static final IntProperty AGE = Properties.AGE_5;
	public static final int MAX_AGE = Properties.AGE_5_MAX;

	public SunstoneCropBlock(AbstractBlock.Settings settings) {
		super(settings);
	}

	@Override
	public MapCodec<? extends CropBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected IntProperty getAgeProperty() {
		return AGE;
	}

	@Override
	public int getMaxAge() {
		return MAX_AGE;
	}

	/** Что выпадает с недозревшей грядки и чем её сажают. */
	@Override
	protected ItemConvertible getSeedsItem() {
		return SunstoneItems.SUNSTONE_SEED;
	}

	/**
	 * Без этого блок унаследовал бы AGE_7 от CropBlock и не сошёлся бы
	 * с getAgeProperty() — игра упала бы при создании состояний блока.
	 */
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}
}
