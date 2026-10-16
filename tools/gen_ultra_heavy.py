#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""生成超重型甲的资源文件：物品模型、合成配方、语言条目。

配方分两类：
  1. 工作台（16 条，有序）—— 按作者给的布局：
         [金属块] [羊毛]   [金属块]
         [羊毛]   [重型甲] [羊毛]
         [金属锭] [羊毛]   [金属锭]
  2. 锻造台（4 条）—— 钻石超重型 + 下界合金块 + 盔甲升级模板 → 合金超重型，
     与既有的「钻石重型 → 合金重型」路线同构。
"""

from __future__ import annotations

import json
from pathlib import Path

PROJ = Path(__file__).resolve().parent.parent
ASSETS = PROJ / "src/main/resources/assets/armor_addon"
RECIPES = PROJ / "src/main/resources/data/armor_addon/recipe"
NS = "armor_addon"

PIECES = ["helmet", "chestplate", "leggings", "boots"]
CN_PIECE = {"helmet": "头盔", "chestplate": "胸甲", "leggings": "护腿", "boots": "靴子"}
EN_PIECE = {"helmet": "Helmet", "chestplate": "Chestplate", "leggings": "Leggings", "boots": "Boots"}

# 前缀、中文前缀、英文前缀、基底（重型甲前缀）、金属块、金属锭
SETS = [
    ("gold_ultra_heavy",    "金制超重型", "Ultra Heavy Golden",  "gold_heavy",
     "minecraft:gold_block",      "minecraft:gold_ingot"),
    ("iron_ultra_heavy",    "铁制超重型", "Ultra Heavy Iron",    "iron_heavy",
     "minecraft:iron_block",      "minecraft:iron_ingot"),
    ("diamond_ultra_heavy", "钻石超重型", "Ultra Heavy Diamond", "diamond_heavy",
     "minecraft:diamond_block",   "minecraft:diamond"),
    ("alloy_ultra_heavy",   "合金超重型", "Ultra Heavy Alloy",   "alloy_heavy",
     "minecraft:netherite_block", "minecraft:netherite_ingot"),
]


def main() -> None:
    print("=== 1. 物品模型 ===")
    n = 0
    for pre, *_ in SETS:
        for p in PIECES:
            f = ASSETS / "models/item" / f"{pre}_{p}.json"
            f.write_text(json.dumps(
                {"parent": "minecraft:item/generated",
                 "textures": {"layer0": f"{NS}:item/{pre}_{p}"}},
                ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
            n += 1
    print(f"  {n} 个")

    print()
    print("=== 2. 工作台配方（有序）===")
    n = 0
    for pre, cn, en, base, block, ingot in SETS:
        for p in PIECES:
            r = {
                "type": "minecraft:crafting_shaped",
                "category": "equipment",
                "key": {
                    "B": {"item": block},
                    "W": {"tag": "minecraft:wool"},
                    "A": {"item": f"{NS}:{base}_{p}"},
                    "I": {"item": ingot},
                },
                "pattern": ["BWB", "WAW", "IWI"],
                "result": {"count": 1, "id": f"{NS}:{pre}_{p}"},
            }
            (RECIPES / f"{pre}_{p}.json").write_text(
                json.dumps(r, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
            n += 1
        print(f"  {cn}甲: {block} / 羊毛 / {ingot} / {base}_*")
    print(f"  {n} 条")

    print()
    print("=== 3. 锻造台配方（钻石超重型 → 合金超重型）===")
    n = 0
    for p in PIECES:
        r = {
            "type": "minecraft:smithing_transform",
            "template": {"item": f"{NS}:armor_upgrade_template"},
            "base": {"item": f"{NS}:diamond_ultra_heavy_{p}"},
            "addition": {"item": "minecraft:netherite_block"},
            "result": {"count": 1, "id": f"{NS}:alloy_ultra_heavy_{p}"},
        }
        (RECIPES / f"alloy_ultra_heavy_{p}_smithing.json").write_text(
            json.dumps(r, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        n += 1
    print(f"  {n} 条")

    print()
    print("=== 4. 语言条目 ===")
    for fname, lang in (("zh_cn.json", "cn"), ("en_us.json", "en")):
        fp = ASSETS / "lang" / fname
        d = json.loads(fp.read_text(encoding="utf-8"))
        before = len(d)
        for pre, cn, en, *_ in SETS:
            for p in PIECES:
                key = f"item.{NS}.{pre}_{p}"
                d[key] = f"{cn}{CN_PIECE[p]}" if lang == "cn" else f"{en} {EN_PIECE[p]}"
        fp.write_text(json.dumps(d, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"  {fname}: {before} -> {len(d)} 条")

    print()
    print("=== 5. 抽查配方 ===")
    f = RECIPES / "gold_ultra_heavy_chestplate.json"
    for line in f.read_text(encoding="utf-8").splitlines():
        print("    " + line)


if __name__ == "__main__":
    main()
