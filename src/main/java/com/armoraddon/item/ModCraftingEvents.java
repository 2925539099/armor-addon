package com.armoraddon.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.function.Predicate;

/**
 * 合成后处理：让升级产物继承基底的附魔、名称与已损失的耐久。
 *
 * <p>有两类配方需要这个处理：</p>
 * <ul>
 *   <li><strong>内衬甲</strong>（工作台，无序）—— 基底是原版的铁 / 金 / 钻石 / 下界合金盔甲；</li>
 *   <li><strong>超重型甲</strong>（工作台，有序）—— 基底是本模组的重型甲。</li>
 * </ul>
 *
 * <p>这类「继承」无法用原版配方表达，因此监听
 * {@link PlayerEvent.ItemCraftedEvent}，在玩家取走产物时把基底的数据搬过去。</p>
 *
 * <p>事件触发时机：{@code ResultSlot#checkTakeAchievements} 先 fire 本事件、
 * 之后才扣减合成格里的材料，所以事件里网格中仍能找到基底，
 * 且 {@code event.getCrafting()} 就是玩家最终拿到手的那个物品栈。</p>
 *
 * <p><strong>注意：本事件只由工作台触发。</strong>锻造台（{@code SmithingMenu}）走的是
 * {@code ItemCombinerMenu} 里的匿名 {@code Slot}，它的 {@code onTake} 只调用
 * {@code SmithingMenu#onTake}，不会 fire 任何 NeoForge 事件；NeoForge 也没有提供
 * 锻造台侧的替代钩子。因此锻造台配方（合金内衬甲、合金重型甲、合金超重型甲）
 * 目前不会继承基底数据。</p>
 */
public final class ModCraftingEvents {
	private ModCraftingEvents() {
	}

	public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
		ItemStack result = event.getCrafting();
		Item item = result.getItem();

		// 按产物类型决定要在网格里找哪种基底。
		Predicate<Item> baseMatcher;

		if (isLiningArmor(item)) {
			baseMatcher = ModCraftingEvents::isVanillaArmor;
		} else if (isUltraHeavyArmor(item)) {
			baseMatcher = ModCraftingEvents::isHeavyArmor;
		} else {
			return;
		}

		ItemStack base = findInGrid(event.getInventory(), baseMatcher);
		if (base == null) {
			return;
		}

