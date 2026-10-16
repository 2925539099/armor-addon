package com.armoraddon.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.ItemCombinerMenu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 读取 {@code ItemCombinerMenu.inputSlots} 的访问器。
 *
 * <p>该字段声明在 {@code ItemCombinerMenu} 上、是 {@code protected} 的。
 * {@code SmithingMenuMixin} 想把锻造台的三个输入槽传给继承逻辑，
 * 但两条路都走不通：</p>
 *
 * <ul>
 *   <li>直接强转访问 —— Mixin 类在 {@code com.armoraddon.mixin} 包，
 *       与 {@code net.minecraft.world.inventory} 不同包，{@code protected} 不可见；</li>
 *   <li>{@code @Shadow} —— Mixin 只查找<strong>目标类自身声明</strong>的成员，
 *       目标类 {@code SmithingMenu} 自己并没有 {@code inputSlots} 字段，
 *       会抛 {@code InvalidMixinException: @Shadow field inputSlots was not located}。</li>
 * </ul>
 *
 * <p>所以改用 Accessor Mixin，直接为 {@code ItemCombinerMenu} 生成一个读取方法。</p>
 */
@Mixin(ItemCombinerMenu.class)
public interface ItemCombinerMenuAccessor {
	@Accessor("inputSlots")
	Container armorAddon$getInputSlots();
}
