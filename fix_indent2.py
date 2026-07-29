#!/usr/bin/env python3
"""Comprehensive fix for AnimatedContent/when block indentation in kinetic/MainActivity.kt"""
import re

MAIN_KT = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(MAIN_KT, "r", encoding="utf-8") as f:
    lines = f.readlines()

# Find the when (tab) { line
when_idx = None
for i, line in enumerate(lines):
    if "when (tab) {" in line:
        when_idx = i
        break

if when_idx is None:
    print("[ERR] Could not find when (tab) {")
    exit(1)

# Find the animatedContent opening to get base indent
ac_idx = None
for i in range(when_idx - 1, max(0, when_idx - 10), -1):
    if "AnimatedContent(" in lines[i]:
        ac_idx = i
        break

if ac_idx is None:
    print("[ERR] Could not find AnimatedContent")
    exit(1)

# Get the indent levels
ac_line = lines[ac_idx]
ac_indent = len(ac_line) - len(ac_line.lstrip())  # AnimatedContent indent
when_line = lines[when_idx]
when_indent = len(when_line) - len(when_line.lstrip())  # when indent
branch_indent = when_indent + 4  # branch open indent

print("[INFO] AnimatedContent indent: " + str(ac_indent))
print("[INFO] when indent: " + str(when_indent))
print("[INFO] branch indent: " + str(branch_indent))

# Fix the when line indent
lines[when_idx] = " " * when_indent + "when (tab) {\n"

# Now scan from when_idx+1 to find all branches and fix their content
i = when_idx + 1
changes = 0
while i < len(lines):
    line = lines[i]
    stripped = line.lstrip()
    current_indent = len(line) - len(stripped)
    
    # Check if this is a branch open "N -> {"
    branch_match = re.match(r'(\d+) -> \{', stripped)
    if branch_match and current_indent == branch_indent:
        tab_num = branch_match.group(1)
        # Fix the branch open line
        lines[i] = " " * branch_indent + tab_num + " -> {\n"
        i += 1
        
        # Now re-indent content inside this branch
        while i < len(lines):
            inner = lines[i]
            inner_stripped = inner.lstrip()
            inner_indent = len(inner) - len(inner_stripped)
            
            if inner_stripped == "" or inner_stripped == "\n":
                i += 1
                continue
            
            # Check if this is the closing } of the branch
            if inner_stripped.startswith("}") and inner_indent <= branch_indent:
                # This is the branch close - fix to branch_indent
                lines[i] = " " * branch_indent + "}\n"
                changes += 1
                i += 1
                break
            
            # Check if this is the next branch or else
            if (re.match(r'\d+ -> \{', inner_stripped) and inner_indent == branch_indent) or \
               inner_stripped.startswith("else ->"):
                break
            
            # Re-indent content to branch_indent + 4
            target_indent = branch_indent + 4
            if inner_indent != target_indent and inner_stripped.strip():
                lines[i] = " " * target_indent + inner_stripped
                changes += 1
            
            i += 1
        continue
    
    # Check for else -> {}
    if stripped.startswith("else -> {}"):
        lines[i] = " " * branch_indent + "else -> {}\n"
        if current_indent != branch_indent:
            changes += 1
        i += 1
        continue
    
    # Check for when closing }
    if stripped.startswith("}") and current_indent == when_indent:
        # This closes the when block
        lines[i] = " " * when_indent + "}\n"
        i += 1
        
        # Next should close the AnimatedContent lambda at ac_indent + 4
        if i < len(lines):
            next_stripped = lines[i].lstrip()
            if next_stripped.startswith("}"):
                lines[i] = " " * (ac_indent + 4) + "}\n"
                changes += 1
                i += 1
        break
    
    i += 1

print("[OK] Fixed " + str(changes) + " indentation issues")

with open(MAIN_KT, "w", encoding="utf-8") as f:
    f.writelines(lines)

print("[DONE] File updated")
