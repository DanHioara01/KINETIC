package com.example.kinetic

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

data class SetEntry(
    val greutateKg: Double,
    val repetari: Int,
    val setType: String = SetTypes.WORKING,
    val rpe: Int = 0
)

/** Tipuri de seturi suportate în înregistrarea antrenamentului. */
object SetTypes {
    const val WARMUP = "warmup"
    const val WORKING = "working"
    const val DROP = "drop"
    const val AMRAP = "amrap"
    const val PAUSED = "paused"
    const val TEMPO = "tempo"
    val ALL = listOf(WARMUP, WORKING, DROP, AMRAP, PAUSED, TEMPO)
}

data class ExerciseListItem(
    val exercise: ExerciseDefinition,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false,
    val equipment: String = ""
)

data class TemplateExercise(
    val grupaMusculara: String,
    val exercise: ExerciseDefinition
)

data class WorkoutTemplate(
    val nume: String,
    val exercitii: List<TemplateExercise>
)

data class ExerciseEntry(
    val numeExercitiu: String,
    val seturi: List<SetEntry> = listOf(),
    val notes: String = ""
)

data class TrainingSession(
    val grupaMusculara: String,
    val data: Date = Date(),
    val exercitii: List<ExerciseEntry> = listOf()
)

data class ExerciseStats(
    val maxGreutate: Double,
    val maxRepetari: Int,
    val maxVolumSet: Double
)

/** 1RM estimat (Epley): weight * (1 + reps/30). Ignoră seturi invalide (>20 reps). */
fun epleyOneRm(weight: Double, reps: Int): Double {
    if (weight <= 0 || reps < 1) return 0.0
    if (reps == 1) return weight
    if (reps > 20) return 0.0
    return weight * (1 + reps / 30.0)
}

data class VolumeSummary(
    val azi: Double,
    val saptamana: Double,
    val luna: Double
)

data class ProgresLunar(
    val luna: String,
    val greutateMaxima: Double
)

data class DeloadAdjustedValues(
    val originalWeight: Double,
    val originalSets: Int,
    val originalReps: Int,
    val deloadWeight: Double,
    val deloadSets: Int,
    val deloadReps: Int,
    val weightReductionPercent: Int,
    val isCompound: Boolean
)

@Entity(tableName = "deload_weeks")
data class DeloadWeekEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "",
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val reason: String = "",
    val reductionFactor: Double = 0.65,
    val completed: Boolean = false
)

data class DeloadExerciseReduction(
    val exerciseName: String = "",
    val originalWeight: Double = 0.0,
    val originalSets: Int = 0,
    val newWeight: Double = 0.0,
    val newSets: Int = 0,
    val weightReductionPercent: Int = 0,
    val setsReduction: Int = 0,
    val isCompound: Boolean = false
)

data class DeloadTrigger(
    val reason: String = "",
    val triggeredAt: Long = System.currentTimeMillis()
)
