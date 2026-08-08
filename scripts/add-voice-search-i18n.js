const fs = require('fs');

// 1. Add RECORD_AUDIO permission to AndroidManifest.xml
let manifest = fs.readFileSync('app/src/main/AndroidManifest.xml', 'utf8');
if (!manifest.includes('RECORD_AUDIO')) {
    manifest = manifest.replace(
        '    <uses-permission android:name="android.permission.VIBRATE" />',
        '    <uses-permission android:name="android.permission.VIBRATE" />\n    <uses-permission android:name="android.permission.RECORD_AUDIO" />'
    );
    fs.writeFileSync('app/src/main/AndroidManifest.xml', manifest, 'utf8');
    console.log('1. Added RECORD_AUDIO permission to AndroidManifest.xml');
} else {
    console.log('1. RECORD_AUDIO already exists');
}

// 2. Add i18n keys to LanguageManager.kt Strings data class
let lm = fs.readFileSync('app/src/main/java/com/example/kinetic/LanguageManager.kt', 'utf8');
let lmChanges = 0;

// Add to Strings data class
const strTarget = '        val tryDifferentFilter: String = m["tryDifferentFilter"] ?: ""';
const strReplace = strTarget + '\n        val voiceSearch: String = m["voiceSearch"] ?: ""\n        val listening: String = m["listening"] ?: ""\n        val voiceSearchError: String = m["voiceSearchError"] ?: ""';
if (lm.includes(strTarget) && !lm.includes('val voiceSearch')) {
    lm = lm.replace(strTarget, strReplace);
    lmChanges++;
    console.log('2. Added 3 keys to Strings data class');
} else {
    console.log('2. Keys already exist in Strings data class');
}

// 3. Add translations to each language map
// Strategy: find each "tryDifferentFilter" translation and append the 3 new keys after it
const langMap = [
    { search: '"tryDifferentFilter" to "\u00censearc\u0103 un alt filtru sau caut\u0103 dup\u0103 nume"', add: ', "voiceSearch" to "C\u0103utare vocal\u0103", "listening" to "Ascult...", "voiceSearchError" to "Nu s-a putut recunoa\u0219te vocea"' },
    { search: '"tryDifferentFilter" to "Try a different filter or search by name"', add: ', "voiceSearch" to "Voice search", "listening" to "Listening...", "voiceSearchError" to "Could not recognize voice"' },
    { search: '"tryDifferentFilter" to "\u041f\u043e\u043f\u0440\u043e\u0431\u0443\u0439\u0442\u0435 \u0434\u0440\u0443\u0433\u043e\u0439 \u0444\u0438\u043b\u044c\u0442\u0440 \u0438\u043b\u0438 \u043f\u043e\u0438\u0441\u043a \u043f\u043e \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u044e"', add: ', "voiceSearch" to "\u0413\u043e\u043b\u043e\u0441\u043e\u0432\u043e\u0439 \u043f\u043e\u0438\u0441\u043a", "listening" to "\u0421\u043b\u0443\u0448\u0430\u044e...", "voiceSearchError" to "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0440\u0430\u0441\u043f\u043e\u0437\u043d\u0430\u0442\u044c \u0433\u043e\u043b\u043e\u0441"' },
    { search: '"tryDifferentFilter" to "\u0421\u043f\u0440\u043e\u0431\u0443\u0439\u0442\u0435 \u0456\u043d\u0448\u0438\u0439 \u0444\u0456\u043b\u044c\u0442\u0440 \u0430\u0431\u043e \u043f\u043e\u0448\u0443\u043a \u0437\u0430 \u043d\u0430\u0437\u0432\u043e\u044e"', add: ', "voiceSearch" to "\u0413\u043e\u043b\u043e\u0441\u043e\u0432\u0438\u0439 \u043f\u043e\u0448\u0443\u043a", "listening" to "\u0421\u043b\u0443\u0445\u0430\u044e...", "voiceSearchError" to "\u041d\u0435 \u0432\u0434\u0430\u043b\u043e\u0441\u044f \u0440\u043e\u0437\u043f\u0456\u0437\u043d\u0430\u0442\u0438 \u0433\u043e\u043b\u043e\u0441"' },
    { search: '"tryDifferentFilter" to "Essayez un autre filtre ou recherchez par nom"', add: ', "voiceSearch" to "Recherche vocale", "listening" to "\u00c9coute...", "voiceSearchError" to "Impossible de reconna\u00eetre la voix"' },
    { search: '"tryDifferentFilter" to "Versuchen Sie einen anderen Filter oder suchen Sie nach Name"', add: ', "voiceSearch" to "Sprachsuche", "listening" to "H\u00f6re zu...", "voiceSearchError" to "Stimme konnte nicht erkannt werden"' },
    { search: '"tryDifferentFilter" to "Prueba con otro filtro o busca por nombre"', add: ', "voiceSearch" to "B\u00fasqueda por voz", "listening" to "Escuchando...", "voiceSearchError" to "No se pudo reconocer la voz"' },
    { search: '"tryDifferentFilter" to "Prova un altro filtro o cerca per nome"', add: ', "voiceSearch" to "Ricerca vocale", "listening" to "In ascolto...", "voiceSearchError" to "Impossibile riconoscere la voce"' },
    { search: '"tryDifferentFilter" to "Farkl\u0131 bir filtre deneyin veya isme g\u00f6re aray\u0131n"', add: ', "voiceSearch" to "Sesli arama", "listening" to "Dinleniyor...", "voiceSearchError" to "Ses tan\u0131namad\u0131"' },
    { search: '"tryDifferentFilter" to "Tente outro filtro ou pesquise por nome"', add: ', "voiceSearch" to "Pesquisa por voz", "listening" to "Ouvindo...", "voiceSearchError" to "N\u00e3o foi poss\u00edvel reconhecer a voz"' },
    { search: '"tryDifferentFilter" to "Spr\u00f3buj innego filtru lub wyszukaj po nazwie"', add: ', "voiceSearch" to "Wyszukiwanie g\u0142osowe", "listening" to "S\u0142ucham...", "voiceSearchError" to "Nie rozpoznano g\u0142osu"' },
];

for (const lang of langMap) {
    if (lm.includes(lang.search) && !lm.includes('"voiceSearch" to')) {
        // Find the first occurrence and append after it
        const idx = lm.indexOf(lang.search);
        if (idx !== -1) {
            lm = lm.substring(0, idx + lang.search.length) + lang.add + lm.substring(idx + lang.search.length);
            lmChanges++;
            console.log('3. Added voice translations for: ' + lang.search.substring(0, 50) + '...');
        }
    }
}

fs.writeFileSync('app/src/main/java/com/example/kinetic/LanguageManager.kt', lm, 'utf8');
console.log('\nLanguageManager.kt saved (' + lmChanges + ' language maps updated)');

// Verify
const verify = fs.readFileSync('app/src/main/java/com/example/kinetic/LanguageManager.kt', 'utf8');
console.log('\nVerification:');
console.log('  voiceSearch count: ' + (verify.match(/"voiceSearch"/g) || []).length);
console.log('  listening count: ' + (verify.match(/"listening"/g) || []).length);
console.log('  voiceSearchError count: ' + (verify.match(/"voiceSearchError"/g) || []).length);
