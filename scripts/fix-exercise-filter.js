const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/MainActivity.kt';
let c = fs.readFileSync(file, 'utf8');
let changes = 0;

// Fix 1: Make the LazyColumn for exercises fill remaining space with weight(1f)
const oldLazy = '                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {';
const newLazy = '                    LazyColumn(\n                        verticalArrangement = Arrangement.spacedBy(10.dp),\n                        modifier = Modifier.weight(1f)\n                    ) {';
if (c.includes(oldLazy)) {
    c = c.replace(oldLazy, newLazy);
    changes++;
    console.log('1. Added weight(1f) to exercise LazyColumn');
} else {
    console.log('WARN: LazyColumn target not found');
}

// Fix 2: Make the outer Column scrollable so filter chips are visible when content is long
const oldColumn = '        Column(\n            modifier = Modifier\n                .padding(paddingValues)\n                .padding(horizontal = 16.dp)\n        ) {';
const newColumn = '        Column(\n            modifier = Modifier\n                .padding(paddingValues)\n                .padding(horizontal = 16.dp)\n                .fillMaxSize()\n        ) {';
if (c.includes(oldColumn)) {
    c = c.replace(oldColumn, newColumn);
    changes++;
    console.log('2. Added fillMaxSize to outer Column');
} else {
    console.log('WARN: Column target not found');
}

// Fix 3: Ensure allEquipTypes includes empty string filter for "All" option
const oldEquip = '                    val allEquipTypes = remember(selectedGroupFilter) {\n                        (DataProvider.exercitiiPeGrupa[selectedGroupFilter] ?: listOf())\n                            .map { it.equipment }.distinct().sorted()\n                    }';
const newEquip = '                    val allEquipTypes = remember(selectedGroupFilter) {\n                        val equips = (DataProvider.exercitiiPeGrupa[selectedGroupFilter] ?: listOf())\n                            .map { it.equipment }.filter { it.isNotEmpty() }.distinct().sorted()\n                        equips\n                    }';
if (c.includes(oldEquip)) {
    c = c.replace(oldEquip, newEquip);
    changes++;
    console.log('3. Added filter for empty equipment strings');
} else {
    console.log('WARN: allEquipTypes target not found');
}

fs.writeFileSync(file, c, 'utf8');
console.log('Total changes: ' + changes);
