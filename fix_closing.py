#!/usr/bin/env python3
"""Fix the broken closing braces for AnimatedContent/when block in kinetic/MainActivity.kt"""
import os

MAIN_KT = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(MAIN_KT, "r", encoding="utf-8") as f:
    content = f.read()

original = content

# The broken closing section (lines ~2250-2258):
#                             )
#                         }
#                     }
#                                 else -> {}
#                             }
#                         },
#                 }
#
#                 // === Floating Bottom Navbar ===

broken = """                                subscriptionTier = subscription.tier
                            )
                        }
                    }
                                else -> {}
                            }
                        },
                }

                // === Floating Bottom Navbar ==="""

fixed = """                                subscriptionTier = subscription.tier
                            )
                        }
                                else -> {}
                            }
                        }
                    }
                }

                // === Floating Bottom Navbar ==="""

if broken in content:
    content = content.replace(broken, fixed)
    print("[OK] Fixed closing braces for when/AnimatedContent block")
else:
    print("[WARN] Broken pattern not found, trying line-by-line approach...")
    # Try to find and fix line by line
    lines = content.split('\n')
    fixed_lines = []
    i = 0
    while i < len(lines):
        line = lines[i]
        # Look for the broken pattern: "                    }" followed by "                                else -> {}"
        if (line.rstrip() == '                    }' and 
            i + 1 < len(lines) and 
            lines[i + 1].rstrip() == '                                else -> {}'):
            # Skip the extra "                    }" line and fix the rest
            # Keep the previous "                        }" (which closes the 4 -> branch)
            # Remove this extra "                    }" line
            # Keep the else -> {} line
            # Fix the closing braces after
            i += 1  # skip the extra "                    }" line
            fixed_lines.append(lines[i])  # keep "                                else -> {}"
            i += 1
            # Fix "                            }" -> keep as when close
            if i < len(lines) and lines[i].rstrip() == '                            }':
                fixed_lines.append(lines[i])  # keep when close
                i += 1
            # Fix "                        }," -> "                        }" (remove comma)
            if i < len(lines) and lines[i].rstrip() == '                        },':
                fixed_lines.append('                        }')
                i += 1
            print("[OK] Fixed closing braces (line-by-line)")
            continue
        fixed_lines.append(line)
        i += 1
    content = '\n'.join(fixed_lines)

# Also fix the AnimatedContent opening - the first tab branch indentation
# The "0 -> {" should have its content at 36 spaces (32 + 4)
# Currently the content after "0 -> {" is at 28 spaces which is wrong

# Fix the AnimatedContent lambda closing - it should not have a comma
if '                        },\n                }' in content:
    content = content.replace('                        },\n                }', '                        }\n                    }\n                }')
    print("[OK] Fixed AnimatedContent lambda closing (removed comma, added content lambda close)")

if content != original:
    with open(MAIN_KT, "w", encoding="utf-8") as f:
        f.write(content)
    diff = len(content) - len(original)
    print("[DONE] Fix applied. Diff: " + str(diff) + " chars")
else:
    print("[WARN] No changes were made")
