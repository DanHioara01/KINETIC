const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/DataProvider.kt';
let content = fs.readFileSync(file, 'utf8');

// Find exercitiiPeGrupa section and deduplicate
// Strategy: find each group's listOf(...) and remove duplicate exercise names

const lines = content.split('\n');
let inGroup = false;
let currentGroup = '';
let seenNames = new Set();
let removedCount = 0;
let groupRemoved = {};
const output = [];

for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    
    // Detect start of a group: "Group Name" to listOf(
    const groupMatch = line.match(/^\s+"([^"]+)"\s+to\s+listOf\(/);
    if (groupMatch) {
        inGroup = true;
        currentGroup = groupMatch[1];
        seenNames = new Set();
        output.push(line);
        continue;
    }
    
    if (inGroup) {
        // Detect exercise definition
        const exMatch = line.match(/ExerciseDefinition\("([^"]+)"/);
        if (exMatch) {
            const name = exMatch[1];
            if (seenNames.has(name)) {
                // Duplicate - skip this line
                removedCount++;
                if (!groupRemoved[currentGroup]) groupRemoved[currentGroup] = 0;
                groupRemoved[currentGroup]++;
                continue;
            }
            seenNames.add(name);
        }
        
        // Detect end of group (closing parenthesis with comma or just closing)
        if (line.match(/^\s+\),?$/)) {
            inGroup = false;
            currentGroup = '';
        }
        
        output.push(line);
        continue;
    }
    
    output.push(line);
}

fs.writeFileSync(file, output.join('\n'));
console.log(`Deduplication complete!`);
console.log(`Total duplicates removed: ${removedCount}`);
Object.entries(groupRemoved).forEach(([group, count]) => {
    console.log(`  ${group}: ${count} duplicates removed`);
});

// Also count remaining exercises per group
const newContent = fs.readFileSync(file, 'utf8');
const groupCounts = {};
const groupRegex = /"([^"]+)" to listOf\(/g;
let match;
let currentGroupName = '';
let exerciseCount = 0;

const lines2 = newContent.split('\n');
for (let i = 0; i < lines2.length; i++) {
    const gm = lines2[i].match(/^\s+"([^"]+)"\s+to\s+listOf\(/);
    if (gm) {
        if (currentGroupName) groupCounts[currentGroupName] = exerciseCount;
        currentGroupName = gm[1];
        exerciseCount = 0;
        continue;
    }
    if (currentGroupName && lines2[i].includes('ExerciseDefinition(')) {
        exerciseCount++;
    }
    if (currentGroupName && lines2[i].match(/^\s+\),?$/)) {
        groupCounts[currentGroupName] = exerciseCount;
        currentGroupName = '';
        exerciseCount = 0;
    }
}
if (currentGroupName) groupCounts[currentGroupName] = exerciseCount;

console.log('\nRemaining exercises per group:');
let total = 0;
Object.entries(groupCounts).forEach(([group, count]) => {
    console.log(`  ${group}: ${count}`);
    total += count;
});
console.log(`Total unique exercises: ${total}`);
