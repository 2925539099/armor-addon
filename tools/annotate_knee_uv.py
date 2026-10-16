#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""生成护膝 UV 标注图，方便在 Blockbench 里补画贴图。

超重型甲的膝盖护板按 texOffs(44, 22) / (44, 28) 取图，w=6 h=4 d=1，
六个面在 64x32 贴图上的分布如下（addBox 的布局规则）：

    east  (u,      v+d)  → 1 x 4
    north (u+d,    v+d)  → 6 x 4     ← 正面，目前是空的
    west  (u+d+w,  v+d)  → 1 x 4
    south (u+2d+w, v+d)  → 6 x 4
    up    (u+d,    v)    → 6 x 1
    down  (u+d+w,  v)    → 6 x 1
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw

PROJ = Path(__file__).resolve().parent.parent
ARM = PROJ / "src/main/resources/assets/armor_addon/textures/models/armor"
OUT = PROJ / "tools" / "model-export"

SCALE = 14          # 每个像素放大成 14x14，方便看清格子
LABELS = [
    ("alloy_ultra_heavy", "合金"),
    ("diamond_ultra_heavy", "钻石"),
    ("gold_ultra_heavy", "金制"),
    ("iron_ultra_heavy", "铁制"),
]

# 修正后的护膝 UV 起点（与 Java 代码一致）：
#   右腿 texOffs(43, 22)   左腿 texOffs(43, 27)
# 左腿不用 bbmodel 里的 28 —— 那样 north 会算到 y29~32，越出 32 高的贴图。
PLATES = [
    ("右腿护膝", 43, 22, (255, 80, 80)),
    ("左腿护膝", 43, 27, (80, 160, 255)),
]


def face_rects(u: int, v: int, w: int, h: int, d: int) -> list[tuple[str, tuple[int, int, int, int]]]:
    return [
        ("up",    (u + d, v, u + d + w, v + d)),
        ("east",  (u, v + d, u + d, v + d + h)),
        ("north", (u + d, v + d, u + d + w, v + d + h)),
        ("west",  (u + d + w, v + d, u + 2 * d + w, v + d + h)),
        ("south", (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h)),
        ("down",  (u + d + w, v, u + 2 * d + w, v + d)),
    ]


def annotate(name: str, label: str) -> Path:
    src = ARM / f"{name}_layer_2.png"
    im = Image.open(src).convert("RGBA")
    W, H = im.width * SCALE, im.height * SCALE

    # 棋盘底 + 放大后的贴图
    bg = Image.new("RGBA", (W, H), (205, 205, 205, 255))
    px = bg.load()
    for y in range(H):
        for x in range(W):
            if ((x // SCALE) + (y // SCALE)) % 2 == 0:
                px[x, y] = (170, 170, 170, 255)
    bg.alpha_composite(im.resize((W, H), Image.NEAREST))

    d = ImageDraw.Draw(bg)

    # 每 8 格画一条浅网格，方便数格子
    for gx in range(0, im.width + 1, 8):
        d.line([(gx * SCALE, 0), (gx * SCALE, H)], fill=(120, 120, 120, 120), width=1)
        d.text((gx * SCALE + 3, 3), str(gx), fill=(20, 20, 20, 255))
    for gy in range(0, im.height + 1, 8):
        d.line([(0, gy * SCALE), (W, gy * SCALE)], fill=(120, 120, 120, 120), width=1)
        d.text((3, gy * SCALE + 3), str(gy), fill=(20, 20, 20, 255))

    # 两块护膝的六个面
    for plate, u, v, color in PLATES:
        for face, (x0, y0, x1, y1) in face_rects(u, v, 6, 4, 1):
            box = [x0 * SCALE, y0 * SCALE, x1 * SCALE - 1, y1 * SCALE - 1]
            d.rectangle(box, outline=color, width=2)
            if face == "north":
                # 正面那块是空的，用斜线强调
                for i in range(0, box[2] - box[0], 6):
                    d.line([box[0] + i, box[3], box[0] + i + 6, box[1]], fill=color, width=1)

    out = OUT / f"knee_uv_{name}.png"
    bg.save(out)
    print(f"  {label}: {out.name}")
    return out


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    print("=== 生成护膝 UV 标注图 ===")
    for name, label in LABELS:
        annotate(name, label)

    print()
    print("  每块护膝的六个面（以 texOffs(43,22) 为例）：")
    for face, (x0, y0, x1, y1) in face_rects(43, 22, 6, 4, 1):
        mark = "   ← 正面" if face == "north" else ""
        print(f"    {face:<6} x{x0}~{x1-1}  y{y0}~{y1-1}{mark}")


if __name__ == "__main__":
    main()
