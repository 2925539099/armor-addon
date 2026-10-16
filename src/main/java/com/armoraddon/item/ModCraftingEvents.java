package com.armoraddon.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 合成后处理：让升级产物继承基底的附魔、自定义名称与已损失的耐久。
 *
 * <p>本模组的升级链是「羊毛系内衬甲 → 重型甲 → 超重型甲」，每一步都无法用原版配方
 * 表达「把基底的数据搬到产物上」，所以放在事件里做。</p>
 *
 * <p>判断方式不按具体物品列举，而是按材质：<strong>凡是材质不是木制或羊毛的盔甲，
 * 都是某个升级步骤的产物</strong>，都需要继承。这样以后新增套装不必再改这里。</p>
 *
 * <p>基底则取网格里第一件 {@link ArmorItem}。工作台的配方里基底是原版盔甲，
 * 锻造台的三格里则有「盔甲升级模板 + 基底 + 添加物」，模板与添加物都不是盔甲，
 * 所以这一条规则对两种台子都成立。</p>
 *
 * <h2>触发来源</h2>
 * <ul>
 *   <li><strong>工作台</strong>：{@link PlayerEvent.ItemCraftedEvent} 由
 *       {@code ResultSlot#checkTakeAchievements} 触发，是 NeoForge 唯一的合成事件；</li>
 *   <li><strong>锻造台</strong>：{@code ItemCombinerMenu} 里的匿名 {@code Slot}
 *       不 fire 任何事件，NeoForge 也没有提供替代钩子，因此由
 *       {@code SmithingMenuMixin} 直接注入 {@code SmithingMenu#onTake} 来调用
 *       {@link #handleCrafted}。</li>
 * </ul>
 *
 * <p>两处的调用时机都在材料被扣减之前，所以网格里仍能找到基底，
 * 且传入的就是玩家最终拿到手的那个物品栈。</p>
 */
public final class ModCraftingEvents {
	private ModCraftingEvents() {
	}

	/** 工作台侧的事件入口。 */
	public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
		handleCrafted(event.getCrafting(), event.getInventory());
	}

	/**
	 * 让升级产物继承基底数据。工作台与锻造台共用。
	 *
	 * @param result 玩家即将取走的产物
	 * @param grid   配方所在的容器（工作台的合成格，或锻造台的三个输入槽）
	 */
	public static void handleCrafted(ItemStack result, Container grid) {
		if (!needsInheritance(result.getItem())) {
			return;
		}

		ItemStack base = findBase(grid);
		if (base == null) {
			return;
		}

		inherit(result, base);
	}

	/**
	 * 该物品是否为需要继承的升级产物。
	 *
	 * <p>木制与羊毛是本模组的两个起点，没有基底可继承；其余套装都是升级来的。</p>
	 */
	private static boolean needsInheritance(Item item) {
		if (!(item instanceof ArmorItem armor)) {
			return false;
		}

		Holder<ArmorMaterial> material = armor.getMaterial();

		return material != ModArmorMaterials.WOOD && material != ModArmorMaterials.WOOL;
	}

	/** 在容器里找出作为基底的那件盔甲。 */
	private static ItemStack findBase(Container grid) {
		for (int i = 0; i < grid.getContainerSize(); i++) {
			ItemStack stack = grid.getItem(i);
			if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem) {
				return stack;
			}
		}

		return null;
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
}
