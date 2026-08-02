const fs = require('fs');
const path = 'app/src/main/java/com/example/kinetic/LanguageManager.kt';
let content = fs.readFileSync(path, 'utf8');

// Step 1: Add missing keys to Strings class after energeticLabel
const stringsMarker = '        val energeticLabel: String = m["energeticLabel"] ?: ""';
const newKeys = `        val energeticLabel: String = m["energeticLabel"] ?: ""
        val energizeLabel: String = m["energizeLabel"] ?: "Energize"
        val performLabel: String = m["performLabel"] ?: "Perform"
        val pushItLabel: String = m["pushItLabel"] ?: "Push It"
        val openSpotifyLabel: String = m["openSpotifyLabel"] ?: "Open Spotify"
        val tapToPlayLabel: String = m["tapToPlayLabel"] ?: "Tap to play on Spotify"
        val startingWorkoutLabel: String = m["startingWorkoutLabel"] ?: "Starting workout music..."
        val fitnessTipsLabel: String = m["fitnessTipsLabel"] ?: "FITNESS TIPS"`;

if (!content.includes('val energizeLabel')) {
    content = content.replace(stringsMarker, newKeys);
    console.log('Step 1: Added new string keys to Strings class');
} else {
    console.log('Step 1: Keys already exist');
}

// Step 2: Add translations to all 11 languages
const languages = [
    { key: '"energizeLabel"', translations: { en: '"Energize"', ro: '"Energizează-te"', ru: '"Энергия"', uk: '"Енергія"', fr: '"Énergie"', de: '"Energie"', es: '"Energía"', it: '"Energia"', tr: '"Enerji"', pt: '"Energizar"', pl: '"Energia"' } },
    { key: '"performLabel"', translations: { en: '"Perform"', ro: '"Performanță"', ru: '"Производительность"', uk: '"Продуктивність"', fr: '"Performance"', de: '"Leistung"', es: '"Rendimiento"', it: '"Prestazioni"', tr: '"Performans"', pt: '"Performar"', pl: '"Wydajność"' } },
    { key: '"pushItLabel"', translations: { en: '"Push It"', ro: '"Impinge"', ru: '"Давай"', uk: '"Давай"', fr: '"Pousse"', de: '"Gib alles"', es: '"Dale"', it: '"Spingi"', tr: '"Zorla"', pt: '"Força"', pl: '"Dawaj"' } },
    { key: '"openSpotifyLabel"', translations: { en: '"Open Spotify"', ro: '"Deschide Spotify"', ru: '"Открыть Spotify"', uk: '"Відкрити Spotify"', fr: '"Ouvrir Spotify"', de: '"Spotify öffnen"', es: '"Abrir Spotify"', it: '"Apri Spotify"', tr: '"Spotify\'ı Aç"', pt: '"Abrir Spotify"', pl: '"Otwórz Spotify"' } },
    { key: '"tapToPlayLabel"', translations: { en: '"Tap to play on Spotify"', ro: '"Apasă pentru a asculta pe Spotify"', ru: '"Нажмите для воспроизведения в Spotify"', uk: '"Натисніть для відтворення в Spotify"', fr: '"Appuyez pour écouter sur Spotify"', de: '"Tippen um auf Spotify abzuspielen"', es: '"Toca para reproducir en Spotify"', it: '"Tocca per ascoltare su Spotify"', tr: '"Spotify\'da çalmak için dokunun"', pt: '"Toque para tocar no Spotify"', pl: '"Dotknij aby odtworzyć na Spotify"' } },
    { key: '"startingWorkoutLabel"', translations: { en: '"Starting workout music..."', ro: '"Pornim muzica de antrenament..."', ru: '"Запускаем музыку для тренировки..."', uk: '"Запускаємо музику для тренування..."', fr: '"Démarrage de la musique d\'entraînement..."', de: '"Trainingsmusik wird gestartet..."', es: '"Iniciando música de entrenamiento..."', it: '"Avvio musica dell\'allenamento..."', tr: '"Antrenman müziği başlatılıyor..."', pt: '"Iniciando música do treino..."', pl: '"Uruchamianie muzyki treningowej..."' } },
    { key: '"fitnessTipsLabel"', translations: { en: '"FITNESS TIPS"', ro: '"SFATURI FITNESS"', ru: '"СОВЕТЫ ПО ФИТНЕСУ"', uk: '"ПОРАДИ ЩОДО ФІТНЕСУ"', fr: '"CONSEILS FITNESS"', de: '"FITNESS-TIPPS"', es: '"CONSEJOS DE FITNESS"', it: '"CONSIGLI FITNESS"', tr: '"FİTNESS İPUÇLARI"', pt: '"DICAS DE FITNESS"', pl: '"WSKAZÓWKI FITNESS"' } }
];

