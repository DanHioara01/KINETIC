const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/LanguageManager.kt';
let content = fs.readFileSync(file, 'utf8');
let changes = 0;

// Find each line containing "tryDifferentFilter" that's in a language map (not the Strings class)
const lines = content.split('\n');
const voiceTranslations = {
    // Romanian
    'Încearcă un alt filtru sau caută după nume': ', "voiceSearch" to "Căutare vocală", "listening" to "Ascult...", "voiceSearchError" to "Nu s-a putut recunoaște vocea"',
    // English
    'Try a different filter or search by name': ', "voiceSearch" to "Voice search", "listening" to "Listening...", "voiceSearchError" to "Could not recognize voice"',
    // Russian
    'Попробуйте другой фильтр или поиск по названию': ', "voiceSearch" to "Голосовой поиск", "listening" to "Слушаю...", "voiceSearchError" to "Не удалось распознать голос"',
    // Ukrainian
    'Спробуйте інший фільтр або пошук за назвою': ', "voiceSearch" to "Голосовий пошук", "listening" to "Слухаю...", "voiceSearchError" to "Не вдалося розпізнати голос"',
    // French
    'Essayez un autre filtre ou recherchez par nom': ', "voiceSearch" to "Recherche vocale", "listening" to "Écoute...", "voiceSearchError" to "Impossible de reconnaître la voix"',
    // German
    'Versuchen Sie einen anderen Filter oder suchen Sie nach Name': ', "voiceSearch" to "Sprachsuche", "listening" to "Höre zu...", "voiceSearchError" to "Stimme konnte nicht erkannt werden"',
    // Spanish
    'Prueba con otro filtro o busca por nombre': ', "voiceSearch" to "Búsqueda por voz", "listening" to "Escuchando...", "voiceSearchError" to "No se pudo reconocer la voz"',
    // Italian
    'Prova un altro filtro o cerca per nome': ', "voiceSearch" to "Ricerca vocale", "listening" to "In ascolto...", "voiceSearchError" to "Impossibile riconoscere la voce"',
    // Turkish
    'Farklı bir filtre deneyin veya isme göre arayın': ', "voiceSearch" to "Sesli arama", "listening" to "Dinleniyor...", "voiceSearchError" to "Ses tanınamadı"',
    // Portuguese
    'Tente outro filtro ou pesquise por nome': ', "voiceSearch" to "Pesquisa por voz", "listening" to "Ouvindo...", "voiceSearchError" to "Não foi possível reconhecer a voz"',
    // Polish
    'Spróbuj innego filtru lub wyszukaj po nazwie': ', "voiceSearch" to "Wyszukiwanie głosowe", "listening" to "Słucham...", "voiceSearchError" to "Nie rozpoznano głosu"',
};

for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if (line.includes('"tryDifferentFilter" to') && !line.includes('val tryDifferentFilter')) {
        for (const [search, add] of Object.entries(voiceTranslations)) {
            if (line.includes(search) && !line.includes('"voiceSearch"')) {
                lines[i] = line + add;
                changes++;
                console.log(`Line ${i + 1}: Added voice translations (${search.substring(0, 30)}...)`);
                break;
            }
        }
    }
}

content = lines.join('\n');
fs.writeFileSync(file, content, 'utf8');
console.log(`\nTotal: ${changes} language maps updated`);

// Verify
const verify = fs.readFileSync(file, 'utf8');
console.log(`voiceSearch count: ${(verify.match(/"voiceSearch"/g) || []).length}`);
console.log(`listening count: ${(verify.match(/"listening"/g) || []).length}`);
console.log(`voiceSearchError count: ${(verify.match(/"voiceSearchError"/g) || []).length}`);
