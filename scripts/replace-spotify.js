const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/TodayWorkoutScreen.kt';
let c = fs.readFileSync(file, 'utf8');
let changes = 0;

// 1. Replace the call site
if (c.includes('SpotifyAnimatedButton(onOpenSpotify = onOpenSpotify)')) {
    c = c.replace('SpotifyAnimatedButton(onOpenSpotify = onOpenSpotify)', 'SpotifyWorkoutCard(onOpenSpotify = onOpenSpotify)');
    changes++;
    console.log('1. Updated call site');
} else {
    console.log('1. Call site not found');
}

// 2. Find and replace the old function with new one
const funcStart = c.indexOf('private fun SpotifyAnimatedButton(');
if (funcStart === -1) {
    console.log('2. Old function not found');
    fs.writeFileSync(file, c, 'utf8');
    process.exit(1);
}

// Find the end of the function by counting braces
let braceCount = 0;
let funcEnd = funcStart;
let foundFirst = false;
for (let i = funcStart; i < c.length; i++) {
    if (c[i] === '{') { braceCount++; foundFirst = true; }
    if (c[i] === '}') braceCount--;
    if (foundFirst && braceCount === 0) { funcEnd = i + 1; break; }
}

const oldFunc = c.substring(funcStart, funcEnd);
console.log('2. Found old function (' + oldFunc.length + ' chars)');

const newFunc = `private fun SpotifyWorkoutCard(onOpenSpotify: () -> Unit) {
    val SpotifyGreen = Color(0xFF1DB954)
    val CardBackground = Color(0xFF1E1E1E)
    val CardBorder = Color(0xFF2A2A2A)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isTriggered by remember { mutableStateOf(false) }
    var triggerTime by remember { mutableStateOf(0L) }

    // Equalizer background animation (runs constantly but invisible until tap)
    val infiniteTransition = rememberInfiniteTransition()
    val eqTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing))
    )

    // Background equalizer alpha (0 invisible, 0.2 visible)
    val eqAlpha by animateFloatAsState(
        targetValue = if (isTriggered) 0.2f else 0f,
        animationSpec = tween(200),
        label = "eqAlpha"
    )

    // Card press state
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "cardScale"
    )

    // Tap -> wait 0.5s -> open Spotify -> wait 1.5s -> reset
    LaunchedEffect(triggerTime) {
        if (triggerTime > 0) {
            isTriggered = true
            delay(500)
            openSpotifyApp(context)
            delay(1500)
            isTriggered = false
        }
    }

    // Card design
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                triggerTime = System.currentTimeMillis()
            },
        contentAlignment = Alignment.Center
    ) {
        // Equalizer bars drawn on Canvas
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .alpha(eqAlpha)
                .clip(RoundedCornerShape(20.dp))
        ) {
            val barCount = 20
            val barWidth = size.width / barCount
            val maxHeight = size.height * 0.8f
            for (i in 0 until barCount) {
                val barHeight = (sin(eqTime + i * 0.5f) * 0.5f + 0.5f) * maxHeight
                drawRect(
                    color = SpotifyGreen,
                    topLeft = Offset(i * barWidth, size.height - barHeight),
                    size = Size(barWidth * 0.7f, barHeight)
                )
            }
        }

        // Spotify logo
        Icon(
            painter = painterResource(id = R.drawable.ic_spotify),
            contentDescription = "Open Spotify",
            tint = Color.Unspecified,
            modifier = Modifier.size(60.dp)
        )
    }
}`;

c = c.substring(0, funcStart) + newFunc + c.substring(funcEnd);
changes++;
console.log('2. Replaced SpotifyAnimatedButton with SpotifyWorkoutCard');

// 3. Add missing imports after the last import line
const importsToAdd = [
    'import androidx.compose.foundation.Canvas',
    'import androidx.compose.foundation.border',
    'import androidx.compose.foundation.interaction.collectIsPressedAsState',
    'import androidx.compose.ui.draw.alpha',
    'import androidx.compose.ui.draw.scale',
    'import androidx.compose.ui.geometry.Size',
    'import androidx.compose.ui.platform.LocalContext',
    'import kotlin.math.sin',
    'import androidx.compose.animation.core.LinearEasing',
    'import androidx.compose.animation.core.animateFloatAsState',
    'import androidx.compose.animation.core.infiniteRepeatable',
    'import androidx.compose.animation.core.rememberInfiniteTransition',
    'import androidx.compose.animation.core.tween',
    'import kotlinx.coroutines.delay'
];

let addedImports = 0;
for (const imp of importsToAdd) {
    if (!c.includes(imp)) {
        // Find last import line
        const lastImportIdx = c.lastIndexOf('\nimport ');
        const endOfLine = c.indexOf('\n', lastImportIdx + 1);
        c = c.substring(0, endOfLine + 1) + imp + '\n' + c.substring(endOfLine + 1);
        addedImports++;
    }
}
if (addedImports > 0) {
    changes++;
    console.log('3. Added ' + addedImports + ' missing imports');
} else {
    console.log('3. All imports already present');
}

fs.writeFileSync(file, c, 'utf8');
console.log('Total changes: ' + changes);
