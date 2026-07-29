#!/usr/bin/env python3
import re

F = r"C:\Users\danhi\OneDrive\Desktop\Kinetic\app\src\main\java\com\example\kinetic\MainActivity.kt"

with open(F, "r", encoding="utf-8") as f:
    L = f.readlines()

c0 = "".join(L).count("{")
print("Original opens:", c0)

# 1. Add slide imports
for i, l in enumerate(L):
    if "import androidx.compose.animation.AnimatedContent" in l:
        if "slideInHorizontally" not in L[i+1]:
            L.insert(i+1, "import androidx.compose.animation.slideInHorizontally\n")
            L.insert(i+2, "import androidx.compose.animation.slideOutHorizontally\n")
        break

# 2. Find "if (currentDashboardTab == 0) {" after line 1500
op = None
for i, l in enumerate(L):
    if "if (currentDashboardTab == 0) {" in l and i > 1500:
        op = i; break

I = "                        "
new = [
    I+"AnimatedContent(\n", I+"    targetState = currentDashboardTab,\n",
    I+"    transitionSpec = {\n",
    I+"        val direction = if (targetState > initialState) 1 else -1\n",
    I+"        slideInHorizontally(tween(300)) { it * direction / 4 } + fadeIn(tween(250)) togetherWith\n",
    I+"            slideOutHorizontally(tween(250)) { -it * direction / 4 } + fadeOut(tween(200))\n",
    I+"    },\n", I+'    label = "tabTransition"\n',
    I+") { tab ->\n", I+"    when (tab) {\n", I+"        0 -> {\n",
]
L[op:op+1] = new
print("Replaced opening")

# 3. Replace ONLY "else if (currentDashboardTab == N) {" with "N -> {"
# The "}" BEFORE it is PRESERVED - it closes the previous branch
for i in range(len(L)-1, op+len(new), -1):
    l = L[i]
    m = re.match(r'^(\s*)\} else if \(currentDashboardTab == (\d+)\) \{', l)
    if m:
        indent = m.group(1)
        tab = m.group(2)
        # Replace entire line: keep "}", change "else if (...) {" to "N -> {"
        L[i] = indent + "} " + tab + " -> {\n"
        print("Tab", tab, "at line", i+1)

# 4. Replace final chain closing "}" with else->{} + when + AC close
# Find: "}" at 24 spaces, then "}" at 20, then "}" at 16
for i in range(len(L)):
    s = L[i].strip()
    ind = len(L[i]) - len(L[i].lstrip())
    if s == "}" and ind == 20 and i+2 < len(L):
        if L[i+1].strip() == "}" and len(L[i+1])-len(L[i+1].lstrip()) == 16:
            # Go back to find } at 24
            for j in range(i-1, op+len(new), -1):
                if L[j].strip() == "}" and len(L[j])-len(L[j].lstrip()) == 24:
                    L[j:j+1] = [I+"        else -> {}\n", I+"    }\n", I+"},\n"]
                    print("Added else->{} + closes")
                    break
            break

# Verify
c1 = "".join(L).count("{")
c2 = "".join(L).count("}")
print("After tabs: opens=", c1, "closes=", c2, "balanced=", c1==c2)

if c1 != c2:
    print("IMBALANCED! Not writing.")
    exit(1)

# 5. Add Spotify button before logout
for i, l in enumerate(L):
    if ".clickable(onClick = onLogout)" in l and i > 5000:
        for j in range(i-1, max(0,i-10), -1):
            if "item {" in L[j]:
                sp = ["        item {\n","            Spacer(Modifier.height(4.dp))\n","            Card(\n","                modifier = Modifier\n","                    .fillMaxWidth()\n","                    .clickable(onClick = onSpotifyConnect),\n","                colors = CardDefaults.cardColors(containerColor = Color(0xFF191414)),\n","                shape = RoundedCornerShape(16.dp)\n","            ) {\n","                Row(\n","                    modifier = Modifier\n","                        .fillMaxWidth()\n","                        .padding(horizontal = 16.dp, vertical = 14.dp),\n","                    verticalAlignment = Alignment.CenterVertically,\n","                    horizontalArrangement = Arrangement.Center\n","                ) {\n","                    Image(\n",'                        painter = painterResource(id = R.drawable.ic_spotify),\n','                        contentDescription = "Spotify",\n',"                        modifier = Modifier.size(22.dp)\n","                    )\n","                    Spacer(modifier = Modifier.width(10.dp))\n","                    Text(\n",'                        "CONNECT SPOTIFY",\n',"                        color = Color(0xFF1DB954),\n","                        style = MaterialTheme.typography.bodyLarge,\n","                        fontWeight = FontWeight.Bold,\n","                        letterSpacing = 0.5.sp\n","                    )\n","                }\n","            }\n","        }\n\n",]
                L[j:j] = sp
                print("Added Spotify button")
                break
        break

# 6. Add onSpotifyConnect param
for i, l in enumerate(L):
    if "onDeleteAccount: (String?) -> Unit = {}" in l and "fun ProfileScreen" not in l:
        L[i] = "    onSpotifyConnect: () -> Unit = {},\n" + l
        print("Added onSpotifyConnect param")
        break

# 7. Add onSpotifyConnect to call site
for i, l in enumerate(L):
    if "subscriptionTier = subscription.tier" in l and i > 2400:
        if i+1 < len(L) and L[i+1].strip() == ")":
            L[i] = l.rstrip() + ",\n"
            call = ["                                onSpotifyConnect = {\n",'                                    val spotifyPackage = "com.spotify.music"\n',"                                    val intent = context.packageManager.getLaunchIntentForPackage(spotifyPackage)\n","                                    if (intent != null) {\n","                                        context.startActivity(intent)\n","                                    } else {\n","                                        try {\n",'                                            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$spotifyPackage")))\n',"                                        } catch (e: Exception) {\n",'                                            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=$spotifyPackage")))\n',"                                        }\n","                                    }\n","                                }\n",]
            L[i+1:i+1] = call
            print("Added onSpotifyConnect to call site")
            break

# FINAL VERIFY
c1 = "".join(L).count("{")
c2 = "".join(L).count("}")
print("FINAL: opens=", c1, "closes=", c2, "balanced=", c1==c2)
if c1 != c2:
    print("IMBALANCED! Not writing.")
    exit(1)

with open(F, "w", encoding="utf-8") as f:
    f.writelines(L)
print("DONE:", len(L), "lines written")
