package com.armoraddon.client;

import com.armoraddon.ArmorAddon;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * 超重型甲的模型层。
 *
 * <p>四套各注册内外两层，一共八个。之所以不能共用，是因为作者的四个 bbmodel
 * 是分别画的 —— 肩甲与膝盖护板的 UV 位置每套都不一样（例如右肩甲合金在
 * {@code (48, 0)}、其余三套在 {@code (48, 6)}；左腿护板金制与钻石是
 * {@code (43, 22)}、铁制 {@code (43, 17)}、合金 {@code (43, 16)}）。
 * 共用一份 UV 会让三套的贴图取到错误区域。</p>
 *
 * <p>各层的具体 UV 在 {@link ModClientSetup} 注册时给出。</p>
 */
public final class ModModelLayers {
	public static final ModelLayerLocation GOLD_OUTER = layer("gold", "outer");
	public static final ModelLayerLocation GOLD_INNER = layer("gold", "inner");
	public static final ModelLayerLocation IRON_OUTER = layer("iron", "outer");
	public static final ModelLayerLocation IRON_INNER = layer("iron", "inner");
	public static final ModelLayerLocation DIAMOND_OUTER = layer("diamond", "outer");
	public static final ModelLayerLocation DIAMOND_INNER = layer("diamond", "inner");
	public static final ModelLayerLocation ALLOY_OUTER = layer("alloy", "outer");
	public static final ModelLayerLocation ALLOY_INNER = layer("alloy", "inner");

	private ModModelLayers() {
	}

	private static ModelLayerLocation layer(String set, String part) {
		return new ModelLayerLocation(
				ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID,
						"ultra_heavy_" + set + "_" + part),
				"main");
	}
}
