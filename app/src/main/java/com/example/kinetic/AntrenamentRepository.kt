package com.example.kinetic

import java.util.Calendar

class AntrenamentRepository(private val db: AppDatabase, private val syncRepo: SyncRepository? = null) {

    suspend fun saveAntrenament(userId: String, session: TrainingSession, notes: String = "", durationMs: Long = 0L): Long {
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
                AntrenamentEntity(userId = userId, grupaMusculara = session.grupaMusculara, data = session.data.time, totalWeight = totalWeight, notes = notes, durationMs = durationMs),
                exercises.flatMap { it.second }
            )
        } else {
            antrenamentId = db.antrenamentDao().insert(
                AntrenamentEntity(userId = userId, grupaMusculara = session.grupaMusculara, data = session.data.time, totalWeight = totalWeight, notes = notes, durationMs = durationMs)
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

        val totalSets = session.exercitii.sumOf { it.seturi.size }
        updateMuscleRecovery(userId, session.grupaMusculara, totalSets)

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

    suspend fun toggleFavorite(name: String, userId: String = "", group: String = "") {
        if (userId.isNotBlank()) {
            val existing = db.exerciseMetadataDao().getByName(userId, name)
            val isFav = !(existing?.isFavorite ?: false)
            val metadata = if (existing != null) {
                existing.copy(isFavorite = isFav)
            } else {
                ExerciseMetadataEntity(exerciseName = name, userId = userId, grupaMusculara = group, isFavorite = isFav)
            }
            db.exerciseMetadataDao().upsert(metadata)
        } else {
            val exercise = db.exerciseDefinitionDao().getByName(name)
            if (exercise != null) {
                db.exerciseDefinitionDao().setFavorite(name, !exercise.isFavorite)
            }
        }
    }

    suspend fun updateWorkoutNotes(antrenamentId: Long, notes: String) {
        val antrenament = db.antrenamentDao().getById(antrenamentId)
        if (antrenament != null) {
            db.antrenamentDao().update(antrenament.copy(notes = notes))
        }
    }

    suspend fun updateExerciseNotes(exerciseId: Long, notes: String) {
        val exercise = db.exercitiuDao().getById(exerciseId) ?: return
        db.exercitiuDao().update(exercise.copy(notes = notes))
    }

    suspend fun deleteExerciseSet(exerciseId: Long) {
        val exercise = db.exercitiuDao().getById(exerciseId) ?: return
        db.exercitiuDao().delete(exercise)
    }

    suspend fun updateExerciseSet(exerciseId: Long, weight: Double, reps: Int) {
        val exercise = db.exercitiuDao().getById(exerciseId) ?: return
        db.exercitiuDao().update(exercise.copy(greutateKg = weight, repetari = reps))
    }

    // ========== Simple callback-style methods for dark-theme UI ==========

    suspend fun getExercitiiPentruGrupaSimple(userId: String, grupa: String): List<ExerciseListItem> {
        val dbExercises = db.exerciseDefinitionDao().getByGroup(grupa)
        val dbExMap = dbExercises.associateBy { it.name }
        val metadata = db.exerciseMetadataDao().getByGroup(userId, grupa)
        val metaMap = metadata.associateBy { it.exerciseName }

        val hardcoded = DataProvider.getDeduplicatedExercises(grupa)
        val customFromMeta = metadata.filter { it.isCustom }

        val allItems = mutableListOf<ExerciseListItem>()
        hardcoded.forEach { ex ->
            val meta = metaMap[ex.name]
            val equipment = dbExMap[ex.name]?.equipment?.ifBlank { ex.equipment } ?: ex.equipment
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

    suspend fun adaugaExercitiuCustom(userId: String, grupa: String, nume: String) {
        val existing = db.exerciseMetadataDao().getByName(userId, nume)
        val metadata = if (existing == null) {
            ExerciseMetadataEntity(exerciseName = nume, userId = userId, grupaMusculara = grupa, isFavorite = false, isCustom = true)
        } else {
            existing
        }
        if (syncRepo != null) {
            syncRepo.saveExerciseMetadata(metadata)
        } else {
            db.exerciseMetadataDao().upsert(metadata)
        }
    }

    suspend fun setFavoriteSimple(userId: String, grupa: String, numeExercitiu: String, isFavorite: Boolean) {
        val existing = db.exerciseMetadataDao().getByName(userId, numeExercitiu)
        val metadata = if (existing != null) {
            existing.copy(isFavorite = isFavorite)
        } else {
            ExerciseMetadataEntity(exerciseName = numeExercitiu, userId = userId, grupaMusculara = grupa, isFavorite = isFavorite)
        }
        if (syncRepo != null) {
            syncRepo.saveExerciseMetadata(metadata)
        } else {
            db.exerciseMetadataDao().upsert(metadata)
        }
    }

    suspend fun getFavoriteExercises(userId: String): Map<String, List<ExerciseListItem>> {
        val favorites = db.exerciseMetadataDao().getFavorites(userId)
        val grouped = mutableMapOf<String, MutableList<ExerciseListItem>>()
        for (meta in favorites) {
            val dbExercise = db.exerciseDefinitionDao().getByName(meta.exerciseName)
            val equipment = dbExercise?.equipment ?: ""
            val item = ExerciseListItem(
                exercise = ExerciseDefinition(meta.exerciseName, meta.grupaMusculara, equipment),
                isFavorite = true,
                isCustom = meta.isCustom,
                equipment = equipment
            )
            grouped.getOrPut(meta.grupaMusculara) { mutableListOf() }.add(item)
        }
        return grouped.toSortedMap()
    }

    suspend fun salveazaAntrenamentSimple(userId: String, grupaMusculara: String, numeExercitiu: String, seturi: List<SetEntry>, note: String, durationMs: Long = 0L): Boolean {
        val totalWeight = seturi.sumOf { it.greutateKg * it.repetari }
        val entries = seturi.mapIndexed { idx, set ->
            ExercitiuEntity(antrenamentId = 0, numeExercitiu = numeExercitiu, setIndex = idx, greutateKg = set.greutateKg, repetari = set.repetari, notes = note)
        }
        val antrenamentId: Long
        if (syncRepo != null) {
            antrenamentId = syncRepo.saveAntrenament(
                AntrenamentEntity(userId = userId, grupaMusculara = grupaMusculara, data = System.currentTimeMillis(), totalWeight = totalWeight, notes = note, durationMs = durationMs),
                entries
            )
        } else {
            antrenamentId = db.antrenamentDao().insert(
                AntrenamentEntity(userId = userId, grupaMusculara = grupaMusculara, data = System.currentTimeMillis(), totalWeight = totalWeight, notes = note, durationMs = durationMs)
            )
            db.exercitiuDao().insertAll(entries)
        }

        val bestSet = seturi.maxByOrNull { it.greutateKg }
        var isNewPR = false
        if (bestSet != null) {
            val volume = seturi.sumOf { it.greutateKg * it.repetari }
            val existingPr = db.personalRecordDao().getBest(userId, numeExercitiu)
            if (existingPr == null || bestSet.greutateKg > existingPr.weight) {
                val pr = PersonalRecordEntity(userId = userId, exerciseName = numeExercitiu, weight = bestSet.greutateKg, reps = bestSet.repetari, volume = volume, date = System.currentTimeMillis())
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
            updateMuscleRecovery(userId, grupaMusculara, seturi.size)
        }

        return isNewPR
    }

    suspend fun getIstoricExercitiu(userId: String, exerciseName: String): List<ExercitiuEntity> {
        return db.exercitiuDao().getHistoryForExercise(userId, exerciseName)
    }

    suspend fun getStatisticiExercitiu(userId: String, exerciseName: String): ExerciseStats {
        val history = db.exercitiuDao().getHistoryForExercise(userId, exerciseName)
        val maxGreutate = history.maxOfOrNull { it.greutateKg } ?: 0.0
        val maxRepetari = history.maxOfOrNull { it.repetari } ?: 0
        val maxVolumSet = history.maxOfOrNull { it.greutateKg * it.repetari } ?: 0.0
        return ExerciseStats(maxGreutate, maxRepetari, maxVolumSet)
    }

    suspend fun getVolumeSummary(userId: String): VolumeSummary {
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

        val azi = db.antrenamentDao().getTotalVolume(userId, dayStart, dayEnd) ?: 0.0
        val saptamana = db.antrenamentDao().getTotalVolume(userId, weekStart, weekEnd) ?: 0.0
        val luna = db.antrenamentDao().getTotalVolume(userId, monthStart, monthEnd) ?: 0.0
        return VolumeSummary(azi, saptamana, luna)
    }

    suspend fun getRecuperareMusculara(userId: String, grupa: String): Double {
        val entity = db.muscleRecoveryDao().getByGroup(userId, grupa) ?: return 0.0
        val recoveryHours = getRecoveryHoursForGroup(grupa)
        val elapsedMs = System.currentTimeMillis() - entity.lastUpdated
        val recoveryMs = recoveryHours * 3_600_000
        val drain = elapsedMs.toDouble() / recoveryMs
        return (entity.level - drain).coerceIn(0.0, 1.0)
    }

    suspend fun getToateRecuperarile(userId: String): List<Pair<String, Double>> {
        val allRecovery = db.muscleRecoveryDao().getAll(userId)
        val now = System.currentTimeMillis()
        val recoveryMap = allRecovery.associate { entity ->
            val recoveryHours = getRecoveryHoursForGroup(entity.grupaMusculara)
            val elapsedMs = now - entity.lastUpdated
            val recoveryMs = recoveryHours * 3_600_000
            val drain = elapsedMs.toDouble() / recoveryMs
            entity.grupaMusculara to (entity.level - drain).coerceIn(0.0, 1.0)
        }
        return DataProvider.grupeMusculare.map { grupa ->
            grupa to (recoveryMap[grupa] ?: 0.0)
        }
    }

    suspend fun updateMuscleRecovery(userId: String, grupa: String, numSets: Int) {
        val currentLevel = getRecuperareMusculara(userId, grupa)
        val fatiguePerSet = 0.12
        val newLevel = (currentLevel + numSets * fatiguePerSet).coerceAtMost(1.0)
        val recovery = MuscleRecoveryEntity(grupaMusculara = grupa, userId = userId, level = newLevel, lastUpdated = System.currentTimeMillis())
        if (syncRepo != null) {
            syncRepo.saveMuscleRecovery(recovery)
        } else {
            db.muscleRecoveryDao().upsert(recovery)
        }
    }

    suspend fun incarcaUltimulAntrenament(userId: String, exerciseName: String): List<SetEntry> {
        val history = db.exercitiuDao().getHistoryForExerciseSimple(userId, exerciseName)
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

    suspend fun getProgresLunar(userId: String, exerciseName: String): List<ProgresLunar> {
        val historyWithDates = db.exercitiuDao().getHistoryWithDates(userId, exerciseName)
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

    suspend fun getExerciseSummaries(userId: String, exerciseNames: List<String>): Map<String, ExerciseSummary> {
        if (exerciseNames.isEmpty()) return emptyMap()
        val results = db.exercitiuDao().getSummariesForExercises(userId, exerciseNames)
        return results.associateBy { it.exerciseName }
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
        val app = KineticApplication.get() ?: return null
        val prefs = PreferencesManager(app, UserProfileManager(app))
        val lastTs = prefs.getLastDeloadTimestamp()
        if (lastTs == 0L) {
            val startDate = prefs.getWorkoutStartDate()
            val weeksSinceStart = java.time.temporal.ChronoUnit.WEEKS.between(startDate, java.time.LocalDate.now()).toInt()
            if (weeksSinceStart >= intervalWeeks) {
                return DeloadTrigger("Time for deload after ${weeksSinceStart} weeks")
            }
            return null
        }
        val weeksSince = weeksSinceLastDeload(userId)
        if (weeksSince >= intervalWeeks) {
            return DeloadTrigger("Deload scheduled after $weeksSince weeks")
        }
        return null
    }

    suspend fun getDeloadHistory(userId: String): List<DeloadWeekEntity> {
        return try {
            db.deloadWeekDao().getHistory(userId)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getDeloadReason(trigger: DeloadTrigger?): String {
        return trigger?.reason ?: "General deload"
    }

    suspend fun startDeload(userId: String, reason: String, reductionFactor: Double): Long {
        val now = System.currentTimeMillis()
        val endDate = now + 7L * 24 * 60 * 60 * 1000
        val entity = DeloadWeekEntity(
            userId = userId,
            startDate = now,
            endDate = endDate,
            reason = reason,
            reductionFactor = reductionFactor,
            completed = false
        )
        return try {
            db.deloadWeekDao().insert(entity)
        } catch (_: Exception) {
            0L
        }
    }

    suspend fun getActiveDeload(userId: String): DeloadWeekEntity? {
        return try {
            db.deloadWeekDao().getActive(userId)
        } catch (_: Exception) {
            null
        }
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
        val app = KineticApplication.get() ?: return 0
        val prefs = PreferencesManager(app, UserProfileManager(app))
        val lastTs = prefs.getLastDeloadTimestamp()
        if (lastTs == 0L) return 0
        val weeks = java.time.temporal.ChronoUnit.WEEKS.between(
            java.time.Instant.ofEpochMilli(lastTs).atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
            java.time.LocalDate.now()
        ).toInt()
        return weeks.coerceAtLeast(0)
    }

    fun getTiredMusclesCount(recoveryMap: Map<String, Double>): Int {
        return recoveryMap.count { it.value > 0.5 }
    }

    fun getAvgRecoveryPercent(recoveryMap: Map<String, Double>): Int {
        if (recoveryMap.isEmpty()) return 100
        val avg = recoveryMap.values.average()
        return ((1.0 - avg) * 100).toInt().coerceIn(0, 100)
    }

    suspend fun endDeload(deloadId: Long) {
        try {
            db.deloadWeekDao().markCompleted(deloadId)
        } catch (_: Exception) { }
    }
}
