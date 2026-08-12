package com.example.kinetic

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Entity(tableName = "antrenamente")
data class AntrenamentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val grupaMusculara: String,
    val data: Long = System.currentTimeMillis(),
    val notes: String = "",
    val totalWeight: Double = 0.0,
    val durationMs: Long = 0L,
    val syncUuid: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "exercitii", foreignKeys = [ForeignKey(
    entity = AntrenamentEntity::class,
    parentColumns = ["id"],
    childColumns = ["antrenamentId"],
    onDelete = ForeignKey.CASCADE
)], indices = [Index(value = ["antrenamentId"])])
data class ExercitiuEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val antrenamentId: Long,
    val numeExercitiu: String,
    val setIndex: Int = 0,
    val greutateKg: Double = 0.0,
    val repetari: Int = 0,
    val notes: String = "",
    val syncUuid: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

data class ExerciseWithDate(
    @Embedded val exercise: ExercitiuEntity,
    @ColumnInfo(name = "antrenamentData") val antrenamentData: Long
)

@Entity(tableName = "exercises")
data class ExerciseDefinitionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val group: String,
    val equipment: String = "",
    val isDefault: Boolean = true,
    val isFavorite: Boolean = false,
    val usageCount: Int = 0,
    val syncUuid: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val name: String,
    val syncUuid: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "template_exercises", foreignKeys = [ForeignKey(
    entity = TemplateEntity::class,
    parentColumns = ["id"],
    childColumns = ["templateId"],
    onDelete = ForeignKey.CASCADE
)], indices = [Index(value = ["templateId"])])
data class TemplateExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val exerciseName: String,
    val group: String,
    val syncUuid: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "personal_records")
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val exerciseName: String,
    val weight: Double,
    val reps: Int,
    val volume: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val syncUuid: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "muscle_recovery", primaryKeys = ["grupaMusculara", "userId"])
data class MuscleRecoveryEntity(
    val grupaMusculara: String,
    val userId: String,
    val level: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncUuid: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "exercise_metadata", primaryKeys = ["exerciseName", "userId"])
data class ExerciseMetadataEntity(
    val exerciseName: String,
    val userId: String,
    val grupaMusculara: String,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false,
    val syncUuid: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "biometric_entries")
data class BiometricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val weightKg: Double = 0.0,
    val bodyFatPercent: Double = 0.0,
    val waistCm: Double = 0.0,
    val hipsCm: Double = 0.0,
    val thighsCm: Double = 0.0,
    val chestCm: Double = 0.0,
    val armsCm: Double = 0.0,
    val notes: String = "",
    val syncUuid: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "weight_goals")
data class WeightGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val targetWeightKg: Double = 0.0,
    val startWeightKg: Double = 0.0,
    val deadlineTimestamp: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val syncUuid: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "injury_risks")
data class InjuryRiskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val muscleGroup: String = "",
    val riskLevel: Double = 0.0,
    val reason: String = "",
    val assessedAt: Long = System.currentTimeMillis(),
    val syncUuid: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "food_entries")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val barcode: String = "",
    val name: String,
    val brand: String = "",
    val mealType: String = "snack",
    val servingSize: Double = 100.0,
    val servingUnit: String = "g",
    val calories: Double = 0.0,
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fatG: Double = 0.0,
    val fiberG: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val syncUuid: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Unitatea de măsură pentru un aliment din baza de date de alimente.
 * GRAM = valori raportate la 100g; PIECE = alimente numărate pe bucăți (ou, banană...).
 */
enum class FoodUnitType {
    GRAM, PIECE
}

class FoodUnitTypeConverter {
    @TypeConverter
    fun fromString(value: String): FoodUnitType = FoodUnitType.valueOf(value)

    @TypeConverter
    fun toString(value: FoodUnitType): String = value.name
}

/**
 * Baza de date statică de alimente (seed local, offline).
 * Valorile sunt per 100g; pentru PIECE, gramsPerPiece spune cât cântărește o bucată,
 * iar valorile efective per bucată se calculează din valorile per 100g.
 * Intrările din jurnal (FoodEntity) stochează însă snapshot — nu referință la acest tabel.
 */
@Entity(tableName = "food_items")
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Numele implicit (română) — folosit ca fallback dacă limba curentă nu are traducere. */
    val name: String,
    /** Nume localizate pe limbă (codurile limbilor din LanguageManager). */
    val nameEn: String = "",
    val nameRu: String = "",
    val nameUk: String = "",
    val nameFr: String = "",
    val nameDe: String = "",
    val nameEs: String = "",
    val nameIt: String = "",
    val nameTr: String = "",
    val namePt: String = "",
    val namePl: String = "",
    val caloriesPer100g: Double = 0.0,
    val proteinPer100g: Double = 0.0,
    val carbsPer100g: Double = 0.0,
    val fatPer100g: Double = 0.0,
    val unitType: FoodUnitType = FoodUnitType.GRAM,
    val gramsPerPiece: Double? = null,
    /** Cheie de căutare normalizată (fără diacritice, lowercase) pentru match case-insensitive. */
    val searchKey: String = ""
) {
    /** Returnează numele alimentului în limba cerută (fallback: română). */
    fun nameFor(lang: String): String = when (lang) {
        "en" -> nameEn.ifBlank { name }
        "ru" -> nameRu.ifBlank { name }
        "uk" -> nameUk.ifBlank { name }
        "fr" -> nameFr.ifBlank { name }
        "de" -> nameDe.ifBlank { name }
        "es" -> nameEs.ifBlank { name }
        "it" -> nameIt.ifBlank { name }
        "tr" -> nameTr.ifBlank { name }
        "pt" -> namePt.ifBlank { name }
        "pl" -> namePl.ifBlank { name }
        else -> name
    }
}

