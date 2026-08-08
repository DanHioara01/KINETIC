// Adds missing trailing comma to the line before each inserted Spotify key block
const fs = require('fs');
const path = 'app/src/main/java/com/example/kinetic/LanguageManager.kt';
const lines = fs.readFileSync(path, 'utf8').split('\n');

const newKeys = [
  '"energizeLabel"',
  '"performLabel"',
  '"pushItLabel"',
  '"openSpotifyLabel"',
  '"tapToPlayLabel"',
  '"startingWorkoutLabel"',
];

let fixed = 0;
for (let i = 0; i < lines.length; i++) {
  // If a line contains one of the new keys and is NOT the class field declaration (has "m[" pattern)
  const isNewKeyLine = newKeys.some(k => lines[i].includes(k)) && !lines[i].includes('m[');
  if (isNewKeyLine) {
    const prev = lines[i - 1];
    // If previous line doesn't end with a comma, add one
    if (prev && !prev.trim().endsWith(',')) {
      lines[i - 1] = prev.trimEnd() + ',';
      fixed++;
    }
  }
}
fs.writeFileSync(path, lines.join('\n'), 'utf8');
console.log('Fixed commas before', fixed, 'key blocks');
