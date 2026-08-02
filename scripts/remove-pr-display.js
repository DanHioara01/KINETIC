// Remove "PR: Xkg × Y" display from exercises in Today's Workout
const fs = require('fs');
const todayPath = 'app/src/main/java/com/example/kinetic/TodayWorkoutScreen.kt';
let today = fs.readFileSync(todayPath, 'utf8');

// Build the exact block using string concatenation to avoid template-literal interpolation
const oldPR =
  '                            val summary = exerciseSummaries[exercise.name]\n' +
  '                            if (summary != null && summary.bestWeight > 0) {\n' +
  '                                Text(\n' +
  '                                    "PR: " + "\\u0024{summary.bestWeight.toInt()}kg × \\u0024{summary.bestReps}",\n' +
  '                                    fontSize = 11.sp,\n' +
  '                                    color = Color(0xFFD94848)\n' +
  '                                )\n' +
  '                            }\n';

if (!today.includes(oldPR)) {
  // Try without the escaped version - maybe file uses different format
  console.error('ERROR: PR block not found with escaped interpolation');
  process.exit(1);
}
today = today.replace(oldPR, '');
fs.writeFileSync(todayPath, today, 'utf8');
console.log('Removed PR display from Today\'s Workout exercises');
console.log('DONE');