for (const lang of languages) {
    for (const [langCode, translation] of Object.entries(lang.translations)) {
        // Find the translation map for this language and add the new keys
        // We look for the last entry before the closing of each language's map
        const searchPattern = new RegExp(`("tiredLabel"\\s*to\\s*"[^"]*")`);
        const matches = content.match(searchPattern);
        if (matches) {
            // Add to each language's translation map
            const keyName = lang.key.replace(/"/g, '');
            const valueStr = `${lang.key} to ${translation}`;
            if (!content.includes(keyName)) {
                // We'll add all keys at once per language below
            }
        }
    }
}

// Add translations to each language map by finding specific patterns
// English (enTranslations)
const enAdd = `, "energizeLabel" to "Energize", "performLabel" to "Perform", "pushItLabel" to "Push It", "openSpotifyLabel" to "Open Spotify", "tapToPlayLabel" to "Tap to play on Spotify", "startingWorkoutLabel" to "Starting workout music...", "fitnessTipsLabel" to "FITNESS TIPS"`;
const roAdd = `, "energizeLabel" to "Energizează-te", "performLabel" to "Performanță", "pushItLabel" to "Impinge", "openSpotifyLabel" to "Deschide Spotify", "tapToPlayLabel" to "Apasă pentru a asculta pe Spotify", "startingWorkoutLabel" to "Pornim muzica de antrenament...", "fitnessTipsLabel" to "SFATURI FITNESS"`;
const ruAdd = `, "energizeLabel" to "Энергия", "performLabel" to "Производительность", "pushItLabel" to "Давай", "openSpotifyLabel" to "Открыть Spotify", "tapToPlayLabel" to "Нажмите для воспроизведения в Spotify", "startingWorkoutLabel" to "Запускаем музыку для тренировки...", "fitnessTipsLabel" to "СОВЕТЫ ПО ФИТНЕСУ"`;
const ukAdd = `, "energizeLabel" to "Енергія", "performLabel" to "Продуктивність", "pushItLabel" to "Давай", "openSpotifyLabel" to "Відкрити Spotify", "tapToPlayLabel" to "Натисніть для відтворення в Spotify", "startingWorkoutLabel" to "Запускаємо музику для тренування...", "fitnessTipsLabel" to "ПОРАДИ ЩОДО ФІТНЕСУ"`;
const frAdd = `, "energizeLabel" to "Énergie", "performLabel" to "Performance", "pushItLabel" to "Pousse", "openSpotifyLabel" to "Ouvrir Spotify", "tapToPlayLabel" to "Appuyez pour écouter sur Spotify", "startingWorkoutLabel" to "Démarrage de la musique d'entraînement...", "fitnessTipsLabel" to "CONSEILS FITNESS"`;
const deAdd = `, "energizeLabel" to "Energie", "performLabel" to "Leistung", "pushItLabel" to "Gib alles", "openSpotifyLabel" to "Spotify öffnen", "tapToPlayLabel" to "Tippen um auf Spotify abzuspielen", "startingWorkoutLabel" to "Trainingsmusik wird gestartet...", "fitnessTipsLabel" to "FITNESS-TIPPS"`;
const esAdd = `, "energizeLabel" to "Energía", "performLabel" to "Rendimiento", "pushItLabel" to "Dale", "openSpotifyLabel" to "Abrir Spotify", "tapToPlayLabel" to "Toca para reproducir en Spotify", "startingWorkoutLabel" to "Iniciando música de entrenamiento...", "fitnessTipsLabel" to "CONSEJOS DE FITNESS"`;
const itAdd = `, "energizeLabel" to "Energia", "performLabel" to "Prestazioni", "pushItLabel" to "Spingi", "openSpotifyLabel" to "Apri Spotify", "tapToPlayLabel" to "Tocca per ascoltare su Spotify", "startingWorkoutLabel" to "Avvio musica dell'allenamento...", "fitnessTipsLabel" to "CONSIGLI FITNESS"`;
const trAdd = `, "energizeLabel" to "Enerji", "performLabel" to "Performans", "pushItLabel" to "Zorla", "openSpotifyLabel" to "Spotify'ı Aç", "tapToPlayLabel" to "Spotify'da çalmak için dokunun", "startingWorkoutLabel" to "Antrenman müziği başlatılıyor...", "fitnessTipsLabel" to "FİTNESS İPUÇLARI"`;
const ptAdd = `, "energizeLabel" to "Energizar", "performLabel" to "Performar", "pushItLabel" to "Força", "openSpotifyLabel" to "Abrir Spotify", "tapToPlayLabel" to "Toque para tocar no Spotify", "startingWorkoutLabel" to "Iniciando música do treino...", "fitnessTipsLabel" to "DICAS DE FITNESS"`;
const plAdd = `, "energizeLabel" to "Energia", "performLabel" to "Wydajność", "pushItLabel" to "Dawaj", "openSpotifyLabel" to "Otwórz Spotify", "tapToPlayLabel" to "Dotknij aby odtworzyć na Spotify", "startingWorkoutLabel" to "Uruchamianie muzyki treningowej...", "fitnessTipsLabel" to "WSKAZÓWKI FITNESS"`;

// Find each language's map and add translations before the closing
// We'll use a simpler approach: find "voiceSearchError" entries and add after them
const langSearchPattern = /"voiceSearchError"\s*to\s*"[^"]*"/g;
let match;
let offset = 0;
const langOrder = [enAdd, roAdd, ruAdd, ukAdd, frAdd, deAdd, esAdd, itAdd, trAdd, ptAdd, plAdd];
let langIndex = 0;

