const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/TodayWorkoutScreen.kt';
let c = fs.readFileSync(file, 'utf8');
let changes = 0;

// Fix 1: Add animateFloat import for InfiniteTransition
const imp1 = 'import androidx.compose.animation.core.animateFloatAsState';
if (!c.includes('import androidx.compose.animation.core.animateFloat')) {
    c = c.replace(imp1, 'import androidx.compose.animation.core.animateFloat\n' + imp1);
    changes++;
    console.log('1. Added animateFloat import');
}

// Fix 2: Use fully qualified Offset and Size inside Canvas to avoid DrawScope shadowing
const oldCanvas = `            for (i in 0 until barCount) {
                val barHeight = (sin(eqTime + i * 0.5f) * 0.5f + 0.5f) * maxHeight
                drawRect(
                    color = SpotifyGreen,
                    topLeft = Offset(i * barWidth, size.height - barHeight),
                    size = Size(barWidth * 0.7f, barHeight)
                )`;

const newCanvas = `            for (i in 0 until barCount) {
                val barHeight = (sin(eqTime + i * 0.5f) * 0.5f + 0.5f) * maxHeight
                drawRect(
                    color = SpotifyGreen,
                    topLeft = androidx.compose.ui.geometry.Offset(i * barWidth, size.height - barHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth * 0.7f, barHeight)
                )`;

if (c.includes(oldCanvas)) {
    c = c.replace(oldCanvas, newCanvas);
    changes++;
    console.log('2. Fixed Offset/Size with fully qualified names');
} else {
    console.log('2. Canvas pattern not found');
}

fs.writeFileSync(file, c, 'utf8');
console.log('Total fixes: ' + changes);
