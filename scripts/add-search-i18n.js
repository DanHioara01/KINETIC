const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/LanguageManager.kt';
let content = fs.readFileSync(file, 'utf8');

// Each language map ends with "changeExercise" to "...", 
// We need to append the 3 new keys after "changeExercise" in each map.

const langTranslations = {
    // English
    '"changeExercise" to "Change exercise",\n': '"changeExercise" to "Change exercise", "all" to "All", "noExercisesFound" to "No exercises found", "tryDifferentFilter" to "Try a different filter or search by name",\n',
    // Russian
    '"changeExercise" to "\u0417\u0430\u043c\u0435\u043d\u0438\u0442\u044c \u0443\u043f\u0440\u0430\u0436\u043d\u0435\u043d\u0438\u0435",\n': '"changeExercise" to "\u0417\u0430\u043c\u0435\u043d\u0438\u0442\u044c \u0443\u043f\u0440\u0430\u0436\u043d\u0435\u043d\u0438\u0435", "all" to "\u0412\u0441\u0435", "noExercisesFound" to "\u0423\u043f\u0440\u0430\u0436\u043d\u0435\u043d\u0438\u044f \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u044b", "tryDifferentFilter" to "\u041f\u043e\u043f\u0440\u043e\u0431\u0443\u0439\u0442\u0435 \u0434\u0440\u0443\u0433\u043e\u0439 \u0444\u0438\u043b\u044c\u0442\u0440 \u0438\u043b\u0438 \u043f\u043e\u0438\u0441\u043a \u043f\u043e \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u044e",\n',
    // Ukrainian
    '"changeExercise" to "\u0417\u0430\u043c\u0456\u043d\u0438\u0442\u0438 \u0432\u043f\u0440\u0430\u0432\u0443",\n': '"changeExercise" to "\u0417\u0430\u043c\u0456\u043d\u0438\u0442\u0438 \u0432\u043f\u0440\u0430\u0432\u0443", "all" to "\u0423\u0441\u0456", "noExercisesFound" to "\u0412\u043f\u0440\u0430\u0432\u0438 \u043d\u0435 \u0437\u043d\u0430\u0439\u0434\u0435\u043d\u043e", "tryDifferentFilter" to "\u0421\u043f\u0440\u043e\u0431\u0443\u0439\u0442\u0435 \u0456\u043d\u0448\u0438\u0439 \u0444\u0456\u043b\u044c\u0442\u0440 \u0430\u0431\u043e \u043f\u043e\u0448\u0443\u043a \u0437\u0430 \u043d\u0430\u0437\u0432\u043e\u044e",\n',
    // French
    '"changeExercise" to "Changer d\'exercice",\n': '"changeExercise" to "Changer d\'exercice", "all" to "Tous", "noExercisesFound" to "Aucun exercice trouv\u00e9", "tryDifferentFilter" to "Essayez un autre filtre ou recherchez par nom",\n',
    // German
    '"changeExercise" to "\u00dcbung wechseln",\n': '"changeExercise" to "\u00dcbung wechseln", "all" to "Alle", "noExercisesFound" to "Keine \u00dcbungen gefunden", "tryDifferentFilter" to "Versuchen Sie einen anderen Filter oder suchen Sie nach Name",\n',
    // Spanish
    '"changeExercise" to "Cambiar ejercicio",\n': '"changeExercise" to "Cambiar ejercicio", "all" to "Todos", "noExercisesFound" to "No se encontraron ejercicios", "tryDifferentFilter" to "Prueba con otro filtro o busca por nombre",\n',
    // Italian
    '"changeExercise" to "Cambia esercizio",\n': '"changeExercise" to "Cambia esercizio", "all" to "Tutti", "noExercisesFound" to "Nessun esercizio trovato", "tryDifferentFilter" to "Prova un altro filtro o cerca per nome",\n',
    // Turkish
    '"changeExercise" to "Egzersizi de\u011fi\u015ftir",\n': '"changeExercise" to "Egzersizi de\u011fi\u015ftir", "all" to "T\u00fcm\u00fc", "noExercisesFound" to "Egzersiz bulunamad\u0131", "tryDifferentFilter" to "Farkl\u0131 bir filtre deneyin veya isme g\u00f6re aray\u0131n",\n',
    // Portuguese
    '"changeExercise" to "Trocar exerc\u00edcio",\n': '"changeExercise" to "Trocar exerc\u00edcio", "all" to "Todos", "noExercisesFound" to "Nenhum exerc\u00edcio encontrado", "tryDifferentFilter" to "Tente outro filtro ou pesquise por nome",\n',
    // Polish
    '"changeExercise" to "Zmie\u0144 \u0107wiczenie",\n': '"changeExercise" to "Zmie\u0144 \u0107wiczenie", "all" to "Wszystkie", "noExercisesFound" to "Nie znaleziono \u0107wicze\u0144", "tryDifferentFilter" to "Spr\u00f3buj innego filtru lub wyszukaj po nazwie",\n',
};

let changes = 0;
for (const [from, to] of Object.entries(langTranslations)) {
    if (content.includes(from) && !content.includes('"noExercisesFound"')) {
        // Only add if this language map doesn't already have noExercisesFound
        // Find the first occurrence and replace
        const idx = content.indexOf(from);
        if (idx !== -1) {
            content = content.substring(0, idx) + to + content.substring(idx + from.length);
            changes++;
        }
    }
}

// Check if Romanian already has the translations (it was added by previous script)
if (content.includes('"noExercisesFound" to "Nu s-au găsit exerciții"')) {
    console.log('Romanian already has translations');
} else {
    console.log('WARNING: Romanian translations missing');
}

console.log('Added translations to ' + changes + ' languages');
fs.writeFileSync(file, content, 'utf8');
console.log('File saved');
