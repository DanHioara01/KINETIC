const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/TodayWorkoutScreen.kt';
let c = fs.readFileSync(file, 'utf8');
let changes = 0;

// Check if graphicsLayer is already imported
if (!c.includes('import androidx.compose.ui.graphicsLayer')) {
    // Add graphicsLayer import after the existing Brush import
    c = c.replace(
        'import androidx.compose.ui.graphics.Brush',
        'import androidx.compose.ui.graphics.Brush\nimport androidx.compose.ui.graphicsLayer'
    );
    changes++;
    console.log('1. Added graphicsLayer import');
}

// Replace the GlassCard modifier in itemsIndexed to add slide-up animation
const oldCard = '            itemsIndexed(exercises) { index, exercise ->\n                GlassCard(\n                    modifier = Modifier.fillMaxWidth(),';

const newCard = `            itemsIndexed(exercises) { index, exercise ->
                // Staggered slide-up animation: each card delays based on index
                val animOffsetY = remember { Animatable(60f) }
                val animAlpha = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(index * 80L)
                    animOffsetY.animateTo(
                        0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    animAlpha.animateTo(1f, animationSpec = spring(dampingRatio = 1f, stiffness = Spring.StiffnessLow))
                }

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = animOffsetY.value
                            alpha = animAlpha.value
                        },`;

if (c.includes(oldCard)) {
    c = c.replace(oldCard, newCard);
    changes++;
    console.log('2. Added slide-up animation to exercise cards');
} else {
    console.log('WARN: Could not find exact itemsIndexed block');
}

fs.writeFileSync(file, c, 'utf8');
console.log('Total changes: ' + changes);
