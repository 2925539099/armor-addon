package com.armoraddon;

import com.armoraddon.item.ModArmorMaterials;
import com.armoraddon.item.ModCraftingEvents;
import com.armoraddon.item.ModItems;
import com.armoraddon.item.SonicResistEvents;
import com.armoraddon.item.WoolBootsEvents;

import net.minecraft.world.item.CreativeModeTabs;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 盔甲附加（Armor Addon）—— NeoForge 版主入口。
 *
 * <p>注册十四个盔甲材料（木制、羊毛、四种内衬甲、四种重型甲、四种超重型甲）
 * 与对应的五十六个盔甲部件，另加一件盔甲升级模板，并把它们加入创造模式「战斗」物品栏。</p>
 *
 * <p>机制：</p>
 * <ul>
 *   <li>羊毛靴子让移动不产生振动；</li>
 *   <li>羊毛 / 内衬 / 重型共九套具备音波抗性，每件减 1 点；超重型甲再升一级，每件减 2 点；</li>
 *   <li>金制与合金制的重型甲、超重型甲让猪灵保持中立；</li>
 *   <li>内衬甲与超重型甲在合成时继承基底的附魔、名称与已损失的耐久；</li>
 *   <li>超重型甲有重量惩罚：金制每件 −7.5%、铁制 −5%、钻石 −2.5%、合金 −10% 移动速度。</li>
 * </ul>
 */
@Mod(ArmorAddon.MOD_ID)
public final class ArmorAddon {
	public static final String MOD_ID = "armor_addon";
	public static final Logger LOGGER = LoggerFactory.getLogger("Armor Addon");

	public ArmorAddon(IEventBus modEventBus, ModContainer modContainer) {
		// 盔甲材料必须先于使用它的盔甲物品注册。
		ModArmorMaterials.register(modEventBus);
		ModItems.register(modEventBus);

		modEventBus.addListener(this::addCreativeTabItems);
		// 内衬甲与超重型甲合成时继承基底的附魔 / 名称并同步损耗耐久。
		NeoForge.EVENT_BUS.addListener(ModCraftingEvents::onItemCrafted);
		// 羊毛靴子：移动时不被幽匿感测体 / 坚守者探测到。
		NeoForge.EVENT_BUS.addListener(WoolBootsEvents::onVanillaGameEvent);
		// 音波抗性：羊毛系九套每件减 1 点，超重型甲每件减 2 点。
		NeoForge.EVENT_BUS.addListener(SonicResistEvents::onIncomingDamage);
		// 超重型甲的重量惩罚没有事件 —— 它写在物品的默认属性修饰符里，
		// 由 Minecraft 在穿脱时自动施加，见 UltraHeavyArmorItem。

		LOGGER.info("[盔甲附加] NeoForge 版模组已加载，木制 / 羊毛盔甲、四种内衬甲、"
				+ "四种重型甲与四种超重型甲已注册；猪灵中立、音波抗性与超重型甲减速已生效。");
	}

