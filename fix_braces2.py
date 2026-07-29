#!/usr/bin/env python3
"""Fix missing closing braces. The original } else if (...) { had } that closed previous branch.
When replaced with N -> {, those } were lost. Need to add them back."""

MAIN_KT = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(MAIN_KT, "r", encoding="utf-8") as f:
    lines = f.readlines()

import re

# Find all "N -> {" lines inside the when block (after line 1880)
branch_opens = []
for i, line in enumerate(lines):
    if i < 1880:
        continue
    stripped = line.strip()
    m = re.match(r'^(\d+) -> \{$', stripped)
    if m:
        tab_num = int(m.group(1))
        indent = len(line) - len(line.lstrip())
        branch_opens.append((i, tab_num, indent))

print("[INFO] Found " + str(len(branch_opens)) + " branches:")
for idx, tab_num, indent in branch_opens:
    print("  Tab " + str(tab_num) + " at line " + str(idx + 1) + " (indent " + str(indent) + ")")

# For each branch, we need a closing "}" at the same indent before the next branch opens.
# The original "} else if (...) {" provided this }.
# Strategy: for each pair of consecutive branches, insert a "}" before the next one.

changes = 0
for idx in range(len(branch_opens)):
    current_line, current_tab, current_indent = branch_opens[idx]
    
    if idx < len(branch_opens) - 1:
        # Not the last branch - find the line before the next branch
        next_line, next_tab, next_indent = branch_opens[idx + 1]
        insert_pos = next_line
    else:
        # Last branch - find else -> {}
        else_idx = None
        for i in range(current_line + 1, len(lines)):
            if "else -> {}" in lines[i]:
                else_idx = i
                break
        if else_idx is None:
            print("[ERR] Could not find else -> {} after tab " + str(current_tab))
            continue
        insert_pos = else_idx
    
    # Check if there's already a "}" at the right indent before insert_pos
    # Look at the line right before insert_pos
    prev_idx = insert_pos - 1
    if prev_idx > current_line:
        prev_stripped = lines[prev_idx].strip()
        prev_indent = len(lines[prev_idx]) - len(lines[prev_idx].lstrip())
        
        if prev_stripped == "}" and prev_indent == current_indent:
            print("[SKIP] Tab " + str(current_tab) + " already has } at correct indent")
            continue
        
        if prev_stripped == "}" and prev_indent != current_indent:
            # Fix the indent
            lines[prev_idx] = " " * current_indent + "}\n"
            changes += 1
            print("[OK] Fixed indent of } for tab " + str(current_tab))
            continue
    
    # Insert closing "}" before the next branch
    closing = " " * current_indent + "}\n"
    lines.insert(insert_pos, closing)
    changes += 1
    print("[OK] Added } for tab " + str(current_tab) + " before tab " + str(branch_opens[idx + 1][1]) if idx < len(branch_opens) - 1 else "[OK] Added } for tab " + str(current_tab) + " before else")

print("[INFO] Total changes: " + str(changes))

# Verify balance
content = "".join(lines)
o, c = content.count('{'), content.count('}')
print("[CHECK] Open=" + str(o) + " Close=" + str(c) + " Balanced=" + str(o == c))

if o != c:
    print("[WARN] Still imbalanced. Diff=" + str(o - c))
else:
    with open(MAIN_KT, "w", encoding="utf-8") as f:
        f.writelines(lines)
    print("[DONE] File written. Lines: " + str(len(lines)))
