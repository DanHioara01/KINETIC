const fs = require('fs');
const path = 'app/src/main/java/com/example/kinetic/TodayWorkoutScreen.kt';
let content = fs.readFileSync(path, 'utf8');
let changed = false;

// Step 1: Add missing imports after 'import coil.compose.AsyncImage'
const importMarker = 'import coil.compose.AsyncImage';
const missingImports = `import coil.compose.AsyncImage
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sin
import kotlinx.coroutines.delay`;

if (!content.includes('import androidx.compose.foundation.Canvas')) {
    content = content.replace(importMarker, missingImports);
    changed = true;
    console.log('Step 1: Added missing imports');
} else {
    console.log('Step 1: Imports already present');
}

// Step 2: Replace old frame1 Image with SpotifyWorkoutCard call
const oldButton = `        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.frame1),
                    contentDescription = "Open Spotify",
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenSpotify() }
                )
            }
        }`;

const newButton = `        item {
            SpotifyWorkoutCard(onOpenSpotify = onOpenSpotify)
        }`;

if (content.includes(oldButton)) {
    content = content.replace(oldButton, newButton);
    changed = true;
    console.log('Step 2: Replaced old frame1 Image with SpotifyWorkoutCard');
} else if (content.includes('SpotifyWorkoutCard(onOpenSpotify = onOpenSpotify)')) {
    console.log('Step 2: SpotifyWorkoutCard call already exists');
} else {
    console.log('Step 2: ERROR - Old button pattern not found and SpotifyWorkoutCard not present');
}

// Step 3: Add SpotifyWorkoutCard and drawEqualizer functions at end of file
if (!content.includes('private fun SpotifyWorkoutCard(')) {
    const spotifyFunctions = `

@Composable
private fun SpotifyWorkoutCard(onOpenSpotify: () -> Unit) {
    val SpotifyGreen = Color(0xFF1DB954)
    val context = LocalContext.current
    var isTriggered by remember { mutableStateOf(false) }
    var triggerTime by remember { mutableStateOf(0L) }

    val infiniteTransition = rememberInfiniteTransition()
    val eqTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing))
    )

    val eqAlpha by animateFloatAsState(
        targetValue = if (isTriggered) 0.7f else 0f,
        animationSpec = tween(200),
        label = "eqAlpha"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "cardScale"
    )

    LaunchedEffect(triggerTime) {
        if (triggerTime > 0) {
            isTriggered = true
            delay(500)
            openSpotifyApp(context)
            delay(1500)
            isTriggered = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                try {
                    val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator?.vibrate(android.os.VibrationEffect.createOneShot(30, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(30)
                    }
                } catch (_: Exception) {}
                triggerTime = System.currentTimeMillis()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .alpha(eqAlpha)
                .clip(RoundedCornerShape(16.dp))
        ) {
            drawEqualizer(eqTime)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SpotifyGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_spotify),
                    contentDescription = "Spotify",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = if (isTriggered) "Starting workout music..." else "Tap to play on Spotify",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = SpotifyGreen,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

private fun DrawScope.drawEqualizer(time: Float) {
    val SpotifyGreen = Color(0xFF1DB954)
    val numBars = 20
    val barSpacing = size.width / numBars
    val barWidth = barSpacing * 0.6f

    for (i in 0 until numBars) {
        val phase = i * 0.8f
        val heightFraction = 0.3f + 0.6f * (Math.sin(time.toDouble() + phase).toFloat() * 0.5f + 0.5f)
        val barHeight = size.height * heightFraction

        drawRect(
            color = SpotifyGreen,
            topLeft = Offset(
                x = i * barSpacing + (barSpacing - barWidth) / 2,
                y = size.height - barHeight
            ),
            size = Size(width = barWidth, height = barHeight)
        )
    }
}
`;
    content = content.trimEnd() + spotifyFunctions;
    changed = true;
    console.log('Step 3: Added SpotifyWorkoutCard and drawEqualizer functions');
} else {
    console.log('Step 3: SpotifyWorkoutCard function already exists');
}

// Step 4: Add Brush import if missing
if (!content.includes('import androidx.compose.ui.graphics.Brush')) {
    content = content.replace(
        'import androidx.compose.ui.graphics.Color',
        'import androidx.compose.ui.graphics.Brush\nimport androidx.compose.ui.graphics.Color'
    );
    changed = true;
    console.log('Step 4: Added Brush import');
} else {
    console.log('Step 4: Brush import already present');
}

if (changed) {
    fs.writeFileSync(path, content, 'utf8');
    console.log('File saved successfully');
} else {
    console.log('No changes needed');
}

// Verify
const finalContent = fs.readFileSync(path, 'utf8');
console.log('\nVerification:');
console.log('- Has SpotifyWorkoutCard call:', finalContent.includes('SpotifyWorkoutCard(onOpenSpotify = onOpenSpotify)'));
console.log('- Has SpotifyWorkoutCard function:', finalContent.includes('private fun SpotifyWorkoutCard('));
console.log('- Has drawEqualizer:', finalContent.includes('private fun DrawScope.drawEqualizer'));
console.log('- Has Canvas import:', finalContent.includes('import androidx.compose.foundation.Canvas'));
console.log('- Has LocalContext import:', finalContent.includes('import androidx.compose.ui.platform.LocalContext'));
console.log('- Has Brush import:', finalContent.includes('import androidx.compose.ui.graphics.Brush'));
console.log('- Has ic_spotify:', finalContent.includes('R.drawable.ic_spotify'));
console.log('- No frame1:', !finalContent.includes('R.drawable.frame1'));
console.log('- Line count:', finalContent.split('\n').length);
