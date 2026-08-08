package com.example.kinetic

import java.util.*

data class MuscleGroupVolume(
    val group: String,
    val volume: Double,
    val sessions: Int,
    val percentage: Double
)

data class WeeklyProgress(
    val weekLabel: String,
    val volume: Double,
    val sessions: Int
)

data class ExerciseProgress(
    val name: String,
    val currentMax: Double,
    val previousMax: Double,
    val changePercent: Double
)

data class PersonalBest(
    val exerciseName: String,
    val maxWeight: Double,
    val reps: Int,
    val achievedAt: Long,
    val isNew: Boolean
)

fun computeMuscleGroupAnalytics(
    workouts: List<AntrenamentEntity>,
    exercises: List<ExercitiuEntity>
): List<MuscleGroupVolume> {
    val groupVolumes = mutableMapOf<String, Double>()
    val groupSessions = mutableMapOf<String, Int>()

    for (w in workouts) {
        val workoutExercises = exercises.filter { it.antrenamentId == w.id }
        val vol = workoutExercises.sumOf { it.greutateKg * it.repetari }
        groupVolumes[w.grupaMusculara] = (groupVolumes[w.grupaMusculara] ?: 0.0) + vol
        groupSessions[w.grupaMusculara] = (groupSessions[w.grupaMusculara] ?: 0) + 1
    }

    val totalVolume = groupVolumes.values.sum()
    return groupVolumes.map { (group, volume) ->
        MuscleGroupVolume(
            group = group,
            volume = volume,
            sessions = groupSessions[group] ?: 0,
            percentage = if (totalVolume > 0) (volume / totalVolume * 100) else 0.0
        )
    }.sortedByDescending { it.volume }
}

fun computeWeeklyProgress(
    workouts: List<AntrenamentEntity>,
    exercises: List<ExercitiuEntity>,
    weeks: Int = 12
): List<WeeklyProgress> {
    val result = mutableListOf<WeeklyProgress>()

    for (i in weeks - 1 downTo 0) {
        val weekCal = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, -i)
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val weekStart = weekCal.timeInMillis
        weekCal.add(Calendar.WEEK_OF_YEAR, 1)
        val weekEnd = weekCal.timeInMillis

        val weekWorkouts = workouts.filter { it.data in weekStart until weekEnd }
        val weekVolume = weekWorkouts.sumOf { w ->
            exercises.filter { it.antrenamentId == w.id }.sumOf { it.greutateKg * it.repetari }
        }

        result.add(WeeklyProgress(
            weekLabel = "W${weeks - i}",
            volume = weekVolume,
            sessions = weekWorkouts.size
        ))
    }
    return result
}

fun computeExerciseProgress(
    allExercises: List<ExercitiuEntity>,
    workouts: List<AntrenamentEntity>
): List<ExerciseProgress> {
    val cal = Calendar.getInstance()
    val now = cal.timeInMillis
    cal.add(Calendar.WEEK_OF_YEAR, -4)
    val fourWeeksAgo = cal.timeInMillis
    cal.add(Calendar.WEEK_OF_YEAR, -4)
    val eightWeeksAgo = cal.timeInMillis

    val recentWorkoutIds = workouts.filter { it.data >= fourWeeksAgo }.map { it.id }.toSet()
    val prevWorkoutIds = workouts.filter { it.data in eightWeeksAgo until fourWeeksAgo }.map { it.id }.toSet()

    val recentExercises = allExercises.filter { it.antrenamentId in recentWorkoutIds }
    val prevExercises = allExercises.filter { it.antrenamentId in prevWorkoutIds }

    val recentMaxes = recentExercises.groupBy { it.numeExercitiu }
        .mapValues { (_, sets) -> sets.maxOfOrNull { it.greutateKg } ?: 0.0 }
    val prevMaxes = prevExercises.groupBy { it.numeExercitiu }
        .mapValues { (_, sets) -> sets.maxOfOrNull { it.greutateKg } ?: 0.0 }

    return recentMaxes.map { (name, currentMax) ->
        val prevMax = prevMaxes[name] ?: 0.0
        val change = if (prevMax > 0) ((currentMax - prevMax) / prevMax * 100) else 0.0
        ExerciseProgress(name, currentMax, prevMax, change)
    }.sortedByDescending { it.changePercent }
}

fun computePersonalBests(
    allExercises: List<ExercitiuEntity>,
    workouts: List<AntrenamentEntity>
): List<PersonalBest> {
    val oneMonthAgo = Calendar.getInstance().apply {
        add(Calendar.MONTH, -1)
    }.timeInMillis

    val exerciseMaxes = allExercises.groupBy { it.numeExercitiu }
        .mapValues { (_, sets) ->
            val maxSet = sets.maxByOrNull { it.greutateKg }
            maxSet?.let {
                val workout = workouts.find { w -> w.id == it.antrenamentId }
                Triple(it.greutateKg, workout?.data ?: 0L, workout?.data ?: 0L >= oneMonthAgo)
            } ?: Triple(0.0, 0L, false)
        }

    val exerciseReps = allExercises.groupBy { it.numeExercitiu }
        .mapValues { (_, sets) ->
            val maxSet = sets.maxByOrNull { it.greutateKg }
            maxSet?.repetari ?: 0
        }

    return exerciseMaxes.map { (name, data) ->
        val (maxWeight, achievedAt, isNew) = data
        PersonalBest(
            exerciseName = name,
            maxWeight = maxWeight,
            reps = exerciseReps[name] ?: 0,
            achievedAt = achievedAt,
            isNew = isNew
        )
    }.sortedByDescending { it.maxWeight }
}
