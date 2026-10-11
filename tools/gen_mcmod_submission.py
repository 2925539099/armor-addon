#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""生成 MC百科提交材料。

百科的资料录入分两部分：
  1. 基础数据（名称 / 原名 / 耐久 / 叠加 / 图标）—— 走 LMS 导出的 JSON 批量导入；
  2. 介绍文本与合成表 —— 百科没有批量接口，只能逐个填。

本脚本把第 2 部分按录入顺序整理成一份文档，让编辑照着一次填完，
不必再回源码里翻。

数据来源全部是项目自身的文件，不手工维护第二份数据：
  ModArmorMaterials.java  护甲值 / 韧性 / 附魔亲和度 / 击退抗性 / 音效
  ModItems.java           每件盔甲的耐久
  lang/*.json             中英文名称
  data/armor_addon/recipe 全部配方
"""

from __future__ import annotations

import json
import re
from pathlib import Path

PROJ = Path(__file__).resolve().parent.parent
JAVA = PROJ / "src/main/java/com/armoraddon"
ASSETS = PROJ / "src/main/resources/assets/armor_addon"
RECIPES = PROJ / "src/main/resources/data/armor_addon/recipe"

BT = chr(96)          # 反引号，避免在源码里直接写
NS = "armor_addon"


# ---------------------------------------------------------------- 材质

def parse_materials() -> dict[str, dict]:
    """从 ModArmorMaterials.java 解析每个材质的属性。"""
    src = (JAVA / "item/ModArmorMaterials.java").read_text(encoding="utf-8")
    out: dict[str, dict] = {}

    # public static final DeferredHolder<...> NAME = ARMOR_MATERIALS.register(
    #         "id",
    #         () -> new ArmorMaterial(
    #                 Map.of(...),          <- 有嵌套括号，所以必须靠结尾的 F)); 定位
    #                 附魔, 音效, 修复材料, List.of(...), 韧性F, 击退F));
    for m in re.finditer(
        r'DeferredHolder<ArmorMaterial, ArmorMaterial>\s+(\w+)\s*=\s*ARMOR_MATERIALS\.register\(\s*'
        r'"([a-z_]+)"\s*,\s*\(\)\s*->\s*new\s+ArmorMaterial\((.*?[\d.]+F)\)\);',
        src, re.S):
        field, mat_id, body = m.group(1), m.group(2), m.group(3)

        # 参数之间夹着 // 注释行，会挡住正则，先剥掉再做字段解析。
        body = re.sub(r'//[^\n]*', '', body)

        defense = {}
        for d in re.finditer(r'ArmorItem\.Type\.(\w+),\s*(\d+)', body):
            defense[d.group(1)] = int(d.group(2))

        # 附魔亲和度是 Map.of(...) 之后的第一个独立整数。
        enchant = re.search(r'\),\s*(\d+)\s*,', body, re.S)
        sound = re.search(r'SoundEvents\.(\w+)', body)
        repair = re.search(r'Ingredient\.of\(Items\.(\w+)\)', body)
        # 韧性与击退抗性是参数表最后两项，写作 x.xF
        floats = re.findall(r'([\d.]+)F', body)

        out[mat_id] = {
            "field": field,
            "defense": defense,
            "enchant": int(enchant.group(1)) if enchant else None,
            "sound": sound.group(1) if sound else None,
            "repair": repair.group(1) if repair else None,
            "toughness": float(floats[-2]) if len(floats) >= 2 else None,
            "knockback": float(floats[-1]) if len(floats) >= 2 else None,
        }
    return out


def parse_items(materials: dict[str, dict]) -> list[dict]:
    """从 ModItems.java 解析每个物品的材质、部位与耐久。"""
    src = (JAVA / "item/ModItems.java").read_text(encoding="utf-8")
    lang_cn = json.loads((ASSETS / "lang/zh_cn.json").read_text(encoding="utf-8"))
    lang_en = json.loads((ASSETS / "lang/en_us.json").read_text(encoding="utf-8"))

    items: list[dict] = []

    # 盔甲物品统一按参数模式匹配，不依赖具体是哪个 register 辅助方法：
    #   "名字", ModArmorMaterials.材质, ArmorItem.Type.部位, 耐久
    # 耐久可能是字面数字，也可能是 _DURABILITY_MULTIPLIER 常量。
    for m in re.finditer(
        r'"([a-z_]+)"\s*,\s*ModArmorMaterials\.(\w+)\s*,\s*'
        r'ArmorItem\.Type\.(\w+)\s*,\s*(\w+)\s*\)', src):
        name, mat_field, slot, dur_arg = m.group(1), m.group(2), m.group(3), m.group(4)
        dur = int(dur_arg) if dur_arg.isdigit() else None
        if dur is None:
            # 常量名形如 WOOL_DURABILITY_MULTIPLIER，去 ModItems 顶部找它的值
            cm = re.search(rf'int\s+{re.escape(dur_arg)}\s*=\s*(\d+)', src)
            if cm:
                base = {"HELMET": 11, "CHESTPLATE": 16, "LEGGINGS": 15, "BOOTS": 13}[slot]
                dur = base * int(cm.group(1))
        items.append({"id": name, "field": mat_field, "slot": slot, "durability": dur})

    # 羊毛靴子走自定义类 WoolBootsItem，上面那条匹配不到，单独补
    if not any(i["id"] == "wool_boots" for i in items):
        m = re.search(r'ITEMS\.register\("wool_boots"[\s\S]{0,500}?'
                      r'ArmorItem\.Type\.(\w+)\.getDurability\((\w+)\)', src)
        if m:
            slot, mult_arg = m.group(1), m.group(2)
            cm = re.search(rf'int\s+{re.escape(mult_arg)}\s*=\s*(\d+)', src)
            base = {"HELMET": 11, "CHESTPLATE": 16, "LEGGINGS": 15, "BOOTS": 13}[slot]
            items.append({"id": "wool_boots", "field": "WOOL", "slot": slot,
                          "durability": base * int(cm.group(1)) if cm else None})

    # 非盔甲物品：ITEMS.register("x", () -> new Item(...))
    for m in re.finditer(r'ITEMS\.register\("([a-z_]+)"\s*,\s*\(\)\s*->\s*new\s+Item\(', src):
        items.append({"id": m.group(1), "field": None, "slot": None, "durability": 0})

    for it in items:
        key = f"item.{NS}.{it['id']}"
        it["cn"] = lang_cn.get(key, it["id"])
        it["en"] = lang_en.get(key, it["id"])
        mat = next((v for v in materials.values() if v["field"] == it["field"]), None)
        it["material"] = mat
        it["material_id"] = next((k for k, v in materials.items() if v["field"] == it["field"]), None)

    items.sort(key=lambda x: (x["material_id"] or "", x["slot"] or ""))
    return items


# ---------------------------------------------------------------- 配方

SLOT_CN = {"HELMET": "头盔", "CHESTPLATE": "胸甲", "LEGGINGS": "护腿", "BOOTS": "靴子"}
SLOT_EN = {"HELMET": "Helmet", "CHESTPLATE": "Chestplate", "LEGGINGS": "Leggings", "BOOTS": "Boots"}


def short(name: str, lang_cn: dict) -> str:
    """把 minecraft:iron_ingot 之类转成中文名。"""
    if ":" in name:
        ns, path = name.split(":", 1)
    else:
        ns, path = "minecraft", name
    if ns == NS:
        return lang_cn.get(f"item.{NS}.{path}", path)
    vanilla = {
        "iron_ingot": "铁锭", "gold_ingot": "金锭", "diamond": "钻石",
        "netherite_ingot": "下界合金锭", "chain": "锁链", "torch": "火把",
        "coal_block": "煤炭块", "bucket": "铁桶",
        "iron_block": "铁块", "gold_block": "金块", "diamond_block": "钻石块",
        "netherite_block": "下界合金块", "wool": "羊毛（任意）", "logs": "原木（任意）",
        "netherite_upgrade_smithing_template": "下界合金升级模板",
        "iron_helmet": "铁头盔", "iron_chestplate": "铁胸甲", "iron_leggings": "铁护腿", "iron_boots": "铁靴子",
        "golden_helmet": "金头盔", "golden_chestplate": "金胸甲", "golden_leggings": "金护腿", "golden_boots": "金靴子",
        "diamond_helmet": "钻石头盔", "diamond_chestplate": "钻石胸甲", "diamond_leggings": "钻石护腿", "diamond_boots": "钻石靴子",
        "netherite_helmet": "下界合金头盔", "netherite_chestplate": "下界合金胸甲",
        "netherite_leggings": "下界合金护腿", "netherite_boots": "下界合金靴子",
    }
    return vanilla.get(path, path)


def parse_recipes(lang_cn: dict) -> list[dict]:
    out = []
    for f in sorted(RECIPES.glob("*.json")):
        d = json.loads(f.read_text(encoding="utf-8"))
        t = d.get("type", "")
        result = d["result"]["id"].replace(f"{NS}:", "")
        rec = {
            "file": f.name,
            "kind": t,
            "result_id": result,
            "result_cn": lang_cn.get(f"item.{NS}.{result}", result),
            "count": d["result"].get("count", 1),
        }
        if t == "minecraft:crafting_shapeless":
            rec["shape"] = "无序"
            rec["materials"] = [short(i.get("item") or i.get("tag") or "?", lang_cn)
                                for i in d.get("ingredients", [])]
        elif t == "minecraft:crafting_shaped":
            rec["shape"] = "有序"
            key = {k: short(v.get("item") or v.get("tag") or "?", lang_cn)
                   for k, v in d.get("key", {}).items()}
            rec["pattern"] = d.get("pattern", [])
            rec["key"] = key
            # 实际用到的材料（去重，保持出现顺序）
            used = []
            for row in d.get("pattern", []):
                for ch in row:
                    if ch != " " and ch in key and key[ch] not in used:
                        used.append(key[ch])
            rec["materials"] = used
        elif t == "minecraft:smithing_transform":
            rec["shape"] = "锻造台"
            rec["template"] = short(d["template"]["item"], lang_cn)
            rec["base"] = short(d["base"]["item"], lang_cn)
            rec["addition"] = short(d["addition"]["item"], lang_cn)
        out.append(rec)
    return out


# ---------------------------------------------------------------- 介绍文本

SET_INTRO = {
    "wood": (
        "木制盔甲是模组里最容易入手的入门套装，用任意原木即可合成。"
        "护甲值与皮革盔甲相同（1/3/2/1），但耐久只有铁盔甲的一半，附魔亲和度也最低。"
        "它不属于羊毛系，因此没有音波抗性。"
    ),
    "wool": (
        "羊毛盔甲是整个羊毛系的起点。护甲值等同皮革，耐久比皮革更高，附魔亲和度 15。"
        "穿着羊毛靴子移动时不会产生振动，幽匿感测体与坚守者无法通过脚步声发现你。"
        "四种内衬甲都必须用对应的羊毛盔甲作为材料合成，因此这一套是后续所有护甲的基础。"
    ),
    "iron_lining": (
        "铁制内衬甲由羊毛盔甲与铁盔甲、锁链一起合成。护甲值略高于铁盔甲，韧性 2.0，"
        "并保留了羊毛系的音波抗性；铁制内衬甲合成时还会继承所用铁盔甲的附魔与自定义名称，"
        "耐久也同步损耗，因此可以先把好附魔打在铁盔甲上再转过来。"
    ),
    "gold_lining": (
        "金制内衬甲由羊毛盔甲与金盔甲、锁链一起合成。护甲值略高于金盔甲，韧性 2.0，"
        "附魔亲和度 25，是全模组最容易附出好属性的套装，代价是耐久偏低。"
        "同样具备羊毛系的音波抗性。"
    ),
    "diamond_lining": (
        "钻石内衬甲由羊毛盔甲与钻石头盔、锁链一起合成。护甲值略高于钻石盔甲，韧性 4.0，"
        "耐久与附魔亲和度也和钻石盔甲相当。它是合成钻石重型甲与合金重型甲的必经之路，"
        "并具备羊毛系的音波抗性。"
    ),
    "alloy_lining": (
        "合金内衬甲由羊毛盔甲与下界合金盔甲、锁链一起合成，耐久按下界合金倍率写定，"
        "韧性 5.0、击退抗性 0.1，并且和原版下界合金盔甲一样带抗火，物品不会被火烧毁。"
        "它既可以直接在锻造台升级为合金重型甲，也可以经由钻石重型甲升级。"
    ),
    "iron_heavy": (
        "铁制重型甲由铁制内衬甲在锻造台升级而来。护甲值在内衬甲基础上按部位 +2/+4/+3/+2，"
        "耐久提升 50%，韧性与附魔亲和度保持不变。与内衬甲一样具备羊毛系的音波抗性。"
    ),
    "gold_heavy": (
        "金制重型甲由金制内衬甲在锻造台升级而来。护甲值在内衬甲基础上按部位 +2/+4/+3/+2，"
        "耐久提升 50%，韧性 2.0 与附魔亲和度 25 保持不变，是性价比最高的重型甲。"
    ),
    "diamond_heavy": (
        "钻石重型甲由钻石内衬甲在锻造台升级而来。护甲值在内衬甲基础上按部位 +2/+4/+3/+2，"
        "耐久提升 50%，韧性 4.0 与附魔亲和度 10 保持不变。它也是升级为合金重型甲的基底之一。"
    ),
    "alloy_heavy": (
        "合金重型甲是本模组护甲值最高的套装，可以用合金内衬甲或钻石重型甲在锻造台升级得到，"
        "两条路线都需要下界合金块。护甲值在内衬甲基础上按部位 +2/+4/+3/+2，耐久提升 50%，"
        "韧性 5.0、击退抗性 0.1，并保留抗火。"
    ),
    "template": (
        "盔甲升级模板是锻造台用的模板，在普通工作台上用铁锭、火把、煤炭块和铁桶合成。"
        "它是四种重型甲升级配方的必需品，把它放进锻造台的模板槽，"
        "再放入对应的内衬甲与矿物块，即可升级为重型甲。"
        "注意每次升级都会消耗一个模板，与基底和添加物一样，因此升级一整套四件需要四个模板。"
    ),
}

SLOT_NOTE = {
    "helmet": "头盔部位，护甲值加成 +2。",
    "chestplate": "胸甲部位，护甲值加成 +4，是四件中提升最多的一件。",
    "leggings": "护腿部位，护甲值加成 +3。",
    "boots": "靴子部位，护甲值加成 +2。",
}


def intro_for(it: dict) -> str:
    mid = it["material_id"]
    base = SET_INTRO.get(mid if mid else "template", "")
    if it["slot"]:
        slot = SLOT_NOTE.get(it["slot"].lower(), "")
        return f"{base}\n\n本件为{slot}"
    return base


# ---------------------------------------------------------------- 输出

def main() -> None:
    materials = parse_materials()
    items = parse_items(materials)
    lang_cn = json.loads((ASSETS / "lang/zh_cn.json").read_text(encoding="utf-8"))
    recipes = parse_recipes(lang_cn)

    L: list[str] = []
    A = L.append

    A("# 盔甲附加 (Armor Addon) —— MC百科提交材料")
    A("")
    A("百科的资料录入分两部分：基础数据走 JSON 批量导入，介绍文本与合成表只能逐个填。")
    A("本文档是后者的完整内容，按录入顺序排好，照着填即可，不必回源码翻数据。")
    A("")
    A("| 项目 | 值 |")
    A("| --- | --- |")
    A("| 模组名称 | 盔甲附加 (Armor Addon) |")
    A("| 模组 ID | " + BT + NS + BT + " |")
    A("| 支持版本 | Minecraft 1.21.1 / NeoForge 21.1.244+ |")
    A("| 物品总数 | " + str(len(items)) + " |")
    A("| 配方总数 | " + str(len(recipes)) + " |")
    A("")
    A("---")
    A("")

    # ---- 论坛发帖模板 ----
    A("## 一、论坛发帖模板")
    A("")
    A("把下面的内容贴到「批量添加资料JSON收纳帖」，附件上传同目录的 "
      + BT + "armor_addon.json" + BT + "。")
    A("")
    A("```")
    A("模组名称：盔甲附加")
    A("模组站内链接：https://www.mcmod.cn/class/31593.html")
    A("模组版本：1.2.1")
    A("游戏版本：1.21.1-neoforge")
    A("导出工具：LMS")
    A("添加物品资料；本次新增 1 件锻造模板与 16 件重型甲，"
      "并已将全部 41 件物品的图标更新为 1.2.1 版本。")
    A("```")
    A("")
    A("> 百科没有合成表的批量接口，四种重型甲与盔甲升级模板的配方需在物品页面逐个添加，")
    A("> 配方内容见本文档第三节。")
    A("")
    A("---")
    A("")

    # ---- 物品介绍 ----
    A("## 二、物品介绍文本")
    A("")
    A("百科要求介绍不少于 30 个字。以下按套装排列，可直接粘贴到各物品页面的「编辑器」字段。")
    A("")
    cur = None
    for it in items:
        mid = it["material_id"] or "template"
        if mid != cur:
            cur = mid
            label = it["material"]["field"] if it["material"] else "盔甲升级模板"
            A("")
            A(f"### {cur}")
            A("")
        mat = it["material"]
        A(f"#### {it['cn']}（" + BT + f"{NS}:{it['id']}" + BT + "）")
        A("")
        if mat:
            d = mat["defense"]
            A("| 护甲值 | 耐久 | 韧性 | 附魔亲和度 | 击退抗性 |")
            A("| --- | --- | --- | --- | --- |")
            A(f"| {d.get('HELMET', '-')} / {d.get('CHESTPLATE', '-')} / {d.get('LEGGINGS', '-')} / {d.get('BOOTS', '-')} "
              f"（头/胸/腿/靴） | {it['durability']} | {mat['toughness']} | {mat['enchant']} | {mat['knockback']} |")
            A("")
        else:
            A("| 最大叠加 | 最大耐久 |")
            A("| --- | --- |")
            A("| 64 | 无 |")
            A("")
        A("介绍文本：")
        A("")
        for line in intro_for(it).split("\n"):
            A("> " + line if line else ">")
        A("")

    A("---")
    A("")

    # ---- 配方 ----
    A("## 三、合成表")
    A("")
    A("百科的合成表是图形界面，需在物品页面点「加合成表」，按下面的图示摆放并填写数量。")
    A("")
    shaped = [r for r in recipes if r["kind"] == "minecraft:crafting_shaped"]
    shapeless = [r for r in recipes if r["kind"] == "minecraft:crafting_shapeless"]
    smithing = [r for r in recipes if r["kind"] == "minecraft:smithing_transform"]

    A("### 3.1 工作台·有序合成")
    A("")
    for r in shaped:
        A(f"**{r['result_cn']}**（" + BT + f"{NS}:{r['result_id']}" + BT + "）—— 摆放要求：**有序**")
        A("")
        A("```")
        for row in r["pattern"]:
            cells = []
            for ch in row:
                cells.append("　空　" if ch == " " else f" {r['key'].get(ch, ch)} ")
            A("│" + "│".join(cells) + "│")
        A("```")
        A("")
        A("材料：" + "、".join(r["materials"]))
        A("")

    A("### 3.2 工作台·无序合成")
    A("")
    for r in shapeless:
        A(f"**{r['result_cn']}**（" + BT + f"{NS}:{r['result_id']}" + BT + "）—— 摆放要求：**无序**")
        A("")
        A("材料：" + "、".join(r["materials"]) + f"（共 {len(r['materials'])} 样，位置随意）")
        A("")

    A("### 3.3 锻造台")
    A("")
    A("锻造台配方在百科里是另一种合成表类型，选择「锻造台」后分三格填写。")
    A("")
    A("| 产物 | 模板 | 基底 | 添加物 |")
    A("| --- | --- | --- | --- |")
    for r in smithing:
        A(f"| {r['result_cn']} | {r['template']} | {r['base']} | {r['addition']} |")
    A("")

    A("---")
    A("")
    A("## 四、机制说明（供写模组介绍用）")
    A("")
    A("- **音波抗性**：羊毛盔甲及其升级的护甲（羊毛盔甲、四种内衬甲、四种重型甲，共九套）"
      "穿戴时受到音波类伤害每件减 1 点，穿满四件减 4 点。木制盔甲不生效。")
    A("- **移动静音**：穿羊毛靴子时脚步、游泳、落地不产生振动，"
      "幽匿感测体与坚守者无法靠声音发现你。")
    A("- **附魔继承**：四种内衬甲合成时会继承所用原版盔甲的附魔与自定义名称，耐久也同步损耗。")
    A("")

    out = Path(__file__).resolve().parent / "mcmod-export"
    out.mkdir(parents=True, exist_ok=True)
    target = out / "MC百科提交材料.md"
    target.write_text("\n".join(L), encoding="utf-8")

    print("  已写出 " + str(target.relative_to(PROJ)))
    print("    物品 " + str(len(items)) + " 件")
    print("    配方 " + str(len(recipes)) + " 条"
          "（有序 " + str(len(shaped)) + " / 无序 " + str(len(shapeless))
          + " / 锻造 " + str(len(smithing)) + "）")
    print("    文件 " + str(len("\n".join(L))) + " 字符")


if __name__ == "__main__":
    main()
