#!/usr/bin/env python3
"""Apply changes using exact line numbers from the clean backup file. V3 - fixed showLanguageDialog search."""
import os

MAIN_KT = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(MAIN_KT, "r", encoding="utf-8") as f:
    lines = f.readlines()

total = len(lines)
print("[INFO] File has " + str(total) + " lines")

# === STEP 1: Add slide imports ===
for i, line in enumerate(lines):
    if "import androidx.compose.animation.AnimatedContent" in line:
        if i + 1 < len(lines) and "slideInHorizontally" in lines[i + 1]:
            print("[SKIP] Imports already present")
            break
        lines.insert(i + 1, "import androidx.compose.animation.slideInHorizontally\n")
        lines.insert(i + 2, "import androidx.compose.animation.slideOutHorizontally\n")
        print("[OK] Added slide imports")
        break

# === STEP 2: Replace tab chain opening ===
open_line = None
for i, line in enumerate(lines):
    if "if (currentDashboardTab == 0) {" in line and i > 1500:
        open_line = i
        break

if open_line is None:
    print("[ERR] Could not find tab chain start")
    exit(1)

indent = "                        "
new_opening = [
    indent + "AnimatedContent(\n",
    indent + "    targetState = currentDashboardTab,\n",
    indent + "    transitionSpec = {\n",
    indent + "        val direction = if (targetState > initialState) 1 else -1\n",
    indent + "        slideInHorizontally(tween(300)) { it * direction / 4 } + fadeIn(tween(250)) togetherWith\n",
    indent + "            slideOutHorizontally(tween(250)) { -it * direction / 4 } + fadeOut(tween(200))\n",
    indent + "    },\n",
    indent + '    label = "tabTransition"\n',
    indent + ") { tab ->\n",
    indent + "    when (tab) {\n",
    indent + "        0 -> {\n",
]

lines[open_line:open_line + 1] = new_opening
inserted = len(new_opening) - 1
print("[OK] Replaced opening (" + str(inserted) + " lines inserted)")

# === STEP 3: Replace else-if lines ===
import re
for i in range(open_line + inserted, len(lines)):
    stripped = lines[i].strip()
    if stripped.startswith("} else if (currentDashboardTab ==") and stripped.endswith("{"):
        m = re.search(r"== (\d+)", stripped)
        if m:
            tab_num = m.group(1)
            old_indent = lines[i][:len(lines[i]) - len(lines[i].lstrip())]
            lines[i] = old_indent + tab_num + " -> {\n"
            print("[OK] Replaced else-if for tab " + tab_num + " at line " + str(i + 1))

# === STEP 4: Add else -> {} and close when/AnimatedContent ===
# Search for the closing pattern AFTER the tab chain (after line 2500+)
# The pattern: "                        }\n                    }\n                }\n            }\n        }\n"
# followed eventually by "    if (showLanguageDialog)"
# Find the LAST occurrence of "                        }" that has "                    }" right after it

closing_start = None
for i in range(open_line + inserted + 100, len(lines)):
    stripped_i = lines[i].strip()
    stripped_next = lines[i + 1].strip() if i + 1 < len(lines) else ""
    stripped_next2 = lines[i + 2].strip() if i + 2 < len(lines) else ""
    # Look for the triple-closing: "} then } then }" at decreasing indents
    if (stripped_i == "}" and stripped_next == "}" and stripped_next2 == "}"
        and len(lines[i]) - len(lines[i].lstrip()) == 24
        and len(lines[i + 1]) - len(lines[i + 1].lstrip()) == 20
        and len(lines[i + 2]) - len(lines[i + 2].lstrip()) == 16):
        closing_start = i
        print("[INFO] Found closing pattern at line " + str(i + 1))
        break

if closing_start is None:
    print("[ERR] Could not find closing pattern")
    exit(1)

