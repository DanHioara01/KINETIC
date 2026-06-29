package com.example.gymlog2

import com.example.gymlog2.AppConstants.DEFAULT_USER_ID
import java.util.Calendar

class AntrenamentRepository(private val db: AppDatabase, private val syncRepo: SyncRepository? = null) {

    suspend fun saveAntrenament(userId: String, session: TrainingSession, notes: String = ""): Long {
        val totalWeight = session.exercitii.sumOf { ex ->
            ex.seturi.sumOf { it.greutateKg * it.repetari }
        }

        val entries = mutableListOf<ExercitiuEntity>()
        val exercises = session.exercitii.map { ex ->
            val volume = ex.seturi.sumOf { it.greutateKg * it.repetari }
            val bestSet = ex.seturi.maxByOrNull { it.greutateKg }
            val setEntities = ex.seturi.mapIndexed { idx, set ->
                ExercitiuEntity(
                    antrenamentId = 0,
                    numeExercitiu = ex.numeExercitiu,
                    setIndex = idx,
                    greutateKg = set.greutateKg,
                    repetari = set.repetari,
                    notes = ex.notes
                )
            }
            Triple(ex, setEntities, bestSet)
        }

        val antrenamentId: Long
        if (syncRepo != null) {
            antrenamentId = syncRepo.saveAntrenament(
                AntrenamentEntity(userId = userId, grupaMusculara = session.grupaMusculara, data = session.data.time, totalWeight = totalWeight, notes = notes),
                exercises.flatMap { it.second }
            )
        } else {
            antrenamentId = db.antrenamentDao().insert(
                AntrenamentEntity(userId = userId, grupaMusculara = session.grupaMusculara, data = session.data.time, totalWeight = totalWeight, notes = notes)
            )
            db.exercitiuDao().insertAll(exercises.flatMap { it.second })
        }

        for ((ex, _, bestSet) in exercises) {
            if (bestSet != null) {
                val volume = ex.seturi.sumOf { it.greutateKg * it.repetari }
                val existingPr = db.personalRecordDao().getBest(userId, ex.numeExercitiu)
                if (existingPr == null || bestSet.greutateKg > existingPr.weight) {
                    val pr = PersonalRecordEntity(
                        userId = userId,
                        exerciseName = ex.numeExercitiu,
                        weight = bestSet.greutateKg,
                        reps = bestSet.repetari,
                        volume = volume,
                        date = System.currentTimeMillis()
                    )
                    if (syncRepo != null) {
                        syncRepo.savePersonalRecord(pr)
                    } else {
                        db.personalRecordDao().upsert(pr)
                    }
                }
            }
            db.exerciseDefinitionDao().incrementUsage(ex.numeExercitiu)
        }

        return antrenamentId
    }

    suspend fun getAntrenamente(userId: String): List<AntrenamentEntity> {
        return db.antrenamentDao().getAllForUser(userId)
    }

    suspend fun getExercitii(antrenamentId: Long): List<ExercitiuEntity> {
        return db.exercitiuDao().getForAntrenament(antrenamentId)
    }

    suspend fun deleteAntrenament(antrenamentId: Long) {
        db.exercitiuDao().deleteForAntrenament(antrenamentId)
        db.antrenamentDao().deleteById(antrenamentId)
    }

    suspend fun getAllExercises(): List<ExerciseDefinitionEntity> {
        return db.exerciseDefinitionDao().getAll()
    }

    suspend fun getExercisesByGroup(group: String): List<ExerciseDefinitionEntity> {
        return db.exerciseDefinitionDao().getByGroup(group)
    }

    suspend fun getExerciseHistory(userId: String, exerciseName: String): List<ExercitiuEntity> {
        return db.exercitiuDao().getHistoryForExercise(userId, exerciseName)
    }

    suspend fun getBestSet(userId: String, exerciseName: String): ExercitiuEntity? {
        return db.exercitiuDao().getBestSetForExercise(userId, exerciseName)
    }

    suspend fun getAllPersonalRecords(userId: String): List<PersonalRecordEntity> {
        return db.personalRecordDao().getAllForUser(userId)
    }

    suspend fun getPersonalRecordsSorted(userId: String): List<PersonalRecordEntity> {
        return db.personalRecordDao().getAllSortedByWeight(userId)
    }

    suspend fun getTotalVolume(userId: String, startTime: Long, endTime: Long): Double {
        return db.antrenamentDao().getTotalVolume(userId, startTime, endTime) ?: 0.0
    }

    suspend fun getMostFrequentExercise(userId: String, startTime: Long, endTime: Long): MostFrequentExercise? {
        return db.exercitiuDao().getMostFrequentExercise(userId, startTime, endTime)
    }

