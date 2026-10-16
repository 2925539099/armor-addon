package com.armoraddon.item;

import com.armoraddon.ArmorAddon;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 「盔甲附加」的物品注册。
 */
public final class ModItems {
	/** 羊毛盔甲的耐久度倍率：皮革为 5，这里取两倍。 */
	public static final int WOOL_DURABILITY_MULTIPLIER = 10;

	/**
	 * 铁制内衬盔甲的耐久度倍率：原版铁盔甲为 15，此处 18 正好是 +20%
	 * （头盔 198 / 胸甲 288 / 护腿 270 / 靴子 234）。
	 */
	public static final int IRON_LINING_DURABILITY_MULTIPLIER = 18;

	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ArmorAddon.MOD_ID);

	// ---- 锻造模板 ----
	/**
	 * 盔甲升级模板：锻造台用的模板。
	 *
	 * <p>非盔甲，因此是普通物品，不带耐久。在工作台按下面的布局合成：</p>
	 *
	 * <pre>
	 *   空   铁锭   空
	 *   火把 煤炭块 空
	 *   空   铁桶   空
	 * </pre>
	 */
	public static final DeferredItem<Item> ARMOR_UPGRADE_TEMPLATE =
			ITEMS.register("armor_upgrade_template", () -> new Item(new Item.Properties()));

	// ---- 木制盔甲 ----
	// 耐久为铁盔甲的一半。铁盔甲是 11/16/15/13 × 15 = 165/240/225/195，
	// 除以 2 得 82.5/120/112.5/97.5，截断取整后逐件写具体值
	// （15 ÷ 2 = 7.5 不是整数，无法用倍率表达）。
	public static final DeferredItem<Item> WOOD_HELMET =
			registerArmorWithDurability("wood_helmet", ModArmorMaterials.WOOD,
					ArmorItem.Type.HELMET, 82);
	public static final DeferredItem<Item> WOOD_CHESTPLATE =
			registerArmorWithDurability("wood_chestplate", ModArmorMaterials.WOOD,
					ArmorItem.Type.CHESTPLATE, 120);
	public static final DeferredItem<Item> WOOD_LEGGINGS =
			registerArmorWithDurability("wood_leggings", ModArmorMaterials.WOOD,
					ArmorItem.Type.LEGGINGS, 112);
	public static final DeferredItem<Item> WOOD_BOOTS =
			registerArmorWithDurability("wood_boots", ModArmorMaterials.WOOD,
					ArmorItem.Type.BOOTS, 97);

	// ---- 羊毛盔甲 ----
	public static final DeferredItem<Item> WOOL_HELMET =
			registerArmor("wool_helmet", ModArmorMaterials.WOOL, ArmorItem.Type.HELMET, WOOL_DURABILITY_MULTIPLIER);
	public static final DeferredItem<Item> WOOL_CHESTPLATE =
			registerArmor("wool_chestplate", ModArmorMaterials.WOOL, ArmorItem.Type.CHESTPLATE, WOOL_DURABILITY_MULTIPLIER);
	public static final DeferredItem<Item> WOOL_LEGGINGS =
			registerArmor("wool_leggings", ModArmorMaterials.WOOL, ArmorItem.Type.LEGGINGS, WOOL_DURABILITY_MULTIPLIER);
	/**
	 * 羊毛靴子使用自定义物品类：可在细雪上行走，并且移动时不会被
	 * 幽匿感测体 / 坚守者探测到（见 {@link WoolBootsItem}、{@link WoolBootsEvents}）。
	 */
	public static final DeferredItem<Item> WOOL_BOOTS =
			ITEMS.register("wool_boots", () -> new WoolBootsItem(
					ModArmorMaterials.WOOL,
					new Item.Properties().durability(
							ArmorItem.Type.BOOTS.getDurability(WOOL_DURABILITY_MULTIPLIER))));

	// ---- 铁制内衬盔甲（羊毛盔甲 + 铁盔甲的升级版本）----
	public static final DeferredItem<Item> IRON_LINING_HELMET =
			registerArmor("iron_lining_helmet", ModArmorMaterials.IRON_LINING, ArmorItem.Type.HELMET, IRON_LINING_DURABILITY_MULTIPLIER);
	public static final DeferredItem<Item> IRON_LINING_CHESTPLATE =
			registerArmor("iron_lining_chestplate", ModArmorMaterials.IRON_LINING, ArmorItem.Type.CHESTPLATE, IRON_LINING_DURABILITY_MULTIPLIER);
	public static final DeferredItem<Item> IRON_LINING_LEGGINGS =
			registerArmor("iron_lining_leggings", ModArmorMaterials.IRON_LINING, ArmorItem.Type.LEGGINGS, IRON_LINING_DURABILITY_MULTIPLIER);
	public static final DeferredItem<Item> IRON_LINING_BOOTS =
			registerArmor("iron_lining_boots", ModArmorMaterials.IRON_LINING, ArmorItem.Type.BOOTS, IRON_LINING_DURABILITY_MULTIPLIER);

	// ---- 金制内衬甲（羊毛盔甲 + 金盔甲的升级版本）----
	// 金盔甲的耐久倍率是 7，×1.2 = 8.4 不是整数，所以这里逐件直接写
	// 「金盔甲耐久 +20%」的具体值：
	//   头盔 11×7=77  → 92
	//   胸甲 16×7=112 → 134
	//   护腿 15×7=105 → 126
	//   靴子 13×7=91  → 109
	public static final DeferredItem<Item> GOLD_LINING_HELMET =
			registerPiglinNeutralArmor("gold_lining_helmet", ModArmorMaterials.GOLD_LINING,
					ArmorItem.Type.HELMET, 92);
	public static final DeferredItem<Item> GOLD_LINING_CHESTPLATE =
			registerPiglinNeutralArmor("gold_lining_chestplate", ModArmorMaterials.GOLD_LINING,
					ArmorItem.Type.CHESTPLATE, 134);
	public static final DeferredItem<Item> GOLD_LINING_LEGGINGS =
			registerPiglinNeutralArmor("gold_lining_leggings", ModArmorMaterials.GOLD_LINING,
					ArmorItem.Type.LEGGINGS, 126);
	public static final DeferredItem<Item> GOLD_LINING_BOOTS =
			registerPiglinNeutralArmor("gold_lining_boots", ModArmorMaterials.GOLD_LINING,
					ArmorItem.Type.BOOTS, 109);

	// ---- 钻石内衬甲（羊毛盔甲 + 钻石盔甲的升级版本）----
	// 钻石盔甲的耐久倍率是 33，×1.2 = 39.6 不是整数，同样逐件写具体值：
	//   头盔 11×33=363 → 435
	//   胸甲 16×33=528 → 633
	//   护腿 15×33=495 → 594
	//   靴子 13×33=429 → 514
	public static final DeferredItem<Item> DIAMOND_LINING_HELMET =
			registerArmorWithDurability("diamond_lining_helmet", ModArmorMaterials.DIAMOND_LINING,
					ArmorItem.Type.HELMET, 435);
	public static final DeferredItem<Item> DIAMOND_LINING_CHESTPLATE =
			registerArmorWithDurability("diamond_lining_chestplate", ModArmorMaterials.DIAMOND_LINING,
					ArmorItem.Type.CHESTPLATE, 633);
	public static final DeferredItem<Item> DIAMOND_LINING_LEGGINGS =
			registerArmorWithDurability("diamond_lining_leggings", ModArmorMaterials.DIAMOND_LINING,
					ArmorItem.Type.LEGGINGS, 594);
	public static final DeferredItem<Item> DIAMOND_LINING_BOOTS =
			registerArmorWithDurability("diamond_lining_boots", ModArmorMaterials.DIAMOND_LINING,
					ArmorItem.Type.BOOTS, 514);

	// ---- 合金内衬甲（羊毛盔甲 + 下界合金盔甲的升级版本）----
	// 下界合金盔甲的耐久倍率是 37，×1.2 = 44.4 不是整数，同样逐件写具体值：
	//   头盔 11×37=407 → 488
	//   胸甲 16×37=592 → 710
	//   护腿 15×37=555 → 666
	//   靴子 13×37=481 → 577
	// 另外与原版下界合金盔甲一致带抗火（物品不会被火烧毁）。
	public static final DeferredItem<Item> ALLOY_LINING_HELMET =
			registerPiglinNeutralFireResistantArmor("alloy_lining_helmet", ModArmorMaterials.ALLOY_LINING,
					ArmorItem.Type.HELMET, 488);
	public static final DeferredItem<Item> ALLOY_LINING_CHESTPLATE =
			registerPiglinNeutralFireResistantArmor("alloy_lining_chestplate", ModArmorMaterials.ALLOY_LINING,
					ArmorItem.Type.CHESTPLATE, 710);
	public static final DeferredItem<Item> ALLOY_LINING_LEGGINGS =
			registerPiglinNeutralFireResistantArmor("alloy_lining_leggings", ModArmorMaterials.ALLOY_LINING,
					ArmorItem.Type.LEGGINGS, 666);
	public static final DeferredItem<Item> ALLOY_LINING_BOOTS =
			registerPiglinNeutralFireResistantArmor("alloy_lining_boots", ModArmorMaterials.ALLOY_LINING,
					ArmorItem.Type.BOOTS, 577);

	// ============================================================
	//  重型甲系列 —— 属性以对应内衬甲为基础，护甲值 +2/+4/+3/+2，
	//  韧性不变，附魔亲和度不变，耐久 ×1.5。
	//  耐久逐件写具体值（×1.5 常出现 .5，按惯例截断取整）。
	// ============================================================

	// ---- 铁制重型甲（铁制内衬甲 198/288/270/234 ×1.5）----
	// 297 / 432 / 405 / 351 均为整数。
	public static final DeferredItem<Item> IRON_HEAVY_HELMET =
			registerArmorWithDurability("iron_heavy_helmet", ModArmorMaterials.IRON_HEAVY,
					ArmorItem.Type.HELMET, 297);
	public static final DeferredItem<Item> IRON_HEAVY_CHESTPLATE =
			registerArmorWithDurability("iron_heavy_chestplate", ModArmorMaterials.IRON_HEAVY,
					ArmorItem.Type.CHESTPLATE, 432);
	public static final DeferredItem<Item> IRON_HEAVY_LEGGINGS =
			registerArmorWithDurability("iron_heavy_leggings", ModArmorMaterials.IRON_HEAVY,
					ArmorItem.Type.LEGGINGS, 405);
	public static final DeferredItem<Item> IRON_HEAVY_BOOTS =
			registerArmorWithDurability("iron_heavy_boots", ModArmorMaterials.IRON_HEAVY,
					ArmorItem.Type.BOOTS, 351);

	// ---- 金制重型甲（金制内衬甲 92/134/126/109 ×1.5）----
	// 138 / 201 / 189 / 163.5→163
	public static final DeferredItem<Item> GOLD_HEAVY_HELMET =
			registerPiglinNeutralArmor("gold_heavy_helmet", ModArmorMaterials.GOLD_HEAVY,
					ArmorItem.Type.HELMET, 138);
	public static final DeferredItem<Item> GOLD_HEAVY_CHESTPLATE =
			registerPiglinNeutralArmor("gold_heavy_chestplate", ModArmorMaterials.GOLD_HEAVY,
					ArmorItem.Type.CHESTPLATE, 201);
	public static final DeferredItem<Item> GOLD_HEAVY_LEGGINGS =
			registerPiglinNeutralArmor("gold_heavy_leggings", ModArmorMaterials.GOLD_HEAVY,
					ArmorItem.Type.LEGGINGS, 189);
	public static final DeferredItem<Item> GOLD_HEAVY_BOOTS =
			registerPiglinNeutralArmor("gold_heavy_boots", ModArmorMaterials.GOLD_HEAVY,
					ArmorItem.Type.BOOTS, 163);

	// ---- 钻石重型甲（钻石内衬甲 435/633/594/514 ×1.5）----
	// 652.5→652 / 949.5→949 / 891 / 771
	public static final DeferredItem<Item> DIAMOND_HEAVY_HELMET =
			registerArmorWithDurability("diamond_heavy_helmet", ModArmorMaterials.DIAMOND_HEAVY,
					ArmorItem.Type.HELMET, 652);
	public static final DeferredItem<Item> DIAMOND_HEAVY_CHESTPLATE =
			registerArmorWithDurability("diamond_heavy_chestplate", ModArmorMaterials.DIAMOND_HEAVY,
					ArmorItem.Type.CHESTPLATE, 949);
	public static final DeferredItem<Item> DIAMOND_HEAVY_LEGGINGS =
			registerArmorWithDurability("diamond_heavy_leggings", ModArmorMaterials.DIAMOND_HEAVY,
					ArmorItem.Type.LEGGINGS, 891);
	public static final DeferredItem<Item> DIAMOND_HEAVY_BOOTS =
			registerArmorWithDurability("diamond_heavy_boots", ModArmorMaterials.DIAMOND_HEAVY,
					ArmorItem.Type.BOOTS, 771);

	// ---- 合金重型甲（合金内衬甲 488/710/666/577 ×1.5）----
	// 732 / 1065 / 999 / 865.5→865，同样带抗火。
	public static final DeferredItem<Item> ALLOY_HEAVY_HELMET =
			registerPiglinNeutralFireResistantArmor("alloy_heavy_helmet", ModArmorMaterials.ALLOY_HEAVY,
					ArmorItem.Type.HELMET, 732);
	public static final DeferredItem<Item> ALLOY_HEAVY_CHESTPLATE =
			registerPiglinNeutralFireResistantArmor("alloy_heavy_chestplate", ModArmorMaterials.ALLOY_HEAVY,
					ArmorItem.Type.CHESTPLATE, 1065);
	public static final DeferredItem<Item> ALLOY_HEAVY_LEGGINGS =
			registerPiglinNeutralFireResistantArmor("alloy_heavy_leggings", ModArmorMaterials.ALLOY_HEAVY,
					ArmorItem.Type.LEGGINGS, 999);
	public static final DeferredItem<Item> ALLOY_HEAVY_BOOTS =
			registerPiglinNeutralFireResistantArmor("alloy_heavy_boots", ModArmorMaterials.ALLOY_HEAVY,
					ArmorItem.Type.BOOTS, 865);

	private ModItems() {
	}

	public static void register(IEventBus modEventBus) {
		ITEMS.register(modEventBus);
	}

	private static DeferredItem<Item> registerArmor(String name, Holder<ArmorMaterial> material,
													ArmorItem.Type type, int durabilityMultiplier) {
		return ITEMS.register(name, () -> new ArmorItem(
				material,
				type,
				new Item.Properties().durability(type.getDurability(durabilityMultiplier))));
	}

	/** 直接指定耐久数值（用于原版倍率 ×1.2 不是整数的情况）。 */
	private static DeferredItem<Item> registerArmorWithDurability(String name, Holder<ArmorMaterial> material,
																  ArmorItem.Type type, int durability) {
		return ITEMS.register(name, () -> new ArmorItem(
				material,
				type,
				new Item.Properties().durability(durability)));
	}

	/** 同上，但额外带抗火 —— 与原版下界合金盔甲一致。 */
	private static DeferredItem<Item> registerFireResistantArmor(String name, Holder<ArmorMaterial> material,
																 ArmorItem.Type type, int durability) {
		return ITEMS.register(name, () -> new ArmorItem(
				material,
				type,
				new Item.Properties().fireResistant().durability(durability)));
	}

	/**
	 * 猪灵中立版盔甲（金制与合金制使用）。
	 *
	 * <p>NeoForge 的 {@code makesPiglinsNeutral} 默认实现只认原版的
	 * {@code ArmorMaterials.GOLD}，本模组的材质不在其中，所以要换成
	 * {@link PiglinNeutralArmorItem} 覆盖该方法，穿上后猪灵才会保持中立。</p>
	 */
	private static DeferredItem<Item> registerPiglinNeutralArmor(String name, Holder<ArmorMaterial> material,
																 ArmorItem.Type type, int durability) {
		return ITEMS.register(name, () -> new PiglinNeutralArmorItem(
				material,
				type,
				new Item.Properties().durability(durability)));
	}

	/** 同上，但额外带抗火（合金制用）。 */
	private static DeferredItem<Item> registerPiglinNeutralFireResistantArmor(String name, Holder<ArmorMaterial> material,
																			   ArmorItem.Type type, int durability) {
		return ITEMS.register(name, () -> new PiglinNeutralArmorItem(
				material,
				type,
				new Item.Properties().fireResistant().durability(durability)));
	}
}
