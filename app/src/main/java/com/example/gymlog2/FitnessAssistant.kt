package com.example.gymlog2

data class UserOnboardingProfile(
    val goal: String = "",
    val experience: String = "",
    val equipment: String = "",
    val sessionsPerWeek: Int = 3,
    val limitations: String = "",
    val selectedGroups: List<String> = emptyList()
)

data class ExerciseRecommendation(
    val name: String,
    val group: String,
    val sets: Int,
    val reps: String,
    val note: String = ""
)

data class FitnessTip(
    val text: String
)

object FitnessAssistant {

    fun generateWorkout(profile: UserOnboardingProfile, mood: Int = 1): List<ExerciseRecommendation> {
        val exercises = mutableListOf<ExerciseRecommendation>()
        val groups = profile.selectedGroups.ifEmpty { listOf("chest", "back", "legs") }

        for (group in groups) {
            val groupExercises = getExercisesForGroup(group, profile.goal, profile.equipment, profile.experience, mood)
            exercises.addAll(groupExercises)
        }

        return distributeAcrossGroups(exercises, groups)
    }

    private fun getExercisesForGroup(
        group: String,
        goal: String,
        equipment: String,
        experience: String,
        mood: Int = 1
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
            0 -> (baseSets - 1).coerceAtLeast(2)
            2 -> baseSets + 1
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
            0 -> "12-15"
            2 -> repRange.split("-").firstOrNull()?.let { repRange }
            else -> repRange
        } ?: repRange