    suspend fun getWorkoutsInPeriod(userId: String, startTime: Long, endTime: Long): List<AntrenamentEntity> {
        return db.antrenamentDao().getWorkoutsInPeriod(userId, startTime, endTime)
    }

    suspend fun addCustomExercise(name: String, group: String) {
        db.exerciseDefinitionDao().upsert(
            ExerciseDefinitionEntity(name = name, group = group, isDefault = false)
        )
    }

    suspend fun toggleFavorite(name: String) {
        val exercise = db.exerciseDefinitionDao().getByName(name)
        if (exercise != null) {
            db.exerciseDefinitionDao().setFavorite(name, !exercise.isFavorite)
        }
    }

    suspend fun updateWorkoutNotes(antrenamentId: Long, notes: String) {
        val antrenament = db.antrenamentDao().getById(antrenamentId)
        if (antrenament != null) {
            db.antrenamentDao().update(antrenament.copy(notes = notes))
        }
    }

    suspend fun updateExerciseNotes(exerciseId: Long, notes: String) {
        val exercise = db.exercitiuDao().getForAntrenament(0).find { it.id == exerciseId }
        if (exercise != null) {
            db.exercitiuDao().update(exercise.copy(notes = notes))
        }
    }

    suspend fun deleteExerciseSet(exerciseId: Long) {
        val exercise = db.exercitiuDao().getForAntrenament(0).find { it.id == exerciseId }
        if (exercise != null) {
            db.exercitiuDao().delete(exercise)
        }
    }

    suspend fun updateExerciseSet(exerciseId: Long, weight: Double, reps: Int) {
        val exercise = db.exercitiuDao().getForAntrenament(0).find { it.id == exerciseId }
        if (exercise != null) {
            db.exercitiuDao().update(exercise.copy(greutateKg = weight, repetari = reps))
        }
    }

    // ========== Simple callback-style methods for dark-theme UI ==========

    suspend fun getExercitiiPentruGrupaSimple(grupa: String): List<ExerciseListItem> {
        val dbExercises = db.exerciseDefinitionDao().getByGroup(grupa)
        val dbExMap = dbExercises.associateBy { it.name }
        val metadata = db.exerciseMetadataDao().getByGroup(grupa)
        val metaMap = metadata.associateBy { it.exerciseName }

        val hardcoded = DataProvider.exercitiiPeGrupa[grupa] ?: listOf()
        val customFromMeta = metadata.filter { it.isCustom }

        val allItems = mutableListOf<ExerciseListItem>()
        hardcoded.forEach { ex ->
            val meta = metaMap[ex.name]
            val equipment = dbExMap[ex.name]?.equipment ?: ex.equipment
            allItems.add(ExerciseListItem(ex, isFavorite = meta?.isFavorite ?: false, equipment = equipment))
        }
        customFromMeta.forEach { me ->
            if (hardcoded.none { it.name == me.exerciseName }) {
                val equipment = dbExMap[me.exerciseName]?.equipment ?: ""
                allItems.add(
                    ExerciseListItem(
                        ExerciseDefinition(me.exerciseName, me.grupaMusculara),
                        isFavorite = me.isFavorite,
                        isCustom = true,
                        equipment = equipment
                    )
                )
            }
        }
        return allItems.sortedByDescending { it.isFavorite }
    }

    suspend fun adaugaExercitiuCustom(grupa: String, nume: String) {
        val existing = db.exerciseMetadataDao().getByName(nume)
        val metadata = if (existing == null) {
            ExerciseMetadataEntity(exerciseName = nume, grupaMusculara = grupa, isFavorite = false, isCustom = true)
        } else {
            existing
        }
        if (syncRepo != null) {
            syncRepo.saveExerciseMetadata(metadata)
        } else {
            db.exerciseMetadataDao().upsert(metadata)
        }
    }

    suspend fun setFavoriteSimple(grupa: String, numeExercitiu: String, isFavorite: Boolean) {
        val existing = db.exerciseMetadataDao().getByName(numeExercitiu)
        val metadata = if (existing != null) {
            existing.copy(isFavorite = isFavorite)
        } else {
            ExerciseMetadataEntity(exerciseName = numeExercitiu, grupaMusculara = grupa, isFavorite = isFavorite)
        }
        if (syncRepo != null) {
            syncRepo.saveExerciseMetadata(metadata)
        } else {
            db.exerciseMetadataDao().upsert(metadata)
        }
    }

