package com.armoraddon.item;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

/**
 * 能让猪灵中立的盔甲。
 *
 * <p>原版的判定在 NeoForge 的 {@code IItemExtension#makesPiglinsNeutral} 里，
 * 写死了只认 {@code ArmorMaterials.GOLD}：</p>
 *
 * <pre>
 * return stack.getItem() instanceof ArmorItem
 *         &amp;&amp; ((ArmorItem) stack.getItem()).getMaterial() == ArmorMaterials.GOLD;
 * </pre>
 *
 * <p>{@link ArmorMaterial} 是固定七个字段的 record，没有可以打开这个行为的开关，
 * 所以本模组的金制甲与合金甲只能靠覆盖该方法来获得同样的待遇。</p>
 *
 * <p>用在 {@code gold_lining}、{@code gold_heavy}、{@code alloy_lining}、
 * {@code alloy_heavy} 四套共 16 件盔甲上。铁制与钻石制不参与。</p>
 */
public class PiglinNeutralArmorItem extends ArmorItem {
	public PiglinNeutralArmorItem(Holder<ArmorMaterial> material, ArmorItem.Type type, Properties properties) {
		super(material, type, properties);
	}

	@Override
	public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
		return true;
	}
}
