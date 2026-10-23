#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""写入 1.3.4 条目：移除合金超重型甲的锻造台捷径。

1.3.4 尚未发布，所以这是正经的新条目，历史条目不碰。
"""

from __future__ import annotations

from pathlib import Path

PROJ = Path(__file__).resolve().parent.parent
CL = PROJ / "CHANGELOG.md"

ENTRY = """## [1.3.4]

### 平衡

- **移除合金超重型甲的锻造台升级路线。**

  原先每件合金超重型甲有两条获取路线：

  | 路线 | 配方 | 成本 |
  | --- | --- | --- |
  | **工作台** | `BWB / WAW / IWI`<br>B 下界合金块、A 合金重型甲、I 下界合金锭、W 羊毛 | 4 块 + 4 锭 + 4 羊毛，且要先做出合金重型甲 |
  | 锻造台 | 钻石超重型甲 + 下界合金块 + 盔甲升级模板 | **1 块 + 1 模板** |

  锻造那条既便宜得多，又跳过了「钻石重型甲 → 合金重型甲」整个层级 ——
  等于从钻石超重型甲一步登顶，绕过了本该付出的一整条升级链。

  现在删掉对应的四个 `alloy_ultra_heavy_*_smithing.json`，
  **合金超重型甲只能由工作台合成**。

  配方总数 69 → 65。钻石超重型甲本身不受影响，仍由工作台从钻石重型甲升级得到。

## [1.3.3]"""


def main() -> None:
    text = CL.read_text(encoding="utf-8")

    if "## [1.3.4]" not in text:
        text = text.replace("## [1.3.3]", ENTRY, 1)
        print("  已插入 1.3.4 条目")
    else:
        print("  1.3.4 已存在，跳过")

    if "[1.3.4]: " not in text:
        link = "[1.3.4]: https://github.com/2925539099/armor-addon/compare/1.3.3...1.3.4\n[1.3.3]: "
        text = text.replace("[1.3.3]: ", link, 1)
        print("  已补对比链接")

    CL.write_text(text, encoding="utf-8")

    print()
    print("  章节顺序:")
    for i, line in enumerate(text.splitlines(), 1):
        if line.startswith("## ["):
            print(f"    {i:4}: {line}")
            if i > 90:
                break


if __name__ == "__main__":
    main()
