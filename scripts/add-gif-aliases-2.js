const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/ExerciseGifs.kt';
let c = fs.readFileSync(file, 'utf8');

// Remaining aliases that weren't found in the first pass
// [templateName, existingFullName in map]
const remainingAliases = [
    ['Tricep Pushdown', 'Cable Triceps Pushdown (v-bar)'],
    ['Lat Pulldown', 'Cable Bar Lateral Pulldown'],
    ['Squat', 'Barbell Full Squat'],
    ['Standing Calf Raise', 'Lever Standing Calf Raise'],
    ['Plank', 'Front Plank With Twist'],
    ['Face Pull', 'Cable Rope Face Pull'],
    ['Leg Press', 'Lever Alternate Leg Press '],  // has trailing space
];

// Find where the map ends
let mapEnd = c.indexOf(')\n\n    fun getGif');
if (mapEnd === -1) {
    mapEnd = c.indexOf(')\n\n    fun getGif');
}
if (mapEnd === -1) {
    console.log('ERROR: Could not find end of gifs map');
    process.exit(1);
}

let addedCount = 0;
let skippedCount = 0;
let warnCount = 0;

for (const [shortName, fullName] of remainingAliases) {
    const shortKeyPattern = '"' + shortName + '"';
    if (c.includes(shortKeyPattern + ' to "')) {
        skippedCount++;
        continue;
    }

    // Try to find the full name (with or without trailing space)
    let fullKeyPattern = '"' + fullName + '" to "';
    let fullIdx = c.indexOf(fullKeyPattern);
    if (fullIdx === -1) {
        // Try without trailing space
        fullKeyPattern = '"' + fullName.trim() + '" to "';
        fullIdx = c.indexOf(fullKeyPattern);
    }
    if (fullIdx === -1) {
        console.log('WARN: Could not find "' + fullName + '" in map');
        warnCount++;
        continue;
    }

    const urlStart = fullIdx + fullKeyPattern.length;
    const urlEnd = c.indexOf('"', urlStart);
    const url = c.substring(urlStart, urlEnd);

    const aliasLine = '        "' + shortName + '" to "' + url + '"';
    c = c.substring(0, mapEnd) + aliasLine + ',\n' + c.substring(mapEnd);
    addedCount++;
}

fs.writeFileSync(file, c, 'utf8');
console.log('Added ' + addedCount + ' remaining aliases');
console.log('Skipped ' + skippedCount + ' (already exist)');
console.log('Warnings ' + warnCount + ' (full name not found)');
