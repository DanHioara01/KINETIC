// Fixes the misplacement: EN keys went into RU map instead of enRaw()
const fs = require('fs');
const path = 'app/src/main/java/com/example/kinetic/LanguageManager.kt';
const lines = fs.readFileSync(path, 'utf8').split('\n');

// 1) Find the wrongly-inserted EN keys (inside RU map) and remove them
const enKeys = [
  '        "energizeLabel" to "Energize",',
  '        "performLabel" to "Perform",',
  '        "pushItLabel" to "Push It",',
  '        "openSpotifyLabel" to "Open Spotify",',
  '        "tapToPlayLabel" to "Tap to play on Spotify",',
  '        "startingWorkoutLabel" to "Starting workout music...",',
];

let removed = 0;
for (let i = lines.length - 1; i >= 0; i--) {
  if (enKeys.includes(lines[i])) {
    lines.splice(i, 1);
    removed++;
  }
}
console.log('Removed misplaced EN keys from RU map:', removed);

// 2) Find the line 'equipBand" to "Лента"' without trailing comma and add comma
for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('"equipBand" to "Лента"') && !lines[i].trim().endsWith(',')) {
    lines[i] = lines[i] + ',';
    console.log('Added comma to RU equipBand line', i + 1);
    break;
  }
}

// 3) Insert RU translations before the closing '))' of createRu
const ruStart = lines.findIndex(l => l.includes('fun createRu'));
if (ruStart === -1) { console.error('createRu not found'); process.exit(1); }
let ruEnd = -1;
for (let i = ruStart; i < lines.length; i++) {
  if (lines[i].trim() === '))') { ruEnd = i; break; }
}
if (ruEnd === -1) { console.error('createRu closing not found'); process.exit(1); }

const ruTranslations = [
  '        "energizeLabel" to "Энергия",',
  '        "performLabel" to "Производительность",',
  '        "pushItLabel" to "Давай",',
  '        "openSpotifyLabel" to "Открыть Spotify",',
  '        "tapToPlayLabel" to "Нажмите для воспроизведения в Spotify",',
  '        "startingWorkoutLabel" to "Запускаем музыку для тренировки...",',
];
// Check they don't already exist in the RU map
const ruExists = (key) => {
  for (let i = ruStart; i < ruEnd; i++) {
    if (lines[i].includes('"' + key + '"')) return true;
  }
  return false;
};
const toInsert = ruTranslations.filter(l => !ruExists(l.match(/"([a-zA-Z0-9]+)"/)[1]));
if (toInsert.length > 0) {
  lines.splice(ruEnd, 0, ...toInsert);
  console.log('Inserted RU translations:', toInsert.length);
} else {
  console.log('RU translations already present');
}

// 4) Insert EN keys into enRaw() before its closing ')'
// enRaw is a large single-line-ish map. Find its closing line: a line that is only ')'
const enRawStart = lines.findIndex(l => l.includes('fun enRaw'));
if (enRawStart === -1) { console.error('enRaw not found'); process.exit(1); }
let enRawEnd = -1;
for (let i = enRawStart; i < lines.length; i++) {
  const trimmed = lines[i].trim();
  if (trimmed === ')') { enRawEnd = i; break; }
}
if (enRawEnd === -1) {
  console.error('enRaw closing not found');
  process.exit(1);
}
const enTranslations = [
  '        "energizeLabel" to "Energize",',
  '        "performLabel" to "Perform",',
  '        "pushItLabel" to "Push It",',
  '        "openSpotifyLabel" to "Open Spotify",',
  '        "tapToPlayLabel" to "Tap to play on Spotify",',
  '        "startingWorkoutLabel" to "Starting workout music...",',
];
const enToInsert = enTranslations.filter(l => {
  const key = l.match(/"([a-zA-Z0-9]+)"/)[1];
  for (let i = enRawStart; i < enRawEnd; i++) {
    if (lines[i].includes('"' + key + '"')) return false;
  }
  return true;
});
if (enToInsert.length > 0) {
  lines.splice(enRawEnd, 0, ...enToInsert);
  console.log('Inserted EN translations into enRaw:', enToInsert.length);
} else {
  console.log('EN translations already present in enRaw');
}

fs.writeFileSync(path, lines.join('\n'), 'utf8');
console.log('DONE');
