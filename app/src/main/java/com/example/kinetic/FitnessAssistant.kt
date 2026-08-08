package com.example.kinetic

data class UserOnboardingProfile(
    val goal: String = "",
    val experience: String = "",
    val equipment: String = "",
    val sessionsPerWeek: Int = 3,
    val selectedDays: List<String> = emptyList(),
    val limitations: String = "",
    val selectedGroups: List<String> = emptyList(),
    val age: Int = 25,
    val gender: String = "",
    val activityLevel: String = "sedentary",
    val weight: Float = 70f,
    val height: Float = 170f
)

data class ExerciseRecommendation(
    val name: String,
    val group: String,
    val sets: Int,
    val reps: String,
    val note: String = "",
    val isStretch: Boolean = false
)

data class FitnessTip(
    val text: String,
    val id: String,
    val category: String
)


object FitnessAssistant {

    /**
     * Maps onboarding equipment keys to the equipment tags used in DataProvider.
     * - home_no_equipment: only bodyweight
     * - home_dumbbells: bodyweight + dumbbells
     * - full_gym: everything (barbell, cable, band, kettlebell, ez bar, machine, etc.)
     */
    private fun allowedEquipmentTags(equipment: String): Set<String> {
        return when (equipment) {
            "home_no_equipment" -> setOf("Bodyweight")
            "home_dumbbells" -> setOf("Bodyweight", "Dumbbells", "Band", "Resistance band", "Weighted")
            else -> setOf(
                "Bodyweight", "Dumbbells", "Barbell", "Cable", "Band",
                "Kettlebell", "EZ Bar", "Machine", "Assisted",
                "Stability Ball", "Medicine Ball", "Rope", "Sled Machine",
                "Ergometer", "Smith Machine", "Weighted", "Roller",
                "Resistance band", "Olympic barbell", "Bosu ball", "Wheel roller"
            )
        }
    }

    /**
     * Maps English onboarding group keys to Romanian DataProvider group names.
     */
    private fun onboardingGroupToProviderGroup(group: String): String? {
        return when (group.lowercase()) {
            "chest" -> "Piept"
            "back" -> "Spate"
            "shoulders" -> "Umeri"
            "biceps" -> "Biceps"
            "triceps" -> "Triceps"
            "abs", "core", "abdomen" -> "Abdomen"
            "legs" -> "Picioare"
            "glutes" -> "Fese"
            "hamstrings" -> "Picioare" // hamstrings exercises live in Picioare
            "calves" -> "Gambe"
            "forearms" -> "Antebrate"
            "traps", "gat" -> "Gat & Trapezi"
            "cardio" -> "Cardio"
            "arms" -> null // arms is a meta-group; handled separately
            else -> null
        }
    }

    fun generateWorkout(profile: UserOnboardingProfile, mood: Int = 2): List<ExerciseRecommendation> {
        val exercises = mutableListOf<ExerciseRecommendation>()
        val groups = profile.selectedGroups.ifEmpty { listOf("chest", "back", "legs") }

        for (group in groups) {
            val groupExercises = getExercisesForGroup(group, profile.goal, profile.equipment, profile.experience, mood, profile.gender)
            exercises.addAll(groupExercises)
        }

        return distributeAcrossGroups(exercises, groups)
    }

    fun generateWorkoutForGroups(groups: List<String>, goal: String, equipment: String, experience: String, mood: Int = 2, gender: String = "", dayIndex: Int = 0): List<ExerciseRecommendation> {
        val exercises = mutableListOf<ExerciseRecommendation>()
        for (group in groups) {
            val groupExercises = getExercisesForGroup(group, goal, equipment, experience, mood, gender)
            exercises.addAll(groupExercises)
        }

        val warmupStretch = getWarmupStretch(groups, equipment, dayIndex)
        val cooldownStretch = getCooldownStretch(groups, equipment, dayIndex)

        val result = mutableListOf<ExerciseRecommendation>()
        warmupStretch?.let { result.add(it) }
        result.addAll(exercises)
        cooldownStretch?.let { result.add(it) }
        return result
    }

    private fun getExercisesForGroup(
        group: String,
        goal: String,
        equipment: String,
        experience: String,
        mood: Int = 1,
        gender: String = ""
    ): List<ExerciseRecommendation> {
        val isStrength = goal == "strength"
        val isWeightLoss = goal == "weight_loss"
        val isMass = goal == "mass"

        val baseSets = when {
            isStrength && experience == "advanced" -> 5
            isStrength && experience == "intermediate" -> 4
            isStrength -> 3
            isMass && experience == "advanced" -> 4
            isMass -> 3
            isWeightLoss -> 3
            else -> 3
        }

        val sets = when (mood) {
            0, 1 -> (baseSets - 1).coerceAtLeast(2)
            3 -> baseSets + 1
            else -> baseSets
        }

        val repRange = when {
            isStrength && experience == "advanced" -> "3-5"
            isStrength && experience == "intermediate" -> "4-6"
            isStrength -> "5-8"
            isMass && experience == "advanced" -> "8-12"
            isMass -> "10-12"
            isWeightLoss -> "12-15"
            else -> "8-12"
        }

        val adjustedReps = when (mood) {
            0, 1 -> "12-15"
            3 -> repRange
            else -> repRange
        }

        // Handle "arms" meta-group: combine biceps + triceps
        if (group == "arms") {
            val biceps = getExercisesFromProvider("Biceps", equipment, sets, adjustedReps, isStrength, isWeightLoss, gender)
            val triceps = getExercisesFromProvider("Triceps", equipment, sets, adjustedReps, isStrength, isWeightLoss, gender)
            return (biceps + triceps).take(8)
        }

        val providerGroup = onboardingGroupToProviderGroup(group) ?: group
        return getExercisesFromProvider(providerGroup, equipment, sets, adjustedReps, isStrength, isWeightLoss, gender)
    }

