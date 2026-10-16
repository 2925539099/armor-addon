#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""从作者的四套 bbmodel 中提取所有装饰方块的 texOffs，并与实际贴图交叉验证。

肩甲和膝盖护板都不是原版方块，UV 由作者在 Blockbench 里逐个指定，
四套之间并不一致，所以必须按套取，不能共用一份 LayerDefinition。

用法：python tools/extract_uv_table.py
"""

from __future__ import annotations

import json
from pathlib import Path

from PIL import Image

PROJ = Path(__file__).resolve().parent.parent
ARM = PROJ / "src/main/resources/assets/armor_addon/textures/models/armor"
SRC = Path(r"C:\Users\LENOVO\Desktop\第二次更新（产物）")

SETS = [
    ("alloy", "合金超重型盔甲"),
    ("gold", "金制超重型盔甲"),
    ("iron", "铁制超重型盔甲"),
    ("diamond", "钻石超重型盔甲"),
]

FACES = ("up", "east", "north", "west", "south", "down")


def infer_uv_offset(north: list[int], d: int) -> tuple[int, int]:
    """从 bbmodel 的 north UV [x1,y1,x2,y2] 反推 texOffs 的 (u, v)。

    Minecraft 的 addBox 布局里 north 面位于 (u+d, v+d)~(u+d+w, v+d+h)，
    所以 u = x1 - d、v = min(y1,y2) - d。
    """
    x1 = north[0]
    lo_y = min(north[1], north[3])
    return x1 - d, lo_y - d


def face_rects(u: int, v: int, w: int, h: int, d: int) -> dict:
    return {
        "up":    (u + d, v, u + d + w, v + d),
        "east":  (u, v + d, u + d, v + d + h),
        "north": (u + d, v + d, u + d + w, v + d + h),
        "west":  (u + d + w, v + d, u + 2 * d + w, v + d + h),
        "south": (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h),
        "down":  (u + d + w, v, u + 2 * d + w, v + d),
    }


def check(texture: Path, u: int, v: int, w: int, h: int, d: int) -> str:
    """返回该 texOffs 六面的填充情况摘要。"""
    im = Image.open(texture).convert("RGBA")
    parts = []
    for f, (x0, y0, x1, y1) in face_rects(u, v, w, h, d).items():
        oob = x1 > im.width or y1 > im.height
        op = sum(1 for y in range(max(0, y0), min(im.height, y1))
                 for x in range(max(0, x0), min(im.width, x1))
                 if im.getpixel((x, y))[3] > 0)
        total = (x1 - x0) * (y1 - y0)
        if oob:
            parts.append(f"{f}:越界")
        elif op == 0:
            parts.append(f"{f}:空")
        elif op == total:
            parts.append(f"{f}:满")
        else:
            parts.append(f"{f}:{op}/{total}")
    return " ".join(parts)


def collect(bbmodel: Path, only_cube: bool) -> list[tuple[str, list, int, int, int, int]]:
    """返回 [(标签, from, w, h, d, texOffs(u,v)), ...]"""
    j = json.loads(bbmodel.read_text(encoding="utf-8"))
    out = []
    for e in j.get("elements", []):
        name = e.get("name", "")
        is_decoration = (name == "cube")
        if only_cube and not is_decoration:
            continue
        if not only_cube and is_decoration:
            continue
        f, t = e["from"], e["to"]
        w = round(t[0] - f[0])
        h = round(t[1] - f[1])
        d = round(t[2] - f[2])
        if "north" not in e.get("faces", {}):
            continue
        u, v = infer_uv_offset(e["faces"]["north"]["uv"], d)
        side = "左(x正)" if f[0] > 0 else "右(x负)"
        out.append((f"{name} {side}", f, w, h, d, (u, v)))
    out.sort(key=lambda r: -r[1][0])
    return out


def main() -> None:
    print("=" * 78)
    print("外层（layer_1，头盔 / 胸甲 / 靴子）里的肩甲")
    print("=" * 78)
    for key, name in SETS:
        p = SRC / name / f"{name}（头盔胸甲靴子）.bbmodel"
        tex = ARM / f"{key}_ultra_heavy_layer_1.png"
        print(f"\n  【{name}】")
        for label, frm, w, h, d, (u, v) in collect(p, only_cube=True):
            print(f"    {label:<12} 尺寸 {w}x{h}x{d}   texOffs({u}, {v})")
            print(f"        {check(tex, u, v, w, h, d)}")

    print()
    print("=" * 78)
    print("内层（layer_2，护腿）里的膝盖护板")
    print("=" * 78)
    for key, name in SETS:
        p = SRC / name / f"{name}(护腿).bbmodel"
        tex = ARM / f"{key}_ultra_heavy_layer_2.png"
        print(f"\n  【{name}】")
        for label, frm, w, h, d, (u, v) in collect(p, only_cube=True):
            print(f"    {label:<12} 尺寸 {w}x{h}x{d}   texOffs({u}, {v})")
            print(f"        {check(tex, u, v, w, h, d)}")


if __name__ == "__main__":
    main()