# Insert else -> {} and when/AnimatedContent closing before the triple-closing
else_close = [
    indent + "        else -> {}\n",
    indent + "    }\n",
    indent + "},\n",
]
lines[closing_start:closing_start] = else_close
print("[OK] Added else -> {} and when/AnimatedContent closing")

# === STEP 5: Add CONNECT SPOTIFY button ===
logout_idx = None
for i, line in enumerate(lines):
    if ".clickable(onClick = onLogout)" in line and i > 5000:
        logout_idx = i
        break

if logout_idx is None:
    print("[WARN] Could not find logout button")
else:
    item_idx = None
    for j in range(logout_idx - 1, max(0, logout_idx - 10), -1):
        if "item {" in lines[j]:
            item_idx = j
            break

    if item_idx is not None:
        spotify_button = [
            "        item {\n",
            "            Spacer(Modifier.height(4.dp))\n",
            "            Card(\n",
            "                modifier = Modifier\n",
            "                    .fillMaxWidth()\n",
            "                    .clickable(onClick = onSpotifyConnect),\n",
            "                colors = CardDefaults.cardColors(containerColor = Color(0xFF191414)),\n",
            "                shape = RoundedCornerShape(16.dp)\n",
            "            ) {\n",
            "                Row(\n",
            "                    modifier = Modifier\n",
            "                        .fillMaxWidth()\n",
            "                        .padding(horizontal = 16.dp, vertical = 14.dp),\n",
            "                    verticalAlignment = Alignment.CenterVertically,\n",
            "                    horizontalArrangement = Arrangement.Center\n",
            "                ) {\n",
            "                    Image(\n",
            '                        painter = painterResource(id = R.drawable.ic_spotify),\n',
            '                        contentDescription = "Spotify",\n',
            "                        modifier = Modifier.size(22.dp)\n",
            "                    )\n",
            "                    Spacer(modifier = Modifier.width(10.dp))\n",
            "                    Text(\n",
            '                        "CONNECT SPOTIFY",\n',
            "                        color = Color(0xFF1DB954),\n",
            "                        style = MaterialTheme.typography.bodyLarge,\n",
            "                        fontWeight = FontWeight.Bold,\n",
            "                        letterSpacing = 0.5.sp\n",
            "                    )\n",
            "                }\n",
            "            }\n",
            "        }\n",
            "\n",
        ]
        lines[item_idx:item_idx] = spotify_button
        print("[OK] Added CONNECT SPOTIFY button")

# Add onSpotifyConnect parameter to ProfileScreen
for i, line in enumerate(lines):
    if "onDeleteAccount: (String?) -> Unit = {}" in line and "fun ProfileScreen" not in line:
        lines[i] = "    onSpotifyConnect: () -> Unit = {},\n" + line
        print("[OK] Added onSpotifyConnect parameter")
        break

# Add onSpotifyConnect to ProfileScreen call site (search after line 2400)
for i, line in enumerate(lines):
    if "subscriptionTier = subscription.tier" in line and i > 2400:
        if i + 1 < len(lines) and lines[i + 1].strip() == ")":
            lines[i] = line.rstrip() + ",\n"
            spotify_call = [
                "                                onSpotifyConnect = {\n",
                '                                    val spotifyPackage = "com.spotify.music"\n',
                "                                    val intent = context.packageManager.getLaunchIntentForPackage(spotifyPackage)\n",
                "                                    if (intent != null) {\n",
                "                                        context.startActivity(intent)\n",
                "                                    } else {\n",
                "                                        try {\n",
                '                                            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$spotifyPackage")))\n',
                "                                        } catch (e: Exception) {\n",
                '                                            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=$spotifyPackage")))\n',
                "                                        }\n",
                "                                    }\n",
                "                                }\n",
            ]
            lines[i + 1:i + 1] = spotify_call
            print("[OK] Added onSpotifyConnect to call site")
            break

# === WRITE ===
with open(MAIN_KT, "w", encoding="utf-8") as f:
    f.writelines(lines)

print("[DONE] File written. Total lines: " + str(len(lines)))
