const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/ExerciseGifs.kt';
let c = fs.readFileSync(file, 'utf8');

const aliases = [
    ['Bench Press', 'Barbell Bench Press'],
    ['Incline Bench Press', 'Barbell Incline Bench Press'],
    ['Overhead Press', 'Barbell Seated Overhead Press'],
    ['Lateral Raise', 'Dumbbell Lateral Raise'],
    ['Tricep Pushdown', 'Cable Triceps Pushdown'],
    ['Deadlift', 'Barbell Deadlift'],
    ['Barbell Row', 'Barbell Bent Over Row'],
    ['Pull-Up', 'Assisted Pull-up'],
    ['Pull-up', 'Assisted Pull-up'],
    ['Lat Pulldown', 'Cable Lateral Pulldown'],
    ['Barbell Shrugs', 'Barbell Shrug'],
    ['Barbell Curl', 'Barbell Biceps Curl'],
    ['Face Pull', 'Cable Face Pull'],
    ['Squat', 'Barbell Squat'],
    ['Romanian Deadlift', 'Barbell Romanian Deadlift'],
    ['Leg Curl', 'Lever Lying Leg Curl'],
    ['Leg Extension', 'Lever Leg Extension'],
    ['Calf Raise', 'Standing Calf Raise'],
    ['Plank', 'Plank'],
    ['Push-up', 'Push-up'],
    ['Push Up', 'Push-up'],
    ['Dip', 'Chest Dip'],
    ['Chin-up', 'Assisted Pull-up'],
    ['Chin Up', 'Assisted Pull-up'],
    ['Jump Rope', 'Jump Rope'],
    ['Jump Squat', 'Jump Squat'],
    ['Lunge', 'Barbell Lunge'],
    ['Bench Dip', 'Bench Dip (knees Bent)'],
    ['Bench Dip (Knees Bent)', 'Bench Dip (knees Bent)'],
    ['Hip Extension', 'Bench Hip Extension'],
    ['Leg Press', 'Lever Alternate Leg Press'],
    ['Hammer Curl', 'Cable Hammer Curl (with Rope)'],
    ['Preacher Curl', 'Barbell Preacher Curl'],
    ['Skull Crusher', 'Barbell Lying Triceps Extension Skull Crusher'],
    ['Cable Row', 'Cable Low Seated Row'],
    ['Chest Press', 'Lever Chest Press'],
    ['Military Press', 'Barbell Seated Overhead Press'],
    ['Seated Row', 'Cable Low Seated Row'],
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

for (const [shortName, fullName] of aliases) {
    const shortKeyPattern = '"' + shortName + '"';
    if (c.includes(shortKeyPattern + ' to "')) {
        skippedCount++;
        continue;
    }

    const fullKeyPattern = '"' + fullName + '" to "';
    const fullIdx = c.indexOf(fullKeyPattern);
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
console.log('Added ' + addedCount + ' short-name aliases');
console.log('Skipped ' + skippedCount + ' (already exist)');
console.log('Warnings ' + warnCount + ' (full name not found)');
