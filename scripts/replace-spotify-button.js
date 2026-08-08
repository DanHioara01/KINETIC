const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/TodayWorkoutScreen.kt';
let c = fs.readFileSync(file, 'utf8');
let changes = 0;

// Fix 1: Add missing imports
const importMarker = 'import androidx.compose.foundation.clickable';
const extraImports = [
    'import androidx.compose.animation.core.Animatable',
    'import androidx.compose.animation.core.Spring',
    'import androidx.compose.animation.core.spring',
    'import androidx.compose.foundation.interaction.MutableInteractionSource'
].join('\n');

if (c.includes(importMarker) && !c.includes('import androidx.compose.animation.core.Animatable')) {
    c = c.replace(importMarker, importMarker + '\n' + extraImports);
    changes++;
    console.log('1. Added Animatable, Spring, spring, MutableInteractionSource imports');
}

// Fix 2: Replace the static Image Spotify button with animated composable call
const oldBlock = [
    '        item {',
    '            Box(',
    '                modifier = Modifier.fillMaxWidth(),',
    '                contentAlignment = Alignment.Center',
    '            ) {',
    '                Image(',
    '                    painter = painterResource(id = R.drawable.frame1),',
    '                    contentDescription = "Open Spotify",',
    '                    modifier = Modifier',
    '                        .fillMaxWidth(0.5f)',
    '                        .clip(RoundedCornerShape(16.dp))',
    '                        .clickable { onOpenSpotify() }',
    '                )',
    '            }',
    '        }'
].join('\n');

const newBlock = [
    '        item {',
    '            SpotifyAnimatedButton(onOpenSpotify = onOpenSpotify)',
    '        }'
].join('\n');

if (c.includes(oldBlock)) {
    c = c.replace(oldBlock, newBlock);
    changes++;
    console.log('2. Replaced static Spotify Image with SpotifyAnimatedButton composable');
} else {
    console.log('WARN: Could not find exact old Spotify button block');
}

fs.writeFileSync(file, c, 'utf8');
console.log('Total changes to TodayWorkoutScreen.kt: ' + changes);
