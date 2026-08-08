const fs = require('fs');

// === Fix 1: LanguageManager.kt — Add 3 new keys to Strings data class + all 11 languages ===
const lmFile = 'app/src/main/java/com/example/kinetic/LanguageManager.kt';
let lm = fs.readFileSync(lmFile, 'utf8');
let lmChanges = 0;

// Add keys to Strings data class (after chooseMuscleGroup)
const stringsTarget = '        val chooseMuscleGroup: String = m["chooseMuscleGroup"] ?: ""\n        val changeExercise: String = m["changeExercise"] ?: ""';
const stringsReplace = '        val chooseMuscleGroup: String = m["chooseMuscleGroup"] ?: ""\n        val changeExercise: String = m["changeExercise"] ?: ""\n        val all: String = m["all"] ?: ""\n        val noExercisesFound: String = m["noExercisesFound"] ?: ""\n        val tryDifferentFilter: String = m["tryDifferentFilter"] ?: ""';

if (lm.includes(stringsTarget)) {
    lm = lm.replace(stringsTarget, stringsReplace);
    lmChanges++;
    console.log('1. Added 3 keys to Strings data class');
} else {
    console.log('1. WARN: Strings target not found');
}

// Add translations to each language map
const translations = {
    // Romanian (line ~833)
    '"changeExercise" to "Schimbă exercițiul",': '"changeExercise" to "Schimbă exercițiul", "all" to "Toate", "noExercisesFound" to "Nu s-au găsit exerciții", "tryDifferentFilter" to "Încearcă un alt filtru sau caută după nume",',
    // English (line ~1052)
    '"changeExercise" to "Change exercise",': '"changeExercise" to "Change exercise", "all" to "All", "noExercisesFound" to "No exercises found", "tryDifferentFilter" to "Try a different filter or search by name",',
    // Russian (line ~1260)
    '"changeExercise" to "Заменить упражнение",': '"changeExercise" to "Заменить упражнение", "all" to "Все", "noExercisesFound" to "Упражнения не найдены", "tryDifferentFilter" to "Попробуйте другой фильтр или поиск по названию",',
    // Ukrainian (line ~1463)
    '"changeExercise" to "Замінити вправу",': '"changeExercise" to "Замінити вправу", "all" to "Усі", "noExercisesFound" to "Вправи не знайдено", "tryDifferentFilter" to "Спробуйте інший фільтр або пошук за назвою",',
    // French (line ~1671)
    '"changeExercise" to "Changer d\'exercice",': '"changeExercise" to "Changer d\'exercice", "all" to "Tous", "noExercisesFound" to "Aucun exercice trouvé", "tryDifferentFilter" to "Essayez un autre filtre ou recherchez par nom",',
    // German (line ~1877)
    '"changeExercise" to "Übung wechseln",': '"changeExercise" to "Übung wechseln", "all" to "Alle", "noExercisesFound" to "Keine Übungen gefunden", "tryDifferentFilter" to "Versuchen Sie einen anderen Filter oder suchen Sie nach Name",',
    // Spanish (line ~2088)
    '"changeExercise" to "Cambiar ejercicio",': '"changeExercise" to "Cambiar ejercicio", "all" to "Todos", "noExercisesFound" to "No se encontraron ejercicios", "tryDifferentFilter" to "Prueba con otro filtro o busca por nombre",',
    // Italian (line ~2296)
    '"changeExercise" to "Cambia esercizio",': '"changeExercise" to "Cambia esercizio", "all" to "Tutti", "noExercisesFound" to "Nessun esercizio trovato", "tryDifferentFilter" to "Prova un altro filtro o cerca per nome",',
    // Turkish (line ~2502)
    '"changeExercise" to "Egzersizi değiştir",': '"changeExercise" to "Egzersizi değiştir", "all" to "Tümü", "noExercisesFound" to "Egzersiz bulunamadı", "tryDifferentFilter" to "Farklı bir filtre deneyin veya isme göre arayın",',
    // Portuguese (line ~2714)
    '"changeExercise" to "Trocar exercício",': '"changeExercise" to "Trocar exercício", "all" to "Todos", "noExercisesFound" to "Nenhum exercício encontrado", "tryDifferentFilter" to "Tente outro filtro ou pesquise por nome",',
    // Polish (line ~2926)
    '"changeExercise" to "Zmień ćwiczenie",': '"changeExercise" to "Zmień ćwiczenie", "all" to "Wszystkie", "noExercisesFound" to "Nie znaleziono ćwiczeń", "tryDifferentFilter" to "Spróbuj innego filtru lub wyszukaj po nazwie",',
};

