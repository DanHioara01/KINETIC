const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/DataProvider.kt';
let content = fs.readFileSync(file, 'utf8');

// Exercises that should be in Gat & Trapezi (target = traps in CSV)
// These are currently in Spate and need to be moved
const trapsExercises = [
    'Band Shrug',
    'Barbell Shrug',
    'Cable Shrug',
    'Dumbbell Decline Shrug',
    'Dumbbell Decline Shrug V. 2',
    'Dumbbell Incline Shrug',
    'Dumbbell Shrug',
    'Lever Gripless Shrug',
    'Lever Gripless Shrug V. 2',
    'Lever Shrug',
    'Smith Back Shrug',
    'Smith Shrug'
];

const lines = content.split('\n');
const output = [];
let removedFromSpate = [];
let addedToTrapzi = [];

// Find the Gat & Trapezi section and the Spate section
let inSpate = false;
let inTrapzi = false;
let trapziInsertIdx = -1;
let spateLines = [];

for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    
    // Detect Spate group start
    if (line.match(/"Spate"\s+to\s+listOf\(/)) {
        inSpate = true;
        output.push(line);
        continue;
    }
    
    // Detect Gat & Trapezi group start
    if (line.match(/"Gat & Trapezi"\s+to\s+listOf\(/)) {
        inTrapzi = true;
        trapziInsertIdx = output.length;
        output.push(line);
        continue;
    }
    
    // If in Spate, check if this line has a traps exercise
    if (inSpate) {
        const exMatch = line.match(/ExerciseDefinition\("([^"]+)"/);
        if (exMatch) {
            const name = exMatch[1];
            if (trapsExercises.includes(name)) {
                // This exercise should be in Gat & Trapezi, not Spate
                // Change the group from "Spate" to "Gat & Trapezi"
                const movedLine = line.replace(/"Spate"/, '"Gat & Trapezi"');
                spateLines.push(movedLine);
                removedFromSpate.push(name);
                continue; // Skip this line from Spate
            }
        }
        // Check if we've left the Spate group
        if (line.match(/^\s+\),?$/) && !line.includes('ExerciseDefinition')) {
            inSpate = false;
        }
        output.push(line);
        continue;
    }
    
    // If in Gat & Trapezi, collect lines to insert moved exercises
    if (inTrapzi) {
        // Check if we've left the Gat & Trapezi group
        if (line.match(/^\s+\),?$/) && !line.includes('ExerciseDefinition')) {
            inTrapzi = false;
            // Insert moved exercises before the closing
            spateLines.forEach(l => {
                output.push(l);
                addedToTrapzi.push(l.match(/ExerciseDefinition\("([^"]+)"/)?.[1] || '');
            });
            spateLines = [];
        }
        output.push(line);
        continue;
    }
    
    output.push(line);
}

// If we collected moves but didn't insert them yet
if (spateLines.length > 0) {
    // Find the last line of Gat & Trapezi and insert before it
    const lastTrapziIdx = output.length - 1;
    for (let i = output.length - 1; i >= 0; i--) {
        if (output[i].match(/"Gat & Trapezi"/)) {
            // Insert after the group header
            for (let j = i + 1; j < output.length; j++) {
                if (output[j].match(/^\s+\),?$/) && !output[j].includes('ExerciseDefinition')) {
                    // Insert before the closing
                    spateLines.forEach(l => output.splice(j, 0, l));
                    break;
                }
            }
            break;
        }
    }
}

fs.writeFileSync(file, output.join('\n'));
console.log(`Moved ${removedFromSpate.length} exercises from Spate to Gat & Trapezi:`);
removedFromSpate.forEach(name => console.log(`  - ${name}`));

// Count remaining in Gat & Trapezi
const newContent = fs.readFileSync(file, 'utf8');
const trapziSection = newContent.match(/"Gat & Trapezi" to listOf\(([\s\S]*?)\n        \),/);
if (trapziSection) {
    const count = (trapziSection[1].match(/ExerciseDefinition/g) || []).length;
    console.log(`\nGat & Trapezi now has ${count} exercises`);
}
