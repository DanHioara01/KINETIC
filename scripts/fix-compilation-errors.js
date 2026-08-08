const fs = require('fs');
const path = 'app/src/main/java/com/example/kinetic/MainActivity.kt';
let content = fs.readFileSync(path, 'utf8');

// FIX 1: Add missing lifecycle imports after the existing LocalLifecycleOwner import (line 75)
const lifecycleImports = `import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver`;

content = content.replace(
  'import androidx.compose.ui.platform.LocalLifecycleOwner',
  lifecycleImports
);

// FIX 2: Move recoveryRefreshTrigger declaration to function level (after workoutDurationMs at ~line 3018)
// Add it after the existing state declarations, before LaunchedEffect(Unit)
const insertAfter = '    val workoutDurationMs = remember { mutableLongStateOf(0L) }\n';
const recoveryDecl = '    var recoveryRefreshTrigger by remember { mutableIntStateOf(0) }\n';

if (!content.includes('val workoutDurationMs = remember { mutableLongStateOf(0L) }\n    var recoveryRefreshTrigger')) {
  content = content.replace(insertAfter, insertAfter + recoveryDecl);
}

// FIX 3: Remove the duplicate recoveryRefreshTrigger declaration inside the item block (line 3240)
// The line is: "                var recoveryRefreshTrigger by remember { mutableIntStateOf(0) }"
// inside an item {} block of a LazyColumn
content = content.replace(
  /                var recoveryRefreshTrigger by remember \{ mutableIntStateOf\(0\) \}\n/,
  ''
);

fs.writeFileSync(path, content, 'utf8');
console.log('All 3 fixes applied successfully');
