package com.armoraddon.item;

import com.armoraddon.ArmorAddon;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

/**
 * 超重型甲的重量惩罚：穿戴时降低移动速度。
 *
 * <p>越重的材质减速越多，每件累加，穿满一套的幅度是单件的四倍：</p>
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
 * <h2>实现方式</h2>
 * <p>用 {@link AttributeModifier.Operation#ADD_MULTIPLIED_BASE}：它按<strong>基础移动速度</strong>
 * 的百分比扣减，多个来源会自然累加，而且不会像 {@code ADD_MULTIPLIED_TOTAL} 那样
 * 把迅捷药水、灵魂疾行之类的加成也一起缩小 —— 惩罚只针对基础值，符合「甲重」的直觉。</p>
 *
 * <p>属性是服务端权威的，客户端会收到同步，所以这里只在服务端施加。
 * {@link PlayerTickEvent} 在两侧都会触发，必须判 {@code isClientSide}，
 * 否则客户端会加一个本地修饰符，和服务端同步过来的叠加成双倍减速。</p>
 *
 * <p>每 tick 都调用 {@code addOrUpdateTransientModifier} 会让属性反复重算，
 * 所以先取出现有修饰符比一下数值，没变就直接返回。</p>
 *
 * <p><strong>只作用于超重型甲。</strong>上一版的四种重型甲（{@code *_heavy}）不加这个惩罚。</p>
 */
public final class UltraHeavySlowEvents {
	/** 修饰符 ID，同时用于查找、更新与移除。 */
	private static final ResourceLocation SLOW_ID =
			ResourceLocation.fromNamespaceAndPath(ArmorAddon.MOD_ID, "ultra_heavy_weight");

	/** 参与判定的四个盔甲槽位。 */
	private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(
			EquipmentSlot.HEAD,
			EquipmentSlot.CHEST,
			EquipmentSlot.LEGS,
			EquipmentSlot.FEET);

	private UltraHeavySlowEvents() {
	}

	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();

		// 只在服务端施加，客户端等同步即可。
		if (player.level().isClientSide()) {
			return;
		}

		AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed == null) {
			return;
		}

		double slow = 0.0;

		for (EquipmentSlot slot : ARMOR_SLOTS) {
			slow += slowPerPiece(player.getItemBySlot(slot));
		}

		if (slow <= 0.0) {
			// 没穿超重型甲就把修饰符摘掉，别留着影响其他装备。
			speed.removeModifier(SLOW_ID);
			return;
		}

		double amount = -slow;

		// 数值没变就不动，避免每 tick 触发属性重算。
		AttributeModifier existing = speed.getModifier(SLOW_ID);
		if (existing != null && existing.amount() == amount) {
			return;
		}

		speed.addOrUpdateTransientModifier(new AttributeModifier(
				SLOW_ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
	}

	/**
	 * 单件盔甲带来的减速比例。
	 *
	 * <p>{@link ArmorItem} 构造时收到的就是我们传进去的那个 {@code DeferredHolder} 实例，
	 * 所以直接比较 {@link Holder} 引用即可。其他模组的盔甲材质与这些都不相等。</p>
	 */
	private static double slowPerPiece(ItemStack stack) {
		if (!(stack.getItem() instanceof ArmorItem armor)) {
			return 0.0;
		}

		Holder<ArmorMaterial> material = armor.getMaterial();

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
