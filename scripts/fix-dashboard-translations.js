const fs = require('fs');
const path = 'app/src/main/java/com/example/kinetic/DashboardScreen.kt';
let content = fs.readFileSync(path, 'utf8');

// Fix 1: Replace hardcoded "Energize", "Perform", "Push It" with strings
// These are in the tipLabel assignments
const old1 = '0 -> Pair("Energize", Icons.Default.Battery1Bar)';
const new1 = '0 -> Pair(strings.energizeLabel, Icons.Default.Battery1Bar)';
if (content.includes(old1)) {
    content = content.replace(old1, new1);
    console.log('Fix 1: Energize -> strings.energizeLabel');
}

const old2 = '0 -> Pair("Perform", Icons.Default.TrendingUp)';
const new2 = '0 -> Pair(strings.performLabel, Icons.Default.TrendingUp)';
if (content.includes(old2)) {
    content = content.replace(old2, new2);
    console.log('Fix 2: Perform -> strings.performLabel');
}

const old3 = '0 -> Pair("Push It", Icons.Default.BatteryFull)';
const new3 = '0 -> Pair(strings.pushItLabel, Icons.Default.BatteryFull)';
if (content.includes(old3)) {
    content = content.replace(old3, new3);
    console.log('Fix 3: Push It -> strings.pushItLabel');
}

// Fix 2: Replace hardcoded color mappings
const old4 = '"Energize" -> Color(0xFF4A90D9)';
const new4 = 'strings.energizeLabel -> Color(0xFF4A90D9)';
if (content.includes(old4)) {
    content = content.replace(old4, new4);
    console.log('Fix 4: Color mapping Energize -> strings.energizeLabel');
}

const old5 = '"Perform" -> Color(0xFF009688)';
const new5 = 'strings.performLabel -> Color(0xFF009688)';
if (content.includes(old5)) {
    content = content.replace(old5, new5);
    console.log('Fix 5: Color mapping Perform -> strings.performLabel');
}

const old6 = '"Push It" -> Color(0xFFFF5722)';
const new6 = 'strings.pushItLabel -> Color(0xFFFF5722)';
if (content.includes(old6)) {
    content = content.replace(old6, new6);
    console.log('Fix 6: Color mapping Push It -> strings.pushItLabel');
}

fs.writeFileSync(path, content, 'utf8');
console.log('\nDashboardScreen.kt updated');
