#!/usr/bin/env python3
"""ALL-IN-ONE: Apply imports, AnimatedContent, Spotify button, verify brace balance."""
import re

MAIN_KT = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(MAIN_KT, "r", encoding="utf-8") as f:
    lines = f.readlines()

print("[INFO] Starting: " + str(len(lines)) + " lines")

# Verify original is balanced
content = "".join(lines)
o0, c0 = content.count('{'), content.count('}')
assert o0 == c0, "Original not balanced!"
print("[OK] Original balanced: " + str(o0))

# ── 1. Add slide imports ──
for i, line in enumerate(lines):
    if "import androidx.compose.animation.AnimatedContent" in line:
        if "slideInHorizontally" not in lines[i+1]:
            lines.insert(i+1, "import androidx.compose.animation.slideInHorizontally\n")
            lines.insert(i+2, "import androidx.compose.animation.slideOutHorizontally\n")
            print("[OK] Added slide imports")
        break

# ── 2. Find key positions ──
# Scaffold content lambda
scl = None
for i, l in enumerate(lines):
    if ") { innerPadding ->" in l and i > 1500:
        scl = i; break
assert scl, "No Scaffold content lambda"

# if (currentDashboardTab == 0) {
op = None
for i in range(scl+1, scl+5):
    if "if (currentDashboardTab == 0) {" in lines[i]:
        op = i; break
assert op, "No tab chain start"

# } else if (currentDashboardTab == N) {
eifs = []
for i in range(op+1, len(lines)):
    s = lines[i].strip()
    if s.startswith("} else if (currentDashboardTab ==") and s.endswith("{"):
        m = re.search(r"== (\d+)", s)
        if m: eifs.append((i, int(m.group(1))))
print("[INFO] Found " + str(len(eifs)) + " else-if: tabs " + str([t for _,t in eifs]))

# Chain closing "}" at indent 24 before Scaffold content lambda close
scl_close = None
for i in range(scl+1, len(lines)):
    if lines[i].strip() == "}" and len(lines[i]) - len(lines[i].lstrip()) == 20:
        if i+1 < len(lines) and lines[i+1].strip() == "}" and len(lines[i+1]) - len(lines[i+1].lstrip()) == 16:
            scl_close = i; break
assert scl_close, "No Scaffold content lambda close"

chain_close = None
for i in range(scl_close-1, op, -1):
    if lines[i].strip() == "}" and len(lines[i]) - len(lines[i].lstrip()) == 24:
        chain_close = i; break
assert chain_close, "No chain close"
print("[INFO] Key positions: open=" + str(op+1) + " chain_close=" + str(chain_close+1))

# ── 3. Replace opening ──
I = "                        "  # 24 spaces
opening = [
    I+"AnimatedContent(\n",
    I+"    targetState = currentDashboardTab,\n",
    I+"    transitionSpec = {\n",
    I+"        val direction = if (targetState > initialState) 1 else -1\n",
    I+"        slideInHorizontally(tween(300)) { it * direction / 4 } + fadeIn(tween(250)) togetherWith\n",
    I+"            slideOutHorizontally(tween(250)) { -it * direction / 4 } + fadeOut(tween(200))\n",
    I+"    },\n",
    I+'    label = "tabTransition"\n',
    I+") { tab ->\n",
    I+"    when (tab) {\n",
    I+"        0 -> {\n",
]
lines[op:op+1] = opening
ins = len(opening) - 1

# ── 4. Replace else-if → when branches (with closing braces!) ──
# Each "} else if (...) {" → "N -> {\n" AND we add a closing "}" before the next one
# The original "}" in "} else if" closed the PREVIOUS branch
eifs_shifted = [(i+ins, t) for i,t in eifs]

