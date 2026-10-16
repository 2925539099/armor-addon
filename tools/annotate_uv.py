#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""为四套超重型甲生成 UV 标注图。

直接读作者的 bbmodel，把他给每个装饰方块指定的 UV 反推成 texOffs，
再在贴图上把六个面画出来，方便对照着补画。

输出（tools/model-export/）：
    shoulder_uv_<set>.png   layer_1 上两个肩甲的六个面
    knee_uv_<set>.png       layer_2 上两块膝盖护板的六个面

用法：python tools/annotate_uv.py
"""

from __future__ import annotations

import json
from pathlib import Path

from PIL import Image, ImageDraw

PROJ = Path(__file__).resolve().parent.parent
ARM = PROJ / "src/main/resources/assets/armor_addon/textures/models/armor"
SRC = Path(r"C:\Users\LENOVO\Desktop\第二次更新（产物）")
OUT = PROJ / "tools" / "model-export"

SETS = [("alloy", "合金超重型盔甲"), ("gold", "金制超重型盔甲"),
        ("iron", "铁制超重型盔甲"), ("diamond", "钻石超重型盔甲")]

SCALE = 14
FACES = ("up", "east", "north", "west", "south", "down")
# 每个面的框色：正面用醒目色，其余用同色系
COLORS = {"A": (230, 60, 60), "B": (60, 130, 230)}


def face_rects(u: int, v: int, w: int, h: int, d: int) -> dict:
    return {
        "up":    (u + d, v, u + d + w, v + d),
        "east":  (u, v + d, u + d, v + d + h),
        "north": (u + d, v + d, u + d + w, v + d + h),
        "west":  (u + d + w, v + d, u + 2 * d + w, v + d + h),
        "south": (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h),
        "down":  (u + d + w, v, u + 2 * d + w, v + d),
    }


def read_blocks(bbmodel: Path) -> list[dict]:
    """读出 bbmodel 里所有叫 cube 的装饰方块，附上反推出来的 texOffs。"""
    j = json.loads(bbmodel.read_text(encoding="utf-8"))
    out = []
    for e in j.get("elements", []):
        if e.get("name") != "cube" or "north" not in e.get("faces", {}):
            continue
        f, t = e["from"], e["to"]
        w, h, d = round(t[0]-f[0]), round(t[1]-f[1]), round(t[2]-f[2])
        n = e["faces"]["north"]["uv"]
        u, v = n[0] - d, min(n[1], n[3]) - d
        # 用方块中心判断左右：护腿那两块 cube 的 x 都是负的，看符号会误判。
        # 肩甲的 x 跨正负，两者都适用。
        center_x = (f[0] + t[0]) / 2
        out.append({"from": f, "w": w, "h": h, "d": d, "u": u, "v": v,
                    "x_positive": center_x > 0})
    # 按 texOffs 的 v 排序，保证两张图里框的编号一致
    out.sort(key=lambda b: b["v"])
    return out


def annotate(texture: Path, blocks: list[dict], out: Path, title: str) -> None:
    im = Image.open(texture).convert("RGBA")
    W, H = im.width * SCALE, im.height * SCALE

    bg = Image.new("RGBA", (W, H), (205, 205, 205, 255))
    px = bg.load()
    for y in range(H):
        for x in range(W):
            if ((x // SCALE) + (y // SCALE)) % 2 == 0:
                px[x, y] = (170, 170, 170, 255)
    bg.alpha_composite(im.resize((W, H), Image.NEAREST))
    dr = ImageDraw.Draw(bg)

    for gx in range(0, im.width + 1, 8):
        dr.line([(gx*SCALE, 0), (gx*SCALE, H)], fill=(120, 120, 120, 100), width=1)
        dr.text((gx*SCALE + 3, 3), str(gx), fill=(20, 20, 20, 255))
    for gy in range(0, im.height + 1, 8):
        dr.line([(0, gy*SCALE), (W, gy*SCALE)], fill=(120, 120, 120, 100), width=1)
        dr.text((3, gy*SCALE + 3), str(gy), fill=(20, 20, 20, 255))

    for i, b in enumerate(blocks):
        color = COLORS.get("A" if i % 2 == 0 else "B", (230, 60, 60))
        for face, (x0, y0, x1, y1) in face_rects(b["u"], b["v"], b["w"], b["h"], b["d"]).items():
            box = [x0*SCALE, y0*SCALE, x1*SCALE - 1, y1*SCALE - 1]
            oob = x1 > im.width or y1 > im.height
            if oob:
                dr.rectangle(box, outline=(255, 255, 0), width=3)
            dr.rectangle(box, outline=color, width=2)
            if face == "north":
                # 正面填一层半透明色，最醒目
                dr.rectangle(box, fill=color + (70,))
                for k in range(0, box[2]-box[0], 7):
                    dr.line([box[0]+k, box[3], box[0]+k+7, box[1]], fill=color, width=2)

    bg.save(out)
    labels = []
    for i, b in enumerate(blocks):
        side = "左" if b["x_positive"] else "右"
        labels.append(f"{'A' if i%2==0 else 'B'}={side} texOffs({b['u']},{b['v']})")
    print(f"  {title}: {out.name}")
    for l in labels:
        print(f"      {l}")


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    print("=== 肩甲（layer_1）===")
    for key, name in SETS:
        bb = SRC / name / f"{name}（头盔胸甲靴子）.bbmodel"
        tex = ARM / f"{key}_ultra_heavy_layer_1.png"
        annotate(tex, read_blocks(bb), OUT / f"shoulder_uv_{key}.png", name)

    print()
    print("=== 膝盖护板（layer_2）===")
    for key, name in SETS:
        bb = SRC / name / f"{name}(护腿).bbmodel"
        tex = ARM / f"{key}_ultra_heavy_layer_2.png"
        annotate(tex, read_blocks(bb), OUT / f"knee_uv_{key}.png", name)

    print()
    print(f"  输出目录: {OUT}")


if __name__ == "__main__":
    main()
