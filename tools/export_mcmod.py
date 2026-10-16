#!/usr/bin/env python3
"""生成 MC百科（mcmod.cn）可导入的物品资料 JSON。

背景
----
MC百科的资料不是手动一条条填的，而是用游戏内导出工具跑出 JSON，
再发到论坛专帖由百科编辑批量导入。

1.21.1 NeoForge 对应的导出工具是 [LMS] 让我看看
（https://github.com/xkball/LetMeSeeSee）。它「按 mcmod 格式导出」的实现见
`ItemDataExporterScreen#itemDataMcMod`，每个物品一行 JSON：

    {
      "name":            中文名,          // zh_cn 语言文件
      "englishName":     英文名,          // en_us 语言文件
      "registerName":    "命名空间:路径",
      "type":            "Item" | "Block",
      "maxStacksSize":   最大堆叠数,
      "maxDurability":   最大耐久,
      "CreativeTabName": 创造模式物品栏名,
      "OredictList":     "[tag1,tag2]",
      "smallIcon":       32x32 PNG 的 base64,
      "largeIcon":       128x128 PNG 的 base64
    }

文件的每一行是一个独立 JSON 对象，按 registerName 排序，文件名取命名空间。

本脚本不启动游戏，直接读本项目的 Java 注册代码与贴图 PNG 复现该格式。
图标放大用邻近插值（NEAREST）：游戏渲染时物品以 scale=min(w,h) 铺满整个画布，
而本模组的物品用 item/generated 平面模型，所以邻域放大与游戏内渲染结果一致。

用法
----
    python tools/export_mcmod.py

产出
----
    tools/mcmod-export/armor_addon.json
"""

from __future__ import annotations

import base64
import io
import json
import re
from pathlib import Path

from PIL import Image
from unicodedata import east_asian_width

PROJ = Path(__file__).resolve().parent.parent
JAVA = PROJ / "src/main/java/com/armoraddon/item/ModItems.java"
ASSETS = PROJ / "src/main/resources/assets/armor_addon"
LANG = ASSETS / "lang"
ITEM_TEX = ASSETS / "textures/item"
OUT_DIR = PROJ / "tools/mcmod-export"

NAMESPACE = "armor_addon"
CREATIVE_TAB = "战斗"          # 中文客户端的「战斗」物品栏
SMALL, LARGE = 32, 128

# 原版 ArmorItem.Type 的基础耐久（见 net.minecraft.world.item.ArmorItem.Type）
BASE_DURABILITY = {"HELMET": 11, "CHESTPLATE": 16, "LEGGINGS": 15, "BOOTS": 13}


def parse_items() -> list[dict]:
    """从 ModItems.java 解析出全部物品：注册名、部位、耐久。"""
    src = JAVA.read_text(encoding="utf-8")

    # 倍率常量，例如 WOOL_DURABILITY_MULTIPLIER = 10
    mult = {m.group(1): int(m.group(2))
            for m in re.finditer(r'int\s+([A-Z_]+_DURABILITY_MULTIPLIER)\s*=\s*(\d+)', src)}

    items: dict[str, dict] = {}

    # 盔甲物品统一按参数模式匹配，不依赖具体是哪个 register 辅助方法 ——
    # 之前写死函数名，导致新增 registerPiglinNeutralArmor 之类的包装后漏抓。
    #   "名字", ModArmorMaterials.材质, ArmorItem.Type.部位, 耐久
    # 材质参数含点号，所以用 [\w.]+；耐久可能是字面数字，也可能是 _DURABILITY_MULTIPLIER 常量。
    for m in re.finditer(
        r'"([a-z_]+)"\s*,\s*ModArmorMaterials\.[\w.]+\s*,\s*'
        r'ArmorItem\.Type\.(\w+)\s*,\s*(\w+)\s*\)', src):
        name, slot, arg = m.group(1), m.group(2), m.group(3)
        if arg.isdigit():
            dur = int(arg)
        elif arg in mult:
            dur = BASE_DURABILITY[slot] * mult[arg]
        else:
            continue
        items[name] = {"slot": slot, "durability": dur}

    # 羊毛靴子走自定义类：ITEMS.register("wool_boots", () -> new WoolBootsItem(...))，单独补
    if "wool_boots" not in items:
        m = re.search(r'ITEMS\.register\("(wool_boots)"[\s\S]{0,400}?'
                      r'ArmorItem\.Type\.(\w+)\.getDurability\((\w+)\)', src)
        if m:
            items[m.group(1)] = {"slot": m.group(2),
                                 "durability": BASE_DURABILITY[m.group(2)] * mult[m.group(3)]}

    # 非盔甲物品（例如锻造模板）：ITEMS.register("xxx", () -> new Item(...))。
    # 这类物品没有耐久，对应 LMS 里的 maxDurability 为 0。
    for m in re.finditer(r'ITEMS\.register\("([a-z_]+)"\s*,\s*\(\)\s*->\s*new\s+Item\(', src):
        name = m.group(1)
        if name not in items:
            items[name] = {"slot": "NONE", "durability": 0}

    out = []
    for name, info in sorted(items.items()):
        out.append({
            "id": name,
            "registerName": f"{NAMESPACE}:{name}",
            "slot": info["slot"],
            "durability": info["durability"],
        })
    return out


