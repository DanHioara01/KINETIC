#!/usr/bin/env python3
"""Final safe apply: ALL changes in one pass, verifying brace balance before write."""
import os, re

MAIN_KT = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(MAIN_KT, "r", encoding="utf-8") as f:
    lines = f.readlines()

original_count = len(lines)
print("[INFO] Starting with " + str(original_count) + " lines")

def count_braces(text):
    return text.count('{'), text.count('}')

def verify_balance(label, lines_list):
    content = "".join(lines_list)
    o, c = count_braces(content)
    ok = o == c
    print("[CHECK] " + label + ": open=" + str(o) + " close=" + str(c) + " balanced=" + str(ok))
    return ok

# Verify original is balanced
verify_balance("Original", lines)

# === STEP 1: Add slide imports ===
for i, line in enumerate(lines):
    if "import androidx.compose.animation.AnimatedContent" in line:
        if "slideInHorizontally" in lines[i+1]:
            print("[SKIP] Imports already present")
            break
        lines.insert(i+1, "import androidx.compose.animation.slideInHorizontally\n")
        lines.insert(i+2, "import androidx.compose.animation.slideOutHorizontally\n")
        print("[OK] Added slide imports")
        break

# === STEP 2: Find tab chain ===
# The Scaffold content lambda: ") { innerPadding ->"
# followed by "if (currentDashboardTab == 0) {"
scaffold_content_line = None
for i, line in enumerate(lines):
    if ") { innerPadding ->" in line and i > 1500:
        scaffold_content_line = i
        break

if scaffold_content_line is None:
    print("[ERR] Could not find Scaffold content lambda")
    exit(1)

# The "if (currentDashboardTab == 0) {" should be right after
open_line = None
for i in range(scaffold_content_line + 1, scaffold_content_line + 5):
    if "if (currentDashboardTab == 0) {" in lines[i]:
        open_line = i
        break

if open_line is None:
    print("[ERR] Could not find tab chain start")
    exit(1)

print("[INFO] Tab chain starts at line " + str(open_line + 1))

# Find the "else if (currentDashboardTab == N)" lines
else_if_lines = []
for i in range(open_line + 1, len(lines)):
    stripped = lines[i].strip()
    if stripped.startswith("} else if (currentDashboardTab ==") and stripped.endswith("{"):
        m = re.search(r"== (\d+)", stripped)
        if m:
            else_if_lines.append((i, int(m.group(1))))

print("[INFO] Found " + str(len(else_if_lines)) + " else-if branches")
for idx, tab in else_if_lines:
    print("  Tab " + str(tab) + " at line " + str(idx + 1))

# Find the closing of the entire tab chain
# It's the last "}" at indent 24 before the Scaffold content lambda ends
# Look for the pattern: the line after the last tab branch content
# The last else-if is for tab 4. After its content, there should be:
#   "                            )"  (closing the composable call)
#   "                        }"      (closing the if/else chain)
#   "                    }"          (closing the Scaffold content lambda)
#   "                }"              (closing the Scaffold)

# Find the Scaffold content lambda closing
scaffold_close_line = None
for i in range(scaffold_content_line + 1, len(lines)):
    stripped = lines[i].strip()
    line_indent = len(lines[i]) - len(lines[i].lstrip())
    if stripped == "}" and line_indent == 20:
        # Check if the next line is also "}" at indent 16
        if i + 1 < len(lines) and lines[i+1].strip() == "}":
            next_indent = len(lines[i+1]) - len(lines[i+1].lstrip())
            if next_indent == 16:
                scaffold_close_line = i
                break

if scaffold_close_line is None:
    print("[ERR] Could not find Scaffold content lambda close")
    exit(1)

print("[INFO] Scaffold content lambda closes at line " + str(scaffold_close_line + 1))

# The if/else chain closing "}" is at indent 24, right before scaffold_close_line
chain_close_line = None
for i in range(scaffold_close_line - 1, open_line, -1):
    stripped = lines[i].strip()
    line_indent = len(lines[i]) - len(lines[i].lstrip())
    if stripped == "}" and line_indent == 24:
        chain_close_line = i
        break

if chain_close_line is None:
    print("[ERR] Could not find chain closing brace")
    exit(1)

print("[INFO] Chain closing brace at line " + str(chain_close_line + 1))

# Now apply all changes:

# 2a: Replace opening
indent = "                        "  # 24 spaces
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
inserted_opening = len(new_opening) - 1
print("[OK] Replaced opening with AnimatedContent + when wrapper")

# Recalculate else_if positions (shifted by inserted_opening)
else_if_shifted = [(idx + inserted_opening, tab) for idx, tab in else_if_lines]

# 2b: Replace else-if lines (from bottom to top)
for idx, tab in reversed(else_if_shifted):
    old_indent = lines[idx][:len(lines[idx]) - len(lines[idx].lstrip())]
    lines[idx] = old_indent + str(tab) + " -> {\n"
    print("[OK] Replaced else-if for tab " + str(tab))

# 2c: Replace chain closing with else -> {} + when/AnimatedContent closing
# chain_close_line has shifted by inserted_opening
chain_close_shifted = chain_close_line + inserted_opening
# The line at chain_close_shifted is "                        }" (24 spaces)
# Replace it with: else -> {} + when close + AC close
closing_replacement = [
    indent + "        else -> {}\n",
    indent + "    }\n",
    indent + "},\n",
]
lines[chain_close_shifted:chain_close_shifted + 1] = closing_replacement
print("[OK] Replaced chain closing with else -> {} + when/AnimatedContent closing")

verify_balance("After tab changes", lines)

# === STEP 3: Add CONNECT SPOTIFY button ===
logout_idx = None
for i, line in enumerate(lines):
    if ".clickable(onClick = onLogout)" in line and i > 5000:
        logout_idx = i
        break

if logout_idx:
    item_idx = None
    for j in range(logout_idx - 1, max(0, logout_idx - 10), -1):
        if "item {" in lines[j]:
            item_idx = j
            break

    if item_idx:
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

# Add onSpotifyConnect to ProfileScreen call site
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

# === FINAL VERIFICATION ===
final_balanced = verify_balance("Final", lines)

if not final_balanced:
    print("[ERR] Brace imbalance detected! NOT writing file.")
    content = "".join(lines)
    o, c = count_braces(content)
    diff = o - c
    print("[ERR] Missing " + str(diff) + " closing braces")
    exit(1)

# === WRITE ===
with open(MAIN_KT, "w", encoding="utf-8") as f:
    f.writelines(lines)

print("[DONE] File written successfully. Lines: " + str(original_count) + " -> " + str(len(lines)))
