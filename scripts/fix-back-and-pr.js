// Fix 1: Back button from ExerciseInputScreen (opened via Today's Workout) returns to Today's Workout main page
// Fix 2: Remove "PR: Xkg × Y" display from exercises in Today's Workout
const fs = require('fs');

// ============ FIX 1: MainActivity.kt ============
const mainPath = 'app/src/main/java/com/example/kinetic/MainActivity.kt';
let main = fs.readFileSync(mainPath, 'utf8');

// 1a) The onBackClick inside the quickExercise branch (line ~1550)
const oldBackClick = `                        onBackClick = {
                            if (quickExerciseIndex < quickExerciseList.size - 1) {
                                quickExerciseIndex++
                                quickExerciseGrupa = quickExerciseList[quickExerciseIndex].first
                                quickExerciseName = quickExerciseList[quickExerciseIndex].second
                            } else {
                                quickExerciseGrupa = null
                                quickExerciseName = null
                                quickExerciseList = emptyList()
                                quickExerciseIndex = 0
                            }
                        },`;
const newBackClick = `                        onBackClick = {
                            // Back from an exercise always returns to the main Today's Workout page
                            if (quickExerciseList.isNotEmpty()) {
                                showTodayWorkout = true
                            }
                            quickExerciseGrupa = null
                            quickExerciseName = null
                            quickExerciseList = emptyList()
                            quickExerciseIndex = 0
                        },`;
if (!main.includes(oldBackClick)) {
  console.error('ERROR: onBackClick block not found in MainActivity.kt');
  process.exit(1);
}
main = main.replace(oldBackClick, newBackClick);
console.log('Fixed onBackClick to return to Today\'s Workout main page');

// 1b) The top-level BackHandler (line ~1038) - also restore Today's Workout when quickExercise came from it
const oldTopBack = `            quickExerciseGrupa != null -> { quickExerciseGrupa = null; quickExerciseName = null; quickExerciseList = emptyList(); quickExerciseIndex = 0 }`;
const newTopBack = `            quickExerciseGrupa != null -> {
                if (quickExerciseList.isNotEmpty()) showTodayWorkout = true
                quickExerciseGrupa = null; quickExerciseName = null; quickExerciseList = emptyList(); quickExerciseIndex = 0
            }`;
if (!main.includes(oldTopBack)) {
  console.error('ERROR: top-level BackHandler line not found in MainActivity.kt');
  process.exit(1);
}
main = main.replace(oldTopBack, newTopBack);
console.log('Fixed top-level BackHandler to restore Today\'s Workout');

fs.writeFileSync(mainPath, main, 'utf8');

// ============ FIX 2: TodayWorkoutScreen.kt ============
const todayPath = 'app/src/main/java/com/example/kinetic/TodayWorkoutScreen.kt';
let today = fs.readFileSync(todayPath, 'utf8');

const oldPR = `                            val summary = exerciseSummaries[exercise.name]
                            if (summary != null && summary.bestWeight > 0) {
                                Text(
                                    "PR: ${summary.bestWeight.toInt()}kg × ${summary.bestReps}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFD94848)
                                )
                            }
`;
if (!today.includes(oldPR)) {
  console.error('ERROR: PR block not found in TodayWorkoutScreen.kt');
  process.exit(1);
}
today = today.replace(oldPR, '');
console.log('Removed PR display from Today\'s Workout exercises');

fs.writeFileSync(todayPath, today, 'utf8');
console.log('DONE');
