package com.leon.sunstone;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * «Солнечный перегрев»: третий порядок дистиллята — уже перебор,
 * и носитель попросту горит.
 */
public class SolarOverloadStatusEffect extends StatusEffect {

	/** Насколько поджигаем за одно срабатывание. */
	private static final int FIRE_TICKS = 60;

	protected SolarOverloadStatusEffect(StatusEffectCategory category, int color) {
		super(category, color);
	}

	/** Раз в секунду — этого хватает, чтобы огонь не гас, пока действует эффект. */
	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return duration % 20 == 0;
	}

	@Override
	public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
		if (!entity.getWorld().isClient) {
			entity.setOnFireForTicks(FIRE_TICKS);
		}
		return true;
	}
}
