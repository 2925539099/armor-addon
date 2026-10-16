#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""核对 LMS 格式的物品资料 JSON，并输出统计。

LMS（让我看看）按 mcmod 格式导出的物品资料，一行一个 JSON 对象，
字段固定为下面这些。这个脚本逐行查字段是否齐全、类型对不对、
图标能不能解码、尺寸是否合理。

用法：python check_lms.py <物品json>
"""

from __future__ import annotations

import base64
import io
import json
import sys
from collections import Counter
from pathlib import Path

# LMS 导出的字段（顺序也正是它在文件里的顺序）
REQUIRED = [
    "name", "englishName", "registerName", "type",
    "maxStacksSize", "maxDurability", "CreativeTabName",
    "OredictList", "smallIcon", "largeIcon",
]

VALID_TYPES = {"Item", "Block", "ItemBlock", "Fluid", "Entity", "Effect"}


def png_size(raw: bytes):
    """从 PNG 字节里读出宽高（不依赖 Pillow，只要 IHDR）。"""
    if len(raw) < 24 or raw[:8] != b"\x89PNG\r\n\x1a\n":
        return None
    w = int.from_bytes(raw[16:20], "big")
    h = int.from_bytes(raw[20:24], "big")
    return (w, h)


def main() -> None:
    if len(sys.argv) < 2:
        raise SystemExit("用法: python check_lms.py <物品json>")

    path = Path(sys.argv[1])
    raw_lines = path.read_text(encoding="utf-8").splitlines()
    lines = [(i + 1, l) for i, l in enumerate(raw_lines) if l.strip()]

    print(f"  文件: {path.name}  ({path.stat().st_size / 1024:.1f} KB)")
    print(f"  总行数: {len(raw_lines)}    非空行: {len(lines)}")
    print()

    errors: list[str] = []
    warnings: list[str] = []

    type_counter: Counter[str] = Counter()
    tab_counter: Counter[str] = Counter()
    icon_small: Counter[tuple] = Counter()
    icon_large: Counter[tuple] = Counter()
    no_en: list[str] = []
    no_durability = 0
    registered: set[str] = set()
    dupes: list[str] = []

    for lineno, line in lines:
        try:
            obj = json.loads(line)
        except json.JSONDecodeError as e:
            errors.append(f"第 {lineno} 行不是合法 JSON: {e}")
            continue

        # 字段齐全
        missing = [k for k in REQUIRED if k not in obj]
        if missing:
            errors.append(f"第 {lineno} 行缺字段: {missing}")
            continue
        extra = [k for k in obj if k not in REQUIRED]
        if extra:
            warnings.append(f"第 {lineno} 行多了字段 {extra}")

        name = obj["name"]
        reg = obj["registerName"]
        tag = reg or name or f"#{lineno}"

        # 注册名
        if ":" not in reg:
            errors.append(f"{tag}: registerName 不是 命名空间:id 形式")
        if reg in registered:
            dupes.append(reg)
        registered.add(reg)

        # 类型
        type_counter[obj["type"]] += 1
        if obj["type"] not in VALID_TYPES:
            warnings.append(f"{tag}: type={obj['type']!r} 不在常见取值里")

        # 数值
        if not isinstance(obj["maxStacksSize"], int):
            errors.append(f"{tag}: maxStacksSize 不是整数")
        dur = obj["maxDurability"]
        if not isinstance(dur, int):
            errors.append(f"{tag}: maxDurability 不是整数")
        elif dur == 0:
            no_durability += 1

        # 创造模式物品栏
        tab_counter[obj["CreativeTabName"]] += 1

        # OredictList 是个 JSON 字符串
        try:
            parsed = json.loads(obj["OredictList"])
            if not isinstance(parsed, list):
                warnings.append(f"{tag}: OredictList 解出来不是数组")
        except Exception:
            errors.append(f"{tag}: OredictList 不是合法的 JSON 字符串")

        # 英文名
        if not obj["englishName"].strip():
            no_en.append(reg)

        # 图标
        for key, counter in (("smallIcon", icon_small), ("largeIcon", icon_large)):
            b64 = obj[key]
            try:
                data = base64.b64decode(b64, validate=True)
            except Exception as e:
                errors.append(f"{tag}: {key} base64 解码失败 ({e})")
                continue
            size = png_size(data)
            if size is None:
                errors.append(f"{tag}: {key} 不是合法 PNG")
                continue
            counter[size] += 1

    # ---- 汇总 ----
    print("  类型分布:")
    for t, n in type_counter.most_common():
        print(f"    {t:<10} {n}")
    print()

    print("  创造模式物品栏:")
    for t, n in tab_counter.most_common():
        print(f"    {t:<10} {n}")
    print()

    print("  图标尺寸:")
    print(f"    smallIcon: {dict(icon_small)}")
    print(f"    largeIcon: {dict(icon_large)}")
    print()

    print(f"  无耐久（maxDurability=0）: {no_durability}")
    print(f"  英文名为空: {len(no_en)}")
    if no_en:
        for r in no_en[:8]:
            print(f"    {r}")
    print(f"  注册名重复: {len(dupes)}")
    if dupes:
        for d in dupes[:8]:
            print(f"    {d}")
    print()

    if errors:
        print(f"  ❌ 错误 {len(errors)} 条:")
        for e in errors[:25]:
            print(f"      {e}")
        if len(errors) > 25:
            print(f"      ...还有 {len(errors)-25} 条")
    else:
        print("  ✅ 字段与结构全部合法")

    if warnings:
        print()
        print(f"  ⚠️  提示 {len(warnings)} 条:")
        for w in warnings[:12]:
            print(f"      {w}")

    print()
    print(f"  预计导入结果: I: {len(lines)}行执行完毕，成功添加 {len(lines)} 个。")
    sys.exit(1 if errors else 0)


if __name__ == "__main__":
    main()
