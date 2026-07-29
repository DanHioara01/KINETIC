#!/usr/bin/env python3
"""Fix indentation inside each when branch in kinetic/MainActivity.kt"""
import re

MAIN_KT = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(MAIN_KT, "r", encoding="utf-8") as f:
    lines = f.readlines()

original_lines = lines[:]

# Find the AnimatedContent block start and when block
# We need to re-indent content inside each when branch
# The pattern: "N -> {" opens a branch, and the content inside should be indented +4 spaces

# Find the line with "when (tab) {"
when_line = None
for i, line in enumerate(lines):
    if "when (tab) {" in line:
        when_line = i
        break

if when_line is None:
    print("[ERR] Could not find when (tab) { line")
    exit(1)

print("[INFO] Found when (tab) at line " + str(when_line + 1))

# Now find each branch "N -> {" and its closing "}"
# We need to add 4 spaces to content inside each branch
# Strategy: find all "N -> {" lines, then find the matching "}" at the same indent level

branch_opens = []
for i in range(when_line + 1, len(lines)):
    line = lines[i]
    stripped = line.lstrip()
    indent_len = len(line) - len(stripped)
    if re.match(r'\d+ -> \{', stripped):
        branch_opens.append((i, indent_len))
    elif stripped.startswith("else -> {}"):
        branch_opens.append((i, indent_len))

print("[INFO] Found " + str(len(branch_opens)) + " branches")

# For each branch, find the content lines and the closing "}"
# The closing "}" is at the same indent level as the branch open
changes = 0
for idx, (open_line, open_indent) in enumerate(branch_opens):
    if "else -> {}" in lines[open_line]:
        continue  # skip else -> {} 

    # Find closing "}" - it's the next "}" at open_indent level after the open
    close_line = None
    for i in range(open_line + 1, len(lines)):
        stripped = lines[i].lstrip()
        indent_len = len(lines[i]) - len(stripped)
        if indent_len == open_indent and stripped.startswith("}"):
            close_line = i
            break

    if close_line is None:
        print("[WARN] Could not find closing } for branch at line " + str(open_line + 1))
        continue

    # Re-indent content between open_line+1 and close_line
    for i in range(open_line + 1, close_line):
        line = lines[i]
        if line.strip() == "":
            continue  # skip empty lines
        # Add 4 spaces to the line
        lines[i] = "    " + line
        changes += 1

    # Fix the closing "}" indentation - should be at open_indent level (already correct)
    # But if it was at wrong indent, fix it
    old_close = lines[close_line]
    correct_close = " " * open_indent + "}\n"
    if old_close.rstrip() != correct_close.rstrip():
        lines[close_line] = correct_close
        changes += 1

print("[OK] Re-indented " + str(changes) + " lines")

# Also fix the "else -> {}" line to be at 32 spaces (same as branch opens)
for i, line in enumerate(lines):
    if "else -> {}" in line and i > when_line:
        current_indent = len(line) - len(line.lstrip())
        if current_indent != 32:
            lines[i] = " " * 32 + "else -> {}\n"
            changes += 1

# Write back
content = "".join(lines)
with open(MAIN_KT, "w", encoding="utf-8") as f:
    f.write(content)

print("[DONE] Fixed indentation. Total changes: " + str(changes))
