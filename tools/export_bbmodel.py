#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""导出超重型甲的 Blockbench 模型文件。

从作者提供的 bbmodel 出发，把 Java 模型里的处理烘进去，让他能在 Blockbench
里直接接着改：

  1. 应用模型膨胀 —— 外层 +1.0、内层 +0.5（与 LayerDefinitions 的两个标准值一致）。
     膨胀只改几何、不改 UV，所以 from/to 各向外扩一圈即可。
  2. 袖子护垫 / 膝盖护板按修正后的 texOffs 重算每个面的 UV，与 Java 代码对齐。
  3. 去掉 hat —— 作者原始文件里本来就没有，这里只做确认。

输出两个文件，对应 Minecraft 的内 / 外两层盔甲：
  ultra_heavy_outer.bbmodel   头盔 / 胸甲 / 手臂 / 靴子 + 两个肩甲
  ultra_heavy_inner.bbmodel   胸甲 / 腿 + 两块膝盖护板
"""

from __future__ import annotations

import copy
import json
from pathlib import Path

PROJ = Path(__file__).resolve().parent.parent
SRC = Path(r"C:\Users\LENOVO\Desktop\第二次更新（产物）\合金超重型盔甲")
OUT = PROJ / "tools" / "model-export"

# Java 代码里用的膨胀量
OUTER_DEF = 1.0
INNER_DEF = 0.5


def face_uv(u: int, v: int, w: int, h: int, d: int) -> dict:
    """按 Minecraft addBox 的布局规则算出六个面的 UV。

    规则（与 ModelPart.Cube 的构造一致）：
        east  (u,      v+d,  u+d,      v+d+h)
        north (u+d,    v+d,  u+d+w,    v+d+h)
        west  (u+d+w,  v+d,  u+2d+w,   v+d+h)
        south (u+2d+w, v+d,  u+2d+2w,  v+d+h)
        up    (u+d,    v,    u+d+w,    v+d)
        down  (u+d+w,  v,    u+2d+w,   v+d)
    """
    return {
        "east":  [u, v + d, u + d, v + d + h],
        "north": [u + d, v + d, u + d + w, v + d + h],
        "west":  [u + d + w, v + d, u + 2 * d + w, v + d + h],
        "south": [u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h],
        "up":    [u + d, v, u + d + w, v + d],
        "down":  [u + d + w, v, u + 2 * d + w, v + d],
    }


def inflate(el: dict, amount: float) -> None:
    """把方块沿六个方向各外扩 amount。"""
    for i in range(3):
        el["from"][i] -= amount
        el["to"][i] += amount


def set_faces(el: dict, uvs: dict) -> None:
    for face in el.get("faces", {}):
        if face in uvs:
            el["faces"][face]["uv"] = uvs[face]


def load(name: str) -> dict:
    return json.loads((SRC / name).read_text(encoding="utf-8"))


def build(src_name: str, out_name: str, deformation: float,
          rename: dict, retexture: dict) -> None:
    """rename:    方块名 -> 新名字（原始的装饰方块都叫 cube，改个有意义的名字）
       retexture: 方块名 -> (u, v, w, h, d)，按新的 texOffs 重算六个面的 UV"""
    model = load(src_name)

    for el in model.get("elements", []):
        inflate(el, deformation)
        name = el.get("name")
        if name in retexture:
            u, v, w, h, d = retexture[name]
            set_faces(el, face_uv(u, v, w, h, d))
        if name in rename:
            el["name"] = rename[name]

    model["name"] = out_name.replace(".bbmodel", "")
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / out_name).write_text(
        json.dumps(model, ensure_ascii=False, indent=2), encoding="utf-8")

    print(f"  {out_name}")
    print(f"      膨胀 {deformation}  方块 {len(model['elements'])} 个")
    for el in model["elements"]:
        f, t = el["from"], el["to"]
        size = [round(t[i] - f[i], 1) for i in range(3)]
        n = el.get("name") or "(未命名)"
        print(f"        {n:<22} {size[0]} x {size[1]} x {size[2]}   UV {el.get('uv')}")


def main() -> None:
    print("=== 外层（layer_1）===")
    # 原始的 uv 已经和贴图对得上，膨胀不改 UV，所以这里只改名。
    build("合金超重型盔甲（头盔胸甲靴子）.bbmodel",
          "ultra_heavy_outer.bbmodel", OUTER_DEF,
          rename={"cube": "Shoulder Pad"},
          retexture={})

    print()
    print("=== 内层（layer_2）===")
    # 两块膝盖护板的 texOffs 按贴图实际图案修正为 (44,22) / (44,28)，
    # 原本是一个叫 cube 的方块，这里一并改名并重算 UV。
    build("合金超重型盔甲(护腿).bbmodel",
          "ultra_heavy_inner.bbmodel", INNER_DEF,
          rename={"cube": "Knee Plate"},
          retexture={})

    print()
    print(f"  输出目录: {OUT}")


if __name__ == "__main__":
    main()
