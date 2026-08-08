// Adds missing Spotify-related string keys to LanguageManager.kt
// Keys: energizeLabel, performLabel, pushItLabel, openSpotifyLabel, tapToPlayLabel, startingWorkoutLabel
const fs = require('fs');
const path = 'app/src/main/java/com/example/kinetic/LanguageManager.kt';
let t = fs.readFileSync(path, 'utf8');
const lines = t.split('\n');

// 1) Add field declarations to Strings class after the equipBand line
const classAnchor = '        val equipBand: String = m["equipBand"] ?: ""';
const classIdx = lines.findIndex(l => l.includes('val equipBand: String'));
if (classIdx === -1) { console.error('ERROR: equipBand field not found'); process.exit(1); }

const fields = [
  '        val energizeLabel: String = m["energizeLabel"] ?: ""',
  '        val performLabel: String = m["performLabel"] ?: ""',
  '        val pushItLabel: String = m["pushItLabel"] ?: ""',
  '        val openSpotifyLabel: String = m["openSpotifyLabel"] ?: ""',
  '        val tapToPlayLabel: String = m["tapToPlayLabel"] ?: ""',
  '        val startingWorkoutLabel: String = m["startingWorkoutLabel"] ?: ""',
];
if (lines.some(l => l.includes('val energizeLabel: String'))) {
  console.log('Fields already present, skipping class insertion');
} else {
  lines.splice(classIdx + 1, 0, ...fields);
  console.log('Added', fields.length, 'field declarations after line', classIdx + 1);
}

// 2) Translations per language. Insert into each createX() map before its closing '    ))'
const translations = {
  // English base (enRaw) - used by createEn
  en: {
    'energizeLabel': 'Energize',
    'performLabel': 'Perform',
    'pushItLabel': 'Push It',
    'openSpotifyLabel': 'Open Spotify',
    'tapToPlayLabel': 'Tap to play on Spotify',
    'startingWorkoutLabel': 'Starting workout music...',
  },
  ro: {
    'energizeLabel': 'Energizează-te',
    'performLabel': 'Performanță',
    'pushItLabel': 'Impinge',
    'openSpotifyLabel': 'Deschide Spotify',
    'tapToPlayLabel': 'Apasă pentru a asculta pe Spotify',
    'startingWorkoutLabel': 'Pornim muzica de antrenament...',
  },
  ru: {
    'energizeLabel': 'Энергия',
    'performLabel': 'Производительность',
    'pushItLabel': 'Давай',
    'openSpotifyLabel': 'Открыть Spotify',
    'tapToPlayLabel': 'Нажмите для воспроизведения в Spotify',
    'startingWorkoutLabel': 'Запускаем музыку для тренировки...',
  },
  uk: {
    'energizeLabel': 'Енергія',
    'performLabel': 'Продуктивність',
    'pushItLabel': 'Давай',
    'openSpotifyLabel': 'Відкрити Spotify',
    'tapToPlayLabel': 'Натисніть для відтворення в Spotify',
    'startingWorkoutLabel': 'Запускаємо музику для тренування...',
  },
  fr: {
    'energizeLabel': 'Énergie',
    'performLabel': 'Performance',
    'pushItLabel': 'Pousse',
    'openSpotifyLabel': 'Ouvrir Spotify',
    'tapToPlayLabel': 'Appuyez pour écouter sur Spotify',
    'startingWorkoutLabel': 'Lancement de la musique d\'entraînement...',
  },
  de: {
    'energizeLabel': 'Energie',
    'performLabel': 'Leistung',
    'pushItLabel': 'Schieben',
    'openSpotifyLabel': 'Spotify öffnen',
    'tapToPlayLabel': 'Tippen um auf Spotify abzuspielen',
    'startingWorkoutLabel': 'Trainingsmusik starten...',
  },
  es: {
    'energizeLabel': 'Energía',
    'performLabel': 'Rendimiento',
    'pushItLabel': 'Empuja',
    'openSpotifyLabel': 'Abrir Spotify',
    'tapToPlayLabel': 'Toca para reproducir en Spotify',
    'startingWorkoutLabel': 'Iniciando música de entrenamiento...',
  },
  it: {
    'energizeLabel': 'Energia',
    'performLabel': 'Performance',
    'pushItLabel': 'Spingi',
    'openSpotifyLabel': 'Apri Spotify',
    'tapToPlayLabel': 'Tocca per ascoltare su Spotify',
    'startingWorkoutLabel': 'Avvio musica da allenamento...',
  },
  tr: {
    'energizeLabel': 'Enerji',
    'performLabel': 'Performans',
    'pushItLabel': 'Hadi',
    'openSpotifyLabel': "Spotify'ı Aç",
    'tapToPlayLabel': "Spotify'da çalmak için dokunun",
    'startingWorkoutLabel': 'Antrenman müziği başlatılıyor...',
  },
  pt: {
    'energizeLabel': 'Energizar',
    'performLabel': 'Desempenho',
    'pushItLabel': 'Vai',
    'openSpotifyLabel': 'Abrir Spotify',
    'tapToPlayLabel': 'Toque para tocar no Spotify',
    'startingWorkoutLabel': 'Iniciando música de treino...',
  },
  pl: {
    'energizeLabel': 'Energia',
    'performLabel': 'Wydajność',
    'pushItLabel': 'Dajesz',
    'openSpotifyLabel': 'Otwórz Spotify',
    'tapToPlayLabel': 'Dotknij aby odtworzyć na Spotify',
    'startingWorkoutLabel': 'Uruchamianie muzyki treningowej...',
  },
};

// Function map: find each createX function's closing '    ))' and insert before it
const funcs = ['ro', 'en', 'ru', 'uk', 'fr', 'de', 'es', 'it', 'tr', 'pt', 'pl'];
let insertCount = 0;
for (const lang of funcs) {
  const fname = 'fun create' + lang.charAt(0).toUpperCase() + lang.slice(1);
  const startIdx = lines.findIndex(l => l.includes(fname));
  if (startIdx === -1) { console.error('ERROR: ' + fname + ' not found'); process.exit(1); }

  // Find closing '    ))' for THIS function (first one after startIdx)
  let endIdx = -1;
  for (let i = startIdx; i < lines.length; i++) {
    const trimmed = lines[i].trim();
    if (trimmed === '))') { endIdx = i; break; }
  }
  if (endIdx === -1) { console.error('ERROR: closing ) for ' + fname + ' not found'); process.exit(1); }

  // Build the translation lines, skipping keys that already exist in the map
  const keys = translations[lang];
  const existing = new Set();
  for (let i = startIdx; i < endIdx; i++) {
    const m2 = lines[i].match(/^\s*"([a-zA-Z0-9]+)"\s*to/);
    if (m2) existing.add(m2[1]);
  }
  const insert = [];
  for (const [k, v] of Object.entries(keys)) {
    if (!existing.has(k)) {
      insert.push('        "' + k + '" to "' + v + '",');
    }
  }
  if (insert.length > 0) {
    lines.splice(endIdx, 0, ...insert);
    insertCount += insert.length;
    console.log(fname + ': inserted ' + insert.length + ' keys at line ' + (endIdx + 1));
  } else {
    console.log(fname + ': all keys already present');
  }
}

t = lines.join('\n');
fs.writeFileSync(path, t, 'utf8');
console.log('DONE - total keys inserted:', insertCount);