while ((match = langSearchPattern.exec(content)) !== null && langIndex < langOrder.length) {
    const insertPos = match.index + match[0].length;
    content = content.slice(0, insertPos) + langOrder[langIndex] + content.slice(insertPos);
    langIndex++;
}

console.log(`Step 2: Added translations to ${langIndex} languages`);

fs.writeFileSync(path, content, 'utf8');
console.log('File saved successfully');

// Verify
const final = fs.readFileSync(path, 'utf8');
console.log('\nVerification:');
console.log('- Has energizeLabel key:', final.includes('val energizeLabel'));
console.log('- Has performLabel key:', final.includes('val performLabel'));
console.log('- Has pushItLabel key:', final.includes('val pushItLabel'));
console.log('- Has openSpotifyLabel key:', final.includes('val openSpotifyLabel'));
console.log('- Has tapToPlayLabel key:', final.includes('val tapToPlayLabel'));
console.log('- Has startingWorkoutLabel key:', final.includes('val startingWorkoutLabel'));
console.log('- Has fitnessTipsLabel key:', final.includes('val fitnessTipsLabel'));
console.log('- EN translations:', final.includes('"energizeLabel" to "Energize"'));
console.log('- RO translations:', final.includes('"energizeLabel" to "Energizează-te"'));
console.log('- RU translations:', final.includes('"energizeLabel" to "Энергия"'));
