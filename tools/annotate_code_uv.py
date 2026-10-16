#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""按代码里实际使用的尺寸重新生成护膝 UV 标注图。

作者 bbmodel 里护板是 6 宽，但代码为了和腿等宽改成了 4 宽，
addBox 的 UV 自动布局会随之变化，所以不能再拿 bbmodel 的数字来标注。

代码里的实际参数：
    addBox(-2.0, 3.0, -3.5, 4, 4, 1, deformation)
    texOffs 见下表

输出 tools/model-export/knee_code_uv_<set>.png
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw

PROJ = Path(__file__).resolve().parent.parent
ARM = PROJ / "src/main/resources/assets/armor_addon/textures/models/armor"
OUT = PROJ / "tools" / "model-export"

SCALE = 14

# 与 ModClientSetup.SETS 保持一致
SETS = [
    ("alloy", "合金", 43, 22, 43, 22),
    ("gold", "金制", 43, 22, 43, 22),
    ("iron", "铁制", 43, 22, 43, 17),
    ("diamond", "钻石", 43, 22, 43, 22),
]

# 代码里的实际尺寸
W, H, D = 4, 4, 1
COLORS = {"右腿": (230, 60, 60), "左腿": (60, 130, 230)}


def face_rects(u: int, v: int) -> dict:
    w, h, d = W, H, D
    return {
        "up":    (u + d, v, u + d + w, v + d),
        "east":  (u, v + d, u + d, v + d + h),
        "north": (u + d, v + d, u + d + w, v + d + h),
        "west":  (u + d + w, v + d, u + 2 * d + w, v + d + h),
        "south": (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h),
        "down":  (u + d + w, v, u + 2 * d + w, v + d),
    }


def annotate(key: str, label: str, ru: int, rv: int, lu: int, lv: int) -> None:
    im = Image.open(ARM / f"{key}_ultra_heavy_layer_2.png").convert("RGBA")
    WW, HH = im.width * SCALE, im.height * SCALE

    bg = Image.new("RGBA", (WW, HH), (205, 205, 205, 255))
    px = bg.load()
    for y in range(HH):
        for x in range(WW):
            if ((x // SCALE) + (y // SCALE)) % 2 == 0:
                px[x, y] = (170, 170, 170, 255)
    bg.alpha_composite(im.resize((WW, HH), Image.NEAREST))
    dr = ImageDraw.Draw(bg)

    for gx in range(0, im.width + 1, 8):
        dr.line([(gx*SCALE, 0), (gx*SCALE, HH)], fill=(120, 120, 120, 100), width=1)
        dr.text((gx*SCALE + 3, 3), str(gx), fill=(20, 20, 20, 255))
    for gy in range(0, im.height + 1, 8):
        dr.line([(0, gy*SCALE), (WW, gy*SCALE)], fill=(120, 120, 120, 100), width=1)
        dr.text((3, gy*SCALE + 3), str(gy), fill=(20, 20, 20, 255))

    for leg, (u, v) in (("右腿", (ru, rv)), ("左腿", (lu, lv))):
        color = COLORS[leg]
        for face, (x0, y0, x1, y1) in face_rects(u, v).items():
            box = [x0*SCALE, y0*SCALE, x1*SCALE - 1, y1*SCALE - 1]
            dr.rectangle(box, outline=color, width=2)
            if face == "north":
                dr.rectangle(box, fill=color + (70,))
                for k in range(0, box[2]-box[0], 6):
                    dr.line([box[0]+k, box[3], box[0]+k+6, box[1]], fill=color, width=2)

    bg.save(OUT / f"knee_code_uv_{key}.png")

    print(f"  {label}  (右腿 texOffs({ru},{rv}) / 左腿 texOffs({lu},{lv}))")
    for face, (x0, y0, x1, y1) in face_rects(ru, rv).items():
        op = sum(1 for y in range(max(0, y0), min(im.height, y1))
                 for x in range(max(0, x0), min(im.width, x1))
                 if im.getpixel((x, y))[3] > 0)
        total = (x1-x0) * (y1-y0)
        mark = "空" if op == 0 else ("满" if op == total else f"{op}/{total}")
        print(f"      {face:<6} x{x0}~{x1-1} y{y0}~{y1-1}  {mark}")


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    print("=== 按代码实际尺寸（4 宽）的护膝 UV ===")
    for key, label, ru, rv, lu, lv in SETS:
        annotate(key, label, ru, rv, lu, lv)
    print()
    print(f"  输出目录: {OUT}")


if __name__ == "__main__":
    main()