        return when (group) {
            "chest" -> listOf(
                ExerciseRecommendation("Barbell Bench Press", "chest", sets, adjustedReps, if (isStrength) "Focus on progressive overload" else "Control the eccentric"),
                ExerciseRecommendation("Incline Dumbbell Press", "chest", sets, adjustedReps),
                ExerciseRecommendation("Cable Flyes", "chest", (sets - 1).coerceAtLeast(2), if (isWeightLoss) "15-20" else "10-12", "Squeeze at the top")
            )
            "back" -> listOf(
                ExerciseRecommendation("Barbell Rows", "back", sets, adjustedReps, "Keep back straight"),
                ExerciseRecommendation("Pull-ups / Lat Pulldown", "back", sets, adjustedReps),
                ExerciseRecommendation("Seated Cable Row", "back", (sets - 1).coerceAtLeast(2), if (isWeightLoss) "12-15" else "10-12", "Retract shoulder blades")
            )
            "legs" -> listOf(
                ExerciseRecommendation("Barbell Squat", "legs", sets, adjustedReps, "Depth below parallel"),
                ExerciseRecommendation("Romanian Deadlift", "legs", sets, adjustedReps, "Feel the hamstring stretch"),
                ExerciseRecommendation("Leg Press", "legs", (sets - 1).coerceAtLeast(2), adjustedReps),
                ExerciseRecommendation("Leg Curls", "legs", (sets - 1).coerceAtLeast(2), if (isWeightLoss) "15-20" else "10-12")
            )
            "shoulders" -> listOf(
                ExerciseRecommendation("Overhead Press", "shoulders", sets, adjustedReps, "Brace core"),
                ExerciseRecommendation("Lateral Raises", "shoulders", (sets - 1).coerceAtLeast(2), if (isWeightLoss) "15-20" else "12-15", "Light weight, controlled"),
                ExerciseRecommendation("Face Pulls", "shoulders", (sets - 1).coerceAtLeast(2), "15-20", "Great for posture")
            )
            "arms" -> listOf(
                ExerciseRecommendation("Barbell Curls", "biceps", (sets - 1).coerceAtLeast(2), adjustedReps),
                ExerciseRecommendation("Tricep Pushdowns", "triceps", (sets - 1).coerceAtLeast(2), adjustedReps),
                ExerciseRecommendation("Hammer Curls", "biceps", (sets - 1).coerceAtLeast(2), if (isWeightLoss) "12-15" else "10-12"),
                ExerciseRecommendation("Overhead Tricep Extension", "triceps", (sets - 1).coerceAtLeast(2), adjustedReps)
            )
            "core" -> listOf(
                ExerciseRecommendation("Hanging Leg Raises", "core", 3, if (isWeightLoss) "15-20" else "10-15"),
                ExerciseRecommendation("Cable Crunches", "core", 3, "12-15"),
                ExerciseRecommendation("Plank Hold", "core", 3, "30-60s", "Keep body straight")
            )
            "cardio" -> listOf(
                ExerciseRecommendation("Treadmill Intervals", "cardio", 1, "20-30 min", if (isWeightLoss) "Alternate 30s sprint / 60s walk" else "Moderate pace"),
                ExerciseRecommendation("Rowing Machine", "cardio", 1, "15-20 min", "Full body engagement")
            )
            else -> emptyList()
        }
    }

    private fun distributeAcrossGroups(
        exercises: List<ExerciseRecommendation>,
        groups: List<String>
    ): List<ExerciseRecommendation> {
        if (groups.size <= 2) return exercises
        val perGroup = exercises.size / groups.size
        return exercises.take(perGroup * groups.size)
    }

    fun generateTips(profile: UserOnboardingProfile, mood: Int = 1): List<FitnessTip> {
        val allTips = mutableListOf<FitnessTip>()
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val seed = (dayOfYear + year * 365 + profile.goal.hashCode() + mood * 17).toLong()
        val random = java.util.Random(seed)

        val techniqueTips = listOf(
            FitnessTip("Keep your elbows at a 45° angle during bench press — protects your shoulders."),
            FitnessTip("Squeeze your shoulder blades together before every back exercise."),
            FitnessTip("Brace your core like someone is about to punch you — on every squat rep."),
            FitnessTip("Drive through your heels on squats and deadlifts, not your toes."),
            FitnessTip("Control the negative — 3 seconds down builds more muscle than rushing."),
            FitnessTip("Keep your wrists neutral during bicep curls to avoid joint pain."),
            FitnessTip("Lock your scapula before overhead press for better stability."),
            FitnessTip("Inhale on the eccentric, exhale on the concentric — never hold your breath."),
            FitnessTip("Avoid flaring elbows on push-ups or dips to protect your shoulder joints."),
            FitnessTip("Don't look up during deadlifts; keep your neck in a neutral line with your spine."),
            FitnessTip("Initiate lateral raises with your elbows, not your hands, to target side delts."),
            FitnessTip("Pull with your elbows on rows, not your hands, to fully engage your lats."),
            FitnessTip("Keep your hips low at the start of a deadlift — it is a pull, not a squat."),
            FitnessTip("Pause for a second at the peak contraction of leg extensions for peak quadriceps engagement."),
            FitnessTip("Walk out squats with only two steps to save energy and stay stable."),
            FitnessTip("Stand tall and keep your chest up during lunges to maintain balance.")
        )

        val nutritionTips = listOf(
            FitnessTip("Drink 500ml of water 30 minutes before your workout for better performance."),
            FitnessTip("Eat 20-40g of protein within 2 hours after training to maximize recovery."),
            FitnessTip("Don't skip carbs before lifting — they fuel your muscles for heavy sets."),
            FitnessTip("Sleep 7-9 hours per night — that's when your muscles actually grow."),
            FitnessTip("Aim for 1.6-2.2g of protein per kg of bodyweight daily."),
            FitnessTip("Creatine 5g daily is the most researched and effective supplement."),
            FitnessTip("Eat fruits and vegetables at every meal for micronutrients and recovery."),
            FitnessTip("Don't cut calories too aggressively — 300-500 kcal deficit is enough."),
            FitnessTip("Hydrate during workouts — drink small sips of water between sets."),
            FitnessTip("Salt before a workout can increase your muscle pump and vascularity."),
            FitnessTip("Casein protein before bed provides a slow release of amino acids overnight."),
            FitnessTip("Omega-3 fatty acids reduce joint inflammation and support muscle repair."),
            FitnessTip("Post-workout meal should combine protein and fast-acting carbs."),
            FitnessTip("Caffeine 30-45 minutes before training increases focus and power output."),
            FitnessTip("Track your fiber intake — aim for at least 30g daily for optimal digestion."),
            FitnessTip("Match your carb intake to your activity levels — eat more on training days.")
        )

        val motivationTips = listOf(
            FitnessTip("Consistency beats intensity — showing up 4x beats 1 crazy session."),
            FitnessTip("Track your workouts. What gets measured gets improved."),
            FitnessTip("Compare yourself to yesterday, not to others in the gym."),
            FitnessTip("Rest days are growth days — your muscles build while you sleep."),
            FitnessTip("The hardest set is the last one you don't want to do. Do it."),
            FitnessTip("You don't have to be extreme, just consistent."),
            FitnessTip("Every expert was once a beginner. Trust the process."),
            FitnessTip("The gym doesn't care about your excuses. Neither does your body."),
            FitnessTip("Motivation gets you started; discipline keeps you going."),
            FitnessTip("Action breeds motivation. If you don't want to go, just do 10 minutes."),
            FitnessTip("Progress is not linear. Trust the trend line, not a single bad day."),
            FitnessTip("Make your fitness goal about what your body can DO, not just how it looks."),
            FitnessTip("Surround yourself with people who share your goals and inspire you."),
            FitnessTip("A bad workout is still better than no workout at all."),
            FitnessTip("Focus on the feeling of achievement after a workout when motivation drops."),
            FitnessTip("Small daily habits accumulate to life-changing physical results.")
        )

        val recoveryTips = listOf(
            FitnessTip("Foam roll tight areas for 2-3 min post-workout to speed up recovery."),
            FitnessTip("If you're sore, light movement (walk, stretch) recovers faster than rest alone."),
            FitnessTip("Take deload weeks every 4-6 weeks — lighter weight, same form."),
            FitnessTip("Magnesium before bed helps with muscle relaxation and sleep quality."),
            FitnessTip("Cold showers after training reduce inflammation and speed recovery."),
            FitnessTip("Stretch your hip flexors daily — sitting shortens them and hurts your squat."),
            FitnessTip("If you feel drained, swap heavy compounds for machines today."),
            FitnessTip("Rest is not laziness. Your muscles grow during recovery, not during sets."),
            FitnessTip("Drink water first thing in the morning to rehydrate after sleeping."),
            FitnessTip("Dynamic warm-ups raise core temperature and lubricate your joints."),
            FitnessTip("Do not push through joint pain. Muscle soreness is fine, joint pain is a warning."),
            FitnessTip("Contrast baths (alternating hot and cold) can stimulate blood circulation."),
            FitnessTip("Active recovery days like yoga or easy walking keep joints healthy."),
            FitnessTip("Get out in the sunlight for 10-15 minutes daily to help synchronize sleep cycles."),
            FitnessTip("Elevated muscle soreness is a sign you need more hydration and sleep."),
            FitnessTip("Focus on nasal breathing between sets to lower heart rate and recover faster.")
        )

        val goalTips = when (profile.goal) {
            "strength" -> listOf(
                FitnessTip("Rest 2-3 minutes between heavy sets — full recovery = max power."),
                FitnessTip("Focus on progressive overload — add small weight each week."),
                FitnessTip("Master the 5x5 for main lifts before adding accessory work."),
                FitnessTip("Film your heavy sets to check form breakdown under fatigue."),
                FitnessTip("Increase your strength by squeezing the bar as hard as you can."),
                FitnessTip("Squeeze your glutes and brace your thighs for a solid overhead press foundation."),
                FitnessTip("Warm up thoroughly with ascending sets before your first work set."),
                FitnessTip("Focus on force production — push the bar as fast as possible on the way up."),
                FitnessTip("Keep accessory exercises to 3-4 per session to preserve recovery capacity."),
                FitnessTip("Eat enough protein and maintain a small caloric surplus to support raw strength.")
            )
            "mass" -> listOf(
                FitnessTip("Time under tension matters — slow eccentrics build more muscle."),
                FitnessTip("Eat in a slight surplus (+200-300 kcal) on training days."),
                FitnessTip("Hit each muscle 2x per week for optimal hypertrophy."),
                FitnessTip("Use drop sets on the last set to fully exhaust the muscle."),
                FitnessTip("Focus on the mind-muscle connection — feel the specific muscle working."),
                FitnessTip("Work in the 8-12 rep range for the sweet spot of muscle hypertrophy."),
                FitnessTip("Use partial reps at the end of a set when full range is no longer possible."),
                FitnessTip("Sleep is the ultimate anabolic agent; prioritize 8 hours of quality rest."),
                FitnessTip("Change exercise angles occasionally (e.g. incline vs flat press) for complete growth."),
                FitnessTip("Ensure your protein intake is spread out across 4-5 meals during the day.")
            )
            "weight_loss" -> listOf(
                FitnessTip("Keep rest periods 30-60 seconds to keep heart rate elevated."),
                FitnessTip("Combine strength training with HIIT for maximum calorie burn."),
                FitnessTip("Protein preserves muscle during a cut — eat 2g per kg minimum."),
                FitnessTip("Walk 8-10k steps daily — NEAT burns more than cardio sessions."),
                FitnessTip("Drink a full glass of water before meals to naturally regulate appetite."),
                FitnessTip("Lift heavy weights even on a calorie deficit to signal muscle retention."),
                FitnessTip("Focus on single-ingredient, high-volume foods like vegetables."),
                FitnessTip("Avoid drinking your calories — swap juices or sodas for water, tea, or black coffee."),
                FitnessTip("Keep track of hidden oils and dressings — they can add hundreds of calories."),
                FitnessTip("Consistency in your calorie deficit is more important than extreme restriction.")
            )
            "maintenance" -> listOf(
                FitnessTip("3 solid sessions per week is enough for maintained fitness."),
                FitnessTip("Mix compound movements with isolation for balanced development."),
                FitnessTip("Try new exercises every 4-6 weeks to keep things interesting."),
                FitnessTip("Listen to your body — some weeks lighter is better than forced."),
                FitnessTip("Maintain your muscle mass easily by lifting heavy at least once a week."),
                FitnessTip("Use maintenance periods to focus on mastering movement form and skills."),
                FitnessTip("Focus on joint health and general cardiovascular fitness during maintenance."),
                FitnessTip("Eat at your maintenance calories (TDEE) to stabilize body weight."),
                FitnessTip("Focus on sports, agility, or flexibility alongside your normal lifts."),
                FitnessTip("Enjoy your food and gym routine without the pressure of bulking or cutting.")
            )
            else -> listOf(
                FitnessTip("Stay consistent and trust the process."),
                FitnessTip("Progress takes time. Celebrate the small victories along the way."),
                FitnessTip("Make movement a daily habit — even a 15-minute walk helps."),
                FitnessTip("Focus on getting better, not perfect."),
                FitnessTip("Listen to your body and adjust your intensity accordingly.")
            )
        }

        when (mood) {
            0 -> {
                allTips.add(recoveryTips[random.nextInt(recoveryTips.size)])
                allTips.add(nutritionTips[random.nextInt(nutritionTips.size)])
                allTips.add(techniqueTips[random.nextInt(techniqueTips.size)])
            }
            1 -> {
                allTips.add(techniqueTips[random.nextInt(techniqueTips.size)])
                allTips.add(nutritionTips[random.nextInt(nutritionTips.size)])
                allTips.add(goalTips[random.nextInt(goalTips.size)])
            }
            2 -> {
                allTips.add(motivationTips[random.nextInt(motivationTips.size)])
                allTips.add(goalTips[random.nextInt(goalTips.size)])
                allTips.add(techniqueTips[random.nextInt(techniqueTips.size)])
            }
        }

        return allTips
    }
}
