package com.leon.sunstone;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

/**
 * Клиентская часть мода. Прописана в fabric.mod.json -> entrypoints.client,
 * поэтому на выделенном сервере этот класс даже не загружается.
 *
 * Нужна из-за того, как игра выбирает слой отрисовки: ваниль держит блоки
 * с прозрачностью в статической таблице RenderLayers, и модовый блок туда,
 * разумеется, не попадает. Без явного указания он рисуется как сплошной,
 * и прозрачные пиксели текстуры превращаются в чёрные — вокруг растения
 * появляется чёрный куб.
 */
public class SunstoneClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		// cutout, а не cutout_mipped: ровно так ваниль рисует свои грядки и цветы.
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
				SunstoneBlocks.SUNSTONE_CROP,
				SunstoneBlocks.SUNSTONE_FLOWER,
				SunstoneBlocks.POTTED_SUNSTONE_FLOWER);

		Sunstone.LOGGER.info("Клиентская часть Sunstone загружена");
	}
}