for idx in range(len(eifs_shifted)):
    eif_line, tab_num = eifs_shifted[idx]
    # Replace "} else if (...) {" with "N -> {"
    old_indent = lines[eif_line][:len(lines[eif_line]) - len(lines[eif_line].lstrip())]
    lines[eif_line] = old_indent + str(tab_num) + " -> {\n"
    
    # Now we need a closing "}" for the PREVIOUS branch
    # The previous branch is either tab 0 or the previous tab
    if idx == 0:
        prev_tab = 0
        prev_indent = 32  # tab 0 opens at 32
    else:
        prev_tab = eifs_shifted[idx-1][1]
        prev_indent = len(old_indent)  # same indent as the branch open
    
    # Find the line right before this line that ends a composable call
    # It should be ")" closing the previous tab's composable
    # Insert "}" at the right indent BEFORE this line
    # Check if prev line already has "}"
    prev_line_text = lines[eif_line - 1].strip()
    if prev_line_text == "}":
        # Already has closing brace - check indent
        prev_line_indent = len(lines[eif_line-1]) - len(lines[eif_line-1].lstrip())
        if prev_line_indent != prev_indent:
            lines[eif_line-1] = " "*prev_indent + "}\n"
    else:
        # Insert closing brace
        lines.insert(eif_line, " "*prev_indent + "}\n")
        # Adjust subsequent positions
        for j in range(idx+1, len(eifs_shifted)):
            eifs_shifted[j] = (eifs_shifted[j][0] + 1, eifs_shifted[j][1])

# ── 5. Add closing for last tab branch (tab 4) before else -> {} ──
# Find else -> {}
else_idx = None
for i in range(eifs_shifted[-1][0]+1, len(lines)):
    if "else -> {}" in lines[i]:
        else_idx = i; break
assert else_idx, "No else -> {}"

# Last tab's indent
last_tab_indent = 32  # all branches open at 32
prev_text = lines[else_idx-1].strip()
if prev_text != "}":
    lines.insert(else_idx, " "*last_tab_indent + "}\n")
elif len(lines[else_idx-1]) - len(lines[else_idx-1].lstrip()) != last_tab_indent:
    lines[else_idx-1] = " "*last_tab_indent + "}\n"

# ── 6. Replace chain closing with else->{} + when close + AC close ──
# Recalculate chain_close position
for i in range(else_idx+1, len(lines)):
    if lines[i].strip() == "}" and len(lines[i]) - len(lines[i].lstrip()) == 24:
        chain_close_new = i; break

# Replace the single "}" with else->{} + when close + AC close
closing = [
    I+"        else -> {}\n",
    I+"    }\n",
    I+"},\n",
]
lines[chain_close_new:chain_close_new+1] = closing

# ── 7. Verify brace balance after tab changes ──
content = "".join(lines)
o1, c1 = content.count('{'), content.count('}')
print("[CHECK] After tabs: open=" + str(o1) + " close=" + str(c1) + " balanced=" + str(o1==c1))

# ── 8. Add CONNECT SPOTIFY button ──
logout_idx = None
for i, l in enumerate(lines):
    if ".clickable(onClick = onLogout)" in l and i > 5000:
        logout_idx = i; break

if logout_idx:
    item_idx = None
    for j in range(logout_idx-1, max(0, logout_idx-10), -1):
        if "item {" in lines[j]:
            item_idx = j; break
    
    if item_idx:
        spotify = [
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
            "        }\n\n",
        ]
        lines[item_idx:item_idx] = spotify
        print("[OK] Added Spotify button")

# Add onSpotifyConnect param
for i, l in enumerate(lines):
    if "onDeleteAccount: (String?) -> Unit = {}" in l and "fun ProfileScreen" not in l:
        lines[i] = "    onSpotifyConnect: () -> Unit = {},\n" + l
        print("[OK] Added onSpotifyConnect param")
        break

# Add onSpotifyConnect to call site
for i, l in enumerate(lines):
    if "subscriptionTier = subscription.tier" in l and i > 2400:
        if i+1 < len(lines) and lines[i+1].strip() == ")":
            lines[i] = l.rstrip() + ",\n"
            call = [
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
            lines[i+1:i+1] = call
            print("[OK] Added onSpotifyConnect to call site")
            break

# ── 9. FINAL VERIFICATION ──
content = "".join(lines)
o2, c2 = content.count('{'), content.count('}')
print("[FINAL] open=" + str(o2) + " close=" + str(c2) + " balanced=" + str(o2==c2))

if o2 != c2:
    print("[ERR] IMBALANCED by " + str(o2-c2) + " — NOT writing!")
    exit(1)

with open(MAIN_KT, "w", encoding="utf-8") as f:
    f.writelines(lines)
print("[DONE] Written " + str(len(lines)) + " lines")
