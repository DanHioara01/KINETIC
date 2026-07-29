#!/usr/bin/env python3
import os, re

MAIN_KT = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(MAIN_KT, "r", encoding="utf-8") as f:
    content = f.read()

original = content

# 1. Add slide imports
if "import androidx.compose.animation.slideInHorizontally" not in content:
    content = content.replace(
        "import androidx.compose.animation.AnimatedContent\nimport androidx.compose.foundation.clickable",
        "import androidx.compose.animation.AnimatedContent\nimport androidx.compose.animation.slideInHorizontally\nimport androidx.compose.animation.slideOutHorizontally\nimport androidx.compose.foundation.clickable",
    )
    print("[OK] Added slide imports")
else:
    print("[SKIP] Slide imports already present")

# 2. Find tab chain boundaries
tab_chain_start = content.find("if (currentDashboardTab == 0) {")
navbar_marker = "// === Floating Bottom Navbar ==="
navbar_pos = content.find(navbar_marker)

if tab_chain_start == -1:
    print("[ERR] Could not find tab chain start")
    exit(1)
if navbar_pos == -1:
    print("[ERR] Could not find navbar marker")
    exit(1)

# Find indentation
if_line_start = content.rfind("\n", 0, tab_chain_start) + 1
if_line = content[if_line_start:content.index("\n", tab_chain_start)]
indent = if_line[:len(if_line) - len(if_line.lstrip())]
print("[INFO] Indent: '" + indent + "'")

# 3. Replace opening if with AnimatedContent + when
if "AnimatedContent(" not in content[tab_chain_start-300:tab_chain_start]:
    replacement = (
        "AnimatedContent(\n"
        + indent + "    targetState = currentDashboardTab,\n"
        + indent + "    transitionSpec = {\n"
        + indent + "        val direction = if (targetState > initialState) 1 else -1\n"
        + indent + "        slideInHorizontally(tween(300)) { it * direction / 4 } + fadeIn(tween(250)) togetherWith\n"
        + indent + "            slideOutHorizontally(tween(250)) { -it * direction / 4 } + fadeOut(tween(200))\n"
        + indent + "    },\n"
        + indent + "    label = \"tabTransition\"\n"
        + indent + ") { tab ->\n"
        + indent + "    when (tab) {\n"
        + indent + "        0 -> {"
    )
    content = content[:tab_chain_start] + replacement + content[tab_chain_start + len("if (currentDashboardTab == 0) {"):]
    print("[OK] Added AnimatedContent + when wrapper")

# 4. Replace else-if branches with when branches
pattern = r"} else if \(currentDashboardTab == (\d+)\) \{"
def replace_else_if(m):
    return indent + "        " + m.group(1) + " -> {"
content_new = re.sub(pattern, replace_else_if, content)
if content_new != content:
    count = len(re.findall(pattern, content))
    content = content_new
    print("[OK] Replaced " + str(count) + " else-if branches")

# 5. Close the when block - find the closing before navbar
# Look for the pattern: the closing "}" of the last tab branch,
# then "}" closing the content lambda, then "}" closing the Scaffold
# Right before "// === Floating Bottom Navbar ==="
closing_search = content[tab_chain_start:navbar_pos]
# The last few lines before navbar should be like:
#   ...subscriptionTier = subscription.tier\n                            )\n                        }\n                    }\n                }\n\n
# We need to add "else -> {}" and close when/AnimatedContent after the last tab's "}"

# Find position of navbar and work backwards
# Find "                }\n\n                // === Floating Bottom Navbar ==="
close_pattern = "                }\n\n                // === Floating Bottom Navbar ==="
close_pos = content.find(close_pattern, tab_chain_start)
if close_pos != -1:
    # Insert else -> {} and close the when/AnimatedContent
    insert = (
        indent + "        else -> {}\n"
        + indent + "    }\n"
        + indent + "},\n"
    )
    content = content[:close_pos] + insert + content[close_pos:]
    print("[OK] Added else -> {} and closed when/AnimatedContent")
else:
    print("[WARN] Could not find exact closing pattern")

