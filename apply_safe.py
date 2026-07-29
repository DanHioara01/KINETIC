#!/usr/bin/env python3
"""Safely apply all changes using line-level edits, no regex on content blocks."""
import os

MAIN_KT = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(MAIN_KT, "r", encoding="utf-8") as f:
    lines = f.readlines()

total = len(lines)
print("[INFO] File has " + str(total) + " lines")

# === STEP 1: Add slideInHorizontally/slideOutHorizontally imports ===
# Find "import androidx.compose.animation.AnimatedContent" and add after it
for i, line in enumerate(lines):
    if "import androidx.compose.animation.AnimatedContent" in line:
        # Check if already added
        if i + 1 < len(lines) and "slideInHorizontally" in lines[i + 1]:
            print("[SKIP] Imports already present")
            break
        lines.insert(i + 1, "import androidx.compose.animation.slideInHorizontally\n")
        lines.insert(i + 2, "import androidx.compose.animation.slideOutHorizontally\n")
        print("[OK] Added slide imports at line " + str(i + 2))
        break

# === STEP 2: Find the if/else-if chain and replace with AnimatedContent + when ===
# Find the line: "                        if (currentDashboardTab == 0) {"
# inside the Scaffold content lambda (not the NavigationBar one at line ~1130)
tab_chain_start = None
for i, line in enumerate(lines):
    stripped = line.strip()
    if stripped == "if (currentDashboardTab == 0) {" and i > 1500:
        tab_chain_start = i
        break

if tab_chain_start is None:
    print("[ERR] Could not find tab chain start")
    exit(1)

print("[INFO] Tab chain starts at line " + str(tab_chain_start + 1))

# Find the end: look for "                }" followed by blank line followed by "                // === Floating Bottom Navbar ==="
tab_chain_end = None
for i in range(tab_chain_start + 1, len(lines)):
    if "// === Floating Bottom Navbar ===" in lines[i]:
        # Go back to find the closing "}"
        for j in range(i - 1, tab_chain_start, -1):
            if lines[j].strip() == "}" and "                " in lines[j][:20]:
                tab_chain_end = j
                break
        break

if tab_chain_end is None:
    print("[ERR] Could not find tab chain end")
    exit(1)

print("[INFO] Tab chain ends at line " + str(tab_chain_end + 1))

# Now we need to:
# 1. Replace the opening "if (currentDashboardTab == 0) {" with AnimatedContent + when wrapper
# 2. Replace each "} else if (currentDashboardTab == N) {" with N -> {
# 3. Add else -> {} before the closing

# Step 2a: Replace opening
indent = "                        "  # 24 spaces
opening_replacement = [
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

# Replace the original line
lines[tab_chain_start:tab_chain_start + 1] = opening_replacement
print("[OK] Replaced opening with AnimatedContent + when wrapper")

# Recalculate positions after insertion
inserted = len(opening_replacement) - 1  # net lines inserted

# Step 2b: Find and replace "} else if (currentDashboardTab == N) {" lines
# These are now shifted by 'inserted' lines
else_if_replacements = []
for i in range(tab_chain_start + inserted, tab_chain_end + inserted):
    line = lines[i]
    stripped = line.strip()
    if stripped.startswith("} else if (currentDashboardTab ==") and stripped.endswith("{"):
        # Extract the tab number
        import re
        m = re.search(r"== (\d+)", stripped)
        if m:
            tab_num = m.group(1)
            else_if_replacements.append((i, tab_num))
            print("[INFO] Found else-if for tab " + tab_num + " at line " + str(i + 1))

# Replace from bottom to top to preserve indices
for i, tab_num in reversed(else_if_replacements):
    old_line = lines[i]
    # Keep the same indentation as the original "if" line
    old_indent = old_line[:len(old_line) - len(old_line.lstrip())]
    lines[i] = old_indent + tab_num + " -> {\n"
    print("[OK] Replaced else-if for tab " + tab_num + " at line " + str(i + 1))

# Step 2c: Find the closing "}" before "Floating Bottom Navbar" and add else -> {}
# Recalculate end position
for i in range(len(lines)):
    if "// === Floating Bottom Navbar ===" in lines[i]:
        # Find the "}" that closes the if/else chain
        for j in range(i - 1, tab_chain_start, -1):
            stripped = lines[j].strip()
            if stripped == "}":
                # Insert else -> {} before this line
                else_line = indent + "        else -> {}\n"
                lines.insert(j, else_line)
                print("[OK] Added else -> {} at line " + str(j + 1))
                # Now add closing for when and AnimatedContent
                when_close = indent + "    }\n"
                ac_close = indent + "},\n"
                lines.insert(j + 1, when_close)
                lines.insert(j + 2, ac_close)
                print("[OK] Added when/AnimatedContent closing braces")
                break
        break

# === STEP 3: Add CONNECT SPOTIFY button before logout ===
# Find ".clickable(onClick = onLogout)" in ProfileScreen
logout_idx = None
for i, line in enumerate(lines):
    if ".clickable(onClick = onLogout)" in line and i > 5000:
        logout_idx = i
        break

if logout_idx is None:
    print("[WARN] Could not find logout button")
else:
    # Go back to find "        item {"
    item_idx = None
    for j in range(logout_idx - 1, max(0, logout_idx - 10), -1):
        if lines[j].strip() == "item {":
            item_idx = j
            break
    
    if item_idx is None:
        print("[WARN] Could not find item block before logout")
    else:
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
        print("[OK] Added CONNECT SPOTIFY button at line " + str(item_idx + 1))
        
        # Also add onSpotifyConnect parameter to ProfileScreen function signature
        for i, line in enumerate(lines):
            if "onDeleteAccount: (String?) -> Unit = {}" in line and "fun ProfileScreen" not in line:
                lines[i] = "    onSpotifyConnect: () -> Unit = {},\n" + line
                print("[OK] Added onSpotifyConnect parameter")
                break
        
        # Add onSpotifyConnect to ProfileScreen call site
        for i, line in enumerate(lines):
            if "subscriptionTier = subscription.tier" in line and i < 3000:
                # Check next line is ")"
                if i + 1 < len(lines) and lines[i + 1].strip() == ")":
                    # Replace the line to add onSpotifyConnect before closing
                    lines[i] = line.rstrip() + ",\n"
                    # Insert onSpotifyConnect before the ")"
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
                    print("[OK] Added onSpotifyConnect to ProfileScreen call site")
                    break

# === WRITE ===
with open(MAIN_KT, "w", encoding="utf-8") as f:
    f.writelines(lines)

print("[DONE] File written. Total lines: " + str(len(lines)))
