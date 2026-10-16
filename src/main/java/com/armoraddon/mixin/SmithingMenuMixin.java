package com.armoraddon.mixin;

import com.armoraddon.item.ModCraftingEvents;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 让锻造台的产物也能继承基底数据。
 *
 * <p>NeoForge 唯一的合成事件 {@code PlayerEvent.ItemCraftedEvent} 由工作台的
 * {@code ResultSlot#checkTakeAchievements} 触发；锻造台走的是
 * {@code ItemCombinerMenu} 里的匿名 {@code Slot}，其 {@code onTake} 只调用
 * {@code SmithingMenu#onTake}，不 fire 任何事件，NeoForge 也没有提供替代钩子。</p>
 *
 * <p>因此直接注入 {@code SmithingMenu#onTake} 的开头。选 HEAD 是因为此时
 * 三个输入槽里的材料还没被 {@code shrinkStackInSlot} 扣减，仍能读到基底。</p>
 *
 * <p>输入槽字段 {@code inputSlots} 声明在父类 {@code ItemCombinerMenu} 上，
 * 无法用 {@code @Shadow} 读取（Mixin 只认目标类自身的成员），
 * 所以走 {@link ItemCombinerMenuAccessor}。</p>
 */
@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin {
	@Inject(method = "onTake", at = @At("HEAD"))
	private void armorAddon$inheritFromBase(Player player, ItemStack result, CallbackInfo callbackInfo) {
		ModCraftingEvents.handleCrafted(result, ((ItemCombinerMenuAccessor) this).armorAddon$getInputSlots());
	}
}
