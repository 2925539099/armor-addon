package com.armoraddon.item;

import com.armoraddon.ArmorAddon;

import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 「盔甲附加」的自定义盔甲材料。
 */
public final class ModArmorMaterials {
	public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
			DeferredRegister.create(Registries.ARMOR_MATERIAL, ArmorAddon.MOD_ID);

	/**
	 * 羊毛盔甲材料。
	 *
	 * <p>防御点与附魔亲和度同原版皮革盔甲，修复材料为羊毛（任意颜色），
	 * 但每件都附带 2.0 的护甲韧性。</p>
	 *
	 * <p>耐久度倍率不在这里设置，而是在注册物品时传给
	 * {@code ArmorItem.Type#getDurability}。</p>
	 */
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> WOOL = ARMOR_MATERIALS.register(
			"wool",
			() -> new ArmorMaterial(
					// 与皮革相同的防御点。
					Map.of(
							ArmorItem.Type.HELMET, 1,
							ArmorItem.Type.CHESTPLATE, 3,
							ArmorItem.Type.LEGGINGS, 2,
							ArmorItem.Type.BOOTS, 1),
					// 与皮革相同的附魔亲和度。
					15,
					// 布甲音效。
					SoundEvents.ARMOR_EQUIP_LEATHER,
					// 修复材料：任意颜色的羊毛（minecraft:wool 标签）。
					() -> Ingredient.of(ItemTags.WOOL),
					List.of(new ArmorMaterial.Layer(
							ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "wool"), "", false)),
					// 韧性 2.0，全套每一件都生效。
					2.0F,
					// 击退抗性同皮革。
					0.0F));

	/**
	 * 木制盔甲材料。
	 *
	 * <ul>
	 *   <li>护甲值与原版皮革完全相同（头盔 1 / 胸甲 3 / 护腿 2 / 靴子 1）；</li>
	 *   <li>附魔亲和度 5，刻意低于全部原版材质（皮革是 15），因此极难附魔；</li>
	 *   <li>无韧性与击退抗性，与皮革一致；</li>
	 *   <li>修复材料为任意原木（{@code minecraft:logs} 标签），与合成材料一致。</li>
	 * </ul>
	 *
	 * <p>耐久为铁盔甲的一半（头盔 82 / 胸甲 120 / 护腿 112 / 靴子 97），
	 * 因 15 ÷ 2 = 7.5 不是整数，故在注册物品时逐件写具体数值，而不是用倍率。</p>
	 *
	 * <p>原版没有木制盔甲音效，这里用通用的 {@code ARMOR_EQUIP_GENERIC}。</p>
	 */
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> WOOD = ARMOR_MATERIALS.register(
			"wood",
			() -> new ArmorMaterial(
					// 与皮革相同的防御点。
					Map.of(
							ArmorItem.Type.HELMET, 1,
							ArmorItem.Type.CHESTPLATE, 3,
							ArmorItem.Type.LEGGINGS, 2,
							ArmorItem.Type.BOOTS, 1),
					// 附魔亲和度 5，远低于原版最低的铁（9）。
					5,
					// 无木制音效，用通用音效。
					SoundEvents.ARMOR_EQUIP_GENERIC,
					// 修复材料：任意原木（minecraft:logs 标签）。
					() -> Ingredient.of(ItemTags.LOGS),
					List.of(new ArmorMaterial.Layer(
							ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "wood"), "", false)),
					// 无韧性，同皮革。
					0.0F,
					// 无击退抗性，同皮革。
					0.0F));

	/**
	 * 铁制内衬盔甲材料：羊毛盔甲与原版铁盔甲的升级版本。
	 *
	 * <ul>
	 *   <li>护甲值 = 原版铁盔甲 + 1（头盔 3 / 胸甲 7 / 护腿 6 / 靴子 3）；</li>
	 *   <li>韧性继承羊毛盔甲，为 2.0；</li>
	 *   <li>耐久倍率 18，恰好是铁盔甲（15）的 +20%。</li>
	 * </ul>
	 *
	 * <p>其余属性沿用铁盔甲：附魔亲和度 9、铁砧修复材料为铁锭、铁甲穿戴音效。</p>
	 */
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> IRON_LINING = ARMOR_MATERIALS.register(
			"iron_lining",
			() -> new ArmorMaterial(
					// 原版铁盔甲（2 / 6 / 5 / 2）各 +1。
					Map.of(
							ArmorItem.Type.HELMET, 3,
							ArmorItem.Type.CHESTPLATE, 7,
							ArmorItem.Type.LEGGINGS, 6,
							ArmorItem.Type.BOOTS, 3),
					// 附魔亲和度沿用铁盔甲。
					9,
					// 穿戴音效沿用铁盔甲。
					SoundEvents.ARMOR_EQUIP_IRON,
					// 铁砧修复材料：铁锭。
					() -> Ingredient.of(Items.IRON_INGOT),
					List.of(new ArmorMaterial.Layer(
							ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "iron_lining"), "", false)),
					// 韧性继承羊毛盔甲。
					2.0F,
					// 击退抗性沿用铁盔甲。
					0.0F));

	/**
	 * 金制内衬甲材料：羊毛盔甲与原版金盔甲的升级版本。
	 *
	 * <ul>
	 *   <li>护甲值 = 原版金盔甲 + 1（头盔 3 / 胸甲 6 / 护腿 4 / 靴子 2）；</li>
	 *   <li>韧性继承羊毛盔甲，为 2.0；</li>
	 *   <li>附魔亲和度、音效、修复材料与击退抗性均沿用金盔甲。</li>
	 * </ul>
	 *
	 * <p>耐久不在这里设置：金盔甲的耐久倍率是 7，×1.2 不是整数，
	 * 因此改为在注册物品时逐件写入「金盔甲耐久 +20%」的具体数值。</p>
	 */
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GOLD_LINING = ARMOR_MATERIALS.register(
			"gold_lining",
			() -> new ArmorMaterial(
					// 原版金盔甲（2 / 5 / 3 / 1）各 +1。
					Map.of(
							ArmorItem.Type.HELMET, 3,
							ArmorItem.Type.CHESTPLATE, 6,
							ArmorItem.Type.LEGGINGS, 4,
							ArmorItem.Type.BOOTS, 2),
					// 附魔亲和度沿用金盔甲。
					25,
					// 穿戴音效沿用金盔甲。
					SoundEvents.ARMOR_EQUIP_GOLD,
					// 铁砧修复材料：金锭。
					() -> Ingredient.of(Items.GOLD_INGOT),
					List.of(new ArmorMaterial.Layer(
							ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "gold_lining"), "", false)),
					// 韧性继承羊毛盔甲。
					2.0F,
					// 击退抗性沿用金盔甲。
					0.0F));

	/**
	 * 钻石内衬甲材料：羊毛盔甲与原版钻石盔甲的升级版本。
	 *
	 * <p><strong>与前两套的唯一区别是韧性</strong>：原版钻石盔甲本身就带 2.0 韧性，
	 * 这里按「钻石甲的韧性 + 羊毛盔甲的韧性」叠加，得到 <strong>4.0</strong>。</p>
	 *
	 * <ul>
	 *   <li>护甲值 = 原版钻石盔甲 + 1（头盔 4 / 胸甲 9 / 护腿 7 / 靴子 4）；</li>
	 *   <li>韧性 = 2.0（钻石甲）+ 2.0（羊毛盔甲）= 4.0；</li>
	 *   <li>附魔亲和度、音效、修复材料与击退抗性均沿用钻石盔甲。</li>
	 * </ul>
	 *
	 * <p>耐久同样不按倍率换算：钻石甲倍率 33，×1.2 = 39.6 不是整数，
	 * 因此与金制内衬甲一样在注册物品时逐件写入具体数值。</p>
	 */
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> DIAMOND_LINING = ARMOR_MATERIALS.register(
			"diamond_lining",
			() -> new ArmorMaterial(
					// 原版钻石盔甲（3 / 8 / 6 / 3）各 +1。
					Map.of(
							ArmorItem.Type.HELMET, 4,
							ArmorItem.Type.CHESTPLATE, 9,
							ArmorItem.Type.LEGGINGS, 7,
							ArmorItem.Type.BOOTS, 4),
					// 附魔亲和度沿用钻石盔甲。
					10,
					// 穿戴音效沿用钻石盔甲。
					SoundEvents.ARMOR_EQUIP_DIAMOND,
					// 铁砧修复材料：钻石。
					() -> Ingredient.of(Items.DIAMOND),
					List.of(new ArmorMaterial.Layer(
							ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "diamond_lining"), "", false)),
					// 韧性 = 钻石甲 2.0 + 羊毛盔甲 2.0（本套与前两套的唯一区别）。
					4.0F,
					// 击退抗性沿用钻石盔甲。
					0.0F));

	/**
	 * 合金内衬甲材料：羊毛盔甲与原版下界合金盔甲的升级版本。
	 *
	 * <p>沿用钻石内衬甲的逻辑（韧性 = 基甲韧性 + 羊毛韧性），因此：</p>
	 *
	 * <ul>
	 *   <li>护甲值 = 原版下界合金盔甲 + 1（头盔 4 / 胸甲 9 / 护腿 7 / 靴子 4）；</li>
	 *   <li>韧性 = 3.0（下界合金）+ 2.0（羊毛盔甲）= <strong>5.0</strong>；</li>
	 *   <li>附魔亲和度、音效、修复材料沿用下界合金盔甲；</li>
	 *   <li>击退抗性沿用下界合金盔甲的 0.1（目前唯一带击退抗性的一套）。</li>
	 * </ul>
	 *
	 * <p>耐久同样逐件写入具体值：下界合金倍率 37，×1.2 = 44.4 不是整数。</p>
	 */
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ALLOY_LINING = ARMOR_MATERIALS.register(
			"alloy_lining",
			() -> new ArmorMaterial(
					// 原版下界合金盔甲（3 / 8 / 6 / 3）各 +1。
					Map.of(
							ArmorItem.Type.HELMET, 4,
							ArmorItem.Type.CHESTPLATE, 9,
							ArmorItem.Type.LEGGINGS, 7,
							ArmorItem.Type.BOOTS, 4),
					// 附魔亲和度沿用下界合金盔甲。
					15,
					// 穿戴音效沿用下界合金盔甲。
					SoundEvents.ARMOR_EQUIP_NETHERITE,
					// 铁砧修复材料：下界合金锭。
					() -> Ingredient.of(Items.NETHERITE_INGOT),
					List.of(new ArmorMaterial.Layer(
							ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "alloy_lining"), "", false)),
					// 韧性 = 下界合金 3.0 + 羊毛盔甲 2.0。
					5.0F,
					// 击退抗性沿用下界合金盔甲。
					0.1F));

	// ============================================================
	//  重型甲系列
	//
	//  属性 = 对应的内衬甲为基础，护甲值按部位额外 +2 / +4 / +3 / +2，
	//  韧性不变，附魔亲和度不变，击退抗性与材质音效、修复材料均沿用内衬甲。
	//  耐久 ×1.5 不在这里设置，而是注册物品时逐件写具体数值。
	// ============================================================

	/**
	 * 铁制重型甲材料。
	 *
	 * <ul>
	 *   <li>护甲值：铁制内衬甲 (3/7/6/3) 各部位 +1/+2/+1/+1 → 4/9/7/4；</li>
	 *   <li>韧性 2.0、附魔亲和度 9，均与铁制内衬甲相同；</li>
	 *   <li>击退抗性 0.0，修复材料铁锭，铁甲音效。</li>
	 * </ul>
	 */
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> IRON_HEAVY = ARMOR_MATERIALS.register(
			"iron_heavy",
			() -> new ArmorMaterial(
					// 铁制内衬甲 3/7/6/3，各部位 +1/+2/+1/+1。
					Map.of(
							ArmorItem.Type.HELMET, 4,
							ArmorItem.Type.CHESTPLATE, 9,
							ArmorItem.Type.LEGGINGS, 7,
							ArmorItem.Type.BOOTS, 4),
					// 附魔亲和度沿用铁制内衬甲。
					9,
					SoundEvents.ARMOR_EQUIP_IRON,
					() -> Ingredient.of(Items.IRON_INGOT),
					List.of(new ArmorMaterial.Layer(
							ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "iron_heavy"), "", false)),
					// 韧性沿用铁制内衬甲。
					2.0F,
					// 击退抗性沿用铁制内衬甲。
					0.0F));

	/**
	 * 金制重型甲材料。
	 *
	 * <ul>
	 *   <li>护甲值：金制内衬甲 (3/6/4/2) 各部位 +1/+2/+1/+1 → 4/8/5/3；</li>
	 *   <li>韧性 2.0、附魔亲和度 25，均与金制内衬甲相同。</li>
	 * </ul>
	 */
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GOLD_HEAVY = ARMOR_MATERIALS.register(
			"gold_heavy",
			() -> new ArmorMaterial(
					// 金制内衬甲 3/6/4/2，各部位 +1/+2/+1/+1。
					Map.of(
							ArmorItem.Type.HELMET, 4,
							ArmorItem.Type.CHESTPLATE, 8,
							ArmorItem.Type.LEGGINGS, 5,
							ArmorItem.Type.BOOTS, 3),
					25,
					SoundEvents.ARMOR_EQUIP_GOLD,
					() -> Ingredient.of(Items.GOLD_INGOT),
					List.of(new ArmorMaterial.Layer(
							ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "gold_heavy"), "", false)),
					2.0F,
					0.0F));

	/**
	 * 钻石重型甲材料。
	 *
	 * <ul>
	 *   <li>护甲值：钻石内衬甲 (4/9/7/4) → 6/13/10/6；</li>
	 *   <li>韧性 4.0、附魔亲和度 10，均与钻石内衬甲相同。</li>
	 * </ul>
	 */
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> DIAMOND_HEAVY = ARMOR_MATERIALS.register(
			"diamond_heavy",
			() -> new ArmorMaterial(
					Map.of(
							ArmorItem.Type.HELMET, 6,
							ArmorItem.Type.CHESTPLATE, 13,
							ArmorItem.Type.LEGGINGS, 10,
							ArmorItem.Type.BOOTS, 6),
					10,
					SoundEvents.ARMOR_EQUIP_DIAMOND,
					() -> Ingredient.of(Items.DIAMOND),
					List.of(new ArmorMaterial.Layer(
							ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "diamond_heavy"), "", false)),
					4.0F,
					0.0F));

	/**
	 * 合金重型甲材料。
	 *
	 * <ul>
	 *   <li>护甲值：合金内衬甲 (4/9/7/4) → 6/13/10/6；</li>
	 *   <li>韧性 5.0、击退抗性 0.1、附魔亲和度 15，均与合金内衬甲相同；</li>
	 *   <li>与合金内衬甲一样带抗火（物品不会被火烧毁）。</li>
	 * </ul>
	 */
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ALLOY_HEAVY = ARMOR_MATERIALS.register(
			"alloy_heavy",
			() -> new ArmorMaterial(
					Map.of(
							ArmorItem.Type.HELMET, 6,
							ArmorItem.Type.CHESTPLATE, 13,
							ArmorItem.Type.LEGGINGS, 10,
							ArmorItem.Type.BOOTS, 6),
					15,
					SoundEvents.ARMOR_EQUIP_NETHERITE,
					() -> Ingredient.of(Items.NETHERITE_INGOT),
					List.of(new ArmorMaterial.Layer(
							ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "alloy_heavy"), "", false)),
					5.0F,
					0.1F));

	// ============================================================
	//  超重型甲系列
	//
	//  在重型甲基础上再强化一级：
	//    护甲值 = 对应重型甲 + 该材质的加成（金 +1/+3/+2/+1、铁 +2/+4/+3/+2、
	//             钻石与合金 +3/+5/+4/+3，按头盔 / 胸甲 / 护腿 / 靴子）；
	//    韧性   = 对应重型甲 + 2.0；
	//    击退抗性 = 对应重型甲 + 0.1（等同于再叠一层下界合金甲的击退抗性）。
	//  附魔亲和度、音效、修复材料均沿用对应重型甲。
	//  耐久 ×1.1 在注册物品时逐件写具体数值。
	//
	//  金制与合金制额外具备猪灵中立（见 PiglinNeutralArmorItem）。
	// ============================================================

	/**
	 * 金制超重型甲材料。
	 *
	 * <ul>
	 *   <li>护甲值：金制重型甲 (4/8/5/3) 各部位 +1/+3/+2/+1 → 5/11/7/4；</li>
	 *   <li>韧性：金制重型甲 2.0 + 2.0 = 4.0；</li>
	 *   <li>击退抗性：金制重型甲 0.0 + 0.1 = 0.1。</li>
	 * </ul>
	 */
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GOLD_ULTRA_HEAVY = ARMOR_MATERIALS.register(
			"gold_ultra_heavy",
			() -> new ArmorMaterial(
					Map.of(
							ArmorItem.Type.HELMET, 5,
							ArmorItem.Type.CHESTPLATE, 11,
							ArmorItem.Type.LEGGINGS, 7,
							ArmorItem.Type.BOOTS, 4),
					25,
					SoundEvents.ARMOR_EQUIP_GOLD,
					() -> Ingredient.of(Items.GOLD_INGOT),
					List.of(new ArmorMaterial.Layer(
							ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "gold_ultra_heavy"), "", false)),
					4.0F,
					0.1F));

	/**
	 * 铁制超重型甲材料。
	 *
	 * <ul>
	 *   <li>护甲值：铁制重型甲 (4/9/7/4) 各部位 +2/+4/+3/+2 → 6/13/10/6；</li>
	 *   <li>韧性：铁制重型甲 2.0 + 2.0 = 4.0；</li>
	 *   <li>击退抗性：铁制重型甲 0.0 + 0.1 = 0.1。</li>
	 * </ul>
	 */
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> IRON_ULTRA_HEAVY = ARMOR_MATERIALS.register(
			"iron_ultra_heavy",
			() -> new ArmorMaterial(
					Map.of(
							ArmorItem.Type.HELMET, 6,
							ArmorItem.Type.CHESTPLATE, 13,
							ArmorItem.Type.LEGGINGS, 10,
							ArmorItem.Type.BOOTS, 6),
					9,
					SoundEvents.ARMOR_EQUIP_IRON,
					() -> Ingredient.of(Items.IRON_INGOT),
					List.of(new ArmorMaterial.Layer(
							ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "iron_ultra_heavy"), "", false)),
					4.0F,
					0.1F));

	/**
	 * 钻石超重型甲材料。
	 *
	 * <ul>
	 *   <li>护甲值：钻石重型甲 (6/13/10/6) 各部位 +3/+5/+4/+3 → 9/18/14/9；</li>
	 *   <li>韧性：钻石重型甲 4.0 + 2.0 = 6.0；</li>
	 *   <li>击退抗性：钻石重型甲 0.0 + 0.1 = 0.1。</li>
	 * </ul>
	 */
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> DIAMOND_ULTRA_HEAVY = ARMOR_MATERIALS.register(
			"diamond_ultra_heavy",
			() -> new ArmorMaterial(
					Map.of(
							ArmorItem.Type.HELMET, 9,
							ArmorItem.Type.CHESTPLATE, 18,
							ArmorItem.Type.LEGGINGS, 14,
							ArmorItem.Type.BOOTS, 9),
					10,
					SoundEvents.ARMOR_EQUIP_DIAMOND,
					() -> Ingredient.of(Items.DIAMOND),
					List.of(new ArmorMaterial.Layer(
							ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "diamond_ultra_heavy"), "", false)),
					6.0F,
					0.1F));

	/**
	 * 合金超重型甲材料。
	 *
	 * <ul>
	 *   <li>护甲值：合金重型甲 (6/13/10/6) 各部位 +3/+5/+4/+3 → 9/18/14/9，与钻石超重型同级；</li>
	 *   <li>韧性：合金重型甲 5.0 + 2.0 = 7.0；</li>
	 *   <li>击退抗性：合金重型甲 0.1 + 0.1 = 0.2；</li>
	 *   <li>与合金重型甲一样带抗火。</li>
	 * </ul>
	 */
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ALLOY_ULTRA_HEAVY = ARMOR_MATERIALS.register(
			"alloy_ultra_heavy",
			() -> new ArmorMaterial(
					Map.of(
							ArmorItem.Type.HELMET, 9,
							ArmorItem.Type.CHESTPLATE, 18,
							ArmorItem.Type.LEGGINGS, 14,
							ArmorItem.Type.BOOTS, 9),
					15,
					SoundEvents.ARMOR_EQUIP_NETHERITE,
					() -> Ingredient.of(Items.NETHERITE_INGOT),
					List.of(new ArmorMaterial.Layer(
							ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "alloy_ultra_heavy"), "", false)),
					7.0F,
					0.2F));

	private ModArmorMaterials() {
	}

	public static void register(IEventBus modEventBus) {
		ARMOR_MATERIALS.register(modEventBus);
	}
}
