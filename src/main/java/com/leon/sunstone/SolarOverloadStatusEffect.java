package com.leon.sunstone;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;

/**
 * «Солнечный перегрев». Носитель горит и осыпается пламенем.
 *
 * Сам по себе эффект чисто вредный — сила третьего порядка приходит
 * от других эффектов зелья. Смысл в том, чтобы пить его подготовившись:
 * с огнестойкостью, в дождь или стоя по пояс в воде.
 */
public class SolarOverloadStatusEffect extends StatusEffect {

	/** Насколько поджигаем за одно срабатывание. */
	private static final int FIRE_TICKS = 60;

	/** Раз в сколько тиков подновляем огонь — чаще незачем, он и так горит. */
	private static final int FIRE_INTERVAL = 20;

	/** Раз в сколько тиков сыплем пламя. Чаще — плотнее шлейф, но больше пакетов по сети. */
	private static final int PARTICLE_INTERVAL = 2;

	protected SolarOverloadStatusEffect(StatusEffectCategory category, int color) {
		super(category, color);
	}

	/** Нужен каждый тик: иначе частицы пошли бы рывками раз в секунду. */
	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return true;
	}

	@Override
	public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
		if (!(entity.getWorld() instanceof ServerWorld world)) {
			return true;
		}

		if (entity.age % FIRE_INTERVAL == 0) {
			entity.setOnFireForTicks(FIRE_TICKS);
		}

		if (entity.age % PARTICLE_INTERVAL == 0) {
			double h = entity.getHeight();
			double w = entity.getWidth();

			// Крупное пламя по корпусу...
			world.spawnParticles(ParticleTypes.FLAME,
					entity.getX(), entity.getY() + h * 0.5, entity.getZ(),
					6, w * 0.5, h * 0.4, w * 0.5, 0.01);

			// ...и мелкие искры повыше, чтобы шлейф тянулся вверх.
			world.spawnParticles(ParticleTypes.SMALL_FLAME,
					entity.getX(), entity.getY() + h * 0.75, entity.getZ(),
					4, w * 0.4, h * 0.25, w * 0.4, 0.03);
		}

		return true;
	}
}
