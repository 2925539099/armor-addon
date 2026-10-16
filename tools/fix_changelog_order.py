#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""修正 CHANGELOG 里被误写的版本标题。

问题：插入 1.3.1 章节时，new_string 的结尾误写成 `## [1.3.0]`（应为 `## [1.2.5]`），
导致：
  1. `## [1.3.0]` 出现两次；
  2. 1.2.5 的标题丢失；
  3. 1.3.1 章节落在 1.3.0 之后，顺序颠倒。

本脚本：把 1.3.1 章节整体移到最前，并把重复的第二个 `## [1.3.0]` 还原为 `## [1.2.5]`。
"""

from __future__ import annotations

import re
from pathlib import Path

PROJ = Path(__file__).resolve().parent.parent
CL = PROJ / "CHANGELOG.md"


def main() -> None:
    text = CL.read_text(encoding="utf-8")
    lines = text.splitlines()

    # 定位所有二级版本标题
    marks = [(i, ln) for i, ln in enumerate(lines) if re.match(r"^## \[\d", ln)]
    print("  修改前的章节：")
    for i, ln in marks:
        print(f"    {i+1:4}: {ln}")

    # ---- 1. 抽出 1.3.1 章节 ----
    start = next((i for i, ln in marks if ln.strip() == "## [1.3.1]"), None)
    if start is None:
        print("  !! 找不到 1.3.1 章节")
        return

    end = next((i for i, ln in marks if i > start), None)
    if end is None:
        print("  !! 找不到 1.3.1 的下一个标题")
        return

    section = lines[start:end]
    while section and section[-1].strip() == "":
        section.pop()

    rest = lines[:start] + lines[end:]

    # ---- 2. 把重复的第二个 `## [1.3.0]` 改回 `## [1.2.5]` ----
    seen = 0
    fixed = False
    for i, ln in enumerate(rest):
        if ln.strip() == "## [1.3.0]":
            seen += 1
            if seen == 2:
                rest[i] = "## [1.2.5]"
                fixed = True
    print(f"  重复标题修正: {fixed}")

    # ---- 3. 把 1.3.1 章节插到最前的版本标题之前 ----
    first = next(i for i, ln in enumerate(rest) if re.match(r"^## \[\d", ln))
    merged = rest[:first] + section + [""] + rest[first:]

    CL.write_text("\n".join(merged) + "\n", encoding="utf-8")

    print()
    print("  修改后的章节：")
    for i, ln in enumerate(merged, 1):
        if re.match(r"^## \[\d", ln):
            print(f"    {i:4}: {ln}")

    # ---- 4. 校验：版本号不重复 ----
    vers = [ln.strip() for ln in merged if re.match(r"^## \[\d", ln)]
    dupes = {v for v in vers if vers.count(v) > 1}
    print()
    print(f"  重复的版本标题: {dupes or '无'}")
    print(f"  版本数: {len(vers)}")


if __name__ == "__main__":
    main()
