package com.leon.sunstone;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * Солнечное яблоко: съедается как обычное, но откатывает время суток назад.
 * Работает только пока солнце в небе — ночью откатывать нечего.
 */
public class SunstoneAppleItem extends Item {

	/**
	 * На сколько тиков отматывается время суток за одно яблоко.
	 *
	 * Яблоко стоит 8 блоков, то есть 72 солнечных камня — за такую цену
	 * прежние 500 тиков (меньше получаса игрового времени) не оправдывались.
	 */
	private static final long REWIND_TICKS = 2000L;

	private static final long DAY_LENGTH = 24000L;

	/** После этого времени суток солнце садится. */
	private static final long SUNSET = 12000L;

	public SunstoneAppleItem(Settings settings) {
		super(settings);
	}

	/**
	 * Ночью яблоко даже не откусывается. Так игрок не потратит впустую предмет
	 * ценой в 72 камня — просто получит подсказку и оставит его на утро.
	 */
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);

		if (isNight(world)) {
			if (!world.isClient) {
				user.sendMessage(Text.translatable("item.sunstone.sunstone_apple.night"), true);
			}
			return TypedActionResult.fail(stack);
		}

		return super.use(world, user, hand);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		if (world instanceof ServerWorld serverWorld && !isNight(world)) {
			long dayTime = Math.floorMod(world.getTimeOfDay(), DAY_LENGTH);

			// Не уводим время за начало текущих суток: иначе сменилась бы фаза луны,
			// которая считается как (DayTime / 24000) % 8.
			long back = Math.min(REWIND_TICKS, dayTime);

			// Время суток общее для всех измерений — двигаем везде, как это делает /time.
			for (ServerWorld w : serverWorld.getServer().getWorlds()) {
				w.setTimeOfDay(w.getTimeOfDay() - back);
			}

			world.playSound(null, user.getX(), user.getY(), user.getZ(),
					SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.6f, 1.5f);
		}

		return super.finishUsing(stack, world, user);
	}

	private static boolean isNight(World world) {
		return Math.floorMod(world.getTimeOfDay(), DAY_LENGTH) >= SUNSET;
	}
}