    private fun isCompoundExercise(name: String): Boolean {
        val lower = name.lowercase()
        val compoundKeywords = listOf(
            "bench press", "incline press", "decline press", "chest press",
            "overhead press", "shoulder press", "military press",
            "squat", "front squat", "goblet squat", "leg press",
            "deadlift", "romanian deadlift", "sumo deadlift",
            "row", "bent over row", "cable row", "barbell row",
            "pull-up", "pull up", "chin-up", "chin up", "lat pulldown",
            "dip", "lunge", "split squat",
            "hip thrust", "glute bridge",
            "clean", "snatch", "jerk"
        )
        return compoundKeywords.any { lower.contains(it) }
    }

    private fun normalizeForDedup(name: String): String {
        return name.lowercase()
            .replace(Regex("\\s*\\(.*?\\)"), "")
            .replace(Regex("\\s+on\\s+(a |the |high |low |straight |dip|smith |cable |bar )?.*"), "")
            .replace(Regex("\\s+with\\s+.*"), "")
            .replace(Regex("\\s*:\\s*.*"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun exercisePriority(name: String, equipment: String): Int {
        val n = normalizeForDedup(name)
        return when (equipment) {
            "home_no_equipment" -> when {
                // Chest
                n == "push-up" -> 0
                n.contains("push-up") && !n.contains("handstand") && !n.contains("superman") && !n.contains("planche") -> 1
                // Back
                n == "australian pull-up" || n == "inverted row" || n == "bodyweight row" -> 0
                n.contains("row") && !n.contains("cable") && !n.contains("barbell") -> 1
                n == "pull-up" || n == "chin-up" -> 0
                n.contains("pull-up") || n.contains("chin-up") -> 1
                // Legs
                n == "squat" -> 0
                n.contains("squat") && !n.contains("barbell") && !n.contains("goblet") -> 1
                n == "lunge" -> 0
                n.contains("lunge") -> 1
                n.contains("step-up") || n.contains("step up") -> 1
                n.contains("glute bridge") || n.contains("hip thrust") -> 1
                // Shoulders
                n == "pike push-up" || n == "pike push up" -> 0
                n.contains("pike push") -> 1
                n.contains("arm raise") && !n.contains("barbell") -> 1
                // Triceps
                n == "dip" -> 2
                n.contains("dip") && !n.contains("assisted") -> 2
                n.contains("diamond push") -> 1
                n.contains("close-grip push") -> 1
                // Biceps
                n.contains("curl") && !n.contains("barbell") && !n.contains("cable") -> 1
                // Core
                n == "plank" -> 0
                n.contains("plank") -> 1
                n.contains("crunch") -> 1
                n.contains("leg raise") -> 1
                n.contains("russian twist") -> 1
                n.contains("mountain climber") -> 1
                else -> 3
            }
            "home_dumbbells" -> when {
                n.contains("dumbbell") -> 1
                n.contains("push-up") -> 1
                n.contains("squat") -> 1
                n.contains("row") -> 1
                n.contains("curl") -> 1
                n.contains("press") -> 1
                n.contains("lunge") -> 1
                else -> 3
            }
            else -> 3
        }
    }

    /**
     * Pulls exercises from DataProvider.exercitiiPeGrupa, filtered by equipment.
     * Falls back to bodyweight-only if no exercises match the equipment filter.
     */
    private fun getExercisesFromProvider(
        providerGroup: String,
        equipment: String,
        sets: Int,
        reps: String,
        isStrength: Boolean,
        isWeightLoss: Boolean,
        gender: String = ""
    ): List<ExerciseRecommendation> {
        val allowed = allowedEquipmentTags(equipment)
        val allExercises = DataProvider.exercitiiPeGrupa[providerGroup] ?: emptyList()

        // Filter by allowed equipment, then deduplicate by normalized name (keep first)
        val seen = mutableSetOf<String>()
        val filtered = allExercises.filter { ex ->
            val eqTag = ex.equipment
            val key = normalizeForDedup(ex.name)
            if (key in seen) false
            else if (allowed.contains(eqTag)) { seen.add(key); true }
            else false
        }

        // If no exercises match the equipment filter, fall back to bodyweight only
        val exercises = if (filtered.isEmpty()) {
            seen.clear()
            allExercises.filter { ex ->
                val key = normalizeForDedup(ex.name)
                if (key in seen) false
                else if (ex.equipment == "Bodyweight") { seen.add(key); true }
                else false
            }
        } else filtered

        // Filter by gender: exclude opposite gender exercises
        val genderFiltered = exercises.filter { ex ->
            when (gender) {
                "male" -> !ex.name.contains("(female)", ignoreCase = true)
                "female" -> !ex.name.contains("(male)", ignoreCase = true)
                else -> true
            }
        }

        // Filter out stretches — only return weight-based exercises per group
        val eq = equipment
        val nonStretches = genderFiltered.filter { !it.name.contains("Stretch", ignoreCase = true) }
            .sortedWith(compareBy<ExerciseDefinition> { exercisePriority(it.name, eq) }
                .thenByDescending { isCompoundExercise(it.name) })
        val count = nonStretches.size.coerceAtMost(4)

        return nonStretches.take(count).map { ex ->
            val note = when {
                isStrength && ex.equipment == "Bodyweight" -> "Add weight if too easy"
                isWeightLoss -> "Minimize rest between sets"
                isStrength -> "Focus on progressive overload"
                else -> "Control the eccentric"
            }
            ExerciseRecommendation(
                name = ex.name,
                group = providerGroup,
                sets = sets,
                reps = reps,
                note = note,
                isStretch = false
            )
        }
    }

    /**
     * Returns the first stretch exercise for a muscle group, filtered by equipment.
     * Returns null if no matching stretch exists.
     */
    private fun getStretchForGroup(
        group: String,
        equipment: String,
        dayIndex: Int = 0
    ): ExerciseDefinition? {
        val providerGroup = onboardingGroupToProviderGroup(group) ?: group
        val allExercises = DataProvider.exercitiiPeGrupa[providerGroup] ?: emptyList()
        val allowed = allowedEquipmentTags(equipment)
        val matches = allExercises.filter { ex ->
            ex.name.contains("Stretch", ignoreCase = true) && allowed.contains(ex.equipment)
        }.ifEmpty {
            allExercises.filter { ex ->
                ex.name.contains("Stretch", ignoreCase = true) && ex.equipment == "Bodyweight"
            }
        }
        if (matches.isEmpty()) return null
        return matches[dayIndex % matches.size]
    }

    /**
     * Returns a warm-up stretch recommendation for the first muscle group,
     * or null if none available.
     */
    fun getWarmupStretch(
        groups: List<String>,
        equipment: String,
        dayIndex: Int = 0
    ): ExerciseRecommendation? {
        if (groups.isEmpty()) return null
        val ex = getStretchForGroup(groups.first(), equipment, dayIndex) ?: return null
        return ExerciseRecommendation(
            name = ex.name,
            group = ex.group,
            sets = 1,
            reps = "60s",
            note = "Warm-up stretch",
            isStretch = true
        )
    }

    /**
     * Returns a cool-down stretch recommendation for the last muscle group,
     * or null if none available.
     */
    fun getCooldownStretch(
        groups: List<String>,
        equipment: String,
        dayIndex: Int = 0
    ): ExerciseRecommendation? {
        if (groups.isEmpty()) return null
        val ex = getStretchForGroup(groups.last(), equipment, dayIndex) ?: return null
        return ExerciseRecommendation(
            name = ex.name,
            group = ex.group,
            sets = 1,
            reps = "60s",
            note = "Cool-down stretch",
            isStretch = true
        )
    }

    private fun distributeAcrossGroups(
        exercises: List<ExerciseRecommendation>,
        groups: List<String>
    ): List<ExerciseRecommendation> {
        if (groups.size <= 2) return exercises
        val perGroup = exercises.size / groups.size
        return exercises.take(perGroup * groups.size)
    }

    fun generateTips(profile: UserOnboardingProfile, mood: Int = 2): List<FitnessTip> {
        val energyLevel = TipsBank.energyKeyForMood(mood)
        val objective = TipsBank.objectiveKeyForGoal(profile.goal)
        val comboKey = "$energyLevel|$objective"

        // Sliding window (FIFO) of recently shown tip ids for this combo — avoids
        // repeating the same tips too soon (see tips_selection_strategy.md §1).
        val recentlyShown = shownHistory[comboKey].orEmpty()

        val picked = TipsBank.getNextTips(
            energyLevel = energyLevel,
            objective = objective,
            count = 3,
            recentlyShown = recentlyShown
        )

        // Keep the last 5 ids for this combination; round-robin reset happens in TipsBank.
        shownHistory[comboKey] = (recentlyShown.toList() + picked.map { it.id }).takeLast(5).toSet()

        return picked.map { FitnessTip(text = it.text, id = it.id, category = it.category) }
    }

    // Per-combination history of recently shown tip ids (no-repetition without AI).
    private val shownHistory = mutableMapOf<String, Set<String>>()
}

