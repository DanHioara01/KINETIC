const fs = require('fs');
const path = require('path');

const CSV_PATH = 'C:/Users/danhi/OneDrive/Desktop/Pentru Kinetic/fitness_exercises.csv';
const APP_DIR = 'C:/Users/danhi/OneDrive/Desktop/Kinetic/app/src/main/java/com/example/kinetic';

// Read CSV
const csvContent = fs.readFileSync(CSV_PATH, 'utf-8');
const lines = csvContent.trim().split('\n');
const header = lines[0].split(',');
const exercises = lines.slice(1).map(line => {
  // Handle commas inside quoted fields
  const parts = [];
  let current = '';
  let inQuotes = false;
  for (let i = 0; i < line.length; i++) {
    if (line[i] === '"') { inQuotes = !inQuotes; continue; }
    if (line[i] === ',' && !inQuotes) { parts.push(current); current = ''; continue; }
    current += line[i];
  }
  parts.push(current);
  return {
    bodyPart: parts[0] || '',
    equipment: parts[1] || '',
    gifUrl: parts[2] || '',
    id: parts[3] || '',
    name: parts[4] || '',
    target: parts[5] || ''
  };
});

console.log(`Total exercises in CSV: ${exercises.length}`);

// Mapping CSV bodyPart/target → Romanian group names
function mapGroup(bodyPart, target, name) {
  const bp = bodyPart.toLowerCase();
  const tgt = target.toLowerCase();
  
  if (bp === 'chest') return 'Piept';
  if (tgt === 'traps') return 'Gat & Trapezi';
  if (bp === 'back') return 'Spate';
  if (bp === 'shoulders') return 'Umeri';
  if (bp === 'lower arms') return 'Antebrate';
  if (bp === 'waist') return 'Abdomen';
  if (bp === 'lower legs') return 'Gambe';
  if (bp === 'cardio') return 'Cardio';
  if (bp === 'neck') return 'Gat & Trapezi';
  
  if (bp === 'upper arms') {
    if (tgt === 'biceps') return 'Biceps';
    return 'Triceps';
  }
  
  if (bp === 'upper legs') {
    if (tgt === 'glutes') return 'Fese';
    return 'Picioare';
  }
  
  return 'Picioare';
}

// Mapping CSV equipment → app equipment names
function mapEquipment(equipment) {
  const eq = equipment.toLowerCase();
  if (eq === 'body weight') return 'Bodyweight';
  if (eq === 'dumbbell') return 'Dumbbells';
  if (eq === 'barbell') return 'Barbell';
  if (eq === 'cable') return 'Cable';
  if (eq === 'band') return 'Band';
  if (eq === 'kettlebell') return 'Kettlebell';
  if (eq === 'ez barbell') return 'EZ Bar';
  if (eq === 'leverage machine') return 'Machine';
  if (eq === 'assisted') return 'Assisted';
  if (eq === 'stability ball') return 'Stability Ball';
  if (eq === 'medicine ball') return 'Medicine Ball';
  if (eq === 'rope') return 'Rope';
  if (eq === 'sled machine') return 'Sled Machine';
  if (eq === 'upper body ergometer') return 'Ergometer';
  if (eq === 'smith machine') return 'Smith Machine';
  return equipment.charAt(0).toUpperCase() + equipment.slice(1);
}

// Capitalize exercise name properly
function capitalizeName(name) {
  return name.split(' ').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
}

// ============================================
// 1. Generate ExerciseGifs.kt
// ============================================
function generateExerciseGifs() {
  const baseUrl = 'https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0';
  const cwUrl = 'https://raw.githubusercontent.com/omercotkd/exercises-gifs/main/assets';
  
  let content = `package com.example.kinetic

object ExerciseGifs {
    private const val BASE = "${baseUrl}"
    private const val CW = "${cwUrl}"

    private val gifs = mapOf(\n`;

  // Group by target for organized output
  const byTarget = {};
  exercises.forEach(ex => {
    const target = ex.target.toLowerCase();
    if (!byTarget[target]) byTarget[target] = [];
    byTarget[target].push(ex);
  });

  const targetOrder = ['pectorals', 'lats', 'upper back', 'spine', 'delts', 'biceps', 'triceps', 
    'abs', 'quads', 'glutes', 'hamstrings', 'adductors', 'abductors', 'calves', 'forearms', 
    'traps', 'cardiovascular system', 'serratus anterior', 'levator scapulae'];

  targetOrder.forEach(target => {
    const exs = byTarget[target];
    if (!exs || exs.length === 0) return;
    
    content += `        // ${target.charAt(0).toUpperCase() + target.slice(1)} (${exs.length} exercises)\n`;
    exs.forEach(ex => {
      const key = capitalizeName(ex.name);
      const url = ex.gifUrl.includes('cloudfront.net') ? ex.gifUrl : `$CW/${ex.id}.gif`;
      content += `        "${key}" to "${url}",\n`;
    });
    content += `\n`;
  });

  content += `    )

    fun getGif(exerciseName: String): String? {
        return gifs[exerciseName] ?: gifs[exerciseName.lowercase().replaceFirstChar { it.uppercase() }]
    }
}
`;

  fs.writeFileSync(path.join(APP_DIR, 'ExerciseGifs.kt'), content);
  console.log(`ExerciseGifs.kt: ${exercises.length} entries written`);
}