@Entity(tableName = "cardio_routes")
data class CardioRouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val name: String,
    val routePoints: String = "",
    val distanceKm: Double = 0.0,
    val durationMs: Long = 0,
    val avgSpeedKmh: Double = 0.0,
    val avgPaceMinKm: Double = 0.0,
    val caloriesBurned: Double = 0.0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0,
    val activityType: String = "running",
    val syncUuid: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

data class CardioSummary(
    val totalDistance: Double?,
    val totalDuration: Long?,
    val totalCalories: Double?
)

@Entity(tableName = "rest_days")
data class RestDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val date: Long = System.currentTimeMillis(),
    val type: String = "rest",
    val notes: String = "",
    val activities: String = "",
    val completed: Boolean = false,
    val syncUuid: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_chat_history")
data class AiChatHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val sessionId: Long = 0,
    val role: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val syncUuid: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

data class MostFrequentExercise(
    val numeExercitiu: String,
    val cnt: Int
)

data class ExerciseSummary(
    val exerciseName: String,
    val bestWeight: Double,
    val bestReps: Int,
    val totalSessions: Int
)

@Dao
interface AntrenamentDao {
    @Insert
    suspend fun insert(antrenament: AntrenamentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertByUuid(antrenament: AntrenamentEntity)

    @Update
    suspend fun update(antrenament: AntrenamentEntity)

    @Delete
    suspend fun delete(antrenament: AntrenamentEntity)

    @Query("SELECT * FROM antrenamente WHERE userId = :userId ORDER BY data DESC")
    suspend fun getAllForUser(userId: String): List<AntrenamentEntity>

    @Query("SELECT * FROM antrenamente WHERE id = :id")
    suspend fun getById(id: Long): AntrenamentEntity?

    @Query("SELECT * FROM antrenamente WHERE syncUuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): AntrenamentEntity?

    @Query("SELECT * FROM antrenamente WHERE syncUuid = ''")
    suspend fun getUnsynced(): List<AntrenamentEntity>

    @Query("DELETE FROM antrenamente WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT SUM(totalWeight) FROM antrenamente WHERE userId = :userId AND data BETWEEN :startTime AND :endTime")
    suspend fun getTotalVolume(userId: String, startTime: Long, endTime: Long): Double?

    @Query("SELECT COUNT(*) FROM antrenamente WHERE userId = :userId")
    suspend fun countForUser(userId: String): Int

    @Query("SELECT COALESCE(SUM(totalWeight), 0) FROM antrenamente WHERE userId = :userId")
    suspend fun sumVolumeForUser(userId: String): Double

    @Query("SELECT * FROM antrenamente WHERE userId = :userId AND data BETWEEN :startTime AND :endTime ORDER BY data DESC")
    suspend fun getWorkoutsInPeriod(userId: String, startTime: Long, endTime: Long): List<AntrenamentEntity>
}

@Dao
interface ExercitiuDao {
    @Insert
    suspend fun insert(exercitiu: ExercitiuEntity)

    @Insert
    suspend fun insertAll(list: List<ExercitiuEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertByUuid(exercitiu: ExercitiuEntity)

    @Update
    suspend fun update(exercitiu: ExercitiuEntity)

    @Delete
    suspend fun delete(exercitiu: ExercitiuEntity)

    @Query("SELECT * FROM exercitii WHERE antrenamentId = :antrenamentId ORDER BY setIndex")
    suspend fun getForAntrenament(antrenamentId: Long): List<ExercitiuEntity>

    @Query("SELECT * FROM exercitii WHERE antrenamentId IN (:antrenamentIds) ORDER BY antrenamentId, setIndex")
    suspend fun getForAntrenaments(antrenamentIds: List<Long>): List<ExercitiuEntity>

    @Query("SELECT * FROM exercitii WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ExercitiuEntity?

    @Query("SELECT * FROM exercitii WHERE syncUuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): ExercitiuEntity?

    @Query("SELECT * FROM exercitii WHERE syncUuid = ''")
    suspend fun getUnsynced(): List<ExercitiuEntity>

    @Query("DELETE FROM exercitii WHERE antrenamentId = :antrenamentId")
    suspend fun deleteForAntrenament(antrenamentId: Long)

    @Query("""
        SELECT e.numeExercitiu, COUNT(*) as cnt
        FROM exercitii e
        INNER JOIN antrenamente a ON e.antrenamentId = a.id
        WHERE a.userId = :userId AND a.data BETWEEN :startTime AND :endTime
        GROUP BY e.numeExercitiu
        ORDER BY cnt DESC
        LIMIT 1
    """)
    suspend fun getMostFrequentExercise(userId: String, startTime: Long, endTime: Long): MostFrequentExercise?

    @Query("""
        SELECT e.* FROM exercitii e
        INNER JOIN antrenamente a ON e.antrenamentId = a.id
        WHERE a.userId = :userId AND e.numeExercitiu = :exerciseName
        ORDER BY a.data DESC
    """)
    suspend fun getHistoryForExercise(userId: String, exerciseName: String): List<ExercitiuEntity>

    @Query("""
        SELECT e.* FROM exercitii e
        INNER JOIN antrenamente a ON e.antrenamentId = a.id
        WHERE a.userId = :userId AND e.numeExercitiu = :exerciseName
        ORDER BY a.data DESC
    """)
    suspend fun getHistoryForExerciseSimple(userId: String, exerciseName: String): List<ExercitiuEntity>

    @Query("""
        SELECT e.*, a.data as antrenamentData FROM exercitii e
        INNER JOIN antrenamente a ON e.antrenamentId = a.id
        WHERE a.userId = :userId AND e.numeExercitiu = :exerciseName
        ORDER BY a.data DESC
    """)
    suspend fun getHistoryWithDates(userId: String, exerciseName: String): List<ExerciseWithDate>

    @Query("""
        SELECT e.* FROM exercitii e
        INNER JOIN antrenamente a ON e.antrenamentId = a.id
        WHERE a.userId = :userId AND e.numeExercitiu = :exerciseName
        AND DATE(a.data / 1000, 'unixepoch') = :todayKey
        ORDER BY a.data DESC
    """)
    suspend fun getHistoryForTodayExerciseSimple(userId: String, exerciseName: String, todayKey: String): List<ExercitiuEntity>

    @Query("""
        SELECT e.* FROM exercitii e
        INNER JOIN antrenamente a ON e.antrenamentId = a.id
        WHERE a.userId = :userId AND e.numeExercitiu = :exerciseName
        ORDER BY e.greutateKg DESC LIMIT 1
    """)
    suspend fun getBestSetForExercise(userId: String, exerciseName: String): ExercitiuEntity?

    @Query("""
        SELECT DISTINCT a.* FROM antrenamente a
        INNER JOIN exercitii e ON e.antrenamentId = a.id
        WHERE a.userId = :userId AND e.numeExercitiu = :exerciseName
        AND a.data BETWEEN :startTime AND :endTime
        ORDER BY a.data DESC
    """)
    suspend fun getWorkoutsWithExercise(userId: String, exerciseName: String, startTime: Long, endTime: Long): List<AntrenamentEntity>

    @Query("""
        SELECT DISTINCT e.numeExercitiu FROM exercitii e
        INNER JOIN antrenamente a ON e.antrenamentId = a.id
        WHERE a.userId = :userId AND a.data BETWEEN :startTime AND :endTime
        ORDER BY e.numeExercitiu
    """)
    suspend fun getDistinctExerciseNames(userId: String, startTime: Long, endTime: Long): List<String>

    @Query("""
        SELECT e.numeExercitiu, COUNT(*) as cnt
        FROM exercitii e
        INNER JOIN antrenamente a ON e.antrenamentId = a.id
        WHERE a.userId = :userId
        GROUP BY e.numeExercitiu
        ORDER BY cnt DESC
        LIMIT :limit
    """)
    suspend fun getMostFrequentExerciseNames(userId: String, limit: Int): List<MostFrequentExercise>

    @Query("SELECT COUNT(*) FROM exercitii WHERE antrenamentId = :antrenamentId")
    suspend fun getSetCountForWorkout(antrenamentId: Long): Int

    @Query("""
        SELECT e.numeExercitiu as exerciseName,
               MAX(e.greutateKg) as bestWeight,
               (SELECT e2.repetari FROM exercitii e2
                INNER JOIN antrenamente a2 ON e2.antrenamentId = a2.id
                WHERE a2.userId = :userId AND e2.numeExercitiu = e.numeExercitiu
                ORDER BY e2.greutateKg DESC LIMIT 1) as bestReps,
               COUNT(DISTINCT a.id) as totalSessions
        FROM exercitii e
        INNER JOIN antrenamente a ON e.antrenamentId = a.id
        WHERE a.userId = :userId AND e.numeExercitiu IN (:exerciseNames)
        GROUP BY e.numeExercitiu
    """)
    suspend fun getSummariesForExercises(userId: String, exerciseNames: List<String>): List<ExerciseSummary>

    @Query("""
        SELECT antrenamentId, COUNT(*) as cnt FROM exercitii
        WHERE antrenamentId IN (:workoutIds)
        GROUP BY antrenamentId
    """)
    suspend fun getSetCountsForWorkouts(workoutIds: List<Long>): List<SetCountResult>
}

data class SetCountResult(val antrenamentId: Long, val cnt: Int)

@Dao
interface ExerciseDefinitionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(exercise: ExerciseDefinitionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(exercises: List<ExerciseDefinitionEntity>)

    @Query("SELECT * FROM exercises WHERE syncUuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): ExerciseDefinitionEntity?

    @Query("SELECT * FROM exercises WHERE syncUuid = ''")
    suspend fun getUnsynced(): List<ExerciseDefinitionEntity>

    @Query("SELECT * FROM exercises ORDER BY isFavorite DESC, usageCount DESC, `group`, name")
    suspend fun getAll(): List<ExerciseDefinitionEntity>

    @Query("SELECT * FROM exercises WHERE `group` = :groupName ORDER BY isFavorite DESC, usageCount DESC, name")
    suspend fun getByGroup(groupName: String): List<ExerciseDefinitionEntity>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    @Query("UPDATE exercises SET usageCount = usageCount + 1 WHERE name = :name")
    suspend fun incrementUsage(name: String)

    @Query("UPDATE exercises SET isFavorite = :isFavorite WHERE name = :name")
    suspend fun setFavorite(name: String, isFavorite: Boolean)

    @Query("SELECT * FROM exercises WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): ExerciseDefinitionEntity?
}

@Dao
interface TemplateDao {
    @Insert
    suspend fun insert(template: TemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertByUuid(template: TemplateEntity)

    @Query("SELECT * FROM templates WHERE userId = :userId")
    suspend fun getAllForUser(userId: String): List<TemplateEntity>

    @Query("SELECT * FROM templates WHERE syncUuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): TemplateEntity?

    @Query("SELECT * FROM templates WHERE syncUuid = ''")
    suspend fun getUnsynced(): List<TemplateEntity>

    @Delete
    suspend fun delete(template: TemplateEntity)
}

@Dao
interface TemplateExerciseDao {
    @Insert
    suspend fun insert(exercise: TemplateExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertByUuid(exercise: TemplateExerciseEntity)

    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId")
    suspend fun getForTemplate(templateId: Long): List<TemplateExerciseEntity>

    @Query("SELECT * FROM template_exercises WHERE syncUuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): TemplateExerciseEntity?

    @Query("SELECT * FROM template_exercises WHERE syncUuid = ''")
    suspend fun getUnsynced(): List<TemplateExerciseEntity>
}

@Dao
interface PersonalRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pr: PersonalRecordEntity)

    @Query("SELECT * FROM personal_records WHERE syncUuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): PersonalRecordEntity?

    @Query("SELECT * FROM personal_records WHERE syncUuid = ''")
    suspend fun getUnsynced(): List<PersonalRecordEntity>

    @Query("SELECT * FROM personal_records WHERE userId = :userId AND exerciseName = :exerciseName ORDER BY weight DESC LIMIT 1")
    suspend fun getBest(userId: String, exerciseName: String): PersonalRecordEntity?

    @Query("SELECT * FROM personal_records WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllForUser(userId: String): List<PersonalRecordEntity>

    @Query("SELECT * FROM personal_records WHERE userId = :userId ORDER BY weight DESC")
    suspend fun getAllSortedByWeight(userId: String): List<PersonalRecordEntity>
}

@Dao
interface MuscleRecoveryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(recovery: MuscleRecoveryEntity)

    @Query("SELECT * FROM muscle_recovery WHERE syncUuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): MuscleRecoveryEntity?

    @Query("SELECT * FROM muscle_recovery WHERE syncUuid = ''")
    suspend fun getUnsynced(): List<MuscleRecoveryEntity>

    @Query("SELECT * FROM muscle_recovery WHERE grupaMusculara = :grupa AND userId = :userId")
    suspend fun getByGroup(userId: String, grupa: String): MuscleRecoveryEntity?

    @Query("SELECT * FROM muscle_recovery WHERE userId = :userId")
    suspend fun getAll(userId: String): List<MuscleRecoveryEntity>
}

@Dao
interface BiometricDao {
    @Insert
    suspend fun insert(entry: BiometricEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertByUuid(entry: BiometricEntity)

    @Update
    suspend fun update(entry: BiometricEntity)

    @Delete
    suspend fun delete(entry: BiometricEntity)

    @Query("DELETE FROM biometric_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM biometric_entries WHERE syncUuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): BiometricEntity?

    @Query("SELECT * FROM biometric_entries WHERE syncUuid = ''")
    suspend fun getUnsynced(): List<BiometricEntity>

    @Query("SELECT * FROM biometric_entries WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllForUser(userId: String): List<BiometricEntity>

    @Query("SELECT * FROM biometric_entries WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(userId: String): BiometricEntity?

    @Query("SELECT * FROM biometric_entries WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(userId: String, limit: Int): List<BiometricEntity>
}

@Dao
interface FoodDao {
    @Insert
    suspend fun insert(entry: FoodEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertByUuid(entry: FoodEntity)

    @Update
    suspend fun update(entry: FoodEntity)

    @Delete
    suspend fun delete(entry: FoodEntity)

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM food_entries WHERE syncUuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): FoodEntity?

    @Query("SELECT * FROM food_entries WHERE syncUuid = ''")
    suspend fun getUnsynced(): List<FoodEntity>

    @Query("SELECT * FROM food_entries WHERE userId = :userId AND timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    suspend fun getForDay(userId: String, start: Long, end: Long): List<FoodEntity>

    @Query("SELECT * FROM food_entries WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(userId: String, limit: Int): List<FoodEntity>

    @Query("SELECT SUM(calories) FROM food_entries WHERE userId = :userId AND timestamp BETWEEN :start AND :end")
    suspend fun getTotalCalories(userId: String, start: Long, end: Long): Double?

    @Query("SELECT SUM(proteinG) FROM food_entries WHERE userId = :userId AND timestamp BETWEEN :start AND :end")
    suspend fun getTotalProtein(userId: String, start: Long, end: Long): Double?

    @Query("SELECT SUM(carbsG) FROM food_entries WHERE userId = :userId AND timestamp BETWEEN :start AND :end")
    suspend fun getTotalCarbs(userId: String, start: Long, end: Long): Double?

    @Query("SELECT SUM(fatG) FROM food_entries WHERE userId = :userId AND timestamp BETWEEN :start AND :end")
    suspend fun getTotalFat(userId: String, start: Long, end: Long): Double?
}

@Dao
interface FoodItemDao {
    @Insert
    suspend fun insertAll(items: List<FoodItemEntity>)

    @Query("DELETE FROM food_items")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM food_items")
    suspend fun count(): Int

    @Query("""
        SELECT * FROM food_items
        WHERE searchKey LIKE '%' || :query || '%'
        ORDER BY name LIMIT 10
    """)
    suspend fun search(query: String): List<FoodItemEntity>

    @Query("SELECT * FROM food_items ORDER BY name LIMIT 20")
    suspend fun getPopular(): List<FoodItemEntity>
}

@Dao
interface CardioRouteDao {
    @Insert
    suspend fun insert(route: CardioRouteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertByUuid(route: CardioRouteEntity)

    @Update
    suspend fun update(route: CardioRouteEntity)

    @Delete
    suspend fun delete(route: CardioRouteEntity)

    @Query("DELETE FROM cardio_routes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM cardio_routes WHERE syncUuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): CardioRouteEntity?

    @Query("SELECT * FROM cardio_routes WHERE syncUuid = ''")
    suspend fun getUnsynced(): List<CardioRouteEntity>

    @Query("SELECT * FROM cardio_routes WHERE userId = :userId ORDER BY startTime DESC")
    suspend fun getAllForUser(userId: String): List<CardioRouteEntity>

    @Query("SELECT * FROM cardio_routes WHERE id = :id")
    suspend fun getById(id: Long): CardioRouteEntity?

    @Query("SELECT SUM(distanceKm) FROM cardio_routes WHERE userId = :userId")
    suspend fun getTotalDistance(userId: String): Double?

    @Query("SELECT SUM(durationMs) FROM cardio_routes WHERE userId = :userId")
    suspend fun getTotalDuration(userId: String): Long?

    @Query("SELECT SUM(caloriesBurned) FROM cardio_routes WHERE userId = :userId")
    suspend fun getTotalCalories(userId: String): Double?

    @Query("SELECT SUM(distanceKm) FROM cardio_routes WHERE userId = :userId AND startTime BETWEEN :start AND :end")
    suspend fun getTotalDistanceBetween(userId: String, start: Long, end: Long): Double?

    @Query("SELECT SUM(durationMs) FROM cardio_routes WHERE userId = :userId AND startTime BETWEEN :start AND :end")
    suspend fun getTotalDurationBetween(userId: String, start: Long, end: Long): Long?

    @Query("SELECT SUM(caloriesBurned) FROM cardio_routes WHERE userId = :userId AND startTime BETWEEN :start AND :end")
    suspend fun getTotalCaloriesBetween(userId: String, start: Long, end: Long): Double?

    @Query("""
        SELECT SUM(distanceKm) as totalDistance, SUM(durationMs) as totalDuration, SUM(caloriesBurned) as totalCalories
        FROM cardio_routes WHERE userId = :userId AND startTime BETWEEN :start AND :end
    """)
    suspend fun getTodaySummary(userId: String, start: Long, end: Long): CardioSummary?
}

@Dao
interface RestDayDao {
    @Insert
    suspend fun insert(restDay: RestDayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertByUuid(restDay: RestDayEntity)

    @Update
    suspend fun update(restDay: RestDayEntity)

    @Delete
    suspend fun delete(restDay: RestDayEntity)

    @Query("DELETE FROM rest_days WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM rest_days WHERE syncUuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): RestDayEntity?

    @Query("SELECT * FROM rest_days WHERE syncUuid = ''")
    suspend fun getUnsynced(): List<RestDayEntity>

    @Query("SELECT * FROM rest_days WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllForUser(userId: String): List<RestDayEntity>

    @Query("SELECT * FROM rest_days WHERE userId = :userId AND date BETWEEN :start AND :end ORDER BY date")
    suspend fun getForPeriod(userId: String, start: Long, end: Long): List<RestDayEntity>

    @Query("SELECT * FROM rest_days WHERE userId = :userId AND date >= :today ORDER BY date ASC LIMIT 1")
    suspend fun getNextRestDay(userId: String, today: Long): RestDayEntity?

    @Query("UPDATE rest_days SET completed = 1 WHERE id = :id")
    suspend fun markCompleted(id: Long)

    @Query("UPDATE rest_days SET completed = 0 WHERE id = :id")
    suspend fun markUncompleted(id: Long)
}

@Dao
interface AiChatHistoryDao {
    @Insert
    suspend fun insert(message: AiChatHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertByUuid(message: AiChatHistoryEntity)

    @Query("SELECT * FROM ai_chat_history WHERE syncUuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): AiChatHistoryEntity?

    @Query("SELECT * FROM ai_chat_history WHERE syncUuid = ''")
    suspend fun getUnsynced(): List<AiChatHistoryEntity>

    @Query("SELECT * FROM ai_chat_history WHERE userId = :userId ORDER BY timestamp ASC")
    suspend fun getAllForUser(userId: String): List<AiChatHistoryEntity>

    @Query("SELECT * FROM ai_chat_history WHERE userId = :userId AND sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getForSession(userId: String, sessionId: Long): List<AiChatHistoryEntity>

    @Query("SELECT DISTINCT sessionId FROM ai_chat_history WHERE userId = :userId ORDER BY sessionId DESC")
    suspend fun getSessionIds(userId: String): List<Long>

    @Query("SELECT * FROM ai_chat_history WHERE userId = :userId AND sessionId = :sessionId AND role = 'user' ORDER BY timestamp ASC LIMIT 1")
    suspend fun getFirstUserMessage(userId: String, sessionId: Long): AiChatHistoryEntity?

    @Query("DELETE FROM ai_chat_history WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("DELETE FROM ai_chat_history WHERE userId = :userId AND sessionId = :sessionId")
    suspend fun deleteSession(userId: String, sessionId: Long)
}

@Dao
interface ExerciseMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: ExerciseMetadataEntity)

    @Query("SELECT * FROM exercise_metadata WHERE syncUuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): ExerciseMetadataEntity?

    @Query("SELECT * FROM exercise_metadata WHERE syncUuid = ''")
    suspend fun getUnsynced(): List<ExerciseMetadataEntity>

    @Query("SELECT * FROM exercise_metadata WHERE exerciseName = :name AND userId = :userId LIMIT 1")
    suspend fun getByName(userId: String, name: String): ExerciseMetadataEntity?

    @Query("SELECT * FROM exercise_metadata WHERE grupaMusculara = :grupa AND userId = :userId")
    suspend fun getByGroup(userId: String, grupa: String): List<ExerciseMetadataEntity>

    @Query("SELECT * FROM exercise_metadata WHERE userId = :userId")
    suspend fun getAll(userId: String): List<ExerciseMetadataEntity>

    @Query("SELECT * FROM exercise_metadata WHERE userId = :userId AND isFavorite = 1 ORDER BY grupaMusculara, exerciseName")
    suspend fun getFavorites(userId: String): List<ExerciseMetadataEntity>

    @Query("UPDATE exercise_metadata SET isFavorite = :isFavorite WHERE exerciseName = :name AND userId = :userId")
    suspend fun setFavorite(userId: String, name: String, isFavorite: Boolean)
}

@Dao
interface DeloadWeekDao {
    @Query("SELECT * FROM deload_weeks WHERE userId = :userId AND completed = 0 AND endDate > :now LIMIT 1")
    suspend fun getActive(userId: String, now: Long = System.currentTimeMillis()): DeloadWeekEntity?

    @Query("SELECT * FROM deload_weeks WHERE userId = :userId ORDER BY startDate DESC")
    suspend fun getHistory(userId: String): List<DeloadWeekEntity>

    @Insert
    suspend fun insert(entity: DeloadWeekEntity): Long

    @Query("UPDATE deload_weeks SET completed = 1 WHERE id = :id")
    suspend fun markCompleted(id: Long)
}

@Dao
interface WeightGoalDao {
    @Insert
    suspend fun insert(goal: WeightGoalEntity): Long

    @Update
    suspend fun update(goal: WeightGoalEntity)

    @Delete
    suspend fun delete(goal: WeightGoalEntity)

    @Query("DELETE FROM weight_goals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM weight_goals WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC LIMIT 1")
    suspend fun getActiveGoal(userId: String): WeightGoalEntity?

    @Query("SELECT * FROM weight_goals WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getAllForUser(userId: String): List<WeightGoalEntity>

    @Query("UPDATE weight_goals SET isActive = 0 WHERE userId = :userId")
    suspend fun deactivateAll(userId: String)
}

@Dao
interface InjuryRiskDao {
    @Insert
    suspend fun insert(risk: InjuryRiskEntity): Long

    @Update
    suspend fun update(risk: InjuryRiskEntity)

    @Delete
    suspend fun delete(risk: InjuryRiskEntity)

    @Query("SELECT * FROM injury_risks WHERE userId = :userId ORDER BY assessedAt DESC")
    suspend fun getAllForUser(userId: String): List<InjuryRiskEntity>

    @Query("SELECT * FROM injury_risks WHERE userId = :userId AND muscleGroup = :group ORDER BY assessedAt DESC LIMIT 1")
    suspend fun getForGroup(userId: String, group: String): InjuryRiskEntity?
}

@Database(
    entities = [
        AntrenamentEntity::class,
        ExercitiuEntity::class,
        ExerciseDefinitionEntity::class,
        TemplateEntity::class,
        TemplateExerciseEntity::class,
        PersonalRecordEntity::class,
        MuscleRecoveryEntity::class,
        ExerciseMetadataEntity::class,
        FriendshipEntity::class,
        FeedPostEntity::class,
        CommentEntity::class,
        LikeEntity::class,
        LeaderboardEntryEntity::class,
        BadgeEntity::class,
        UserBadgeEntity::class,
        StreakEntity::class,
        SubscriptionEntity::class,
        FeatureFlagEntity::class,
        UserProfileEntity::class,
        BiometricEntity::class,
        FoodEntity::class,
        FoodItemEntity::class,
        CardioRouteEntity::class,
        RestDayEntity::class,
        AiChatHistoryEntity::class,
        UserSubscriptionEntity::class,
        AdUnlockEntity::class,
        DeloadWeekEntity::class,
        WeightGoalEntity::class,
        InjuryRiskEntity::class
    ],
    version = 26,
    exportSchema = false
)
@TypeConverters(FoodUnitTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun antrenamentDao(): AntrenamentDao
    abstract fun exercitiuDao(): ExercitiuDao
    abstract fun exerciseDefinitionDao(): ExerciseDefinitionDao
    abstract fun templateDao(): TemplateDao
    abstract fun templateExerciseDao(): TemplateExerciseDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun muscleRecoveryDao(): MuscleRecoveryDao
    abstract fun exerciseMetadataDao(): ExerciseMetadataDao
    abstract fun friendshipDao(): FriendshipDao
    abstract fun feedDao(): FeedDao
    abstract fun commentDao(): CommentDao
    abstract fun likeDao(): LikeDao
    abstract fun leaderboardDao(): LeaderboardDao
    abstract fun badgeDao(): BadgeDao
    abstract fun userBadgeDao(): UserBadgeDao
    abstract fun streakDao(): StreakDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun featureFlagDao(): FeatureFlagDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun biometricDao(): BiometricDao
    abstract fun foodDao(): FoodDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun cardioRouteDao(): CardioRouteDao
    abstract fun restDayDao(): RestDayDao
    abstract fun aiChatHistoryDao(): AiChatHistoryDao
    abstract fun userSubscriptionDao(): UserSubscriptionDao
    abstract fun adUnlockDao(): AdUnlockDao
    abstract fun deloadWeekDao(): DeloadWeekDao
    abstract fun weightGoalDao(): WeightGoalDao
    abstract fun injuryRiskDao(): InjuryRiskDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS friendships (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, userId TEXT NOT NULL, friendId TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'pending', createdAt INTEGER NOT NULL DEFAULT 0)")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS feed_posts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, authorId TEXT NOT NULL, content TEXT NOT NULL, activityType TEXT NOT NULL DEFAULT 'post', createdAt INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("CREATE TABLE IF NOT EXISTS comments (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, postId INTEGER NOT NULL, authorId TEXT NOT NULL, content TEXT NOT NULL, createdAt INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("CREATE TABLE IF NOT EXISTS likes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, postId INTEGER NOT NULL, userId TEXT NOT NULL, createdAt INTEGER NOT NULL DEFAULT 0)")
            }
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS leaderboard_entries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, userId TEXT NOT NULL, metric TEXT NOT NULL, value REAL NOT NULL, periodStart INTEGER NOT NULL, periodEnd INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS badges (key TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, description TEXT NOT NULL, icon TEXT NOT NULL DEFAULT '')")
                db.execSQL("CREATE TABLE IF NOT EXISTS user_badges (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, userId TEXT NOT NULL, badgeKey TEXT NOT NULL, awardedAt INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("CREATE TABLE IF NOT EXISTS streaks (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, userId TEXT NOT NULL, currentStreak INTEGER NOT NULL DEFAULT 0, bestStreak INTEGER NOT NULL DEFAULT 0, lastDate INTEGER NOT NULL DEFAULT 0)")
            }
        }
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS subscriptions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, userId TEXT NOT NULL, provider TEXT NOT NULL DEFAULT 'stripe', subscriptionId TEXT NOT NULL DEFAULT '', planId TEXT NOT NULL DEFAULT '', status TEXT NOT NULL DEFAULT 'inactive', currentPeriodEnd INTEGER NOT NULL DEFAULT 0, createdAt INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("CREATE TABLE IF NOT EXISTS feature_flags (key TEXT NOT NULL PRIMARY KEY, enabled INTEGER NOT NULL DEFAULT 0)")
            }
        }
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS personal_records (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, userId TEXT NOT NULL, exerciseName TEXT NOT NULL, weight REAL NOT NULL, reps INTEGER NOT NULL, date INTEGER NOT NULL DEFAULT 0)")
            }
        }
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercitii ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE exercises ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE exercises ADD COLUMN usageCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE personal_records ADD COLUMN volume REAL NOT NULL DEFAULT 0")
            }
        }
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS muscle_recovery (grupaMusculara TEXT NOT NULL PRIMARY KEY, level REAL NOT NULL DEFAULT 0, lastUpdated INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("CREATE TABLE IF NOT EXISTS exercise_metadata (exerciseName TEXT NOT NULL PRIMARY KEY, grupaMusculara TEXT NOT NULL, isFavorite INTEGER NOT NULL DEFAULT 0, isCustom INTEGER NOT NULL DEFAULT 0)")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercises ADD COLUMN equipment TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS user_profiles (userId TEXT NOT NULL PRIMARY KEY, loginKey TEXT NOT NULL, name TEXT NOT NULL, photoUri TEXT NOT NULL DEFAULT '', createdAt INTEGER NOT NULL DEFAULT 0)")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS biometric_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        timestamp INTEGER NOT NULL DEFAULT 0,
                        weightKg REAL NOT NULL DEFAULT 0,
                        bodyFatPercent REAL NOT NULL DEFAULT 0,
                        waistCm REAL NOT NULL DEFAULT 0,
                        hipsCm REAL NOT NULL DEFAULT 0,
                        thighsCm REAL NOT NULL DEFAULT 0,
                        chestCm REAL NOT NULL DEFAULT 0,
                        armsCm REAL NOT NULL DEFAULT 0,
                        notes TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS food_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        barcode TEXT NOT NULL DEFAULT '',
                        name TEXT NOT NULL,
                        brand TEXT NOT NULL DEFAULT '',
                        mealType TEXT NOT NULL DEFAULT 'snack',
                        servingSize REAL NOT NULL DEFAULT 100,
                        servingUnit TEXT NOT NULL DEFAULT 'g',
                        calories REAL NOT NULL DEFAULT 0,
                        proteinG REAL NOT NULL DEFAULT 0,
                        carbsG REAL NOT NULL DEFAULT 0,
                        fatG REAL NOT NULL DEFAULT 0,
                        fiberG REAL NOT NULL DEFAULT 0,
                        timestamp INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS cardio_routes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        routePoints TEXT NOT NULL DEFAULT '',
                        distanceKm REAL NOT NULL DEFAULT 0,
                        durationMs INTEGER NOT NULL DEFAULT 0,
                        avgSpeedKmh REAL NOT NULL DEFAULT 0,
                        avgPaceMinKm REAL NOT NULL DEFAULT 0,
                        caloriesBurned REAL NOT NULL DEFAULT 0,
                        startTime INTEGER NOT NULL DEFAULT 0,
                        endTime INTEGER NOT NULL DEFAULT 0,
                        activityType TEXT NOT NULL DEFAULT 'running'
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS rest_days (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        date INTEGER NOT NULL DEFAULT 0,
                        type TEXT NOT NULL DEFAULT 'rest',
                        notes TEXT NOT NULL DEFAULT '',
                        activities TEXT NOT NULL DEFAULT '',
                        completed INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS ai_chat_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        sessionId INTEGER NOT NULL DEFAULT 0,
                        role TEXT NOT NULL,
                        message TEXT NOT NULL,
                        timestamp INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val tables = listOf(
                    "antrenamente",
                    "exercitii",
                    "exercises",
                    "templates",
                    "template_exercises",
                    "personal_records",
                    "biometric_entries",
                    "food_entries",
                    "cardio_routes",
                    "rest_days",
                    "ai_chat_history",
                    "subscriptions"
                )
                for (table in tables) {
                    db.execSQL("ALTER TABLE `$table` ADD COLUMN syncUuid TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE `$table` ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                }
                db.execSQL("ALTER TABLE `muscle_recovery` ADD COLUMN syncUuid TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `muscle_recovery` ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `exercise_metadata` ADD COLUMN syncUuid TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `exercise_metadata` ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // muscle_recovery: add userId, change PK to (grupaMusculara, userId)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `muscle_recovery_new` (
                        `grupaMusculara` TEXT NOT NULL,
                        `userId` TEXT NOT NULL DEFAULT 'simple',
                        `level` REAL NOT NULL DEFAULT 0,
                        `lastUpdated` INTEGER NOT NULL DEFAULT 0,
                        `syncUuid` TEXT NOT NULL DEFAULT '',
                        `updatedAt` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`grupaMusculara`, `userId`)
                    )
                """.trimIndent())
                db.execSQL("INSERT INTO `muscle_recovery_new` (`grupaMusculara`, `userId`, `level`, `lastUpdated`, `syncUuid`, `updatedAt`) SELECT `grupaMusculara`, 'simple', `level`, `lastUpdated`, `syncUuid`, `updatedAt` FROM `muscle_recovery`")
                db.execSQL("DROP TABLE `muscle_recovery`")
                db.execSQL("ALTER TABLE `muscle_recovery_new` RENAME TO `muscle_recovery`")

                // exercise_metadata: add userId, change PK to (exerciseName, userId)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `exercise_metadata_new` (
                        `exerciseName` TEXT NOT NULL,
                        `userId` TEXT NOT NULL DEFAULT 'simple',
                        `grupaMusculara` TEXT NOT NULL,
                        `isFavorite` INTEGER NOT NULL DEFAULT 0,
                        `isCustom` INTEGER NOT NULL DEFAULT 0,
                        `syncUuid` TEXT NOT NULL DEFAULT '',
                        `updatedAt` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`exerciseName`, `userId`)
                    )
                """.trimIndent())
                db.execSQL("INSERT INTO `exercise_metadata_new` (`exerciseName`, `userId`, `grupaMusculara`, `isFavorite`, `isCustom`, `syncUuid`, `updatedAt`) SELECT `exerciseName`, 'simple', `grupaMusculara`, `isFavorite`, `isCustom`, `syncUuid`, `updatedAt` FROM `exercise_metadata`")
                db.execSQL("DROP TABLE `exercise_metadata`")
                db.execSQL("ALTER TABLE `exercise_metadata_new` RENAME TO `exercise_metadata`")
            }
        }

        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `badges` ADD COLUMN `hint` TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_subscriptions` (
                        `userId` TEXT NOT NULL,
                        `subscriptionType` TEXT NOT NULL DEFAULT 'FREE',
                        `subscriptionStatus` TEXT NOT NULL DEFAULT 'ACTIVE',
                        `expiryDate` INTEGER,
                        `isLifetime` INTEGER NOT NULL DEFAULT 0,
                        `revenueCatId` TEXT NOT NULL DEFAULT '',
                        `lastSyncedAt` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`userId`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ad_unlocks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `featureId` TEXT NOT NULL,
                        `unlockedUntil` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ad_unlocks_userId` ON `ad_unlocks` (`userId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ad_unlocks_userId_featureId` ON `ad_unlocks` (`userId`, `featureId`)")
            }
        }

        private val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `deload_weeks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `startDate` INTEGER NOT NULL,
                        `endDate` INTEGER NOT NULL,
                        `reason` TEXT NOT NULL,
                        `reductionFactor` REAL NOT NULL,
                        `completed` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_deload_weeks_userId` ON `deload_weeks` (`userId`)")
            }
        }

        private val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `antrenamente` ADD COLUMN `durationMs` INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `food_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `caloriesPer100g` REAL NOT NULL,
                        `proteinPer100g` REAL NOT NULL,
                        `carbsPer100g` REAL NOT NULL,
                        `fatPer100g` REAL NOT NULL,
                        `unitType` TEXT NOT NULL,
                        `gramsPerPiece` REAL
                    )
                """.trimIndent())
            }
        }

        private fun columnExists(db: SupportSQLiteDatabase, table: String, column: String): Boolean {
            var exists = false
            db.query("PRAGMA table_info(`$table`)").use { cursor ->
                val nameIdx = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameIdx) == column) {
                        exists = true
                        break
                    }
                }
            }
            return exists
        }

        private fun addColumnIfMissing(db: SupportSQLiteDatabase, table: String, column: String, definition: String) {
            if (!columnExists(db, table, column)) {
                db.execSQL("ALTER TABLE `$table` ADD COLUMN $column $definition")
            }
        }

        private val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfMissing(db, "food_items", "nameEn", "TEXT NOT NULL DEFAULT ''")
                addColumnIfMissing(db, "food_items", "nameRu", "TEXT NOT NULL DEFAULT ''")
                addColumnIfMissing(db, "food_items", "nameUk", "TEXT NOT NULL DEFAULT ''")
                addColumnIfMissing(db, "food_items", "nameFr", "TEXT NOT NULL DEFAULT ''")
                addColumnIfMissing(db, "food_items", "nameDe", "TEXT NOT NULL DEFAULT ''")
                addColumnIfMissing(db, "food_items", "nameEs", "TEXT NOT NULL DEFAULT ''")
                addColumnIfMissing(db, "food_items", "nameIt", "TEXT NOT NULL DEFAULT ''")
                addColumnIfMissing(db, "food_items", "nameTr", "TEXT NOT NULL DEFAULT ''")
                addColumnIfMissing(db, "food_items", "namePt", "TEXT NOT NULL DEFAULT ''")
                addColumnIfMissing(db, "food_items", "namePl", "TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfMissing(db, "food_items", "searchKey", "TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `weight_goals` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `targetWeightKg` REAL NOT NULL,
                        `startWeightKg` REAL NOT NULL,
                        `deadlineTimestamp` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `syncUuid` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `injury_risks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `muscleGroup` TEXT NOT NULL,
                        `riskLevel` REAL NOT NULL,
                        `reason` TEXT NOT NULL,
                        `assessedAt` INTEGER NOT NULL,
                        `syncUuid` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kinetic.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25, MIGRATION_25_26)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
