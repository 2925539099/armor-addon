package com.armoraddon.item;

import com.armoraddon.ArmorAddon;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * 带重量惩罚的超重型甲。
 *
 * <p>越重的材质减速越多，每件累加，穿满一套是单件的四倍：</p>
 *
 * <table border="1">
 *   <caption>减速幅度</caption>
 *   <tr><th>套装</th><th>每件</th><th>四件</th></tr>
 *   <tr><td>金制超重型甲</td><td>7.5%</td><td>30%</td></tr>
 *   <tr><td>铁制超重型甲</td><td>5%</td><td>20%</td></tr>
 *   <tr><td>钻石超重型甲</td><td>2.5%</td><td>10%</td></tr>
 *   <tr><td>合金超重型甲</td><td>10%</td><td>40%</td></tr>
 * </table>
 *
 * <h2>为什么写在属性上而不是用事件</h2>
 * <p>把修饰符放进 {@link #getDefaultAttributeModifiers()} 之后，Minecraft 会在穿脱时
 * 自动施加和移除，而且<strong>物品提示里会像药水那样列出</strong>
 * 「当穿戴时：−7.5% 移动速度」。用 {@code PlayerTickEvent} 每 tick 手动加修饰符
 * 虽然也能生效，但玩家在物品栏里看不到任何说明。</p>
 *
 * <p>用 {@link AttributeModifier.Operation#ADD_MULTIPLIED_BASE}：它按<strong>基础</strong>
 * 移动速度的百分比扣减，多个来源会自然累加，而且不会像
 * {@code ADD_MULTIPLIED_TOTAL} 那样把迅捷药水、灵魂疾行之类的加成也一起缩小 ——
 * 惩罚只针对基础值，符合「甲重」的直觉。</p>
 */
public class UltraHeavyArmorItem extends ArmorItem {
	/** 修饰符 ID。同一件甲只用一个，靠 {@link #weightOf} 区分幅度。 */
	public static final ResourceLocation WEIGHT_ID =
			ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "ultra_heavy_weight");

	public UltraHeavyArmorItem(Holder<ArmorMaterial> material, ArmorItem.Type type,
							   Item.Properties properties) {
		super(material, type, properties);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers() {
		ItemAttributeModifiers base = super.getDefaultAttributeModifiers();

		double slow = weightOf(getMaterial());
		if (slow <= 0.0) {
			return base;
		}

		return base.withModifierAdded(
				Attributes.MOVEMENT_SPEED,
				new AttributeModifier(WEIGHT_ID, -slow,
						AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
				EquipmentSlotGroup.bySlot(getType().getSlot()));
	}

	/**
	 * 某个材质单件带来的减速比例。
	 *
	 * <p>只有四种超重型甲有值，其余材质返回 0（不会添加修饰符）。
	 * {@link PiglinNeutralUltraHeavyArmorItem} 也复用这里。</p>
	 */
	public static double weightOf(Holder<ArmorMaterial> material) {
		if (material == ModArmorMaterials.GOLD_ULTRA_HEAVY) {
			return 0.075;
		}
		if (material == ModArmorMaterials.IRON_ULTRA_HEAVY) {
			return 0.05;
		}
		if (material == ModArmorMaterials.DIAMOND_ULTRA_HEAVY) {
			return 0.025;
		}
		if (material == ModArmorMaterials.ALLOY_ULTRA_HEAVY) {
			return 0.10;
		}

		return 0.0;
	}
}
