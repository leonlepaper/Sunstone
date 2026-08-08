package com.leon.sunstone;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/**
 * Солнечное яблоко: съедается как обычное, но откатывает время суток назад.
 */
public class SunstoneAppleItem extends Item {

	/** На сколько тиков отматывается время суток за одно яблоко. */
	private static final long REWIND_TICKS = 500L;

	private static final long DAY_LENGTH = 24000L;

	public SunstoneAppleItem(Settings settings) {
		super(settings);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		if (world instanceof ServerWorld serverWorld) {
			long dayTime = Math.floorMod(world.getTimeOfDay(), DAY_LENGTH);

			// Не уводим время за начало текущих суток: иначе сменилась бы фаза луны,
			// которая считается как (DayTime / 24000) % 8.
			long back = Math.min(REWIND_TICKS, dayTime);

			// Время суток общее для всех измерений — двигаем везде, как это делает /time.
			for (ServerWorld w : serverWorld.getServer().getWorlds()) {
				w.setTimeOfDay(w.getTimeOfDay() - back);
			}
		}

		return super.finishUsing(stack, world, user);
	}
}
