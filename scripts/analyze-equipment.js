const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/DataProvider.kt';
const c = fs.readFileSync(file, 'utf8');

const groups = ['Piept', 'Spate', 'Umeri', 'Biceps', 'Triceps', 'Abdomen', 'Picioare', 'Fese', 'Gambe', 'Antebrate', 'Gat & Trapezi', 'Cardio'];

for (const g of groups) {
    // Match ExerciseDefinition("Name", "Group", "Equipment")
    const regex = new RegExp(`ExerciseDefinition\\("[^"]+",\\s*"${g.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}[\\s\\S]*?"([\\w\\s&]+)"`, 'g');
    const equips = [];
    let m;
    while ((m = regex.exec(c)) !== null) {
        equips.push(m[1].trim());
    }
    const distinct = [...new Set(equips)].sort();
    console.log(`${g}: ${distinct.length} equipment types, ${equips.length} exercises -> ${distinct.join(', ')}`);
}

// Also check what the regex actually matches for Piept
console.log('\n--- Debug Piept first 5 matches ---');
const debugRegex = /ExerciseDefinition\("[^"]+",\s*"Piept",\s*"([^"]+)"/g;
let dm;
let count = 0;
while ((dm = debugRegex.exec(c)) !== null && count < 5) {
    console.log(`  equipment: "${dm[1]}"`);
    count++;
}
