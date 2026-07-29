#!/usr/bin/env python3
"""Fix missing closing braces for when branches. The original } else if (...) { was replaced
with N -> { but the } that closed the previous branch was lost. Need to add } before each N -> {."""

MAIN_KT = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(MAIN_KT, "r", encoding="utf-8") as f:
    lines = f.readlines()

# Find all "N -> {" lines inside the when block (after line 1880)
import re
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

print("[INFO] Found " + str(len(branch_opens)) + " branch openings:")
for idx, tab_num, indent in branch_opens:
    print("  Tab " + str(tab_num) + " at line " + str(idx + 1) + " (indent " + str(indent) + ")")

# For each branch except the last one, we need to find where its content ends
# and add a closing "}" before the next branch opens.
# The original code had "} else if (...) {" where } closed the previous branch.
# Now we have "N -> {" without the closing }.

# Strategy: for each branch N (except the last), find the line right before the next
# branch's "N+1 -> {" and insert a closing "}" there.

changes = 0
for idx in range(len(branch_opens) - 1):
    current_line, current_tab, current_indent = branch_opens[idx]
    next_line, next_tab, next_indent = branch_opens[idx + 1]
    
    # Find the line right before the next branch opening
    # It should be a ")" or "}" or similar closing
    insert_before = next_line
    
    # Check if the line before next_line already has a "}"
    prev_line = lines[insert_before - 1].strip() if insert_before > 0 else ""
    
    if prev_line == "}":
        # Already has a closing brace - check indent
        prev_indent = len(lines[insert_before - 1]) - len(lines[insert_before - 1].lstrip())
        if prev_indent == current_indent:
            print("[SKIP] Tab " + str(current_tab) + " already has closing } at correct indent")
            continue
        else:
            # Fix the indent of the existing closing brace
            lines[insert_before - 1] = " " * current_indent + "}\n"
            changes += 1
            print("[OK] Fixed indent of closing } for tab " + str(current_tab))
            continue
    
    # Insert a closing "}" before the next branch
    closing_line = " " * current_indent + "}\n"
    lines.insert(insert_before, closing_line)
    changes += 1
    print("[OK] Added closing } for tab " + str(current_tab) + " before tab " + str(next_tab))

# Also check the last branch (tab 4) - it should close before else -> {}
# Find else -> {} 
else_idx = None
for i, line in enumerate(lines):
    if i < 1880:
        continue
    if "else -> {}" in line:
        else_idx = i
        break

if else_idx and branch_opens:
    last_line, last_tab, last_indent = branch_opens[-1]
    # Check if there's a } before else -> {}
    prev_line = lines[else_idx - 1].strip() if else_idx > 0 else ""
    if prev_line != "}":
        closing_line = " " * last_indent + "}\n"
        lines.insert(else_idx, closing_line)
        changes += 1
        print("[OK] Added closing } for tab " + str(last_tab) + " before else -> {}")
    else:
        prev_indent = len(lines[else_idx - 1]) - len(lines[else_idx - 1].lstrip())
        if prev_indent != last_indent:
            lines[else_idx - 1] = " " * last_indent + "}\n"
            changes += 1
            print("[OK] Fixed indent of closing } for tab " + str(last_tab))

print("[INFO] Total changes: " + str(changes))

# Verify brace balance
content = "".join(lines)
opens = content.count('{')
closes = content.count('}')
print("[INFO] Open braces: " + str(opens))
print("[INFO] Close braces: " + str(closes))
print("[INFO] Balanced: " + str(opens == closes))

with open(MAIN_KT, "w", encoding="utf-8") as f:
    f.writelines(lines)

print("[DONE] File updated. Total lines: " + str(len(lines)))
