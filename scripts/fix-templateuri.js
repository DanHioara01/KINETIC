const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/DataProvider.kt';
let content = fs.readFileSync(file, 'utf8');

const templateuri = `
    val templateuri = listOf(
        WorkoutTemplate("Push", listOf(
            TemplateExercise("Piept", ExerciseDefinition("Bench Press", "Piept", "Barbell")),
            TemplateExercise("Piept", ExerciseDefinition("Incline Bench Press", "Piept", "Barbell")),
            TemplateExercise("Piept", ExerciseDefinition("Dumbbell Fly", "Piept", "Dumbbells")),
            TemplateExercise("Umeri", ExerciseDefinition("Overhead Press", "Umeri", "Barbell")),
            TemplateExercise("Umeri", ExerciseDefinition("Lateral Raise", "Umeri", "Dumbbells")),
            TemplateExercise("Triceps", ExerciseDefinition("Tricep Pushdown", "Triceps", "Cable"))
        )),
        WorkoutTemplate("Pull", listOf(
            TemplateExercise("Spate", ExerciseDefinition("Deadlift", "Spate", "Barbell")),
            TemplateExercise("Spate", ExerciseDefinition("Barbell Row", "Spate", "Barbell")),
            TemplateExercise("Spate", ExerciseDefinition("Pull-Up", "Spate", "Bodyweight")),
            TemplateExercise("Spate", ExerciseDefinition("Lat Pulldown", "Spate", "Cable")),
            TemplateExercise("Gat & Trapezi", ExerciseDefinition("Barbell Shrugs", "Gat & Trapezi", "Barbell")),
            TemplateExercise("Biceps", ExerciseDefinition("Barbell Curl", "Biceps", "Barbell")),
            TemplateExercise("Umeri", ExerciseDefinition("Face Pull", "Umeri", "Cable"))
        )),
        WorkoutTemplate("Legs", listOf(
            TemplateExercise("Picioare", ExerciseDefinition("Squat", "Picioare", "Barbell")),
            TemplateExercise("Picioare", ExerciseDefinition("Romanian Deadlift", "Picioare", "Barbell")),
            TemplateExercise("Gambe", ExerciseDefinition("Leg Curl", "Gambe", "Machine")),
            TemplateExercise("Gambe", ExerciseDefinition("Leg Extension", "Gambe", "Machine")),
            TemplateExercise("Gambe", ExerciseDefinition("Calf Raise", "Gambe", "Machine"))
        )),
        WorkoutTemplate("Upper", listOf(
            TemplateExercise("Piept", ExerciseDefinition("Bench Press", "Piept", "Barbell")),
            TemplateExercise("Spate", ExerciseDefinition("Barbell Row", "Spate", "Barbell")),
            TemplateExercise("Umeri", ExerciseDefinition("Overhead Press", "Umeri", "Barbell")),
            TemplateExercise("Biceps", ExerciseDefinition("Barbell Curl", "Biceps", "Barbell")),
            TemplateExercise("Triceps", ExerciseDefinition("Tricep Pushdown", "Triceps", "Cable"))
        )),
        WorkoutTemplate("Full Body", listOf(
            TemplateExercise("Piept", ExerciseDefinition("Bench Press", "Piept", "Barbell")),
            TemplateExercise("Spate", ExerciseDefinition("Barbell Row", "Spate", "Barbell")),
            TemplateExercise("Picioare", ExerciseDefinition("Squat", "Picioare", "Barbell")),
            TemplateExercise("Umeri", ExerciseDefinition("Overhead Press", "Umeri", "Barbell")),
            TemplateExercise("Abdomen", ExerciseDefinition("Plank", "Abdomen", "Bodyweight"))
        ))
    )
`;

const marker = '    val defaultExercises: List<ExerciseDef> = listOf(';
const idx = content.indexOf(marker);
if (idx === -1) { console.log('MARKER NOT FOUND'); process.exit(1); }
content = content.slice(0, idx) + templateuri + '\n\n' + content.slice(idx);
fs.writeFileSync(file, content);
console.log('TEMPLATEURI INSERTED at position ' + idx);
