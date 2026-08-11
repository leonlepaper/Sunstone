package com.leon.sunstone;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

	/**
	 * Кто недавно метнул взрывной дистиллят третьего порядка.
	 * Ключ — UUID игрока, значение — тик, до которого вспышку ещё засчитываем.
	 */
	private static final Map<UUID, Long> RECENT_THROWERS = new ConcurrentHashMap<>();

	/**
	 * Сколько тиков после броска эффект считается «своим». До земли зелье летит
	 * доли секунды, но игрок может и подкинуть его вверх — берём с запасом.
	 */
	private static final int THROW_WINDOW = 200;

	/** Незаконченные попытки пережить эффект. Ключ — UUID игрока. */
	private static final Map<UUID, Attempt> ATTEMPTS = new ConcurrentHashMap<>();

	/**
	 * Запас на последний тик эффекта: точный остаток на финальном вызове зависит
	 * от порядка обновления эффектов в ванили. Молоко и смерть обрывают эффект
	 * на остатке в сотни тиков, так что перепутать эти случаи невозможно.
	 */
	private static final int EXPIRY_SLACK = 5;

	/**
	 * Ниже этой длительности забег вообще не начинается.
	 *
	 * Туманное зелье выдаёт эффект на четверть срока, стрела — на восьмую.
	 * Восемь секунд горения стоят девять здоровья вместо тридцати пяти и
	 * переживаются без всякой подготовки, так что «Закалённый» доставался бы
	 * даром. Запас в 20 тиков — на случай, если эффект замечен не первым тиком.
	 */
	private static final int MIN_FULL_DURATION = SunstonePotions.DURATION_3 - 20;

	/** Ход одной попытки пережить третий порядок. */
	private static final class Attempt {
		/** Не пользовался ли игрок огнестойкостью и золотыми яблоками. */
		boolean clean = true;

		/** Сколько тиков эффекта оставалось, когда мы видели его в последний раз. */
		int lastSeenDuration;
	}

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

		if (entity instanceof ServerPlayerEntity player) {
			trackAdvancements(player);
		}

		return true;
	}

	/**
	 * Достижения ведём отсюда: этот метод и так вызывается каждый тик, пока
	 * эффект висит на игроке, — отдельный обход игроков был бы лишним.
	 */
	private static void trackAdvancements(ServerPlayerEntity player) {
		// «Сам себя»: перегрев начался вскоре после того, как игрок метнул
		// собственное взрывное зелье. Выпитое залпом сюда не попадает.
		Long deadline = RECENT_THROWERS.remove(player.getUuid());
		if (deadline != null && player.getWorld().getTime() <= deadline) {
			SunstoneCriteria.SELF_IGNITION.trigger(player);
		}

		StatusEffectInstance instance = player.getStatusEffect(SunstoneEffects.SOLAR_OVERLOAD);
		if (instance == null) {
			return;
		}

		// Засчитываем только полноразмерный эффект: укороченный туманным зельем
		// или стрелой — это другое испытание, вчетверо и ввосьмеро легче.
		if (!ATTEMPTS.containsKey(player.getUuid()) && instance.getDuration() < MIN_FULL_DURATION) {
			return;
		}

		// Остаток эффекта нужен, чтобы потом отличить «догорел сам»
		// от «сняли молоком». Сами условия забега проверяет sweep.
		ATTEMPTS.computeIfAbsent(player.getUuid(), uuid -> new Attempt())
				.lastSeenDuration = instance.getDuration();
	}

	/**
	 * Всё, что превращает третий порядок в формальность.
	 *
	 * Огнестойкость и поглощение с золотого яблока снимают урон напрямую;
	 * вода, дождь и снежный порошок просто тушат игрока; броня срезает урон
	 * от огня. Горение стоит игроку 35 здоровья при двадцати имеющихся, так что
	 * законный способ дожить остаётся ровно один — регенерация. Длительность
	 * третьего порядка подогнана так, что одной склянки второго уровня хватает
	 * впритык: остаётся одно сердце (расчёт — в SunstonePotions).
	 */
	private static boolean isCheating(ServerPlayerEntity player) {
		if (player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)
				|| player.hasStatusEffect(StatusEffects.ABSORPTION)) {
			return true;
		}

		// isWet() — это вода, дождь и пузырьковый столб разом.
		if (player.isWet() || player.inPowderSnow) {
			return true;
		}

		for (ItemStack armor : player.getArmorItems()) {
			if (!armor.isEmpty()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Запоминает бросок взрывного дистиллята третьего порядка.
	 *
	 * Иначе «поджёг себя сам» от «выпил залпом» не отличить: в эффекте,
	 * который в итоге получает игрок, способ доставки никак не записан.
	 * Вешается на UseItemCallback в SunstoneEffects.
	 */
	public static TypedActionResult<ItemStack> onItemUsed(PlayerEntity player, World world, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);

		if (!world.isClient && stack.isOf(Items.SPLASH_POTION)) {
			PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
			if (contents != null && contents.matches(SunstonePotions.SOLAR_DISTILLATE_3)) {
				RECENT_THROWERS.put(player.getUuid(), world.getTime() + THROW_WINDOW);
			}
		}

		// Поведение предмета не трогаем — только подглядываем.
		return TypedActionResult.pass(stack);
	}

	/**
	 * Досматривает попытки после того, как эффект уже снят: applyUpdateEffect
	 * в этот момент не вызывается, а хука «эффект истёк» у StatusEffect нет.
	 * Заодно выбрасывает броски, которые так ни в кого и не попали.
	 */
	static void sweep(MinecraftServer server) {
		long now = server.getOverworld().getTime();
		RECENT_THROWERS.values().removeIf(deadline -> now > deadline);

		if (ATTEMPTS.isEmpty()) {
			return;
		}

		ATTEMPTS.entrySet().removeIf(e -> {
			ServerPlayerEntity player = server.getPlayerManager().getPlayer(e.getKey());

			// Игрок вышел или погиб — попытку просто забываем.
			if (player == null || !player.isAlive()) {
				return true;
			}

			Attempt attempt = e.getValue();
			boolean stillBurningUp = player.hasStatusEffect(SunstoneEffects.SOLAR_OVERLOAD);

			// Условия проверяем только пока идёт сам эффект: догорающий
			// хвост — это уже последствия испытания, а не оно само.
			if (stillBurningUp) {
				if (isCheating(player)) {
					attempt.clean = false;
				}
				return false;
			}

			// Эффект должен был догореть сам. Молоко снимает его на полном
			// остатке — такая попытка не считается.
			if (attempt.lastSeenDuration > EXPIRY_SLACK) {
				return true;
			}

			// Последнее поджигание тянется ещё до трёх секунд после эффекта.
			// Ждём, пока игрок потухнет: сгореть на хвосте — не пережить.
			if (player.isOnFire()) {
				return false;
			}

			if (attempt.clean) {
				SunstoneCriteria.OVERLOAD_SURVIVED.trigger(player);
			}
			return true;
		});
	}
}
