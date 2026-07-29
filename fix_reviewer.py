#!/usr/bin/env python3
"""Fix the remaining issues:4-> branch close indent + add onSpotifyConnect callback"""
import re

MAIN_KT = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(MAIN_KT, "r", encoding="utf-8") as f:
    content = f.read()

original = content

# Fix 1: The closing } of 4 -> branch should be at 32 spaces, not 24
# Current: "                                }\n                                else -> {}"
# The "}" before else-> should be at 32 spaces (branch_indent = 32)
broken_close = "                            )\n                                }\n                                else -> {}"
fixed_close = "                            )\n                                }\n                                else -> {}"

# Actually let me look for the exact pattern more carefully
# The issue is the "}" before "else -> {}" is at wrong indent
# Let me find "                        }" followed by "                                else -> {}"
lines = content.split('\n')
new_lines = []
i = 0
changes = 0
while i < len(lines):
    line = lines[i]
    stripped = line.lstrip()
    indent = len(line) - len(stripped)
    
    # Check if this is the closing } of 4-> branch (at 24 spaces) followed by else -> {} (at 32 spaces)
    if (stripped == '}' and indent == 24 and 
        i + 1 < len(lines) and 
        lines[i + 1].lstrip().startswith('else -> {}')):
        # Fix the indent to 32 spaces
        new_lines.append(' ' * 32 + '}')
        changes += 1
        print("[OK] Fixed4-> branch close indent at line " + str(i + 1))
        i += 1
        continue
    
    new_lines.append(line)
    i += 1

content = '\n'.join(new_lines)

# Fix 2: Add onSpotifyConnect parameter to ProfileScreen function signature
# Find "onDeleteAccount: (String?) -> Unit = {}" and add onSpotifyConnect before it
old_sig = "    onDeleteAccount: (String?) -> Unit = {}"
new_sig = "    onSpotifyConnect: () -> Unit = {},\n    onDeleteAccount: (String?) -> Unit = {}"
if old_sig in content and "onSpotifyConnect" not in content:
    content = content.replace(old_sig, new_sig)
    changes += 1
    print("[OK] Added onSpotifyConnect parameter to ProfileScreen")

# Fix 3: Add onSpotifyConnect to ProfileScreen call site
# Find the ProfileScreen call and add onSpotifyConnect parameter
old_call = "                                subscriptionTier = subscription.tier\n                            )"
new_call = "                                subscriptionTier = subscription.tier,\n                                onSpotifyConnect = {\n                                    val spotifyPackage = \"com.spotify.music\"\n                                    val intent = context.packageManager.getLaunchIntentForPackage(spotifyPackage)\n                                    if (intent != null) {\n                                        context.startActivity(intent)\n                                    } else {\n                                        try {\n                                            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(\"market://details?id=$spotifyPackage\")))\n                                        } catch (e: Exception) {\n                                            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(\"https://play.google.com/store/apps/details?id=$spotifyPackage\")))\n                                        }\n                                    }\n                                }\n                            )"
if old_call in content and "onSpotifyConnect = {" not in content:
    content = content.replace(old_call, new_call)
    changes += 1
    print("[OK] Added onSpotifyConnect to ProfileScreen call site")

# Fix 4: Update the Spotify button in ProfileScreen to use the callback
old_button_click = '.clickable {\n                        val spotifyPackage = "com.spotify.music"\n                        val intent = context.packageManager.getLaunchIntentForPackage(spotifyPackage)\n                        if (intent != null) {\n                            context.startActivity(intent)\n                        } else {\n                            try {\n                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$spotifyPackage")))\n                            } catch (e: Exception) {\n                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=$spotifyPackage")))\n                            }\n                        }\n                    }'
new_button_click = '.clickable(onClick = onSpotifyConnect)'
if old_button_click in content:
    content = content.replace(old_button_click, new_button_click)
    changes += 1
    print("[OK] Updated Spotify button to use onSpotifyConnect callback")

if content != original:
    with open(MAIN_KT, "w", encoding="utf-8") as f:
        f.write(content)
    print("[DONE] Applied " + str(changes) + " fixes")
else:
    print("[WARN] No changes made")
