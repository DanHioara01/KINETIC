const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/MainActivity.kt';
let c = fs.readFileSync(file, 'utf8');

let changes = 0;

// 1. Add SpeechRecognizer imports after import android.content.Intent
if (!c.includes('import android.speech.SpeechRecognizer')) {
    const target = 'import android.content.Intent';
    const replacement = target + '\nimport android.content.pm.PackageManager\nimport android.speech.SpeechRecognizer\nimport android.speech.RecognitionListener\nimport android.speech.RecognizerIntent';
    if (c.includes(target)) {
        c = c.replace(target, replacement);
        changes++;
        console.log('1. Added SpeechRecognizer imports');
    }
}

// 2. Add rememberLauncherForActivityResult, ActivityResultContracts, ActivityCompat
if (!c.includes('import androidx.activity.compose.rememberLauncherForActivityResult')) {
    const target2 = 'import androidx.activity.compose.setContent';
    const replacement2 = target2 + '\nimport androidx.activity.compose.rememberLauncherForActivityResult\nimport androidx.activity.result.contract.ActivityResultContracts\nimport androidx.core.app.ActivityCompat';
    if (c.includes(target2)) {
        c = c.replace(target2, replacement2);
        changes++;
        console.log('2. Added compose + permission imports');
    }
}

// 3. Fix conflicting declarations - remove duplicate context/activity
// The script added these inside CalendarScreen but they may conflict with existing ones
// Check for 'val context = LocalContext.current' duplicates
const contextMatches = c.match(/val context = LocalContext\.current/g);
if (contextMatches && contextMatches.length > 1) {
    // Remove the second occurrence (the one we added)
    let secondIdx = c.indexOf('val context = LocalContext.current');
    if (secondIdx !== -1) {
        secondIdx = c.indexOf('val context = LocalContext.current', secondIdx + 1);
        if (secondIdx !== -1) {
            // Find the line and remove it
            const lineStart = c.lastIndexOf('\n', secondIdx) + 1;
            const lineEnd = c.indexOf('\n', secondIdx);
            const line = c.substring(lineStart, lineEnd);
            console.log('3. Found duplicate context at: ' + line.trim());
            // Don't remove - they may be in different scopes
        }
    }
}

fs.writeFileSync(file, c, 'utf8');
console.log('\nTotal: ' + changes + ' changes');
