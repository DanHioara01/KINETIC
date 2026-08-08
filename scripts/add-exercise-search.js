const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/MainActivity.kt';
let content = fs.readFileSync(file, 'utf8');

// 1. Add state variables for search and equipment filter
const stateMarker = '    var selectedGroupFilter by remember { mutableStateOf<String?>(null) }';
const stateReplacement = '    var selectedGroupFilter by remember { mutableStateOf<String?>(null) }\n    var exerciseSearchQuery by remember { mutableStateOf("") }\n    var exerciseEquipmentFilter by remember { mutableStateOf<String?>(null) }';

if (content.includes(stateMarker) && !content.includes('exerciseSearchQuery')) {
    content = content.replace(stateMarker, stateReplacement);
    console.log('State variables added');
} else {
    console.log('State variables already present or marker not found');
}

// 2. Replace the exercise list section
const searchBlock = [
    '                    // Search + Equipment Filter',
    '                    OutlinedTextField(',
    '                        value = exerciseSearchQuery,',
    '                        onValueChange = { exerciseSearchQuery = it },',
    '                        placeholder = { Text(strings.search, color = textSecondary) },',
    '                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary) },',
    '                        trailingIcon = {',
    '                            if (exerciseSearchQuery.isNotEmpty()) {',
    '                                IconButton(onClick = { exerciseSearchQuery = "" }) {',
    '                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = textSecondary)',
    '                                }',
    '                            }',
    '                        },',
    '                        singleLine = true,',
    '                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),',
    '                        colors = OutlinedTextFieldDefaults.colors(',
    '                            focusedBorderColor = accentColor(),',
    '                            unfocusedBorderColor = dividerColor(),',
    '                            cursorColor = accentColor(),',
    '                            focusedTextColor = textColor(),',
    '                            unfocusedTextColor = textColor(),',
    '                            focusedContainerColor = cardColor().copy(alpha = 0.5f),',
    '                            unfocusedContainerColor = cardColor().copy(alpha = 0.5f)',
    '                        ),',
    '                        shape = RoundedCornerShape(12.dp)',
    '                    )',
    '                    val allEquipTypes = remember(selectedGroupFilter) {',
    '                        (DataProvider.exercitiiPeGrupa[selectedGroupFilter] ?: listOf())',
    '                            .map { it.equipment }.distinct().sorted()',
    '                    }',
    '                    if (allEquipTypes.size > 1) {',
    '                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 10.dp)) {',
    '                            item {',
    '                                FilterChip(',
    '                                    selected = exerciseEquipmentFilter == null,',
    '                                    onClick = { exerciseEquipmentFilter = null },',
    '                                    label = { Text("All", fontSize = 12.sp) },',
    '                                    colors = FilterChipDefaults.filterChipColors(',
    '                                        selectedContainerColor = accentColor(),',
    '                                        selectedLabelColor = Color.White',
    '                                    ),',
    '                                    shape = RoundedCornerShape(20.dp)',
    '                                )',
    '                            }',
    '                            items(allEquipTypes) { equip ->',
    '                                FilterChip(',
    '                                    selected = exerciseEquipmentFilter == equip,',
    '                                    onClick = { exerciseEquipmentFilter = if (exerciseEquipmentFilter == equip) null else equip },',
    '                                    label = { Text(equip, fontSize = 12.sp) },',
    '                                    colors = FilterChipDefaults.filterChipColors(',
    '                                        selectedContainerColor = accentColor(),',
    '                                        selectedLabelColor = Color.White',
    '                                    ),',
    '                                    shape = RoundedCornerShape(20.dp)',
    '                                )',
    '                            }',
    '                        }',
    '                    }',
    '                    val allGroupExercises = DataProvider.exercitiiPeGrupa[selectedGroupFilter] ?: listOf()',
    '                    val filteredGroupExercises = allGroupExercises.filter { ex ->',
    '                        val matchesSearch = exerciseSearchQuery.isBlank() ||',
    '                            ex.name.contains(exerciseSearchQuery, ignoreCase = true) ||',
    '                            ex.equipment.contains(exerciseSearchQuery, ignoreCase = true)',
    '                        val matchesEquip = exerciseEquipmentFilter == null ||',
    '                            ex.equipment == exerciseEquipmentFilter',
    '                        matchesSearch && matchesEquip',
    '                    }',
    '                    Text(',
    '                        text = "${"$"}{filteredGroupExercises.size} ${"$"}{strings.exercises}",',
    '                        fontSize = 12.sp,',
    '                        color = textSecondary,',
    '                        modifier = Modifier.padding(bottom = 8.dp)',
    '                    )',
    '                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {',
    '                        items(filteredGroupExercises) { exercitiu ->',
].join('\n');

const oldSection = '                    val exercitiiDinGrupa = DataProvider.exercitiiPeGrupa[selectedGroupFilter] ?: listOf()\n                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {\n                        items(exercitiiDinGrupa) { exercitiu ->';

if (content.includes(oldSection)) {
    content = content.replace(oldSection, searchBlock);
    console.log('Exercise list section replaced with search + chips');
} else {
    console.log('WARNING: Old section not found');
}

fs.writeFileSync(file, content);
console.log('Done!');