def pad(s: str, width: int, right: bool = False) -> str:
    """按显示宽度对齐。中文/日文/韩文在终端里占 2 列，不能按字符数算。"""
    w = sum(2 if east_asian_width(c) in "WF" else 1 for c in s)
    fill = " " * max(0, width - w)
    return fill + s if right else s + fill


def icon_b64(path: Path, size: int) -> str:
    """把 16x16 贴图邻域放大到 size，返回 PNG 的 base64。"""
    img = Image.open(path).convert("RGBA").resize((size, size), Image.NEAREST)
    buf = io.BytesIO()
    img.save(buf, format="PNG")
    return base64.b64encode(buf.getvalue()).decode("ascii")


def main() -> None:
    zh = json.loads((LANG / "zh_cn.json").read_text(encoding="utf-8"))
    en = json.loads((LANG / "en_us.json").read_text(encoding="utf-8"))

    items = parse_items()
    print(f"从 ModItems.java 解析到 {len(items)} 个物品\n")

    records, missing = [], []
    print(pad("注册名", 38) + pad("中文名", 18) + pad("英文名", 26) + pad("耐久", 6, True))
    print("-" * 88)
    for it in items:
        key = f"item.{NAMESPACE}.{it['id']}"
        cn, enname = zh.get(key), en.get(key)
        tex = ITEM_TEX / f"{it['id']}.png"
        if not cn or not enname:
            missing.append(f"语言文件缺少 {key}")
            continue
        if not tex.exists():
            missing.append(f"贴图缺失 {tex.name}")
            continue

        # 输出顺序与 LMS 的 itemDataMcMod 一致
        rec = {
            "name": cn,
            "englishName": enname,
            "registerName": it["registerName"],
            "type": "Item",
            # 有耐久的物品（盔甲）不可堆叠；普通物品默认堆叠 64。
            "maxStacksSize": 1 if it["durability"] > 0 else 64,
            "maxDurability": it["durability"],
            "CreativeTabName": CREATIVE_TAB,
            "OredictList": "[]",         # 本模组物品未挂任何 tag
            "smallIcon": icon_b64(tex, SMALL),
            "largeIcon": icon_b64(tex, LARGE),
        }
        records.append(rec)
        print(pad(it["registerName"], 38) + pad(cn, 18) + pad(enname, 26)
              + pad(str(it["durability"]), 6, True))

    if missing:
        print("\n!! 存在问题，已中止：")
        for m in missing:
            print("   " + m)
        raise SystemExit(1)

    records.sort(key=lambda r: r["registerName"])

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    out = OUT_DIR / f"{NAMESPACE}.json"
    # 每行一个 JSON 对象，与 LMS 导出的格式一致
    out.write_text("".join(json.dumps(r, ensure_ascii=False) + "\n" for r in records),
                   encoding="utf-8")

    kb = out.stat().st_size / 1024
    print(f"\n已写出 {out.relative_to(PROJ)}")
    print(f"  物品数 {len(records)}，文件大小 {kb:.1f} KB")
    print(f"  每行一个 JSON 对象，共 {len(records)} 行")


if __name__ == "__main__":
    main()
