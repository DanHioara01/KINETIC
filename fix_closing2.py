#!/usr/bin/env python3
"""Fix the closing braces of AnimatedContent/when block"""
import re

MAIN_KT = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(MAIN_KT, "r", encoding="utf-8") as f:
    content = f.read()

original = content

# Find the broken closing section
# Current state:
#                             )
#                         }
#                                 else -> {}
#                             }
#                             }
#                     }
#                 }
#
# Should be:
#                             )
#                         }
#                                 else -> {}
#                             }
#                         }
#                     }
#                 }

broken = """                            )
                        }
                                else -> {}
                            }
                            }
                    }
                }

                // === Floating Bottom Navbar ==="""

fixed = """                            )
                        }
                                else -> {}
                            }
                        }
                    }
                }

                // === Floating Bottom Navbar ==="""

if broken in content:
    content = content.replace(broken, fixed)
    print("[OK] Fixed closing braces structure")
else:
    print("[WARN] Exact broken pattern not found")

if content != original:
    with open(MAIN_KT, "w", encoding="utf-8") as f:
        f.write(content)
    print("[DONE] File updated")
else:
    print("[WARN] No changes")