# 6. Create ic_spotify.xml
spotify_xml = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\res\drawable\ic_spotify.xml"
spotify_content = """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#1DB954">
    <path
        android:fillColor="#1DB954"
        android:pathData="M12,0C5.4,0 0,5.4 0,12s5.4,12 12,12 12,-5.4 12,-12S18.66,0 12,0zM17.51,17.34c-0.23,0.38 -0.69,0.5 -1.07,0.27 -2.93,-1.79 -6.61,-2.2 -10.95,-1.21c-0.42,0.09 -0.83,-0.15 -0.92,-0.57c-0.09,-0.42 0.15,-0.83 0.57,-0.92c4.82,-1.09 8.93,-0.62 12.21,1.37c0.38,0.23 0.5,0.69 0.27,1.07l-0.11,0.06zM19.77,14.32c-0.29,0.47 -0.87,0.62 -1.34,0.33 -3.34,-2.05 -8.43,-2.65 -12.36,-1.45c-0.49,0.15 -1.01,-0.12 -1.15,-0.61c-0.15,-0.49 0.12,-1.01 0.61,-1.15c4.53,-1.37 10.18,-0.71 14.01,1.64c0.47,0.29 0.62,0.87 0.33,1.34l-0.1,0.0zM19.95,11.27c-4.01,-2.38 -10.62,-2.59 -14.44,-1.43c-0.58,0.18 -1.2,-0.14 -1.38,-0.72c-0.18,-0.58 0.14,-1.2 0.72,-1.38c4.37,-1.33 11.72,-1.08 16.41,1.71c0.53,0.31 0.7,1.02 0.39,1.55c-0.31,0.53 -1.02,0.7 -1.55,0.39l-0.15,-0.12z"/>
</vector>"""

with open(spotify_xml, "w", encoding="utf-8") as f:
    f.write(spotify_content)
print("[OK] Created ic_spotify.xml")

# 7. Add CONNECT SPOTIFY button before logout
logout_marker = ".clickable(onClick = onLogout)"
if logout_marker in content:
    logout_pos = content.index(logout_marker)
    item_start = content.rfind("        item {", 0, logout_pos)
    if item_start != -1:
        spotify_button = (
            "        item {\n"
            "            Spacer(Modifier.height(4.dp))\n"
            "            Card(\n"
            "                modifier = Modifier\n"
            "                    .fillMaxWidth()\n"
            "                    .clickable {\n"
            "                        val spotifyPackage = \"com.spotify.music\"\n"
            "                        val intent = context.packageManager.getLaunchIntentForPackage(spotifyPackage)\n"
            "                        if (intent != null) {\n"
            "                            context.startActivity(intent)\n"
            "                        } else {\n"
            "                            try {\n"
            "                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(\"market://details?id=$spotifyPackage\")))\n"
            "                            } catch (e: Exception) {\n"
            "                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(\"https://play.google.com/store/apps/details?id=$spotifyPackage\")))\n"
            "                            }\n"
            "                        }\n"
            "                    },\n"
            "                colors = CardDefaults.cardColors(containerColor = Color(0xFF191414)),\n"
            "                shape = RoundedCornerShape(16.dp)\n"
            "            ) {\n"
            "                Row(\n"
            "                    modifier = Modifier\n"
            "                        .fillMaxWidth()\n"
            "                        .padding(horizontal = 16.dp, vertical = 14.dp),\n"
            "                    verticalAlignment = Alignment.CenterVertically,\n"
            "                    horizontalArrangement = Arrangement.Center\n"
            "                ) {\n"
            "                    Image(\n"
            "                        painter = painterResource(id = R.drawable.ic_spotify),\n"
            "                        contentDescription = \"Spotify\",\n"
            "                        modifier = Modifier.size(22.dp)\n"
            "                    )\n"
            "                    Spacer(modifier = Modifier.width(10.dp))\n"
            "                    Text(\n"
            "                        \"CONNECT SPOTIFY\",\n"
            "                        color = Color(0xFF1DB954),\n"
            "                        style = MaterialTheme.typography.bodyLarge,\n"
            "                        fontWeight = FontWeight.Bold,\n"
            "                        letterSpacing = 0.5.sp\n"
            "                    )\n"
            "                }\n"
            "            }\n"
            "        }\n\n"
        )
        content = content[:item_start] + spotify_button + content[item_start:]
        print("[OK] Added CONNECT SPOTIFY button before logout")
    else:
        print("[ERR] Could not find item block before logout")
else:
    print("[ERR] Could not find logout marker")

# Write
if content != original:
    with open(MAIN_KT, "w", encoding="utf-8") as f:
        f.write(content)
    diff = len(content) - len(original)
    print("[DONE] All changes applied. Diff: " + str(diff) + " chars")
else:
    print("[WARN] No changes were made")
