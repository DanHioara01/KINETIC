const fs = require('fs');
const path = require('path');

const filePath = 'C:/Users/danhi/OneDrive/Desktop/Kinetic/app/src/main/java/com/example/kinetic/MainActivity.kt';
let content = fs.readFileSync(filePath, 'utf8');

// FIX 1: Exercise detail screen - add recoveryRefreshTrigger to LaunchedEffect
// The save callback at line ~3203 needs to increment a trigger
// And the LaunchedEffect at line ~3237 needs to use that trigger

// First, find the save callback and add a trigger increment
// Look for the pattern: showSaveConfirmation = true
const saveCallbackPattern = '                                isPR = newPR\n                                showSaveConfirmation = true\n                            }';
const saveCallbackReplacement = '                                isPR = newPR\n                                showSaveConfirmation = true\n                                recoveryRefreshTrigger++\n                            }';

if (content.includes(saveCallbackPattern)) {
    content = content.replace(saveCallbackPattern, saveCallbackReplacement);
    console.log('FIX 1a: Added recoveryRefreshTrigger++ to save callback');
} else {
    console.log('WARNING: Could not find save callback pattern');
}

// Now find the LaunchedEffect(grupaMusculara) in the recovery bar item
// and add recoveryRefreshTrigger as a key
const launchEffectPattern = '                var groupLevel by remember { mutableStateOf(0.0) }\n                LaunchedEffect(grupaMusculara) {';
const launchEffectReplacement = '                var groupLevel by remember { mutableStateOf(0.0) }\n                var recoveryRefreshTrigger by remember { mutableIntStateOf(0) }\n                LaunchedEffect(grupaMusculara, recoveryRefreshTrigger) {';

if (content.includes(launchEffectPattern)) {
    content = content.replace(launchEffectPattern, launchEffectReplacement);
    console.log('FIX 1b: Added recoveryRefreshTrigger to LaunchedEffect for recovery bar');
} else {
    console.log('WARNING: Could not find LaunchedEffect(grupaMusculara) pattern');
}

// FIX 2: MuscleRecoveryScreen - add lifecycle-based refresh
// Find the MuscleRecoveryScreen and add a isVisible trigger
const muscleRecoveryPattern = '    var isLoading by remember { mutableStateOf(true) }\n\n    LaunchedEffect(Unit) {\n        viewModel.getToateRecuperarile(userId) { data ->\n            recoveryData = data\n            isLoading = false\n        }\n    }';

const muscleRecoveryReplacement = '    var isLoading by remember { mutableStateOf(true) }\n    var refreshTrigger by remember { mutableIntStateOf(0) }\n\n    // Refresh when screen becomes visible\n    val lifecycleOwner = LocalLifecycleOwner.current\n    DisposableEffect(lifecycleOwner) {\n        val observer = LifecycleEventObserver { _, event ->\n            if (event == Lifecycle.Event.ON_RESUME) {\n                refreshTrigger++\n            }\n        }\n        lifecycleOwner.lifecycle.addObserver(observer)\n        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }\n    }\n\n    LaunchedEffect(refreshTrigger) {\n        viewModel.getToateRecuperarile(userId) { data ->\n            recoveryData = data\n            isLoading = false\n        }\n    }';

if (content.includes(muscleRecoveryPattern)) {
    content = content.replace(muscleRecoveryPattern, muscleRecoveryReplacement);
    console.log('FIX 2: Added lifecycle-based refresh to MuscleRecoveryScreen');
} else {
    console.log('WARNING: Could not find MuscleRecoveryScreen pattern');
}

// Make sure mutableIntStateOf import exists
if (!content.includes('import androidx.compose.runtime.mutableIntStateOf')) {
    content = content.replace(
        'import androidx.compose.runtime.mutableStateOf',
        'import androidx.compose.runtime.mutableStateOf\nimport androidx.compose.runtime.mutableIntStateOf'
    );
    console.log('Added mutableIntStateOf import');
}

// Make sure lifecycle imports exist
if (!content.includes('import androidx.lifecycle.LifecycleEventObserver')) {
    // Find the last lifecycle import and add after it
    const lifecycleImport = 'import androidx.lifecycle.Lifecycle';
    if (content.includes(lifecycleImport)) {
        content = content.replace(
            lifecycleImport,
            lifecycleImport + '\nimport androidx.lifecycle.LifecycleEventObserver'
        );
        console.log('Added LifecycleEventObserver import');
    }
}

if (!content.includes('import androidx.compose.ui.platform.LocalLifecycleOwner')) {
    // Add after LocalContext import
    content = content.replace(
        'import androidx.compose.ui.platform.LocalContext',
        'import androidx.compose.ui.platform.LocalContext\nimport androidx.compose.ui.platform.LocalLifecycleOwner'
    );
    console.log('Added LocalLifecycleOwner import');
}

if (!content.includes('import androidx.compose.runtime.DisposableEffect')) {
    content = content.replace(
        'import androidx.compose.runtime.mutableIntStateOf',
        'import androidx.compose.runtime.DisposableEffect\nimport androidx.compose.runtime.mutableIntStateOf'
    );
    console.log('Added DisposableEffect import');
}

fs.writeFileSync(filePath, content, 'utf8');
console.log('All fixes applied to MainActivity.kt');
