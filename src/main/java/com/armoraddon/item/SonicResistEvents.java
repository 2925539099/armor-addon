package com.armoraddon.item;

import com.armoraddon.ArmorAddon;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;

/**
 * 音波抗性：羊毛盔甲及其升级的护甲，受到音波类伤害时每穿一件减 1 点。
 *
 * <p>生效范围是羊毛系的九套 —— 羊毛盔甲，以及由它升级来的四种内衬甲
 * （铁质 / 金质 / 钻石 / 合金）和四种重型甲。木制盔甲不属于羊毛系，不在其中。</p>
 *
 * <p>判断依据是伤害类型标签 {@code armor_addon:sonic_like}，默认只含
 * {@code minecraft:sonic_boom}（坚守者的音波）。往标签里追加条目即可扩展。</p>
 */
public final class SonicResistEvents {
	/** 羊毛系的九个材质。木制盔甲不在这里。 */
	private static final List<Holder<ArmorMaterial>> WOOL_LINE = List.of(
			ModArmorMaterials.WOOL,
			ModArmorMaterials.IRON_LINING,
			ModArmorMaterials.GOLD_LINING,
			ModArmorMaterials.DIAMOND_LINING,
			ModArmorMaterials.ALLOY_LINING,
			ModArmorMaterials.IRON_HEAVY,
			ModArmorMaterials.GOLD_HEAVY,
			ModArmorMaterials.DIAMOND_HEAVY,
			ModArmorMaterials.ALLOY_HEAVY);

	/** 超重型甲的四个材质。它们也属于羊毛系，但减免翻倍。 */
	private static final List<Holder<ArmorMaterial>> ULTRA_HEAVY = List.of(
			ModArmorMaterials.IRON_ULTRA_HEAVY,
			ModArmorMaterials.GOLD_ULTRA_HEAVY,
			ModArmorMaterials.DIAMOND_ULTRA_HEAVY,
			ModArmorMaterials.ALLOY_ULTRA_HEAVY);

	/** 参与判定的四个盔甲槽位。 */
	private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(
			EquipmentSlot.HEAD,
			EquipmentSlot.CHEST,
			EquipmentSlot.LEGS,
			EquipmentSlot.FEET);

	/** 音波类伤害类型标签，默认含 {@code minecraft:sonic_boom}。 */
	public static final TagKey<DamageType> SONIC_LIKE = TagKey.create(
			Registries.DAMAGE_TYPE,
			ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "sonic_like"));

	private SonicResistEvents() {
	}

	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}

		if (!event.getSource().is(SONIC_LIKE)) {
			return;
		}

		int reduction = 0;

		for (EquipmentSlot slot : ARMOR_SLOTS) {
			reduction += reductionOf(player.getItemBySlot(slot));
		}

		if (reduction <= 0) {
			return;
		}

		// 最低减到 0。
		event.setAmount(Math.max(0.0F, event.getAmount() - reduction));
	}

	/** 羊毛系每件减 1 点；超重型甲是该系列的再升级，每件减 2 点。 */
	private static int reductionOf(ItemStack stack) {
		if (isUltraHeavyArmor(stack)) {
			return 2;
		}

		return isWoolLineArmor(stack) ? 1 : 0;
	}

	/** 是否为超重型甲。 */
	private static boolean isUltraHeavyArmor(ItemStack stack) {
		if (!(stack.getItem() instanceof ArmorItem armor)) {
			return false;
		}

		Holder<ArmorMaterial> material = armor.getMaterial();

		for (Holder<ArmorMaterial> ultraHeavy : ULTRA_HEAVY) {
			if (material == ultraHeavy) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 是否为羊毛系盔甲。
	 *
	 * <p>{@link ArmorItem} 构造时收到的就是我们传进去的那个 {@code DeferredHolder} 实例，
	 * 所以直接比较 {@link Holder} 引用即可。其他模组的盔甲材质与这些都不相等。</p>
	 */
	private static boolean isWoolLineArmor(ItemStack stack) {
		if (!(stack.getItem() instanceof ArmorItem armor)) {
			return false;
		}

		Holder<ArmorMaterial> material = armor.getMaterial();

		for (Holder<ArmorMaterial> woolLine : WOOL_LINE) {
			if (material == woolLine) {
				return true;
			}
		}

		return false;
	}
}
