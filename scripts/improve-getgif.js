const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/ExerciseGifs.kt';
let c = fs.readFileSync(file, 'utf8');

// Replace the simple getGif function with a fuzzy-matching version
const oldGetGif = `    fun getGif(exerciseName: String): String? {
        return gifs[exerciseName] ?: gifs[exerciseName.lowercase().replaceFirstChar { it.uppercase() }]
    }`;

const newGetGif = `    /**
     * Fuzzy GIF lookup: tries exact match, then case-insensitive, then strips
     * equipment prefixes, then partial-match (contains). This ensures that
     * template exercises with short names like "Bench Press" still resolve
     * to "Barbell Bench Press" even if no explicit alias was added.
     */
    fun getGif(exerciseName: String): String? {
        // 1. Exact match
        gifs[exerciseName]?.let { return it }

        // 2. Case-insensitive first-char capitalization
        val titleCase = exerciseName.lowercase().replaceFirstChar { it.uppercase() }
        gifs[titleCase]?.let { return it }

        // 3. Strip common equipment prefixes and try again
        val prefixes = listOf(
            "Barbell ", "Dumbbell ", "Cable ", "Band ", "Kettlebell ",
            "Smith ", "Lever ", "EZ Bar ", "Bodyweight ", "Assisted ",
            "Stability Ball ", "Medicine Ball ", "Rope ", "Sled ",
            "Ergometer ", "Weighted ", "Roller "
        )
        for (prefix in prefixes) {
            if (exerciseName.startsWith(prefix, ignoreCase = true)) {
                val stripped = exerciseName.removePrefix(prefix).trimStart()
                val strippedTitle = stripped.lowercase().replaceFirstChar { it.uppercase() }
                gifs[stripped]?.let { return it }
                gifs[strippedTitle]?.let { return it }
            }
        }

        // 4. Partial match: find any key that contains the exercise name (case-insensitive)
        val lowerName = exerciseName.lowercase()
        for ((key, url) in gifs) {
            if (key.lowercase().contains(lowerName) || lowerName.contains(key.lowercase())) {
                return url
            }
        }

        return null
    }`;

if (c.includes(oldGetGif)) {
    c = c.replace(oldGetGif, newGetGif);
    fs.writeFileSync(file, c, 'utf8');
    console.log('SUCCESS: Replaced getGif with fuzzy-matching version');
} else {
    console.log('WARN: Could not find exact old getGif function, trying tail replacement');
    // Try to find and replace just the function body
    const funcStart = c.indexOf('    fun getGif(exerciseName: String): String? {');
    if (funcStart === -1) {
        console.log('ERROR: Could not find getGif function');
        process.exit(1);
    }
    const funcEnd = c.indexOf('\n}', funcStart) + 2;
    c = c.substring(0, funcStart) + newGetGif + c.substring(funcEnd);
    fs.writeFileSync(file, c, 'utf8');
    console.log('SUCCESS: Replaced getGif via tail replacement');
}
