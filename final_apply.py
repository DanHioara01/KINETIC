#!/usr/bin/env python3
import re

MAIN_KT = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(MAIN_KT, "r", encoding="utf-8") as f:
    lines = f.readlines()

print("[INFO] Starting: " + str(len(lines)) + " lines")

# Verify original balance
content = "".join(lines)
o0, c0 = content.count('{'), content.count('}')
assert o0 == c0, "Original not balanced!"
print("[OK] Original balanced: " + str(o0) + " open, " + str(c0) + " close")

# 1. Add slide imports
for i, line in enumerate(lines):
    if "import androidx.compose.animation.AnimatedContent" in line:
        if "slideInHorizontally" not in lines[i+1]:
            lines.insert(i+1, "import androidx.compose.animation.slideInHorizontally\n")
            lines.insert(i+2, "import androidx.compose.animation.slideOutHorizontally\n")
            print("[OK] Added slide imports")
        break

# 2. Find Scaffold content lambda and tab chain
scl = None
for i, l in enumerate(lines):
    if ") { innerPadding ->" in l and i > 1500:
        scl = i; break
assert scl, "No Scaffold content lambda"

op = None
for i in range(scl+1, scl+5):
    if "if (currentDashboardTab == 0) {" in lines[i]:
        op = i; break
assert op, "No tab chain start"

# Find the FINAL closing "}" of the if/else chain
# It's "}" at 24 spaces, followed by "}" at 20 spaces (Scaffold content close)
scl_close = None
for i in range(op+1, len(lines)):
    if lines[i].strip() == "}" and len(lines[i]) - len(lines[i].lstrip()) == 20:
        if i+1 < len(lines) and lines[i+1].strip() == "}" and len(lines[i+1]) - len(lines[i+1].lstrip()) == 16:
            scl_close = i; break
assert scl_close, "No Scaffold content lambda close"

chain_close = None
for i in range(scl_close-1, op, -1):
    if lines[i].strip() == "}" and len(lines[i]) - len(lines[i].lstrip()) == 24:
        chain_close = i; break
assert chain_close, "No chain close"

print("[INFO] Open at line " + str(op+1) + ", chain close at line " + str(chain_close+1))

# 3. Replace "if (currentDashboardTab == 0) {" with AnimatedContent wrapper
I = "                        "
new_open = [
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
lines[op:op+1] = new_open
ins = len(new_open) - 1
print("[OK] Replaced opening (" + str(ins) + " lines inserted)")

# 4. Replace "} else if (currentDashboardTab == N) {" with "}\nN -> {"
# KEY: KEEP the } that closes the previous branch!
eifs = []
for i in range(op + ins + 1, len(lines)):
    s = lines[i].strip()
    if s.startswith("} else if (currentDashboardTab ==") and s.endswith("{"):
        m = re.search(r"== (\d+)", s)
        if m:
            eifs.append((i, int(m.group(1))))

print("[INFO] Found " + str(len(eifs)) + " else-if branches")

# Replace from bottom to top to preserve indices
for idx in range(len(eifs)-1, -1, -1):
    eif_line, tab_num = eifs[idx]
    old_line = lines[eif_line]
    old_indent = old_line[:len(old_line) - len(old_line.lstrip())]
    # Replace with: "}\n" + indent + "N -> {\n"
    # The } keeps the old indent (closing previous branch)
    # The N -> { uses indent+4 (opening new branch)
    lines[eif_line] = old_indent + "}\n" + old_indent + "    " + str(tab_num) + " -> {\n"
    print("[OK] Tab " + str(tab_num) + " at line " + str(eif_line+1))

# 5. Replace final chain closing "}" with else -> {} + when close + AC close
# Recalculate chain_close after insertions
for i in range(len(lines)):
    if lines[i].strip() == "}" and len(lines[i]) - len(lines[i].lstrip()) == 20:
        if i+1 < len(lines) and lines[i+1].strip() == "}" and len(lines[i+1]) - len(lines[i+1].lstrip()) == 16:
            # Go back to find the 24-space }
            for j in range(i-1, op+ins, -1):
                if lines[j].strip() == "}" and len(lines[j]) - len(lines[j].lstrip()) == 24:
                    chain_close = j
                    break
            break

closing = [
    I+"        else -> {}\n",
    I+"    }\n",
    I+"},\n",
]
lines[chain_close:chain_close+1] = closing
print("[OK] Added else -> {} + when/AC closing")

# 6. Verify balance after tab changes
content = "".join(lines)
o1, c1 = content.count('{'), content.count('}')
print("[CHECK] After tabs: open=" + str(o1) + " close=" + str(c1) + " balanced=" + str(o1==c1))

if o1 != c1:
    print("[ERR] IMBALANCED! Diff=" + str(o1-c1))
    exit(1)

# 7. Add CONNECT SPOTIFY button
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
                "                                    if (intent != 
