package com.leon.sunstone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

/**
 * Свои триггеры достижений.
 *
 * Ванильных триггеров на «поджёг себя собственным зельем» и «пережил эффект
 * до конца» не существует: в готовом эффекте не остаётся никаких следов того,
 * как он был получен, а хука «эффект истёк» у StatusEffect нет вовсе.
 * Поэтому оба события мод определяет сам (см. SolarOverloadStatusEffect)
 * и дёргает отсюда критерий, а JSON достижения называет его по имени —
 * например "sunstone:self_ignition".
 */
public class SunstoneCriteria {

	/** Игрок вспыхнул от собственного взрывного дистиллята третьего порядка. */
	public static final SunstoneCriterion SELF_IGNITION = register("self_ignition");

	/** Игрок продержался весь третий порядок без огнестойкости и золотых яблок. */
	public static final SunstoneCriterion OVERLOAD_SURVIVED = register("overload_survived");

	private static SunstoneCriterion register(String name) {
		return Registry.register(Registries.CRITERION, Sunstone.id(name), new SunstoneCriterion());
	}

	public static void registerCriteria() {
		Sunstone.LOGGER.info("Регистрирую критерии достижений Sunstone");
	}

	/**
	 * Критерий без собственных условий: срабатывает от одного вызова trigger(),
	 * вся проверка живёт в Java.
	 *
	 * Поле "player" всё же есть — оно обязательно для любого критерия и позволяет
	 * дописать в JSON условия на самого игрока, не трогая код.
	 */
	public static class SunstoneCriterion extends AbstractCriterion<SunstoneCriterion.Conditions> {

		@Override
		public Codec<Conditions> getConditionsCodec() {
			return Conditions.CODEC;
		}

		/** Засчитать событие игроку. Условия из JSON проверит сама ваниль. */
		public void trigger(ServerPlayerEntity player) {
			trigger(player, conditions -> true);
		}

		public record Conditions(Optional<LootContextPredicate> player)
				implements AbstractCriterion.Conditions {

			public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance
					.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC
							.optionalFieldOf("player")
							.forGetter(Conditions::player))
					.apply(instance, Conditions::new));
		}
	}
}