// ============================================
// 2. Generate DataProvider.kt
// ============================================
function generateDataProvider() {
  // Group exercises by Romanian group name
  const byGroup = {};
  exercises.forEach(ex => {
    const group = mapGroup(ex.bodyPart, ex.target, ex.name);
    if (!byGroup[group]) byGroup[group] = [];
    byGroup[group].push({
      name: capitalizeName(ex.name),
      group: group,
      equipment: mapEquipment(ex.equipment)
    });
  });

  const groups = ['Piept', 'Spate', 'Umeri', 'Biceps', 'Triceps', 'Abdomen', 'Picioare', 'Fese', 'Gambe', 'Antebrate', 'Gat & Trapezi', 'Cardio'];

  let content = `package com.example.kinetic

object DataProvider {
    data class ExerciseDef(val name: String, val group: String, val equipment: String = "")

    val grupeMusculare = listOf(${groups.map(g => `"${g}"`).join(', ')})

    val exercitiiPeGrupa = mapOf(\n`;

  groups.forEach(group => {
    const exs = byGroup[group] || [];
    content += `        "${group}" to listOf(\n`;
    exs.forEach(ex => {
      content += `            ExerciseDefinition("${ex.name}", "${ex.group}", "${ex.equipment}"),\n`;
    });
    content += `        ),\n`;
  });

  content += `    )

    val defaultExercises: List<ExerciseDef> = listOf(\n`;
  
  // Add a representative subset for defaultExercises
  groups.forEach(group => {
    const exs = byGroup[group] || [];
    exs.slice(0, 10).forEach(ex => {
      content += `        ExerciseDef("${ex.name}", "${group}", "${ex.equipment}"),\n`;
    });
  });

  content += `    )

    val defaultTemplates = listOf(
        WorkoutTemplate("Push Day", listOf(
            TemplateExercise("Chest", ExerciseDefinition("Bench Press", "Chest", "Barbell")),
            TemplateExercise("Chest", ExerciseDefinition("Incline Bench Press", "Chest", "Barbell")),
            TemplateExercise("Chest", ExerciseDefinition("Dumbbell Fly", "Chest", "Dumbbells")),
            TemplateExercise("Shoulders", ExerciseDefinition("Overhead Press", "Shoulders", "Barbell")),
            TemplateExercise("Shoulders", ExerciseDefinition("Lateral Raise", "Shoulders", "Dumbbells")),
            TemplateExercise("Arms", ExerciseDefinition("Tricep Pushdown", "Arms", "Cable"))
        )),
        WorkoutTemplate("Pull Day", listOf(
            TemplateExercise("Back", ExerciseDefinition("Deadlift", "Back", "Barbell")),
            TemplateExercise("Back", ExerciseDefinition("Barbell Row", "Back", "Barbell")),
            TemplateExercise("Back", ExerciseDefinition("Pull-Up", "Back", "Bodyweight")),
            TemplateExercise("Back", ExerciseDefinition("Lat Pulldown", "Back", "Cable")),
            TemplateExercise("Arms", ExerciseDefinition("Barbell Curl", "Arms", "Barbell")),
            TemplateExercise("Shoulders", ExerciseDefinition("Face Pull", "Shoulders", "Cable"))
        )),
        WorkoutTemplate("Leg Day", listOf(
            TemplateExercise("Legs", ExerciseDefinition("Squat", "Legs", "Barbell")),
            TemplateExercise("Legs", ExerciseDefinition("Romanian Deadlift", "Legs", "Barbell")),
            TemplateExercise("Legs", ExerciseDefinition("Leg Press", "Legs", "Machine")),
            TemplateExercise("Legs", ExerciseDefinition("Leg Curl", "Legs", "Machine")),
            TemplateExercise("Legs", ExerciseDefinition("Leg Extension", "Legs", "Machine")),
            TemplateExercise("Legs", ExerciseDefinition("Calf Raise", "Legs", "Machine"))
        )),
        WorkoutTemplate("Upper Body", listOf(
            TemplateExercise("Chest", ExerciseDefinition("Bench Press", "Chest", "Barbell")),
            TemplateExercise("Back", ExerciseDefinition("Barbell Row", "Back", "Barbell")),
            TemplateExercise("Shoulders", ExerciseDefinition("Overhead Press", "Shoulders", "Barbell")),
            TemplateExercise("Arms", ExerciseDefinition("Barbell Curl", "Arms", "Barbell")),
            TemplateExercise("Arms", ExerciseDefinition("Tricep Pushdown", "Arms", "Cable"))
        )),
        WorkoutTemplate("Full Body", listOf(
            TemplateExercise("Chest", ExerciseDefinition("Bench Press", "Chest", "Barbell")),
            TemplateExercise("Back", ExerciseDefinition("Barbell Row", "Back", "Barbell")),
            TemplateExercise("Legs", ExerciseDefinition("Squat", "Legs", "Barbell")),
            TemplateExercise("Shoulders", ExerciseDefinition("Overhead Press", "Shoulders", "Barbell")),
            TemplateExercise("Core", ExerciseDefinition("Plank", "Core", "Bodyweight"))
        ))
    )
}

data class ExerciseDefinition(val name: String, val group: String, val equipment: String = "") {
    val nume: String get() = name
}
`;

  fs.writeFileSync(path.join(APP_DIR, 'DataProvider.kt'), content);
  console.log(`DataProvider.kt: ${Object.values(byGroup).flat().length} exercises across ${groups.length} groups`);
  
  // Print summary
  groups.forEach(group => {
    console.log(`  ${group}: ${(byGroup[group] || []).length} exercises`);
  });
}

// Run generators
generateExerciseGifs();
generateDataProvider();

console.log('\nDone! Files generated successfully.');
