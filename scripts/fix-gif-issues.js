const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/ExerciseGifs.kt';
let c = fs.readFileSync(file, 'utf8');
let changes = 0;

// Fix 1: Replace risky bidirectional partial match with safer startsWith (min 5 chars)
const oldPartialMatch = [
    '        // 4. Partial match: find any key that contains the exercise name (case-insensitive)',
    '        val lowerName = exerciseName.lowercase()',
    '        for ((key, url) in gifs) {',
    '            if (key.lowercase().contains(lowerName) || lowerName.contains(key.lowercase())) {',
    '                return url',
    '            }',
    '        }'
].join('\n');

const newPartialMatch = [
    '        // 4. Partial match: find any key starting with the exercise name (min 5 chars to avoid false positives)',
    '        if (exerciseName.length >= 5) {',
    '            for ((key, url) in gifs) {',
    '                if (key.lowercase().startsWith(exerciseName.lowercase())) {',
    '                    return url',
    '                }',
    '            }',
    '        }'
].join('\n');

if (c.includes(oldPartialMatch)) {
    c = c.replace(oldPartialMatch, newPartialMatch);
    changes++;
    console.log('1. Fixed partial match: removed risky bidirectional contains, now uses startsWith with min 5 chars');
}

// Fix 2: Add Face Pull alias (the exercise exists in templates but has no GIF entry)
const facePullEntry = '        "Face Pull" to "https://raw.githubusercontent.com/omercotkd/exercises-gifs/main/assets/0988.gif"';
if (!c.includes('"Face Pull" to')) {
    const mapEnd = c.indexOf(')\n\n    fun getGif');
    if (mapEnd !== -1) {
        c = c.substring(0, mapEnd) + facePullEntry + ',\n' + c.substring(mapEnd);
        changes++;
        console.log('2. Added Face Pull alias');
    }
}

// Fix 3: Fix Plank - use a better GIF (standard plank from CSV: 0338.gif)
// First remove the incorrect Plank -> Front Plank With Twist alias
const oldPlank = '"Plank" to "https://raw.githubusercontent.com/omercotkd/exercises-gifs/main/assets/0464.gif"';
const newPlank = '"Plank" to "https://d205bpvrqc9yn1.cloudfront.net/0338.gif"';
if (c.includes(oldPlank)) {
    c = c.replace(oldPlank, newPlank);
    changes++;
    console.log('3. Fixed Plank GIF: now points to standard Plank (0338.gif) instead of Front Plank With Twist');
}

fs.writeFileSync(file, c, 'utf8');
console.log('Total fixes applied: ' + changes);
