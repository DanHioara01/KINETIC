const fs = require('fs');
const path = 'app/src/main/java/com/example/kinetic/TodayWorkoutScreen.kt';
let content = fs.readFileSync(path, 'utf8');

// Fix 1: Add missing import kotlin.math.sin
const delayImport = 'import kotlinx.coroutines.delay';
if (!content.includes('import kotlin.math.sin')) {
    content = content.replace(delayImport, delayImport + '\nimport kotlin.math.sin');
    console.log('Fix 1: Added import kotlin.math.sin');
} else {
    console.log('Fix 1: import kotlin.math.sin already exists');
}

// Fix 2: Make logo box visible - use white/transparent background instead of same green
const logoBoxGreen = '.size(48.dp)\n                    .padding(start = 16.dp)\n                    .clip(RoundedCornerShape(12.dp))\n                    .background(SpotifyGreen),';
const logoBoxFixed = '.size(48.dp)\n                    .padding(start = 16.dp)\n                    .clip(RoundedCornerShape(12.dp))\n                    .background(Color.White),';
if (content.includes(logoBoxGreen)) {
    content = content.replace(logoBoxGreen, logoBoxFixed);
    console.log('Fix 2: Changed logo box background from SpotifyGreen to White');
} else {
    console.log('Fix 2: Logo box pattern not found');
}

// Fix 3: Clean up Math.sin -> sin (now that import is added)
const mathSin = 'Math.sin(time.toDouble() + phase).toFloat()';
const cleanSin = 'sin(time + phase)';
if (content.includes(mathSin)) {
    content = content.replace(mathSin, cleanSin);
    console.log('Fix 3: Replaced Math.sin with sin()');
} else {
    console.log('Fix 3: Math.sin not found');
}

fs.writeFileSync(path, content, 'utf8');
console.log('All fixes applied');
