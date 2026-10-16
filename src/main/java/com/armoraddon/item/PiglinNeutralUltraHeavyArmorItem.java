package com.armoraddon.item;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * 既让猪灵保持中立、又带重量惩罚的超重型甲。
 *
 * <p>金制与合金制用这个类。减速的幅度与实现同 {@link UltraHeavyArmorItem}，
 * 只是额外继承了 {@link PiglinNeutralArmorItem} 的 {@code makesPiglinsNeutral}。</p>
 *
 * <p>继承关系是 {@code PiglinNeutralArmorItem} 而不是 {@code UltraHeavyArmorItem} ——
 * 两者都直接继承 {@code ArmorItem}，Java 没有多继承，所以只能各写一份
 * {@code getDefaultAttributeModifiers}，减速幅度则共用
 * {@link UltraHeavyArmorItem#weightOf}。</p>
 */
public class PiglinNeutralUltraHeavyArmorItem extends PiglinNeutralArmorItem {
	public PiglinNeutralUltraHeavyArmorItem(Holder<ArmorMaterial> material, ArmorItem.Type type,
											Item.Properties properties) {
		super(material, type, properties);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers() {
		ItemAttributeModifiers base = super.getDefaultAttributeModifiers();

		double slow = UltraHeavyArmorItem.weightOf(getMaterial());
		if (slow <= 0.0) {
			return base;
		}

		return base.withModifierAdded(
				Attributes.MOVEMENT_SPEED,
				new AttributeModifier(UltraHeavyArmorItem.WEIGHT_ID, -slow,
						AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
				EquipmentSlotGroup.bySlot(getType().getSlot()));
	}
}
