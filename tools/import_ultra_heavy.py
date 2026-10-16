#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""导入超重型甲的贴图。

素材来自作者提供的「第二次更新（产物）」目录，命名是中文，
这里按标识符重命名后放进项目。

注意两点：
  1. 四张胸甲图标是真 32×32（不是 16×16 放大），按作者要求**原样保留**，
     不做缩放 —— Minecraft 会在物品栏格子里自动缩到 16×16 显示。
  2. .bbmodel 是 Blockbench 的作图参考文件（resolution 64×32，与穿戴贴图一致），
     标准盔甲用不上，不导入。
"""

from __future__ import annotations

import shutil
from pathlib import Path

from PIL import Image

PROJ = Path(__file__).resolve().parent.parent
SRC = Path(r"C:\Users\LENOVO\Desktop\第二次更新（产物）")
ITEM = PROJ / "src/main/resources/assets/armor_addon/textures/item"
ARMOR = PROJ / "src/main/resources/assets/armor_addon/textures/models/armor"

# 中文目录名 -> 标识符前缀
SETS = {
    "金制超重型盔甲": "gold_ultra_heavy",
    "铁制超重型盔甲": "iron_ultra_heavy",
    "钻石超重型盔甲": "diamond_ultra_heavy",
    "合金超重型盔甲": "alloy_ultra_heavy",
}

ICONS = {
    "头盔": "helmet",
    "胸甲": "chestplate",
    "护腿": "leggings",
    "靴子": "boots",
}

# 合金胸甲的文件名里带了「（新）」，单独列出
ICON_OVERRIDE = {
    ("合金超重型盔甲", "chestplate"): "合金超重型胸甲（新）.png",
}


def main() -> None:
    ITEM.mkdir(parents=True, exist_ok=True)
    ARMOR.mkdir(parents=True, exist_ok=True)

    print("=== 物品图标 ===")
    for cn_dir, prefix in SETS.items():
        for cn_piece, en_piece in ICONS.items():
            fname = ICON_OVERRIDE.get((cn_dir, en_piece), f"{cn_dir[:-2]}{cn_piece}.png")
            src = SRC / cn_dir / fname
            if not src.exists():
                # 退一步：按通配找
                cands = list((SRC / cn_dir).glob(f"*{cn_piece}*.png"))
                if not cands:
                    print(f"  !! 找不到 {cn_dir}/{fname}")
                    continue
                src = cands[0]
            dst = ITEM / f"{prefix}_{en_piece}.png"
            shutil.copy2(src, dst)
            im = Image.open(dst).convert("RGBA")
            px = list(im.getdata())
            semi = sum(1 for c in px if 0 < c[3] < 255)
            note = "  <- 非 16x16，按要求保留" if im.size != (16, 16) else ""
            print(f"  {dst.name:<38} {str(im.size):<10} colors={len(set(px)):<5} semi={semi}{note}")

    print()
    print("=== 穿戴贴图 ===")
    for cn_dir, prefix in SETS.items():
        for layer in ("1", "2"):
            src = SRC / cn_dir / f"{cn_dir}_layer_{layer}.png"
            if not src.exists():
                cands = list((SRC / cn_dir).glob(f"*layer_{layer}.png"))
                if not cands:
                    print(f"  !! 找不到 {cn_dir} layer_{layer}")
                    continue
                src = cands[0]
            dst = ARMOR / f"{prefix}_layer_{layer}.png"
            shutil.copy2(src, dst)
            im = Image.open(dst).convert("RGBA")
            px = list(im.getdata())
            semi = sum(1 for c in px if 0 < c[3] < 255)
            print(f"  {dst.name:<38} {str(im.size):<10} colors={len(set(px)):<5} semi={semi}")

    print()
    print("=== 汇总 ===")
    n_item = len(list(ITEM.glob("*ultra_heavy*.png")))
    n_armor = len(list(ARMOR.glob("*ultra_heavy*.png")))
    print(f"  物品图标 {n_item} / 16")
    print(f"  穿戴贴图 {n_armor} / 8")


if __name__ == "__main__":
    main()
