package com.leon.sunstone;

import net.minecraft.block.Blocks;
import net.minecraft.block.LightBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * «Солнечный свет»: носитель эффекта реально освещает вокруг себя, как факел.
 *
 * В Minecraft нет штатного способа заставить сущность светиться — свет считается
 * только от блоков. Поэтому мы ставим за игроком невидимый блок minecraft:light
 * и таскаем его следом. Отсюда два обязательства: ставить его только в воздух,
 * чтобы ничего не затереть, и обязательно убирать за собой (см. SunstoneEffects).
 */
public class SunlitStatusEffect extends StatusEffect {

	/** Ровно как у факела. */
	public static final int LIGHT_LEVEL = 14;

	/** Где сейчас стоит наш блок света. Ключ — UUID сущности. */
	static final Map<UUID, LightRef> ACTIVE = new ConcurrentHashMap<>();

	record LightRef(RegistryKey<World> world, BlockPos pos) {}

	protected SunlitStatusEffect(StatusEffectCategory category, int color) {
		super(category, color);
	}

	/** Нужен каждый тик, иначе свет отстаёт от игрока на бегу. */
	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return true;
	}

	@Override
	public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
		if (entity.getWorld() instanceof ServerWorld world) {
			moveLight(world, entity);
		}
		return true;
	}

	/** Смерть или выгрузка сущности — свет надо снять сразу, не дожидаясь сборщика. */
	@Override
	public void onEntityRemoval(LivingEntity entity, int amplifier, Entity.RemovalReason reason) {
		LightRef ref = ACTIVE.remove(entity.getUuid());
		if (ref != null && entity.getWorld() instanceof ServerWorld world) {
			clear(world, ref.pos());
		}
		super.onEntityRemoval(entity, amplifier, reason);
	}

	static void moveLight(ServerWorld world, LivingEntity entity) {
		BlockPos target = entity.getBlockPos().up();
		LightRef ref = ACTIVE.get(entity.getUuid());

		if (ref != null && ref.world().equals(world.getRegistryKey()) && ref.pos().equals(target)) {
			return;
		}

		if (ref != null) {
			ServerWorld old = world.getServer().getWorld(ref.world());
			if (old != null) clear(old, ref.pos());
		}

		if (world.getBlockState(target).isAir()) {
			world.setBlockState(target, Blocks.LIGHT.getDefaultState()
					.with(LightBlock.LEVEL_15, LIGHT_LEVEL)
					.with(LightBlock.WATERLOGGED, false));
			ACTIVE.put(entity.getUuid(), new LightRef(world.getRegistryKey(), target));
		} else {
			// В стену или воду не ставим — иначе затрём чужой блок.
			ACTIVE.remove(entity.getUuid());
		}
	}

	/** Снимаем только свой блок света: чужие и любые другие блоки не трогаем. */
	static void clear(ServerWorld world, BlockPos pos) {
		if (pos != null && world.getBlockState(pos).isOf(Blocks.LIGHT)) {
			world.removeBlock(pos, false);
		}
	}

	/**
	 * Подчищает свет за теми, у кого эффект уже кончился.
	 * Без этого блок остался бы висеть навсегда: у StatusEffect нет хука
	 * «эффект истёк», который давал бы доступ к самой сущности.
	 */
	static void sweep(MinecraftServer server) {
		if (ACTIVE.isEmpty()) return;

		ACTIVE.entrySet().removeIf(e -> {
			Entity found = null;
			for (ServerWorld w : server.getWorlds()) {
				found = w.getEntity(e.getKey());
				if (found != null) break;
			}

			boolean stillLit = found instanceof LivingEntity le
					&& le.hasStatusEffect(SunstoneEffects.SUNLIT);
			if (stillLit) return false;

			ServerWorld w = server.getWorld(e.getValue().world());
			if (w != null) clear(w, e.getValue().pos());
			return true;
		});
	}
}
