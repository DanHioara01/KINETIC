const fs = require('fs');
const path = 'app/src/main/java/com/example/gymlog2/MainActivity.kt';
const lines = fs.readFileSync(path, 'utf8').split('\n');

// Lines 5116-5171 (1-indexed) = indices 5115-5170 contain the old card
const startLine = 5116; // "        item {"
const endLine = 5171;   // "        }"

// Verify we're replacing the right lines
const startContent = lines[startLine - 1].trim();
const endContent = lines[endLine - 1].trim();
console.log('Start line (' + startLine + '):', startContent);
console.log('End line (' + endLine + '):', endContent);

if (!startContent.includes('item {') || !endContent.includes('}')) {
    console.log('ERROR: Lines do not match expected pattern');
    process.exit(1);
}

const newBlock = `        item {
            // Premium Stat Cards
            val cyanAccent = Color(0xFF00E5FF)
            val greenAccent = Color(0xFF1DB954)
            val purpleAccent = Color(0xFF9F7AEA)
            val purpleTitleColor = Color(0xFFB794F4)
            val goldAccent = Color(0xFFF6E05E)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Total Volume (Cyan)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (isDark) listOf(
                                    cyanAccent.copy(alpha = 0.08f),
                                    Color(0xFF0A1A2A),
                                    Color(0xFF050F15)
                                ) else listOf(
                                    cyanAccent.copy(alpha = 0.06f),
                                    Color(0xFFF0F9FF),
                                    Color(0xFFFFFFFF)
                                )
                            )
                        )
                        .border(1.dp, cyanAccent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(cyanAccent.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(22.dp)) {
                            val barColor = cyanAccent
                            drawRect(
                                color = barColor,
                                topLeft = Offset(size.width * 0.15f, size.height * 0.42f),
                                size = Size(size.width * 0.7f, size.height * 0.16f)
                            )
                            drawRect(
                                color = barColor,
                                topLeft = Offset(0f, size.height * 0.22f),
                                size = Size(size.width * 0.2f, size.height * 0.56f)
                            )
                            drawRect(
                                color = barColor,
                                topLeft = Offset(size.width * 0.8f, size.height * 0.22f),
                                size = Size(size.width * 0.2f, size.height * 0.56f)
                            )
                            drawRect(
                                color = barColor.copy(alpha = 0.5f),
                                topLeft = Offset(size.width * 0.03f, size.height * 0.3f),
                                size = Size(size.width * 0.1f, size.height * 0.4f)
                            )
                            drawRect(
                                color = barColor.copy(alpha = 0.5f),
                                topLeft = Offset(size.width * 0.87f, size.height * 0.3f),
                                size = Size(size.width * 0.1f, size.height * 0.4f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val volFormatted = if (totalVolume >= 1000) String.format("%.1fK", totalVolume / 1000) else String.format("%.0f", totalVolume)
                    Text(
                        text = volFormatted,
                        color = textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "VOLUME",
                        color = cyanAccent.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }

                // Card 2: Workouts (Green)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (isDark) listOf(
                                    greenAccent.copy(alpha = 0.08f),
                                    Color(0xFF0A1A0A),
                                    Color(0xFF050F05)
                                ) else listOf(
                                    greenAccent.copy(alpha = 0.06f),
                                    Color(0xFFF0FFF4),
                                    Color(0xFFFFFFFF)
                                )
                            )
                        )
                        .border(1.dp, greenAccent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(greenAccent.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(22.dp)) {
                            val calColor = greenAccent
                            drawRect(
                                color = calColor,
                                topLeft = Offset(size.width * 0.1f, size.height * 0.3f),
                                size = Size(size.width * 0.8f, size.height * 0.6f)
                            )
                            drawRect(
                                color = calColor,
                                topLeft = Offset(size.width * 0.1f, size.height * 0.3f),
                                size = Size(size.width * 0.8f, size.height * 0.15f)
                            )
                            drawRect(
                                color = calColor,
                                topLeft = Offset(size.width * 0.28f, size.height * 0.12f),
                                size = Size(size.width * 0.08f, size.height * 0.25f)
                            )
                            drawRect(
                                color = calColor,
                                topLeft = Offset(size.width * 0.64f, size.height * 0.12f),
                                size = Size(size.width * 0.08f, size.height * 0.25f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$totalWorkouts",
                        color = textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "WORKOUTS",
                        color = greenAccent.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PB Card — Full Width with Purple-Gold gradient border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isDark) listOf(
                                purpleAccent.copy(alpha = 0.10f),
                                Color(0xFF12091A),
                                Color(0xFF0A0510)
                            ) else listOf(
                                purpleAccent.copy(alpha = 0.06f),
                                Color(0xFFF5F0FF),
                                Color(0xFFFFFFFF)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                purpleAccent.copy(alpha = 0.6f),
                                goldAccent.copy(alpha = 0.6f),
                                purpleAccent.copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(purpleAccent.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            val tColor = goldAccent
                            drawRect(
                                color = tColor,
                                topLeft = Offset(size.width * 0.25f, size.height * 0.08f),
                                size = Size(size.width * 0.5f, size.height * 0.45f)
                            )
                            drawRect(
                                color = tColor,
                                topLeft = Offset(size.width * 0.4f, size.height * 0.53f),
                                size = Size(size.width * 0.2f, size.height * 0.2f)
                            )
                            drawRect(
                                color = tColor,
                                topLeft = Offset(size.width * 0.22f, size.height * 0.73f),
                                size = Size(size.width * 0.56f, size.height * 0.12f)
                            )
                            drawRect(
                                color = tColor.copy(alpha = 0.5f),
                                topLeft = Offset(size.width * 0.1f, size.height * 0.12f),
                                size = Size(size.width * 0.15f, size.height * 0.3f)
                            )
                            drawRect(
                                color = tColor.copy(alpha = 0.5f),
                                topLeft = Offset(size.width * 0.75f, size.height * 0.12f),
                                size = Size(size.width * 0.15f, size.height * 0.3f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "NEW PB'S",
                            color = purpleTitleColor.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$bestStreak best streak",
                            color = textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "\\uD83C\\uDFC6",
                        fontSize = 26.sp
                    )
                }
            }
        }`;

// Replace lines startLine-1 through endLine-1 (0-indexed)
const newLines = [
    ...lines.slice(0, startLine - 1),
    newBlock,
    ...lines.slice(endLine)
];

fs.writeFileSync(path, newLines.join('\n'), 'utf8');
console.log('SUCCESS: Replaced lines ' + startLine + '-' + endLine + ' with premium stat cards');
console.log('New file length:', newLines.length, 'lines');
