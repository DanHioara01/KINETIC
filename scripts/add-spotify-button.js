const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/TodayWorkoutScreen.kt';
let c = fs.readFileSync(file, 'utf8');

// The SpotifyAnimatedButton composable to add
const spotifyButton = `
@Composable
private fun SpotifyAnimatedButton(onOpenSpotify: () -> Unit) {
    val SpotifyGreen = Color(0xFF1DB954)
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()

    val buttonScale = remember { Animatable(1f) }
    val logoScale = remember { Animatable(1f) }
    val logoAlpha = remember { Animatable(1f) }
    val flashAlpha = remember { Animatable(0f) }
    var triggerCount by remember { mutableIntStateOf(0) }
    var isTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(triggerCount) {
        if (triggerCount == 0) return@LaunchedEffect
        isTriggered = true

        // Press down
        scope.launch {
            buttonScale.animateTo(
                0.88f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessHigh)
            )
            buttonScale.animateTo(
                1f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
            )
        }

        // Logo pulse
        scope.launch {
            logoScale.animateTo(1.15f, animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessHigh))
            logoAlpha.animateTo(0.7f, animationSpec = spring(dampingRatio = 0.5f))
            logoScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium))
            logoAlpha.animateTo(1f, animationSpec = spring(dampingRatio = 0.7f))
        }

        // Flash
        scope.launch {
            flashAlpha.snapTo(0.6f)
            flashAlpha.animateTo(0f, animationSpec = spring(dampingRatio = 1f, stiffness = Spring.StiffnessLow))
        }

        isTriggered = false
        onOpenSpotify()
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Shockwave ring
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(Color(0xFF1DB954).copy(alpha = 0.05f))
        )

        // Flash overlay
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = flashAlpha.value))
        )

        // Main button (3D effect)
        Box(
            modifier = Modifier
                .size(130.dp)
                .scale(buttonScale.value)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF2A2A2A), Color(0xFF0A0A0A)),
                        center = Offset(40f, 40f)
                    )
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    if (!isTriggered) {
                        triggerCount++
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_spotify),
                contentDescription = "Open Spotify",
                tint = SpotifyGreen,
                modifier = Modifier
                    .size(75.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )
        }
    }
}`;

// Add the composable function right before the last closing brace of the file
const lastBrace = c.lastIndexOf('}');
if (lastBrace !== -1) {
    c = c.substring(0, lastBrace) + spotifyButton + '\n' + c.substring(lastBrace);
    fs.writeFileSync(file, c, 'utf8');
    console.log('SUCCESS: Added SpotifyAnimatedButton composable function');
} else {
    console.log('ERROR: Could not find last closing brace');
}