    suspend fun salveazaAntrenamentSimple(grupaMusculara: String, numeExercitiu: String, seturi: List<SetEntry>, note: String): Boolean {
        val totalWeight = seturi.sumOf { it.greutateKg * it.repetari }
        val entries = seturi.mapIndexed { idx, set ->
            ExercitiuEntity(antrenamentId = 0, numeExercitiu = numeExercitiu, setIndex = idx, greutateKg = set.greutateKg, repetari = set.repetari, notes = note)
        }
        val antrenamentId: Long
        if (syncRepo != null) {
            antrenamentId = syncRepo.saveAntrenament(
                AntrenamentEntity(userId = DEFAULT_USER_ID, grupaMusculara = grupaMusculara, data = System.currentTimeMillis(), totalWeight = totalWeight, notes = note),
                entries
            )
        } else {
            antrenamentId = db.antrenamentDao().insert(
                AntrenamentEntity(userId = DEFAULT_USER_ID, grupaMusculara = grupaMusculara, data = System.currentTimeMillis(), totalWeight = totalWeight, notes = note)
            )
            db.exercitiuDao().insertAll(entries)
        }

        val bestSet = seturi.maxByOrNull { it.greutateKg }
        var isNewPR = false
        if (bestSet != null) {
            val volume = seturi.sumOf { it.greutateKg * it.repetari }
            val existingPr = db.personalRecordDao().getBest(DEFAULT_USER_ID, numeExercitiu)
            if (existingPr == null || bestSet.greutateKg > existingPr.weight) {
                val pr = PersonalRecordEntity(userId = DEFAULT_USER_ID, exerciseName = numeExercitiu, weight = bestSet.greutateKg, reps = bestSet.repetari, volume = volume, date = System.currentTimeMillis())
                if (syncRepo != null) {
                    syncRepo.savePersonalRecord(pr)
                } else {
                    db.personalRecordDao().upsert(pr)
                }
                isNewPR = true
            }
        }
        db.exerciseDefinitionDao().incrementUsage(numeExercitiu)

        val lastSet = seturi.lastOrNull()
        if (lastSet != null) {
            updateMuscleRecovery(grupaMusculara, seturi.size)
        }

        return isNewPR
    }

    suspend fun getIstoricExercitiu(exerciseName: String): List<ExercitiuEntity> {
        val cal = Calendar.getInstance()
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val todayKey = fmt.format(cal.time)
        return db.exercitiuDao().getHistoryForTodayExerciseSimple(exerciseName, todayKey)
    }

    suspend fun getStatisticiExercitiu(exerciseName: String): ExerciseStats {
        val cal = Calendar.getInstance()
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val todayKey = fmt.format(cal.time)
        val history = db.exercitiuDao().getHistoryForTodayExerciseSimple(exerciseName, todayKey)
        val maxGreutate = history.maxOfOrNull { it.greutateKg } ?: 0.0
        val maxRepetari = history.maxOfOrNull { it.repetari } ?: 0
        val maxVolumSet = history.maxOfOrNull { it.greutateKg * it.repetari } ?: 0.0
        return ExerciseStats(maxGreutate, maxRepetari, maxVolumSet)
    }

    suspend fun getVolumeSummary(): VolumeSummary {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val dayStart = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val dayEnd = cal.timeInMillis

        cal.timeInMillis = dayStart
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val weekStart = cal.timeInMillis
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        val weekEnd = cal.timeInMillis

        cal.timeInMillis = dayStart
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val monthStart = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val monthEnd = cal.timeInMillis

        val azi = db.antrenamentDao().getTotalVolume(DEFAULT_USER_ID, dayStart, dayEnd) ?: 0.0
        val saptamana = db.antrenamentDao().getTotalVolume(DEFAULT_USER_ID, weekStart, weekEnd) ?: 0.0
        val luna = db.antrenamentDao().getTotalVolume(DEFAULT_USER_ID, monthStart, monthEnd) ?: 0.0
        return VolumeSummary(azi, saptamana, luna)
    }

    suspend fun getRecuperareMusculara(grupa: String): Double {
        val entity = db.muscleRecoveryDao().getByGroup(grupa) ?: return 0.0
        val recoveryHours = getRecoveryHoursForGroup(grupa)
        val elapsedMs = System.currentTimeMillis() - entity.lastUpdated
        val recoveryMs = recoveryHours * 3_600_000
        val drain = elapsedMs.toDouble() / recoveryMs
        return (entity.level - drain).coerceIn(0.0, 1.0)
    }

    suspend fun getToateRecuperarile(): List<Pair<String, Double>> {
        val allRecovery = db.muscleRecoveryDao().getAll().associate { it.grupaMusculara to it.level }
        return DataProvider.grupeMusculare.map { grupa ->
            grupa to (allRecovery[grupa] ?: 0.0)
        }
    }

