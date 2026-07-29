#!/usr/bin/env python3
"""Fix remaining indentation issues identified by code reviewer"""
import re

MAIN_KT = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(MAIN_KT, "r", encoding="utf-8") as f:
    lines = f.readlines()

original_lines = lines[:]
changes = 0

# Fix 1: The closing } of the last tab branch (4 -> {) should be at 32 spaces, not 24
# Find the pattern: "                        }" followed by "                                else -> {}"
for i in range(len(lines) - 1):
    if (lines[i].rstrip() == "                        }" and 
        i + 1 < len(lines) and 
        lines[i + 1].rstrip() == "                                else -> {}"):
        # This } is closing the 4 -> branch but at wrong indent (24 instead of 32)
        lines[i] = "                                }\n"
        changes += 1
        print("[OK] Fixed 4-> branch close indent at line " + str(i + 1))

# Fix 2: Check LaunchedEffect body indentation
# Find "LaunchedEffect(Unit) {" and ensure next non-empty line is indented +4
for i in range(len(lines)):
    if "LaunchedEffect(Unit) {" in lines[i]:
        launched_indent = len(lines[i]) - len(lines[i].lstrip())
        # Check next non-empty line
        for j in range(i + 1, min(i + 3, len(lines))):
            if lines[j].strip():
                body_indent = len(lines[j]) - len(lines[j].lstrip())
                if body_indent <= launched_indent:
                    # Fix: add 4 more spaces
                    lines[j] = " " * (launched_indent + 4) + lines[j].lstrip()
                    changes += 1
                    print("[OK] Fixed LaunchedEffect body indent at line " + str(j + 1))
                break

# Fix 3: Ensure else -> {} is at 32 spaces (same as branch opens)
for i in range(len(lines)):
    if "else -> {}" in lines[i]:
        current_indent = len(lines[i]) - len(lines[i].lstrip())
        if current_indent != 32:
            lines[i] = " " * 32 + "else -> {}\n"
            changes += 1
            print("[OK] Fixed else-> indent at line " + str(i + 1))

# Fix 4: Ensure when closing } is at 28 spaces
for i in range(len(lines)):
    stripped = lines[i].lstrip()
    if stripped == "}" and i > 0:
        # Check if previous line is else -> {} or a branch close
        prev_stripped = lines[i-1].lstrip() if i > 0 else ""
        if "else -> {}" in prev_stripped or (prev_stripped.startswith("}") and len(lines[i-1]) - len(lines[i-1].lstrip()) == 32):
            current_indent = len(lines[i]) - len(lines[i].lstrip())
            if current_indent != 28:
                lines[i] = " " * 28 + "}\n"
                changes += 1
                print("[OK] Fixed when close indent at line " + str(i + 1))

# Fix 5: Ensure AnimatedContent lambda close is at 24 spaces
for i in range(len(lines)):
    stripped = lines[i].lstrip()
    if stripped == "}" and i > 0:
        prev_stripped = lines[i-1].lstrip() if i > 0 else ""
        if prev_stripped == "}" and len(lines[i-1]) - len(lines[i-1].lstrip()) == 28:
            current_indent = len(lines[i]) - len(lines[i].lstrip())
            if current_indent != 24:
                lines[i] = " " * 24 + "}\n"
                changes += 1
                print("[OK] Fixed AnimatedContent close indent at line " + str(i + 1))

with open(MAIN_KT, "w", encoding="utf-8") as f:
    f.writelines(lines)

print("[DONE] Total changes: " + str(changes))