for (const [from, to] of Object.entries(translations)) {
    if (lm.includes(from) && !lm.includes(to.split(',')[0] + ',')) {
        lm = lm.split(from).join(to);
        lmChanges++;
    }
}
console.log('2. Added translations to ' + lmChanges + ' language maps');

fs.writeFileSync(lmFile, lm, 'utf8');
console.log('LanguageManager.kt saved (' + lmChanges + ' changes)');

// === Fix 2: MainActivity.kt — SearchOff → Search, remove isDark, wrap in remember ===
const mainFile = 'app/src/main/java/com/example/kinetic/MainActivity.kt';
let main = fs.readFileSync(mainFile, 'utf8');
let mainChanges = 0;

// Replace SearchOff with Search (since material-icons-extended is not available)
if (main.includes('Icons.Default.SearchOff')) {
    main = main.replace(/Icons\.Default\.SearchOff/g, 'Icons.Default.Search');
    mainChanges++;
    console.log('3. Replaced SearchOff with Search icon');
}

// Remove dead isDark variable (keep textSecondary but inline it)
const isDarkLine = '    val isDark = isSystemInDarkTheme()\n    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary';
const textSecondaryInline = '    val textSecondary = if (isSystemInDarkTheme()) secondaryTextColor() else LightTextSecondary';
if (main.includes(isDarkLine)) {
    main = main.replace(isDarkLine, textSecondaryInline);
    mainChanges++;
    console.log('4. Removed dead isDark variable, inlined textSecondary');
}

// Replace hardcoded Romanian in empty state with strings references
const emptyStateOld = `                                    Text(\n                                        "Nu s-au gasit exercitii",`;
const emptyStateNew = `                                    Text(\n                                        strings.noExercisesFound,`;
if (main.includes(emptyStateOld)) {
    main = main.replace(emptyStateOld, emptyStateNew);
    mainChanges++;
    console.log('5. Replaced hardcoded "Nu s-au gasit exercitii" with strings.noExercisesFound');
}

const hintOld = `                                    Text(\n                                        "Incearca un alt filtru sau cauta dupa nume",`;
const hintNew = `                                    Text(\n                                        strings.tryDifferentFilter,`;
if (main.includes(hintOld)) {
    main = main.replace(hintOld, hintNew);
    mainChanges++;
    console.log('6. Replaced hardcoded hint with strings.tryDifferentFilter');
}

// Replace "Toate" with strings.all
const toateOld = 'label = { Text("Toate", fontSize = 12.sp) }';
const toateNew = 'label = { Text(strings.all, fontSize = 12.sp) }';
if (main.includes(toateOld)) {
    main = main.replace(toateOld, toateNew);
    mainChanges++;
    console.log('7. Replaced hardcoded "Toate" with strings.all');
}

// Wrap filteredGroupExercises in remember
const filterOld = '                    val filteredGroupExercises = allGroupExercises.filter { ex ->\n                        val matchesSearch = exerciseSearchQuery.isBlank() ||\n                            ex.name.contains(exerciseSearchQuery, ignoreCase = true) ||\n                            ex.equipment.contains(exerciseSearchQuery, ignoreCase = true)\n                        val matchesEquip = exerciseEquipmentFilter == null ||\n                            ex.equipment == exerciseEquipmentFilter\n                        matchesSearch && matchesEquip\n                    }';
const filterNew = '                    val filteredGroupExercises = remember(allGroupExercises, exerciseSearchQuery, exerciseEquipmentFilter) {\n                        allGroupExercises.filter { ex ->\n                            val matchesSearch = exerciseSearchQuery.isBlank() ||\n                                ex.name.contains(exerciseSearchQuery, ignoreCase = true) ||\n                                ex.equipment.contains(exerciseSearchQuery, ignoreCase = true)\n                            val matchesEquip = exerciseEquipmentFilter == null ||\n                                ex.equipment == exerciseEquipmentFilter\n                            matchesSearch && matchesEquip\n                        }\n                    }';
if (main.includes(filterOld)) {
    main = main.replace(filterOld, filterNew);
    mainChanges++;
    console.log('8. Wrapped filteredGroupExercises in remember()');
}

fs.writeFileSync(mainFile, main, 'utf8');
console.log('MainActivity.kt saved (' + mainChanges + ' changes)');
console.log('\nTotal: ' + (lmChanges + mainChanges) + ' changes across 2 files');
