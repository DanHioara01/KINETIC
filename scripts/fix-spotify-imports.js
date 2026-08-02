const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/TodayWorkoutScreen.kt';
let c = fs.readFileSync(file, 'utf8');
let changes = 0;

// Add all missing imports after the existing Animatable import block
const marker = 'import androidx.compose.foundation.interaction.MutableInteractionSource';
const extraImports = [
    'import androidx.compose.ui.geometry.Offset',
    'import androidx.compose.ui.graphics.Brush',
    'import androidx.compose.ui.graphics.graphicsLayer',
    'import androidx.compose.ui.graphics.vector.ImageVector',
    'import kotlinx.coroutines.launch'
].join('\n');

if (c.includes(marker) && !c.includes('import androidx.compose.ui.geometry.Offset')) {
    c = c.replace(marker, marker + '\n' + extraImports);
    changes++;
    console.log('1. Added Offset, Brush, graphicsLayer, ImageVector, launch imports');
}

// Fix the private modifier issue - SpotifyAnimatedButton should not have 'private' in the composable annotation
// The function needs to be accessible from within the same file, so private is fine
// But the error says "Modifier 'private' is not applicable to 'local function'"
// This means the function is defined INSIDE another function - it should be at file level

// Check if SpotifyAnimatedButton is inside CycleWorkoutContent
const cycleEnd = c.indexOf('\n}\n\n@Composable\nprivate fun SpotifyAnimatedButton');
if (cycleEnd === -1) {
    // It might be inside the function - let's check
    const spotifyIdx = c.indexOf('private fun SpotifyAnimatedButton');
    const cycleContentIdx = c.indexOf('private fun CycleWorkoutContent');
    if (spotifyIdx > cycleContentIdx) {
        console.log('2. SpotifyAnimatedButton is inside CycleWorkoutContent - need to move it out');
        // Find the end of CycleWorkoutContent function
        // Count braces to find the end
        let braceCount = 0;
        let cycleStart = c.indexOf('@Composable\nprivate fun CycleWorkoutContent');
        let foundStart = false;
        let cycleEndIdx = cycleStart;
        
        for (let i = cycleStart; i < c.length; i++) {
            if (c[i] === '{') { braceCount++; foundStart = true; }
            if (c[i] === '}') { braceCount--; }
            if (foundStart && braceCount === 0) {
                cycleEndIdx = i + 1;
                break;
            }
        }
        
        // Extract SpotifyAnimatedButton
        const spotifyStart = c.indexOf('@Composable\nprivate fun SpotifyAnimatedButton');
        const spotifyBlock = c.substring(spotifyStart);
        
        // Remove SpotifyAnimatedButton from inside CycleWorkoutContent
        c = c.substring(0, spotifyStart).trimEnd() + '\n}\n\n' + spotifyBlock;
        changes++;
        console.log('2. Moved SpotifyAnimatedButton outside CycleWorkoutContent');
    }
}

// Also need to add mutableIntStateOf import if not present
if (!c.includes('import androidx.compose.runtime.mutableIntStateOf')) {
    // mutableIntStateOf is available via the wildcard import androidx.compose.runtime.*
    // But let's make sure the wildcard import exists
    if (!c.includes('import androidx.compose.runtime.*')) {
        const runtimeMarker = 'import androidx.compose.runtime.remember';
        if (c.includes(runtimeMarker)) {
            c = c.replace(runtimeMarker, 'import androidx.compose.runtime.*');
            changes++;
            console.log('3. Added wildcard runtime import for mutableIntStateOf');
        }
    }
}

fs.writeFileSync(file, c, 'utf8');
console.log('Total changes: ' + changes);