		inherit(result, base);
	}

	/** 把基底的数据搬到产物上。 */
	private static void inherit(ItemStack result, ItemStack base) {
		// 继承附魔。
		var enchantments = base.get(DataComponents.ENCHANTMENTS);
		if (enchantments != null) {
			result.set(DataComponents.ENCHANTMENTS, enchantments);
		}

		// 继承自定义名称。
		var customName = base.get(DataComponents.CUSTOM_NAME);
		if (customName != null) {
			result.set(DataComponents.CUSTOM_NAME, customName);
		}

		// 同步已损失的耐久。
		// 只搬 DAMAGE 一个组件，不能整体 applyComponents ——
		// 否则会把基底的 MAX_DAMAGE 一起带过来，耐久上限就退回基底了。
		int lostDurability = base.getDamageValue();
		if (lostDurability > 0) {
			result.setDamageValue(Math.min(lostDurability, result.getMaxDamage() - 1));
		}
	}

	/** 在合成网格里找出符合条件的那一格。 */
	private static ItemStack findInGrid(Container grid, Predicate<Item> matcher) {
		for (int i = 0; i < grid.getContainerSize(); i++) {
			ItemStack stack = grid.getItem(i);
			if (!stack.isEmpty() && matcher.test(stack.getItem())) {
				return stack;
			}
		}

		return null;
	}

	/** 产物是否为内衬甲（铁制 / 金制 / 钻石 / 合金）。 */
	private static boolean isLiningArmor(Item item) {
		return item == ModItems.IRON_LINING_HELMET.get()
				|| item == ModItems.IRON_LINING_CHESTPLATE.get()
				|| item == ModItems.IRON_LINING_LEGGINGS.get()
				|| item == ModItems.IRON_LINING_BOOTS.get()
				|| item == ModItems.GOLD_LINING_HELMET.get()
				|| item == ModItems.GOLD_LINING_CHESTPLATE.get()
				|| item == ModItems.GOLD_LINING_LEGGINGS.get()
				|| item == ModItems.GOLD_LINING_BOOTS.get()
				|| item == ModItems.DIAMOND_LINING_HELMET.get()
				|| item == ModItems.DIAMOND_LINING_CHESTPLATE.get()
				|| item == ModItems.DIAMOND_LINING_LEGGINGS.get()
				|| item == ModItems.DIAMOND_LINING_BOOTS.get()
				|| item == ModItems.ALLOY_LINING_HELMET.get()
				|| item == ModItems.ALLOY_LINING_CHESTPLATE.get()
				|| item == ModItems.ALLOY_LINING_LEGGINGS.get()
				|| item == ModItems.ALLOY_LINING_BOOTS.get();
	}

	/** 产物是否为超重型甲。 */
	private static boolean isUltraHeavyArmor(Item item) {
		return item == ModItems.IRON_ULTRA_HEAVY_HELMET.get()
				|| item == ModItems.IRON_ULTRA_HEAVY_CHESTPLATE.get()
				|| item == ModItems.IRON_ULTRA_HEAVY_LEGGINGS.get()
				|| item == ModItems.IRON_ULTRA_HEAVY_BOOTS.get()
				|| item == ModItems.GOLD_ULTRA_HEAVY_HELMET.get()
				|| item == ModItems.GOLD_ULTRA_HEAVY_CHESTPLATE.get()
				|| item == ModItems.GOLD_ULTRA_HEAVY_LEGGINGS.get()
				|| item == ModItems.GOLD_ULTRA_HEAVY_BOOTS.get()
				|| item == ModItems.DIAMOND_ULTRA_HEAVY_HELMET.get()
				|| item == ModItems.DIAMOND_ULTRA_HEAVY_CHESTPLATE.get()
				|| item == ModItems.DIAMOND_ULTRA_HEAVY_LEGGINGS.get()
				|| item == ModItems.DIAMOND_ULTRA_HEAVY_BOOTS.get()
				|| item == ModItems.ALLOY_ULTRA_HEAVY_HELMET.get()
				|| item == ModItems.ALLOY_ULTRA_HEAVY_CHESTPLATE.get()
				|| item == ModItems.ALLOY_ULTRA_HEAVY_LEGGINGS.get()
				|| item == ModItems.ALLOY_ULTRA_HEAVY_BOOTS.get();
	}

	/** 是否为原版盔甲（内衬甲的基底）。 */
	private static boolean isVanillaArmor(Item item) {
		return item == Items.IRON_HELMET
				|| item == Items.IRON_CHESTPLATE
				|| item == Items.IRON_LEGGINGS
				|| item == Items.IRON_BOOTS
				|| item == Items.GOLDEN_HELMET
				|| item == Items.GOLDEN_CHESTPLATE
				|| item == Items.GOLDEN_LEGGINGS
				|| item == Items.GOLDEN_BOOTS
				|| item == Items.DIAMOND_HELMET
				|| item == Items.DIAMOND_CHESTPLATE
				|| item == Items.DIAMOND_LEGGINGS
				|| item == Items.DIAMOND_BOOTS
				|| item == Items.NETHERITE_HELMET
				|| item == Items.NETHERITE_CHESTPLATE
				|| item == Items.NETHERITE_LEGGINGS
				|| item == Items.NETHERITE_BOOTS;
	}

	/** 是否为本模组的重型甲（超重型甲的基底）。 */
	private static boolean isHeavyArmor(Item item) {
		return item == ModItems.IRON_HEAVY_HELMET.get()
				|| item == ModItems.IRON_HEAVY_CHESTPLATE.get()
				|| item == ModItems.IRON_HEAVY_LEGGINGS.get()
				|| item == ModItems.IRON_HEAVY_BOOTS.get()
				|| item == ModItems.GOLD_HEAVY_HELMET.get()
				|| item == ModItems.GOLD_HEAVY_CHESTPLATE.get()
				|| item == ModItems.GOLD_HEAVY_LEGGINGS.get()
				|| item == ModItems.GOLD_HEAVY_BOOTS.get()
				|| item == ModItems.DIAMOND_HEAVY_HELMET.get()
				|| item == ModItems.DIAMOND_HEAVY_CHESTPLATE.get()
				|| item == ModItems.DIAMOND_HEAVY_LEGGINGS.get()
				|| item == ModItems.DIAMOND_HEAVY_BOOTS.get()
				|| item == ModItems.ALLOY_HEAVY_HELMET.get()
				|| item == ModItems.ALLOY_HEAVY_CHESTPLATE.get()
				|| item == ModItems.ALLOY_HEAVY_LEGGINGS.get()
				|| item == ModItems.ALLOY_HEAVY_BOOTS.get();
	}
}
