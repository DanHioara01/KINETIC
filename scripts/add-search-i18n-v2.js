const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/LanguageManager.kt';
let content = fs.readFileSync(file, 'utf8');

// The language maps are single long lines. Each ends with "changeExercise" to "...",
// but the changeExercise is NOT the last entry - there are more entries after it.
// We need to find each changeExercise entry and append noExercisesFound + tryDifferentFilter after it.

const replacements = [
    // Romanian
    { find: '"changeExercise" to "Schimbă exercițiul",', add: ' "noExercisesFound" to "Nu s-au găsit exerciții", "tryDifferentFilter" to "Încearcă un alt filtru sau caută după nume",' },
    // English
    { find: '"changeExercise" to "Change exercise",', add: ' "noExercisesFound" to "No exercises found", "tryDifferentFilter" to "Try a different filter or search by name",' },
    // Russian
    { find: '"changeExercise" to "Заменить упражнение",', add: ' "noExercisesFound" to "Упражнения не найдены", "tryDifferentFilter" to "Попробуйте другой фильтр или поиск по названию",' },
    // Ukrainian
    { find: '"changeExercise" to "Замінити вправу",', add: ' "noExercisesFound" to "Вправи не знайдено", "tryDifferentFilter" to "Спробуйте інший фільтр або пошук за назвою",' },
    // French
    { find: '"changeExercise" to "Changer d\'exercice",', add: ' "noExercisesFound" to "Aucun exercice trouvé", "tryDifferentFilter" to "Essayez un autre filtre ou recherchez par nom",' },
    // German
    { find: '"changeExercise" to "Übung wechseln",', add: ' "noExercisesFound" to "Keine Übungen gefunden", "tryDifferentFilter" to "Versuchen Sie einen anderen Filter oder suchen Sie nach Name",' },
    // Spanish
    { find: '"changeExercise" to "Cambiar ejercicio",', add: ' "noExercisesFound" to "No se encontraron ejercicios", "tryDifferentFilter" to "Prueba con otro filtro o busca por nombre",' },
    // Italian
    { find: '"changeExercise" to "Cambia esercizio",', add: ' "noExercisesFound" to "Nessun esercizio trovato", "tryDifferentFilter" to "Prova un altro filtro o cerca per nome",' },
    // Turkish
    { find: '"changeExercise" to "Egzersizi değiştir",', add: ' "noExercisesFound" to "Egzersiz bulunamadı", "tryDifferentFilter" to "Farklı bir filtre deneyin veya isme göre arayın",' },
    // Portuguese
    { find: '"changeExercise" to "Trocar exercício",', add: ' "noExercisesFound" to "Nenhum exercício encontrado", "tryDifferentFilter" to "Tente outro filtro ou pesquise por nome",' },
    // Polish
    { find: '"changeExercise" to "Zmień ćwiczenie",', add: ' "noExercisesFound" to "Nie znaleziono ćwiczeń", "tryDifferentFilter" to "Spróbuj innego filtru lub wyszukaj po nazwie",' },
];

let changes = 0;
for (const r of replacements) {
    // Check if this language already has the keys
    if (!content.includes(r.find + r.add) && content.includes(r.find)) {
        // Use split/join to replace all occurrences (should be exactly 1 per language)
        const parts = content.split(r.find);
        if (parts.length === 2) {
            content = parts[0] + r.find + r.add + parts[1];
            changes++;
            console.log('Added translations after: ' + r.find.substring(0, 40) + '...');
        } else {
            console.log('WARN: ' + r.find.substring(0, 30) + ' has ' + (parts.length - 1) + ' occurrences');
        }
    }
}

console.log('\nTotal language maps updated: ' + changes);
fs.writeFileSync(file, content, 'utf8');
console.log('File saved');

// Verify
const verify = fs.readFileSync(file, 'utf8');
console.log('\nVerification:');
console.log('  noExercisesFound count: ' + (verify.match(/noExercisesFound/g) || []).length);
console.log('  tryDifferentFilter count: ' + (verify.match(/tryDifferentFilter/g) || []).length);
