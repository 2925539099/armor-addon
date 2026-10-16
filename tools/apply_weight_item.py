#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""把 16 件超重型甲改成带重量惩罚的物品类。

金制 / 合金原本用 registerPiglinNeutral*，铁制 / 钻石用 registerArmorWithDurability。
现在分别换成新的 UltraHeavy* 版本，并在文件末尾补上三个辅助方法。

注意 registerArmorWithDurability 也被四种重型甲使用，**不能全局替换**，
所以这里按变量名精确匹配。
"""

from __future__ import annotations

import re
from pathlib import Path

PROJ = Path(__file__).resolve().parent.parent
SRC = PROJ / "src/main/java/com/armoraddon/item/ModItems.java"

# 铁制 / 钻石 用普通版；金制 用猪灵中立版；合金 用猪灵中立 + 抗火版
REPLACEMENTS = [
    ("GOLD_ULTRA_HEAVY_", "registerPiglinNeutralArmor", "registerPiglinNeutralUltraHeavyArmor"),
    ("ALLOY_ULTRA_HEAVY_", "registerPiglinNeutralFireResistantArmor",
     "registerPiglinNeutralFireResistantUltraHeavyArmor"),
    ("IRON_ULTRA_HEAVY_", "registerArmorWithDurability", "registerUltraHeavyArmor"),
    ("DIAMOND_ULTRA_HEAVY_", "registerArmorWithDurability", "registerUltraHeavyArmor"),
]

HELPERS = '''
	/**
	 * 带重量惩罚的超重型甲（铁制 / 钻石制）。
	 *
	 * <p>减速写进了物品的默认属性修饰符，所以物品提示里会像药水那样列出
	 * 「当穿戴时：−5% 移动速度」。详见 {@link UltraHeavyArmorItem}。</p>
	 */
	private static DeferredItem<Item> registerUltraHeavyArmor(String name, Holder<ArmorMaterial> material,
															  ArmorItem.Type type, int durability) {
		return ITEMS.register(name, () -> new UltraHeavyArmorItem(
				material,
				type,
				new Item.Properties().durability(durability)));
	}

	/** 带重量惩罚 + 猪灵中立的超重型甲（金制）。 */
	private static DeferredItem<Item> registerPiglinNeutralUltraHeavyArmor(String name,
																		   Holder<ArmorMaterial> material,
																		   ArmorItem.Type type, int durability) {
		return ITEMS.register(name, () -> new PiglinNeutralUltraHeavyArmorItem(
				material,
				type,
				new Item.Properties().durability(durability)));
	}

	/** 带重量惩罚 + 猪灵中立 + 抗火的超重型甲（合金）。 */
	private static DeferredItem<Item> registerPiglinNeutralFireResistantUltraHeavyArmor(
			String name, Holder<ArmorMaterial> material, ArmorItem.Type type, int durability) {
		return ITEMS.register(name, () -> new PiglinNeutralUltraHeavyArmorItem(
				material,
				type,
				new Item.Properties().fireResistant().durability(durability)));
	}
'''


def main() -> None:
    text = SRC.read_text(encoding="utf-8")
    total = 0

    # 每个超重型甲定义形如：
    #   registerXxx("gold_ultra_heavy_helmet", ModArmorMaterials.GOLD_ULTRA_HEAVY, ...)
    # old 里已经带 register 前缀，不要再加。
    for prefix, old, new in REPLACEMENTS:
        pattern = re.compile(re.escape(old) + r'(\("' + prefix.lower() + r')')
        text, n = pattern.subn(new + r'\1', text)
        if n:
            print(f"  {prefix:<22} {old} -> {new}   ({n} 件)")
            total += n

    # 补辅助方法（放在最后的 } 之前）
    if "registerUltraHeavyArmor" not in text.split("private ModItems()")[0]:
        pass
    if "return ITEMS.register(name, () -> new UltraHeavyArmorItem(" not in text:
        idx = text.rstrip().rfind("}")
        text = text[:idx].rstrip() + "\n" + HELPERS + "}\n"
        print("  已追加 3 个辅助方法")

    SRC.write_text(text, encoding="utf-8")
    print(f"  共替换 {total} 件")


if __name__ == "__main__":
    main()
