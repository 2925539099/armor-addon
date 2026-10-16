#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""写入 1.3.3 条目：重量惩罚改为写在物品属性上。

1.3.2 已发布，所以它的条目内容保持当时的样子（事件实现），
这次的改动单独记在 1.3.3。
"""

from __future__ import annotations

from pathlib import Path

PROJ = Path(__file__).resolve().parent.parent
CL = PROJ / "CHANGELOG.md"

# 1.3.2 当时是事件实现，把之前误改的描述还原
# 注意：这里必须用全角减号「−」（U+2212），CHANGELOG 里写的是这个
WRONG_DESC = """- **超重型甲的重量惩罚。** 按材质每件降低移动速度，**写在物品的默认属性修饰符里**，
  所以物品提示会像药水那样列出「当穿戴时：\u22127.5% 移动速度」，由 Minecraft 在穿脱时
  自动施加与移除，不依赖任何每 tick 的事件。"""

RIGHT_DESC = """- **超重型甲的重量惩罚。** 按材质每件降低移动速度。"""

ENTRY = """## [1.3.3]

### 变更

- **重量惩罚改为写在物品的默认属性修饰符里。** 1.3.2 是用 `PlayerTickEvent`
  每 tick 手动追加修饰符实现的 —— 效果对，但**玩家在物品栏里看不到任何说明**。

  现在改成重写 `ArmorItem#getDefaultAttributeModifiers`，在它原本返回的
  护甲值 / 韧性 / 击退抗性之上追加一个移动速度修饰符：

  ```java
  return base.withModifierAdded(
          Attributes.MOVEMENT_SPEED,
          new AttributeModifier(WEIGHT_ID, -slow,
                  AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
          EquipmentSlotGroup.bySlot(getType().getSlot()));
  ```

  穿脱时由游戏自动施加与移除，物品提示会像药水那样显示
  「当穿戴时：-7.5% 移动速度」。减速幅度与 1.3.2 完全一致
  （金 7.5% / 铁 5% / 钻石 2.5% / 合金 10%，每件累加）。

  配套改动：

  | | 内容 |
  | --- | --- |
  | 新增 | `UltraHeavyArmorItem`（铁制 / 钻石制）、`PiglinNeutralUltraHeavyArmorItem`（金制 / 合金制，多带猪灵中立） |
  | 删除 | `UltraHeavySlowEvents`（旧的每 tick 事件）及其在主类里的注册 |
  | 改动 | 16 件超重型甲换用新类 |

## [1.3.2]"""


def main() -> None:
    text = CL.read_text(encoding="utf-8")

    if WRONG_DESC in text:
        text = text.replace(WRONG_DESC, RIGHT_DESC, 1)
        print("  1.3.2 条目已还原为当时的描述（事件实现）")

    if "## [1.3.3]" not in text:
        text = text.replace("## [1.3.2]", ENTRY, 1)
        print("  已插入 1.3.3 条目")

    if "[1.3.3]: " not in text:
        link_new = "[1.3.3]: https://github.com/2925539099/armor-addon/compare/1.3.2...1.3.3\n[1.3.2]: "
        text = text.replace("[1.3.2]: ", link_new, 1)
        print("  已补对比链接")

    CL.write_text(text, encoding="utf-8")

    print()
    print("  章节顺序:")
    for i, line in enumerate(text.splitlines(), 1):
        if line.startswith("## ["):
            print(f"    {i:4}: {line}")


if __name__ == "__main__":
    main()
