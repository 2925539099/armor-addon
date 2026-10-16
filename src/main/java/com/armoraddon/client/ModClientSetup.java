package com.armoraddon.client;

import com.armoraddon.ArmorAddon;
import com.armoraddon.item.ModItems;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 超重型甲的客户端注册：八个模型层，以及把每个物品指向它自己那套模型。
 *
 * <p>这些类只在客户端存在（{@code net.minecraft.client.*}），所以整个类用
 * {@link Dist#CLIENT} 限定，服务端不会加载。</p>
 *
 * <p>作者的四套 bbmodel 是分别画的，肩甲和膝盖护板的 UV 位置每套都不同。
 * 下面那组数字就是从四个 bbmodel 里逐个读出来的 texOffs；改动前请先用
 * {@code tools/extract_uv_table.py} 复核一遍。</p>
 *
 * <p>{@code getHumanoidArmorModel} 在<strong>每次渲染</strong>时都会被调用，
 * 所以模型必须在 {@link EntityRenderersEvent.AddLayers} 里烘焙一次并缓存，
 * 不能在里面 new。</p>
 */
@EventBusSubscriber(modid = ArmorAddon.MOD_ID, value = Dist.CLIENT)
public final class ModClientSetup {
	/**
	 * 一套甲的模型配置。
	 *
	 * @param leftPadU/V  左肩甲 texOffs
	 * @param rightPadU/V 右肩甲 texOffs
	 * @param rightKneeU/V 右腿护板 texOffs
	 * @param leftKneeU/V  左腿护板 texOffs
	 */
	private record SetModels(ModelLayerLocation outer, ModelLayerLocation inner,
							 int leftPadU, int leftPadV, int rightPadU, int rightPadV,
							 int rightKneeU, int rightKneeV, int leftKneeU, int leftKneeV) {
	}

	/**
	 * 四套的 UV 配置，数据来自作者 bbmodel 的 north 面 UV 反推。
	 *
	 * <p>肩甲：左肩四套一律 {@code (33, 0)}；右肩合金是 {@code (48, 0)}、
	 * 金制 / 铁制 / 钻石是 {@code (48, 6)}。</p>
	 * <p>膝盖护板：右腿四套一律 {@code (43, 22)}；左腿金制 / 钻石 / 合金都是
	 * {@code (43, 22)}，只有铁制是 {@code (43, 17)}。</p>
	 */
	private static final List<SetModels> SETS = List.of(
			new SetModels(ModModelLayers.GOLD_OUTER, ModModelLayers.GOLD_INNER,
					33, 0, 48, 6, 43, 22, 43, 22),
			new SetModels(ModModelLayers.IRON_OUTER, ModModelLayers.IRON_INNER,
					33, 0, 48, 6, 43, 22, 43, 17),
			new SetModels(ModModelLayers.DIAMOND_OUTER, ModModelLayers.DIAMOND_INNER,
					33, 0, 48, 6, 43, 22, 43, 22),
			new SetModels(ModModelLayers.ALLOY_OUTER, ModModelLayers.ALLOY_INNER,
					33, 0, 48, 0, 43, 22, 43, 22));

	/** 烘焙好的模型实例，键是模型层。 */
	private static final Map<ModelLayerLocation, HumanoidModel<?>> BAKED = new HashMap<>();

	private ModClientSetup() {
	}

	/** 注册八个模型层。 */
	@SubscribeEvent
	public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		for (SetModels s : SETS) {
			event.registerLayerDefinition(s.outer(),
					() -> UltraHeavyArmorModel.createOuterLayer(
							s.leftPadU(), s.leftPadV(), s.rightPadU(), s.rightPadV()));
			event.registerLayerDefinition(s.inner(),
					() -> UltraHeavyArmorModel.createInnerLayer(
							s.rightKneeU(), s.rightKneeV(), s.leftKneeU(), s.leftKneeV()));
		}
	}

	/** 烘焙并缓存模型实例。 */
	@SubscribeEvent
	public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
		EntityModelSet models = event.getEntityModels();
		for (SetModels s : SETS) {
			BAKED.put(s.outer(), new UltraHeavyArmorModel(models.bakeLayer(s.outer())));
			BAKED.put(s.inner(), new UltraHeavyArmorModel(models.bakeLayer(s.inner())));
		}
	}

	/** 把 16 件超重型甲分别指向自己那套的模型。 */
	@SubscribeEvent
	public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
		bind(new Item[]{
				ModItems.GOLD_ULTRA_HEAVY_HELMET.get(), ModItems.GOLD_ULTRA_HEAVY_CHESTPLATE.get(),
				ModItems.GOLD_ULTRA_HEAVY_LEGGINGS.get(), ModItems.GOLD_ULTRA_HEAVY_BOOTS.get()
		}, SETS.get(0), event);

		bind(new Item[]{
				ModItems.IRON_ULTRA_HEAVY_HELMET.get(), ModItems.IRON_ULTRA_HEAVY_CHESTPLATE.get(),
				ModItems.IRON_ULTRA_HEAVY_LEGGINGS.get(), ModItems.IRON_ULTRA_HEAVY_BOOTS.get()
		}, SETS.get(1), event);

		bind(new Item[]{
				ModItems.DIAMOND_ULTRA_HEAVY_HELMET.get(), ModItems.DIAMOND_ULTRA_HEAVY_CHESTPLATE.get(),
				ModItems.DIAMOND_ULTRA_HEAVY_LEGGINGS.get(), ModItems.DIAMOND_ULTRA_HEAVY_BOOTS.get()
		}, SETS.get(2), event);

		bind(new Item[]{
				ModItems.ALLOY_ULTRA_HEAVY_HELMET.get(), ModItems.ALLOY_ULTRA_HEAVY_CHESTPLATE.get(),
				ModItems.ALLOY_ULTRA_HEAVY_LEGGINGS.get(), ModItems.ALLOY_ULTRA_HEAVY_BOOTS.get()
		}, SETS.get(3), event);
	}

	/** 把一套的四件甲绑到对应的模型上。 */
	private static void bind(Item[] items, SetModels set, RegisterClientExtensionsEvent event) {
		IClientItemExtensions ext = new IClientItemExtensions() {
			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
														  EquipmentSlot slot, HumanoidModel<?> original) {
				// 护腿走内层（layer_2），其余部位走外层（layer_1）。
				// Minecraft 的盔甲分层就是这个规则，作者的 Blockbench 工程也照此做了两个模型。
				HumanoidModel<?> model = BAKED.get(
						slot == EquipmentSlot.LEGS ? set.inner() : set.outer());

				// 理论上 AddLayers 一定先于渲染发生；万一顺序有变就退回原版模型，
				// 宁可显示成原版形状也不要崩。
				return model != null ? model : original;
			}
		};

		event.registerItem(ext, items);
	}
}
