package com.leon.sunstone;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

/**
 * Своя вкладка в творческом инвентаре.
 * Название вкладки берётся из lang-файла по ключу "itemGroup.sunstone".
 */
public class SunstoneItemGroups {

	public static final RegistryKey<ItemGroup> SUNSTONE_GROUP =
			RegistryKey.of(RegistryKeys.ITEM_GROUP, Sunstone.id("sunstone"));

	public static void registerItemGroups() {
		Registry.register(Registries.ITEM_GROUP, SUNSTONE_GROUP, FabricItemGroup.builder()
				.icon(() -> new ItemStack(SunstoneItems.SUNSTONE))
				.displayName(Text.translatable("itemGroup.sunstone"))
				.build());

		// Наполняем вкладку. Порядок здесь = порядок в инвентаре.
		ItemGroupEvents.modifyEntriesEvent(SUNSTONE_GROUP).register(entries -> {
			entries.add(SunstoneBlocks.SUNSTONE_ORE);
			entries.add(SunstoneBlocks.DEEPSLATE_SUNSTONE_ORE);
			entries.add(SunstoneItems.RAW_SUNSTONE);
			entries.add(SunstoneItems.SUNSTONE);
			entries.add(SunstoneBlocks.SUNSTONE_BLOCK);
			entries.add(SunstoneItems.SUNSTONE_APPLE);
			entries.add(SunstoneItems.SUNSTONE_SEED);
			entries.add(SunstoneItems.SUNSTONE_FLOWER);

			// Зелья кладём во вкладку сами: ваниль наполняет только свою «Зелья»,
			// а сваренные варианты руками в креативе иначе не достать.
			for (RegistryEntry<Potion> potion : SunstonePotions.ALL) {
				entries.add(PotionContentsComponent.createStack(Items.POTION, potion));
				entries.add(PotionContentsComponent.createStack(Items.SPLASH_POTION, potion));
				entries.add(PotionContentsComponent.createStack(Items.LINGERING_POTION, potion));
				entries.add(PotionContentsComponent.createStack(Items.TIPPED_ARROW, potion));
			}
		});
	}
}
