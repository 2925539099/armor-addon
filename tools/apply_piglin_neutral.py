#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""把金制与合金制的 16 件盔甲改用 PiglinNeutralArmorItem。

NeoForge 的 makesPiglinsNeutral 默认实现写死只认 ArmorMaterials.GOLD，
所以本模组的金制甲与合金甲需要靠自定义 Item 类覆盖它，才能让猪灵中立。
"""

from __future__ import annotations

import re
from pathlib import Path

PROJ = Path(__file__).resolve().parent.parent
SRC = PROJ / "src/main/java/com/armoraddon/item/ModItems.java"

# 需要改用的套装前缀 -> 是否带抗火
TARGETS = {
    "gold_lining": False,
    "gold_heavy": False,
    "alloy_lining": True,
    "alloy_heavy": True,
}

NEW_NORMAL = "registerPiglinNeutralArmor"
NEW_FIRE = "registerPiglinNeutralFireResistantArmor"

HELPERS = '''
	/** 猪灵中立版盔甲（金制与合金制使用）。 */
	private static DeferredItem<Item> registerPiglinNeutralArmor(String name, Holder<ArmorMaterial> material,
																 ArmorItem.Type type, int durability) {
		return ITEMS.register(name, () -> new PiglinNeutralArmorItem(
				material,
				type,
				new Item.Properties().durability(durability)));
	}

	/** 同上，但额外带抗火。 */
	private static DeferredItem<Item> registerPiglinNeutralFireResistantArmor(String name, Holder<ArmorMaterial> material,
																			   ArmorItem.Type type, int durability) {
		return ITEMS.register(name, () -> new PiglinNeutralArmorItem(
				material,
				type,
				new Item.Properties().fireResistant().durability(durability)));
	}
'''


def main() -> None:
    src = SRC.read_text(encoding="utf-8")
    changes = []

    for prefix, fire in TARGETS.items():
        old_fn = "registerFireResistantArmor" if fire else "registerArmorWithDurability"
        new_fn = NEW_FIRE if fire else NEW_NORMAL

        # 匹配： old_fn("prefix_xxx", ...
        pattern = re.compile(rf'\b{old_fn}\("{re.escape(prefix)}_')
        found = pattern.findall(src)
        if found:
            src = pattern.sub(f'{new_fn}("{prefix}_', src)
            changes.append((prefix, old_fn, new_fn, len(found)))

    # 插入两个辅助方法（放在 registerFireResistantArmor 之后）
    if NEW_NORMAL not in src.split("private static DeferredItem<Item> registerArmor")[0]:
        anchor = "\tprivate ModItems() {"
        if anchor in src and "registerPiglinNeutralArmor" not in src:
            src = src.replace(anchor, HELPERS.lstrip("\n") + "\n" + anchor, 1)
            changes.append(("(辅助方法)", "-", "已插入 2 个", 1))

    SRC.write_text(src, encoding="utf-8")

    print("  替换明细：")
    for prefix, o, n, c in changes:
        print(f"    {prefix:<16} {o:<32} -> {n:<38} {c} 处")

    # 复查
    print()
    print("  复查：各套装当前的注册函数")
    for prefix in list(TARGETS) + ["iron_lining", "iron_heavy", "diamond_lining",
                                   "diamond_heavy", "wood", "wool"]:
        m = re.search(rf'(\w+)\("{re.escape(prefix)}_[a-z]+"', src)
        fn = m.group(1) if m else "?"
        flag = " <-- 猪灵中立" if "PiglinNeutral" in fn else ""
        print(f"    {prefix:<16} {fn}{flag}")


if __name__ == "__main__":
    main()
