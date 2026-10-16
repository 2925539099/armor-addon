#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""同步 1.2.5 的两处改动到文档与生成脚本。

1. 铁制重型甲、金制重型甲的护甲值下调（加成由 +2/+4/+3/+2 改为 +1/+2/+1/+1）；
2. 金制甲与合金甲新增猪灵中立。

CHANGELOG 的 1.2.0 条目是历史记录，保持原值不动，改动只在 1.2.5 条目里说明。
因此本脚本只改 README（描述当前状态）与 gen_mcmod_submission.py（生成当前说明）。
"""

from __future__ import annotations

from pathlib import Path

PROJ = Path(__file__).resolve().parent.parent

# ---- README：护甲值表 + 加成说明 ----
README_EDITS = [
    (
        "| 铁制重型甲 | 5 / 11 / 9 / 5 | 297 / 432 / 405 / 351 | 2.0 | 9 |",
        "| 铁制重型甲 | 4 / 9 / 7 / 4 | 297 / 432 / 405 / 351 | 2.0 | 9 |",
    ),
    (
        "| 金制重型甲 | 5 / 10 / 7 / 4 | 138 / 201 / 189 / 163 | 2.0 | 25 |",
        "| 金制重型甲 | 4 / 8 / 5 / 3 | 138 / 201 / 189 / 163 | 2.0 | 25 |",
    ),
    (
        "- 护甲值 = 对应内衬甲 **+2 / +4 / +3 / +2**（按头盔 / 胸甲 / 护腿 / 靴子）",
        "- 护甲值 = 对应内衬甲 **+1 / +2 / +1 / +1**（铁制、金制）"
        "或 **+2 / +4 / +3 / +2**（钻石制、合金制），按头盔 / 胸甲 / 护腿 / 靴子",
    ),
]

# ---- 生成脚本：重型甲介绍文本 ----
SCRIPT_EDITS = [
    (
        '"铁制重型甲由铁制内衬甲在锻造台升级而来。护甲值在内衬甲基础上按部位 +2/+4/+3/+2，"',
        '"铁制重型甲由铁制内衬甲在锻造台升级而来。护甲值在内衬甲基础上按部位 +1/+2/+1/+1，"',
    ),
    (
        '"金制重型甲由金制内衬甲在锻造台升级而来。护甲值在内衬甲基础上按部位 +2/+4/+3/+2，"',
        '"金制重型甲由金制内衬甲在锻造台升级而来。护甲值在内衬甲基础上按部位 +1/+2/+1/+1，"',
    ),
]


def apply(path: Path, edits: list[tuple[str, str]]) -> int:
    text = path.read_text(encoding="utf-8")
    n = 0
    for old, new in edits:
        if old in text:
            text = text.replace(old, new, 1)
            n += 1
        else:
            print(f"    !! 未匹配: {old[:52]}...")
    path.write_text(text, encoding="utf-8")
    return n


def main() -> None:
    print("  README.md")
    print(f"    改动 {apply(PROJ / 'README.md', README_EDITS)} / {len(README_EDITS)} 处")
    print("  tools/gen_mcmod_submission.py")
    print(f"    改动 {apply(PROJ / 'tools/gen_mcmod_submission.py', SCRIPT_EDITS)} / {len(SCRIPT_EDITS)} 处")

    print()
    print("  复查 README 的重型甲表：")
    for line in (PROJ / "README.md").read_text(encoding="utf-8").splitlines():
        if "重型甲 |" in line and line.startswith("|"):
            print("    " + line)

    print()
    print("  复查加成说明：")
    for line in (PROJ / "README.md").read_text(encoding="utf-8").splitlines():
        if "护甲值 = 对应内衬甲" in line:
            print("    " + line)


if __name__ == "__main__":
    main()
