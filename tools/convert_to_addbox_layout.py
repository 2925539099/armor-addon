#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""把肩甲 / 护膝的贴图从 Blockbench 布局转成 Minecraft 的 addBox 布局。

两者的六个面在贴图上的位置不同，左右与上下都相反：

    面            Blockbench        Minecraft addBox
    -------------------------------------------------
    (u,   v+d)    east       →      WEST
    (u+d, v+d)    north      →      NORTH      （相同）
    (u+d+w, v+d)  west       →      EAST
    (u+2d+w, v+d) south      →      SOUTH      （相同）
    (u+d, v)      up         →      DOWN
    (u+d+w, v)    down       →      UP

所以只需把第 1 列与第 3 列、上排的左半与右半对调，其余两块原位不动。

只处理带装饰方块的区域，护腿/胸甲等原版部位不动。
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image

PROJ = Path(__file__).resolve().parent.parent
ARM = PROJ / "src/main/resources/assets/armor_addon/textures/models/armor"
BACKUP = PROJ / "tools" / "texture-backup"

# 肩甲：左肩 (33,0,w=3,h=5,d=5)，右肩合金 (48,0)、其余 (48,6)
SHOULDERS = {
    "alloy": [(33, 0, 3, 5, 5), (48, 0, 3, 5, 5)],
    "gold": [(33, 0, 3, 5, 5), (48, 6, 3, 5, 5)],
    "iron": [(33, 0, 3, 5, 5), (48, 6, 3, 5, 5)],
    "diamond": [(33, 0, 3, 5, 5), (48, 6, 3, 5, 5)],
}

# 护膝：右腿 (43,22)、左腿见下
KNEES = {
    "alloy": [(43, 22, 4, 4, 1), (43, 22, 4, 4, 1)],
    "gold": [(43, 22, 4, 4, 1), (43, 22, 4, 4, 1)],
    "iron": [(43, 22, 4, 4, 1), (43, 17, 4, 4, 1)],
    "diamond": [(43, 22, 4, 4, 1), (43, 22, 4, 4, 1)],
}


def swap_region(im: Image.Image, a: tuple, b: tuple) -> None:
    """交换两块矩形区域（尺寸必须一致）。"""
    ax, ay, aw, ah = a
    bx, by, bw, bh = b
    if (aw, ah) != (bw, bh):
        raise ValueError(f"区域尺寸不一致: {(aw,ah)} vs {(bw,bh)}")
    pa = im.crop((ax, ay, ax + aw, ay + ah))
    pb = im.crop((bx, by, bx + bw, by + bh))
    im.paste(pb, (ax, ay))
    im.paste(pa, (bx, by))


def convert(path: Path, blocks: list[tuple]) -> int:
    """把一块贴图上的所有装饰方块区域转成 Minecraft 布局。

    **只对调第 1 列与第 3 列**，不要动 up/down。

    理由：bbmodel 把 (u, v+d) 记作 east、(u+d+w, v+d) 记作 west，
    而 Minecraft 反过来把前者渲染成 WEST、后者渲染成 EAST，所以这两列要换。

    而 up/down **不要换** —— 作者把"内衬"画在上排右侧（bbmodel 的 down），
    Minecraft 的 UP 正好取右侧那块，不对调就是对的。
    这一条是试错得来的，别凭直觉改回去。
    """
    im = Image.open(path).convert("RGBA")
    done = set()

    for (u, v, w, h, d) in blocks:
        if (u, v, w, h, d) in done:
            continue
        done.add((u, v, w, h, d))

        # 第 1 列 (u, v+d) 与第 3 列 (u+d+w, v+d) 对调
        swap_region(im,
                    (u, v + d, d, h),
                    (u + d + w, v + d, d, h))

    im.save(path)
    return len(done)


def main() -> None:
    BACKUP.mkdir(parents=True, exist_ok=True)

    print("=== 外层：肩甲（layer_1）===")
    for key, blocks in SHOULDERS.items():
        p = ARM / f"{key}_ultra_heavy_layer_1.png"
        Image.open(p).convert("RGBA").save(BACKUP / p.name)
        n = convert(p, blocks)
        print(f"  {p.name}  处理 {n} 个方块区域")

    print()
    print("=== 内层：膝盖护板（layer_2）===")
    for key, blocks in KNEES.items():
        p = ARM / f"{key}_ultra_heavy_layer_2.png"
        Image.open(p).convert("RGBA").save(BACKUP / p.name)
        n = convert(p, blocks)
        print(f"  {p.name}  处理 {n} 个方块区域")

    print()
    print(f"  原图已备份到 {BACKUP}")


if __name__ == "__main__":
    main()