    suspend fun updateMuscleRecovery(grupa: String, numSets: Int) {
        val currentLevel = getRecuperareMusculara(grupa)
        val fatiguePerSet = 0.12
        val newLevel = (currentLevel + numSets * fatiguePerSet).coerceAtMost(1.0)
        val recovery = MuscleRecoveryEntity(grupaMusculara = grupa, level = newLevel, lastUpdated = System.currentTimeMillis())
        if (syncRepo != null) {
            syncRepo.saveMuscleRecovery(recovery)
        } else {
            db.muscleRecoveryDao().upsert(recovery)
        }
    }

    suspend fun incarcaUltimulAntrenament(exerciseName: String): List<SetEntry> {
        val history = db.exercitiuDao().getHistoryForExerciseSimple(exerciseName)
        if (history.isEmpty()) return listOf(SetEntry(0.0, 0))

        val latestAntrenamentId = history.firstOrNull()?.antrenamentId ?: return listOf(SetEntry(0.0, 0))
        val latestSets = db.exercitiuDao().getForAntrenament(latestAntrenamentId)
        return latestSets.map { SetEntry(it.greutateKg, it.repetari) }
    }

    suspend fun updateSetSimple(updated: ExercitiuEntity) {
        db.exercitiuDao().update(updated)
    }

    suspend fun deleteSetSimple(set: ExercitiuEntity) {
        db.exercitiuDao().delete(set)
    }

    suspend fun getProgresLunar(exerciseName: String): List<ProgresLunar> {
        val historyWithDates = db.exercitiuDao().getHistoryWithDates(exerciseName)
        val grouped = mutableMapOf<String, Double>()
        for (item in historyWithDates) {
            val cal = Calendar.getInstance().apply { timeInMillis = item.antrenamentData }
            val monthKey = "%d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
            val current = grouped[monthKey] ?: 0.0
            if (item.exercise.greutateKg > current) {
                grouped[monthKey] = item.exercise.greutateKg
            }
        }
        return grouped.map { (luna, greutate) -> ProgresLunar(luna, greutate) }.sortedBy { it.luna }
    }

    private fun getRecoveryHoursForGroup(grupa: String): Long {
        return when (grupa) {
            "Abdomen" -> 48
            "Biceps" -> 36
            "Triceps" -> 48
            "Umeri" -> 48
            "Piept" -> 48
            "Spate" -> 72
            "Picioare" -> 72
            "Fese" -> 72
            "Gambe" -> 36
            "Antebrate" -> 36
            "Gat & Trapezi" -> 48
            else -> 48
        }
    }

    fun shouldTriggerDeload(userId: String, intervalWeeks: Int): DeloadTrigger? {
        return null
    }

    fun getDeloadHistory(userId: String): List<DeloadWeekEntity> {
        return emptyList()
    }

    fun getDeloadReason(trigger: DeloadTrigger?): String {
        return trigger?.reason ?: "General deload"
    }

    fun startDeload(userId: String, reason: String, reductionFactor: Double): Long {
        return 0L
    }

    fun getActiveDeload(userId: String): DeloadWeekEntity? {
        return null
    }

    fun applyDeloadReduction(maxWeight: Double, setCount: Int, avgReps: Int, exerciseName: String): DeloadExerciseReduction {
        val reductionPercent = 35
        val newWeight = maxWeight * (1.0 - reductionPercent / 100.0)
        val newSets = (setCount * 0.6).toInt().coerceAtLeast(2)
        val isCompound = exerciseName in listOf("Bench Press", "Squat", "Deadlift", "Barbell Row", "Overhead Press")
        return DeloadExerciseReduction(
            exerciseName = exerciseName,
            originalWeight = maxWeight,
            originalSets = setCount,
            newWeight = newWeight,
            newSets = newSets,
            weightReductionPercent = reductionPercent,
            setsReduction = setCount - newSets,
            isCompound = isCompound
        )
    }

    fun weeksSinceLastDeload(userId: String): Int {
        return 0
    }

    fun getTiredMusclesCount(recoveryMap: Map<String, Double>): Int {
        return recoveryMap.count { it.value > 0.5 }
    }

    fun getAvgRecoveryPercent(recoveryMap: Map<String, Double>): Int {
        if (recoveryMap.isEmpty()) return 100
        val avg = recoveryMap.values.average()
        return ((1.0 - avg) * 100).toInt().coerceIn(0, 100)
    }

    fun endDeload(deloadId: Long) {
    }
}
