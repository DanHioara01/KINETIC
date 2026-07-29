#!/usr/bin/env python3
"""Fix the broken closing braces for when/AnimatedContent block."""
import os

MAIN_KT = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(MAIN_KT, "r", encoding="utf-8") as f:
    lines = f.readlines()

original = lines[:]

# Find the broken closing pattern:
#                                 else -> {}
#                             }
#                         },      <-- WRONG: should be just }
#                         }       <-- WRONG: extra brace
#                     }
#                 }
#
# Should be:
#                                 else -> {}
#                             }
#                         }
#                     }
#                 }

# Search for the pattern after line 2500
for i in range(2500, len(lines)):
    stripped = lines[i].strip()
    if stripped == "else -> {}":
        # Found it. Check the next few lines
        print("[INFO] Found else -> {} at line " + str(i + 1))
        print("[INFO] Line " + str(i + 1) + ": " + repr(lines[i]))
        print("[INFO] Line " + str(i + 2) + ": " + repr(lines[i + 1]) if i + 1 < len(lines) else "")
        print("[INFO] Line " + str(i + 3) + ": " + repr(lines[i + 2]) if i + 2 < len(lines) else "")
        print("[INFO] Line " + str(i + 4) + ": " + repr(lines[i + 3]) if i + 3 < len(lines) else "")
        print("[INFO] Line " + str(i + 5) + ": " + repr(lines[i + 4]) if i + 4 < len(lines) else "")
        
        # Fix: the pattern should be:
        # else -> {}  (at 32 spaces)
        # }           (at 28 spaces - closes when)
        # }           (at 24 spaces - closes AnimatedContent lambda)
        # }           (at 20 spaces - closes content lambda)
        # }           (at 16 spaces - closes Scaffold)
        
        # Current broken pattern has:
        # else -> {}  (at 32 spaces)
        # }           (at 28 spaces - closes when) 
        # },          (at 24 spaces with comma - WRONG)
        # }           (at 24 spaces - extra)
        # }           (at 20 spaces)
        # }           (at 16 spaces)
        
        # Find the "}," line (with comma) and remove it
        for j in range(i + 1, min(i + 6, len(lines))):
            if lines[j].strip() == "},":
                print("[INFO] Found broken '},' at line " + str(j + 1))
                # Remove this line
                lines.pop(j)
                print("[OK] Removed extra '},' line")
                break
        
        # Also check if there's an extra } at 24 spaces after the when close
        for j in range(i + 1, min(i + 6, len(lines))):
            if lines[j].strip() == "}":
                indent = len(lines[j]) - len(lines[j].lstrip())
                if indent == 24:
                    # This should be the AnimatedContent lambda close
                    # But if there are two } at 24 spaces, remove the extra
                    # Check next line
                    if j + 1 < len(lines) and lines[j + 1].strip() == "}":
                        next_indent = len(lines[j + 1]) - len(lines[j + 1].lstrip())
                        if next_indent == 20:
                            # The next line is the content lambda close at 20 spaces
                            # So this 24-space } is the AnimatedContent close
                            # But we need to make sure there's only one
                            # Check if previous line is also } at 28 (when close)
                            pass
                break
        
        break

# Write
with open(MAIN_KT, "w", encoding="utf-8") as f:
    f.writelines(lines)

print("[DONE] File updated. Total lines: " + str(len(lines)))
