const fs = require('fs');
const path = 'app/src/main/java/com/example/kinetic/TodayWorkoutScreen.kt';
let content = fs.readFileSync(path, 'utf8');

// Step 1: Check if SpotifyWorkoutCard already exists
if (content.includes('fun SpotifyWorkoutCard')) {
    console.log('SpotifyWorkoutCard already exists, skipping');
    process.exit(0);
}

// Step 2: Add SpotifyWorkoutCard function and drawEqualizer helper at end of file
const spotifyFunctions = `
@Composable
private fun SpotifyWorkoutCard(onOpenSpotify: () -> Unit) {
    val SpotifyGreen = Color(0xFF1DB954)
    val context = LocalContext.current
    var isTriggered by remember { mutableStateOf(false) }
    var triggerTime by remember { mutableStateOf(0L) }

    // Equalizer animation
    val infiniteTransition = rememberInfiniteTransition()
    val eqTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing))
    )
    val eqAlpha by animateFloatAsState(
        targetValue = if (isTriggered) 0.15f else 0f,
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

    // Card design - green background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clip(RoundedCornerShape(16.dp))
            .background(SpotifyGreen)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                triggerTime = System.currentTimeMillis()
            },
        contentAlignment = Alignment.Center
    ) {
        // Equalizer bars drawn on Canvas (background animation)
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .alpha(eqAlpha)
                .clip(RoundedCornerShape(16.dp))
        ) {
            drawEqualizer(eqTime)
        }

        // Row content
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spotify logo box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(start = 16.dp)
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

            Spacer(modifier = Modifier.width(16.dp))

            // Text
            Text(
                text = if (isTriggered) "Starting workout music..." else "Tap to play on Spotify",
                color = Color.White.copy(alpha = if (isTriggered) 0.5f else 1f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            // Play arrow
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = if (isTriggered) SpotifyGreen else Color.Gray,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))
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
fs.writeFileSync(path, content, 'utf8');
console.log('SUCCESS: Added SpotifyWorkoutCard + drawEqualizer');