	private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.COMBAT) {
			// 锻造模板放在最前，它是升级配方的入口。
			event.accept(ModItems.ARMOR_UPGRADE_TEMPLATE);

			event.accept(ModItems.WOOD_HELMET);
			event.accept(ModItems.WOOD_CHESTPLATE);
			event.accept(ModItems.WOOD_LEGGINGS);
			event.accept(ModItems.WOOD_BOOTS);

			event.accept(ModItems.WOOL_HELMET);
			event.accept(ModItems.WOOL_CHESTPLATE);
			event.accept(ModItems.WOOL_LEGGINGS);
			event.accept(ModItems.WOOL_BOOTS);

			event.accept(ModItems.IRON_LINING_HELMET);
			event.accept(ModItems.IRON_LINING_CHESTPLATE);
			event.accept(ModItems.IRON_LINING_LEGGINGS);
			event.accept(ModItems.IRON_LINING_BOOTS);

			event.accept(ModItems.GOLD_LINING_HELMET);
			event.accept(ModItems.GOLD_LINING_CHESTPLATE);
			event.accept(ModItems.GOLD_LINING_LEGGINGS);
			event.accept(ModItems.GOLD_LINING_BOOTS);

			event.accept(ModItems.DIAMOND_LINING_HELMET);
			event.accept(ModItems.DIAMOND_LINING_CHESTPLATE);
			event.accept(ModItems.DIAMOND_LINING_LEGGINGS);
			event.accept(ModItems.DIAMOND_LINING_BOOTS);

			event.accept(ModItems.ALLOY_LINING_HELMET);
			event.accept(ModItems.ALLOY_LINING_CHESTPLATE);
			event.accept(ModItems.ALLOY_LINING_LEGGINGS);
			event.accept(ModItems.ALLOY_LINING_BOOTS);

			// ---- 重型甲（由对应内衬甲在锻造台升级而来）----
			event.accept(ModItems.IRON_HEAVY_HELMET);
			event.accept(ModItems.IRON_HEAVY_CHESTPLATE);
			event.accept(ModItems.IRON_HEAVY_LEGGINGS);
			event.accept(ModItems.IRON_HEAVY_BOOTS);

			event.accept(ModItems.GOLD_HEAVY_HELMET);
			event.accept(ModItems.GOLD_HEAVY_CHESTPLATE);
			event.accept(ModItems.GOLD_HEAVY_LEGGINGS);
			event.accept(ModItems.GOLD_HEAVY_BOOTS);

			event.accept(ModItems.DIAMOND_HEAVY_HELMET);
			event.accept(ModItems.DIAMOND_HEAVY_CHESTPLATE);
			event.accept(ModItems.DIAMOND_HEAVY_LEGGINGS);
			event.accept(ModItems.DIAMOND_HEAVY_BOOTS);

			event.accept(ModItems.ALLOY_HEAVY_HELMET);
			event.accept(ModItems.ALLOY_HEAVY_CHESTPLATE);
			event.accept(ModItems.ALLOY_HEAVY_LEGGINGS);
			event.accept(ModItems.ALLOY_HEAVY_BOOTS);

			// ---- 超重型甲（由对应重型甲升级而来）----
			event.accept(ModItems.GOLD_ULTRA_HEAVY_HELMET);
			event.accept(ModItems.GOLD_ULTRA_HEAVY_CHESTPLATE);
			event.accept(ModItems.GOLD_ULTRA_HEAVY_LEGGINGS);
			event.accept(ModItems.GOLD_ULTRA_HEAVY_BOOTS);

			event.accept(ModItems.IRON_ULTRA_HEAVY_HELMET);
			event.accept(ModItems.IRON_ULTRA_HEAVY_CHESTPLATE);
			event.accept(ModItems.IRON_ULTRA_HEAVY_LEGGINGS);
			event.accept(ModItems.IRON_ULTRA_HEAVY_BOOTS);

			event.accept(ModItems.DIAMOND_ULTRA_HEAVY_HELMET);
			event.accept(ModItems.DIAMOND_ULTRA_HEAVY_CHESTPLATE);
			event.accept(ModItems.DIAMOND_ULTRA_HEAVY_LEGGINGS);
			event.accept(ModItems.DIAMOND_ULTRA_HEAVY_BOOTS);

			event.accept(ModItems.ALLOY_ULTRA_HEAVY_HELMET);
			event.accept(ModItems.ALLOY_ULTRA_HEAVY_CHESTPLATE);
			event.accept(ModItems.ALLOY_ULTRA_HEAVY_LEGGINGS);
			event.accept(ModItems.ALLOY_ULTRA_HEAVY_BOOTS);

			// ---- 锁链系列（第三次更新）----
			// 材料与液体先放，它们是整套装备的起点。
			event.accept(ModItems.EXCHANGE_FLUID);
			event.accept(ModItems.NETHERITE_PLATING_FLUID);
			event.accept(ModItems.STACK_OF_CHAIN);
			event.accept(ModItems.CHAIN_MATERIAL);
			event.accept(ModItems.STACK_OF_GOLD_CHAIN);
			event.accept(ModItems.GOLD_CHAIN_MATERIAL);
			event.accept(ModItems.DIAMOND_CHAIN_MATERIAL);
			event.accept(ModItems.STACK_OF_ALLOY_CHAIN);
			event.accept(ModItems.ALLOY_CHAIN_MATERIAL);


			event.accept(ModItems.GOLD_CHAIN_HELMET);
			event.accept(ModItems.GOLD_CHAIN_CHESTPLATE);
			event.accept(ModItems.GOLD_CHAIN_LEGGINGS);
			event.accept(ModItems.GOLD_CHAIN_BOOTS);

			event.accept(ModItems.DIAMOND_CHAIN_HELMET);
			event.accept(ModItems.DIAMOND_CHAIN_CHESTPLATE);
			event.accept(ModItems.DIAMOND_CHAIN_LEGGINGS);
			event.accept(ModItems.DIAMOND_CHAIN_BOOTS);

			event.accept(ModItems.ALLOY_CHAIN_HELMET);
			event.accept(ModItems.ALLOY_CHAIN_CHESTPLATE);
			event.accept(ModItems.ALLOY_CHAIN_LEGGINGS);
			event.accept(ModItems.ALLOY_CHAIN_BOOTS);
		}
	}
}
