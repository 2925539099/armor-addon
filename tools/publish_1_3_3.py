#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""发布 1.3.3 的收尾工作：生成发布说明、更新 releases/README.md 的版本表与 SHA256。

用法：python tools/publish_1_3_3.py
"""

from __future__ import annotations

import hashlib
import re
from pathlib import Path

PROJ = Path(__file__).resolve().parent.parent
VERSION = "1.3.3"
JAR = PROJ / "build" / "libs" / f"盔甲附加-{VERSION}.jar"
RELEASES = PROJ / "releases"
CHANGELOG = PROJ / "CHANGELOG.md"

INSTALL = """
## 安装

1. 安装 Minecraft 1.21.1 的 **NeoForge**（21.1.244 或更高的 21.1.x）。
2. 将附件中的 `盔甲附加-{v}.jar` 放入 `.minecraft/mods/`。
3. **不需要任何前置模组。**
"""


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    h.update(path.read_bytes())
    return h.hexdigest().upper()


def main() -> None:
    if not JAR.exists():
        raise SystemExit(f"找不到 {JAR}，先构建")

    # 1. 发布说明
    text = CHANGELOG.read_text(encoding="utf-8")
    start = text.index(f"## [{VERSION}]")
    nxt = text.index("## [", start + 1)
    body = text[start:nxt].rstrip()
    note = f"# 盔甲附加 (Armor Addon) {VERSION}\n\nMinecraft **1.21.1** / **NeoForge** 模组。\n\n"
    note += body + "\n" + INSTALL.format(v=VERSION)
    out = RELEASES / f"发布说明-{VERSION}.md"
    out.write_text(note, encoding="utf-8")
    print(f"  已生成 {out.name}  ({len(note)} 字符)")

    # 2. releases/README.md
    rf = RELEASES / "README.md"
    rt = rf.read_text(encoding="utf-8")
    digest = sha256(JAR)
    size = f"{JAR.stat().st_size / 1024:.1f} KB"
    url = ("https://github.com/2925539099/armor-addon/raw/main/releases/"
           "%E7%9B%94%E7%94%B2%E9%99%84%E5%8A%A0-" + VERSION + ".jar")

    if f"| **{VERSION}**（最新） |" not in rt:
        m = re.search(r"\| \*\*[\d.]+\*\*（最新） \| \[盔甲附加-[\d.]+\.jar\]", rt)
        if m:
            rt = rt[:m.start()] + (
                f"| **{VERSION}**（最新） | [盔甲附加-{VERSION}.jar]({url}) | {size} | "
                f"重量惩罚改为写在物品属性上 |\n"
                f"| 1.3.2 | [盔甲附加-1.3.2.jar]"
            ) + rt[m.end():]
            print("  版本表已更新")

    if f"{digest}  盔甲附加-{VERSION}.jar" not in rt:
        m = re.search(r"^\s+[0-9A-F]{64}\s+盔甲附加-[\d.]+\.jar", rt, re.MULTILINE)
        if m:
            line_start = rt.rfind("\n", 0, m.start()) + 1
            rt = rt[:line_start] + f"{digest}  盔甲附加-{VERSION}.jar\n" + rt[line_start:]
            print(f"  SHA256 已写入: {digest}")

    old_latest = re.search(r"直接用最新的 \*\*([\d.]+)\*\*。", rt)
    if old_latest and old_latest.group(1) != VERSION:
        rt = rt.replace(old_latest.group(0), f"直接用最新的 **{VERSION}**。")
        print("  「直接用最新的」已更新")

    rf.write_text(rt, encoding="utf-8")

    print()
    print(f"  jar: {JAR.name}  {size}  SHA256 {digest}")


if __name__ == "__main__":
    main()
