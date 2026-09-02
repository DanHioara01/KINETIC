package com.example.kinetic

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncRepository(
    private val db: AppDatabase,
    private val api: KineticApi,
    private val preferencesManager: PreferencesManager
) {

    // ========================================
    // ANTRENAMENTE + EXERCITII
    // ========================================

    suspend fun saveAntrenament(antrenament: AntrenamentEntity, exercises: List<ExercitiuEntity>): Long {
        return withContext(Dispatchers.IO) {
            val uuid = antrenament.syncUuid.ifEmpty { AppConstants.generateUuid() }
            val now = System.currentTimeMillis()
            val withUuid = antrenament.copy(syncUuid = uuid, updatedAt = now)
            val localId = db.antrenamentDao().insert(withUuid)

            for (ex in exercises) {
                val exUuid = ex.syncUuid.ifEmpty { AppConstants.generateUuid() }
                db.exercitiuDao().insert(ex.copy(
                    syncUuid = exUuid,
                    antrenamentId = localId,
                    updatedAt = now
                ))
            }

            try {
                api.upsertAntrenament(mapOf(
                    "uuid" to uuid,
                    "userId" to withUuid.userId,
                    "grupaMusculara" to withUuid.grupaMusculara,
                    "data" to withUuid.data,
                    "notes" to withUuid.notes,
                    "totalWeight" to withUuid.totalWeight,
                    "durationMs" to withUuid.durationMs,
                    "updatedAt" to now
                ))
                for (ex in exercises) {
                    val exUuid = ex.syncUuid.ifEmpty { AppConstants.generateUuid() }
                    api.upsertExercitiu(mapOf(
                        "uuid" to exUuid,
                        "antrenamentUuid" to uuid,
                        "numeExercitiu" to ex.numeExercitiu,
                        "exerciseId" to (ex.exerciseId.ifEmpty { exerciseIdFor(ex.numeExercitiu) }),
                        "setIndex" to ex.setIndex,
                        "greutateKg" to ex.greutateKg,
                        "repetari" to ex.repetari,
                        "setType" to ex.setType,
                        "rpe" to ex.rpe,
                        "notes" to ex.notes,
                        "updatedAt" to now
                    ))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            localId
        }
    }

    suspend fun syncAntrenamenteFromServer(userId: String): List<AntrenamentEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val since = preferencesManager.getLastSyncTimestamp("antrenamente")
                Log.d("SyncRepository", "syncAntrenamente: userId=$userId, since=$since")
                val serverData = api.syncAntrenamente(userId, since)
                Log.d("SyncRepository", "syncAntrenamente: got ${serverData.size} items from server")
                for (item in serverData) {
                    db.antrenamentDao().upsertByUuid(AntrenamentEntity(
                        syncUuid = item["uuid"] as? String ?: continue,
                        userId = item["userId"] as? String ?: userId,
                        grupaMusculara = item["grupaMusculara"] as? String ?: "",
                        data = (item["data"] as? Number)?.toLong() ?: 0L,
                        notes = item["notes"] as? String ?: "",
                        totalWeight = (item["totalWeight"] as? Number)?.toDouble() ?: 0.0,
                        durationMs = (item["durationMs"] as? Number)?.toLong() ?: 0L,
                        updatedAt = (item["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    ))
                }
                preferencesManager.setLastSyncTimestamp("antrenamente", System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            db.antrenamentDao().getAllForUser(userId)
        }
    }

    suspend fun syncExercitiiFromServer(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                val since = preferencesManager.getLastSyncTimestamp("exercitii")
                val serverData = api.syncExercitii(userId, since)
                for (item in serverData) {
                    val antrenamentUuid = item["antrenamentUuid"] as? String ?: continue
                    val antrenament = db.antrenamentDao().getByUuid(antrenamentUuid) ?: continue
                    val pulledName = item["numeExercitiu"] as? String ?: ""
                    db.exercitiuDao().upsertByUuid(ExercitiuEntity(
                        syncUuid = item["uuid"] as? String ?: continue,
                        antrenamentId = antrenament.id,
                        numeExercitiu = pulledName,
                        exerciseId = (item["exerciseId"] as? String)?.takeIf { it.isNotBlank() } ?: exerciseIdFor(pulledName),
                        setIndex = (item["setIndex"] as? Number)?.toInt() ?: 0,
                        greutateKg = (item["greutateKg"] as? Number)?.toDouble() ?: 0.0,
                        repetari = (item["repetari"] as? Number)?.toInt() ?: 0,
                        setType = (item["setType"] as? String)?.takeIf { it.isNotBlank() } ?: "working",
                        rpe = (item["rpe"] as? Number)?.toInt() ?: 0,
                        notes = item["notes"] as? String ?: "",
                        updatedAt = (item["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    ))
                }
                preferencesManager.setLastSyncTimestamp("exercitii", System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ========================================
    // EXERCISE DEFINITIONS
    // ========================================

    suspend fun syncExercisesFromServer(userId: String): List<ExerciseDefinitionEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val since = preferencesManager.getLastSyncTimestamp("exercises")
                val serverData = api.syncExercises(userId, since)
                for (item in serverData) {
                    val pulledDefName = item["name"] as? String ?: ""
                    db.exerciseDefinitionDao().upsert(ExerciseDefinitionEntity(
                        syncUuid = item["uuid"] as? String ?: continue,
                        name = pulledDefName,
                        group = item["groupName"] as? String ?: "",
                        equipment = item["equipment"] as? String ?: "",
                        exerciseId = (item["exerciseId"] as? String)?.takeIf { it.isNotBlank() } ?: exerciseIdFor(pulledDefName),
                        isDefault = (item["isDefault"] as? Number)?.toInt() == 1,
                        isFavorite = (item["isFavorite"] as? Number)?.toInt() == 1,
                        usageCount = (item["usageCount"] as? Number)?.toInt() ?: 0,
                        updatedAt = (item["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    ))
                }
                preferencesManager.setLastSyncTimestamp("exercises", System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            db.exerciseDefinitionDao().getAll()
        }
    }

    // ========================================
    // TEMPLATES + TEMPLATE EXERCISES
    // ========================================

    suspend fun saveTemplate(template: TemplateEntity, exercises: List<TemplateExerciseEntity>): Long {
        return withContext(Dispatchers.IO) {
            val uuid = template.syncUuid.ifEmpty { AppConstants.generateUuid() }
            val now = System.currentTimeMillis()
            val withUuid = template.copy(syncUuid = uuid, updatedAt = now)
            val localId = db.templateDao().insert(withUuid)

            for (ex in exercises) {
                val exUuid = ex.syncUuid.ifEmpty { AppConstants.generateUuid() }
                db.templateExerciseDao().insert(ex.copy(
                    syncUuid = exUuid,
                    templateId = localId,
                    updatedAt = now
                ))
            }

            try {
                api.upsertTemplate(mapOf(
                    "uuid" to uuid,
                    "userId" to withUuid.userId,
                    "name" to withUuid.name,
                    "updatedAt" to now
                ))
                for (ex in exercises) {
                    val exUuid = ex.syncUuid.ifEmpty { AppConstants.generateUuid() }
                    api.upsertTemplateExercise(mapOf(
                        "uuid" to exUuid,
                        "templateUuid" to uuid,
                        "exerciseName" to ex.exerciseName,
                        "exerciseId" to (ex.exerciseId.ifEmpty { exerciseIdFor(ex.exerciseName) }),
                        "groupName" to ex.group,
                        "updatedAt" to now
                    ))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            localId
        }
    }

    suspend fun syncTemplatesFromServer(userId: String): List<TemplateEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val since = preferencesManager.getLastSyncTimestamp("templates")
                val serverData = api.syncTemplates(userId, since)
                for (item in serverData) {
                    db.templateDao().upsertByUuid(TemplateEntity(
                        syncUuid = item["uuid"] as? String ?: continue,
                        userId = item["userId"] as? String ?: userId,
                        name = item["name"] as? String ?: "",
                        updatedAt = (item["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    ))
                }
                preferencesManager.setLastSyncTimestamp("templates", System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            db.templateDao().getAllForUser(userId)
        }
    }

    suspend fun syncTemplateExercisesFromServer(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                val since = preferencesManager.getLastSyncTimestamp("template_exercises")
                val serverData = api.syncTemplateExercises(userId, since)
                for (item in serverData) {
                    val templateUuid = item["templateUuid"] as? String ?: continue
                    val template = db.templateDao().getByUuid(templateUuid) ?: continue
                    val pulledTplName = item["exerciseName"] as? String ?: ""
                    db.templateExerciseDao().upsertByUuid(TemplateExerciseEntity(
                        syncUuid = item["uuid"] as? String ?: continue,
                        templateId = template.id,
                        exerciseName = pulledTplName,
                        exerciseId = (item["exerciseId"] as? String)?.takeIf { it.isNotBlank() } ?: exerciseIdFor(pulledTplName),
                        group = item["groupName"] as? String ?: "",
                        updatedAt = (item["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    ))
                }
                preferencesManager.setLastSyncTimestamp("template_exercises", System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ========================================
    // PERSONAL RECORDS
    // ========================================

    suspend fun savePersonalRecord(pr: PersonalRecordEntity) {
        withContext(Dispatchers.IO) {
            val uuid = pr.syncUuid.ifEmpty { AppConstants.generateUuid() }
            val now = System.currentTimeMillis()
            val withUuid = pr.copy(syncUuid = uuid, updatedAt = now)
            db.personalRecordDao().upsert(withUuid)

            try {
                api.upsertPersonalRecord(mapOf(
                    "uuid" to uuid,
                    "userId" to withUuid.userId,
                    "exerciseName" to withUuid.exerciseName,
                    "exerciseId" to (withUuid.exerciseId.ifEmpty { exerciseIdFor(withUuid.exerciseName) }),
                    "weight" to withUuid.weight,
                    "reps" to withUuid.reps,
                    "volume" to withUuid.volume,
                    "date" to withUuid.date,
                    "updatedAt" to now
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun syncPersonalRecordsFromServer(userId: String): List<PersonalRecordEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val since = preferencesManager.getLastSyncTimestamp("personal_records")
                val serverData = api.syncPersonalRecords(userId, since)
                for (item in serverData) {
                    val pulledPrName = item["exerciseName"] as? String ?: ""
                    db.personalRecordDao().upsert(PersonalRecordEntity(
                        syncUuid = item["uuid"] as? String ?: continue,
                        userId = item["userId"] as? String ?: userId,
                        exerciseName = pulledPrName,
                        exerciseId = (item["exerciseId"] as? String)?.takeIf { it.isNotBlank() } ?: exerciseIdFor(pulledPrName),
                        weight = (item["weight"] as? Number)?.toDouble() ?: 0.0,
                        reps = (item["reps"] as? Number)?.toInt() ?: 0,
                        volume = (item["volume"] as? Number)?.toDouble() ?: 0.0,
                        date = (item["date"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        updatedAt = (item["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    ))
                }
                preferencesManager.setLastSyncTimestamp("personal_records", System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            db.personalRecordDao().getAllForUser(userId)
        }
    }

    // ========================================
    // MUSCLE RECOVERY
    // ========================================

    suspend fun saveMuscleRecovery(recovery: MuscleRecoveryEntity) {
        withContext(Dispatchers.IO) {
            val uuid = recovery.syncUuid.ifEmpty { AppConstants.generateUuid() }
            val now = System.currentTimeMillis()
            val withUuid = recovery.copy(syncUuid = uuid, updatedAt = now)
            db.muscleRecoveryDao().upsert(withUuid)

            try {
                api.upsertMuscleRecovery(mapOf(
                    "uuid" to uuid,
                    "userId" to withUuid.userId,
                    "grupaMusculara" to withUuid.grupaMusculara,
                    "level" to withUuid.level,
                    "lastUpdated" to withUuid.lastUpdated,
                    "updatedAt" to now
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun syncMuscleRecoveryFromServer(userId: String): List<MuscleRecoveryEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val since = preferencesManager.getLastSyncTimestamp("muscle_recovery")
                val serverData = api.syncMuscleRecovery(userId, since)
                for (item in serverData) {
                    db.muscleRecoveryDao().upsert(MuscleRecoveryEntity(
                        grupaMusculara = item["grupaMusculara"] as? String ?: continue,
                        userId = item["userId"] as? String ?: userId,
                        level = (item["level"] as? Number)?.toDouble() ?: 0.0,
                        lastUpdated = (item["lastUpdated"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        syncUuid = item["uuid"] as? String ?: AppConstants.generateUuid(),
                        updatedAt = (item["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    ))
                }
                preferencesManager.setLastSyncTimestamp("muscle_recovery", System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            db.muscleRecoveryDao().getAll(userId)
        }
    }

    // ========================================
    // EXERCISE METADATA
    // ========================================

    suspend fun saveExerciseMetadata(metadata: ExerciseMetadataEntity) {
        withContext(Dispatchers.IO) {
            val uuid = metadata.syncUuid.ifEmpty { AppConstants.generateUuid() }
            val now = System.currentTimeMillis()
            val withUuid = metadata.copy(syncUuid = uuid, updatedAt = now)
            db.exerciseMetadataDao().upsert(withUuid)

            try {
                api.upsertExerciseMetadata(mapOf(
                    "uuid" to uuid,
                    "userId" to withUuid.userId,
                    "exerciseName" to withUuid.exerciseName,
                    "exerciseId" to (withUuid.exerciseId.ifEmpty { exerciseIdFor(withUuid.exerciseName) }),
                    "grupaMusculara" to withUuid.grupaMusculara,
                    "isFavorite" to withUuid.isFavorite,
                    "isCustom" to withUuid.isCustom,
                    "updatedAt" to now
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun syncExerciseMetadataFromServer(userId: String): List<ExerciseMetadataEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val since = preferencesManager.getLastSyncTimestamp("exercise_metadata")
                val serverData = api.syncExerciseMetadata(userId, since)
                for (item in serverData) {
                    val pulledMetaName = item["exerciseName"] as? String ?: continue
                    db.exerciseMetadataDao().upsert(ExerciseMetadataEntity(
                        exerciseName = pulledMetaName,
                        userId = item["userId"] as? String ?: userId,
                        exerciseId = (item["exerciseId"] as? String)?.takeIf { it.isNotBlank() } ?: exerciseIdFor(pulledMetaName),
                        grupaMusculara = item["grupaMusculara"] as? String ?: "",
                        isFavorite = item["isFavorite"] as? Boolean ?: (item["isFavorite"] as? Number)?.toInt() == 1,
                        isCustom = item["isCustom"] as? Boolean ?: (item["isCustom"] as? Number)?.toInt() == 1,
                        syncUuid = item["uuid"] as? String ?: AppConstants.generateUuid(),
                        updatedAt = (item["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    ))
                }
                preferencesManager.setLastSyncTimestamp("exercise_metadata", System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            db.exerciseMetadataDao().getAll(userId)
        }
    }

    // ========================================
    // BIOMETRIC ENTRIES
    // ========================================

    suspend fun saveBiometricEntry(entry: BiometricEntity): Long {
        return withContext(Dispatchers.IO) {
            val uuid = entry.syncUuid.ifEmpty { AppConstants.generateUuid() }
            val now = System.currentTimeMillis()
            val withUuid = entry.copy(syncUuid = uuid, updatedAt = now)
            val localId = db.biometricDao().insert(withUuid)

            try {
                api.upsertBiometricEntry(mapOf(
                    "uuid" to uuid,
                    "userId" to withUuid.userId,
                    "timestamp" to withUuid.timestamp,
                    "weightKg" to withUuid.weightKg,
                    "bodyFatPercent" to withUuid.bodyFatPercent,
                    "waistCm" to withUuid.waistCm,
                    "hipsCm" to withUuid.hipsCm,
                    "thighsCm" to withUuid.thighsCm,
                    "chestCm" to withUuid.chestCm,
                    "armsCm" to withUuid.armsCm,
                    "notes" to withUuid.notes,
                    "updatedAt" to now
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            localId
        }
    }

    suspend fun syncBiometricFromServer(userId: String): List<BiometricEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val since = preferencesManager.getLastSyncTimestamp("biometric_entries")
                val serverData = api.syncBiometricEntries(userId, since)
                for (item in serverData) {
                    db.biometricDao().upsertByUuid(BiometricEntity(
                        syncUuid = item["uuid"] as? String ?: continue,
                        userId = item["userId"] as? String ?: userId,
                        timestamp = (item["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        weightKg = (item["weightKg"] as? Number)?.toDouble() ?: 0.0,
                        bodyFatPercent = (item["bodyFatPercent"] as? Number)?.toDouble() ?: 0.0,
                        waistCm = (item["waistCm"] as? Number)?.toDouble() ?: 0.0,
                        hipsCm = (item["hipsCm"] as? Number)?.toDouble() ?: 0.0,
                        thighsCm = (item["thighsCm"] as? Number)?.toDouble() ?: 0.0,
                        chestCm = (item["chestCm"] as? Number)?.toDouble() ?: 0.0,
                        armsCm = (item["armsCm"] as? Number)?.toDouble() ?: 0.0,
                        notes = item["notes"] as? String ?: "",
                        updatedAt = (item["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    ))
                }
                preferencesManager.setLastSyncTimestamp("biometric_entries", System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            db.biometricDao().getAllForUser(userId)
        }
    }

    // ========================================
    // FOOD ENTRIES
    // ========================================

    suspend fun saveFoodEntry(entry: FoodEntity): Long {
        return withContext(Dispatchers.IO) {
            val uuid = entry.syncUuid.ifEmpty { AppConstants.generateUuid() }
            val now = System.currentTimeMillis()
            val withUuid = entry.copy(syncUuid = uuid, updatedAt = now)
            val localId = db.foodDao().insert(withUuid)

            try {
                api.upsertFoodEntry(mapOf(
                    "uuid" to uuid,
                    "userId" to withUuid.userId,
                    "barcode" to withUuid.barcode,
                    "name" to withUuid.name,
                    "brand" to withUuid.brand,
                    "mealType" to withUuid.mealType,
                    "servingSize" to withUuid.servingSize,
                    "servingUnit" to withUuid.servingUnit,
                    "calories" to withUuid.calories,
                    "proteinG" to withUuid.proteinG,
                    "carbsG" to withUuid.carbsG,
                    "fatG" to withUuid.fatG,
                    "fiberG" to withUuid.fiberG,
                    "timestamp" to withUuid.timestamp,
                    "updatedAt" to now
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            localId
        }
    }

    suspend fun syncFoodFromServer(userId: String): List<FoodEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val since = preferencesManager.getLastSyncTimestamp("food_entries")
                val serverData = api.syncFoodEntries(userId, since)
                for (item in serverData) {
                    db.foodDao().upsertByUuid(FoodEntity(
                        syncUuid = item["uuid"] as? String ?: continue,
                        userId = item["userId"] as? String ?: userId,
                        barcode = item["barcode"] as? String ?: "",
                        name = item["name"] as? String ?: "",
                        brand = item["brand"] as? String ?: "",
                        mealType = item["mealType"] as? String ?: "snack",
                        servingSize = (item["servingSize"] as? Number)?.toDouble() ?: 100.0,
                        servingUnit = item["servingUnit"] as? String ?: "g",
                        calories = (item["calories"] as? Number)?.toDouble() ?: 0.0,
                        proteinG = (item["proteinG"] as? Number)?.toDouble() ?: 0.0,
                        carbsG = (item["carbsG"] as? Number)?.toDouble() ?: 0.0,
                        fatG = (item["fatG"] as? Number)?.toDouble() ?: 0.0,
                        fiberG = (item["fiberG"] as? Number)?.toDouble() ?: 0.0,
                        timestamp = (item["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        updatedAt = (item["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    ))
                }
                preferencesManager.setLastSyncTimestamp("food_entries", System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            db.foodDao().getForDay(userId, 0, System.currentTimeMillis())
        }
    }

    // ========================================
    // CARDIO ROUTES
    // ========================================

    suspend fun saveCardioRoute(route: CardioRouteEntity): Long {
        return withContext(Dispatchers.IO) {
            val uuid = route.syncUuid.ifEmpty { AppConstants.generateUuid() }
            val now = System.currentTimeMillis()
            val withUuid = route.copy(syncUuid = uuid, updatedAt = now)
            val localId = db.cardioRouteDao().insert(withUuid)

            try {
                api.upsertCardioRoute(mapOf(
                    "uuid" to uuid,
                    "userId" to withUuid.userId,
                    "name" to withUuid.name,
                    "routePoints" to withUuid.routePoints,
                    "distanceKm" to withUuid.distanceKm,
                    "durationMs" to withUuid.durationMs,
                    "avgSpeedKmh" to withUuid.avgSpeedKmh,
                    "avgPaceMinKm" to withUuid.avgPaceMinKm,
                    "caloriesBurned" to withUuid.caloriesBurned,
                    "startTime" to withUuid.startTime,
                    "endTime" to withUuid.endTime,
                    "activityType" to withUuid.activityType,
                    "updatedAt" to now
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            localId
        }
    }

    suspend fun syncCardioFromServer(userId: String): List<CardioRouteEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val since = preferencesManager.getLastSyncTimestamp("cardio_routes")
                val serverData = api.syncCardioRoutes(userId, since)
                for (item in serverData) {
                    db.cardioRouteDao().upsertByUuid(CardioRouteEntity(
                        syncUuid = item["uuid"] as? String ?: continue,
                        userId = item["userId"] as? String ?: userId,
                        name = item["name"] as? String ?: "",
                        routePoints = item["routePoints"] as? String ?: "",
                        distanceKm = (item["distanceKm"] as? Number)?.toDouble() ?: 0.0,
                        durationMs = (item["durationMs"] as? Number)?.toLong() ?: 0L,
                        avgSpeedKmh = (item["avgSpeedKmh"] as? Number)?.toDouble() ?: 0.0,
                        avgPaceMinKm = (item["avgPaceMinKm"] as? Number)?.toDouble() ?: 0.0,
                        caloriesBurned = (item["caloriesBurned"] as? Number)?.toDouble() ?: 0.0,
                        startTime = (item["startTime"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        endTime = (item["endTime"] as? Number)?.toLong() ?: 0L,
                        activityType = item["activityType"] as? String ?: "running",
                        updatedAt = (item["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    ))
                }
                preferencesManager.setLastSyncTimestamp("cardio_routes", System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            db.cardioRouteDao().getAllForUser(userId)
        }
    }

    // ========================================
    // REST DAYS
    // ========================================

    suspend fun saveRestDay(restDay: RestDayEntity): Long {
        return withContext(Dispatchers.IO) {
            val uuid = restDay.syncUuid.ifEmpty { AppConstants.generateUuid() }
            val now = System.currentTimeMillis()
            val withUuid = restDay.copy(syncUuid = uuid, updatedAt = now)
            val localId = db.restDayDao().insert(withUuid)

            try {
                api.upsertRestDay(mapOf(
                    "uuid" to uuid,
                    "userId" to withUuid.userId,
                    "date" to withUuid.date,
                    "type" to withUuid.type,
                    "notes" to withUuid.notes,
                    "activities" to withUuid.activities,
                    "completed" to withUuid.completed,
                    "updatedAt" to now
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            localId
        }
    }

    suspend fun syncRestDaysFromServer(userId: String): List<RestDayEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val since = preferencesManager.getLastSyncTimestamp("rest_days")
                val serverData = api.syncRestDays(userId, since)
                for (item in serverData) {
                    db.restDayDao().upsertByUuid(RestDayEntity(
                        syncUuid = item["uuid"] as? String ?: continue,
                        userId = item["userId"] as? String ?: userId,
                        date = (item["date"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        type = item["type"] as? String ?: "rest",
                        notes = item["notes"] as? String ?: "",
                        activities = item["activities"] as? String ?: "",
                        completed = (item["completed"] as? Number)?.toInt() == 1,
                        updatedAt = (item["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    ))
                }
                preferencesManager.setLastSyncTimestamp("rest_days", System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            db.restDayDao().getAllForUser(userId)
        }
    }

    // ========================================
    // AI CHAT HISTORY
    // ========================================

    suspend fun saveAiChatMessage(message: AiChatHistoryEntity): Long {
        return withContext(Dispatchers.IO) {
            val uuid = message.syncUuid.ifEmpty { AppConstants.generateUuid() }
            val now = System.currentTimeMillis()
            val withUuid = message.copy(syncUuid = uuid, updatedAt = now)
            val localId = db.aiChatHistoryDao().insert(withUuid)

            try {
                api.upsertAiChatHistory(mapOf(
                    "uuid" to uuid,
                    "userId" to withUuid.userId,
                    "sessionId" to withUuid.sessionId,
                    "role" to withUuid.role,
                    "message" to withUuid.message,
                    "timestamp" to withUuid.timestamp,
                    "updatedAt" to now
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            localId
        }
    }

    // ========================================
    // SUBSCRIPTIONS
    // ========================================

    suspend fun saveSubscription(subscription: SubscriptionEntity) {
        withContext(Dispatchers.IO) {
            val uuid = subscription.syncUuid.ifEmpty { AppConstants.generateUuid() }
            val now = System.currentTimeMillis()
            val withUuid = subscription.copy(syncUuid = uuid, updatedAt = now)
            db.subscriptionDao().upsert(withUuid)

            try {
                api.upsertSubscription(mapOf(
                    "uuid" to uuid,
                    "userId" to withUuid.userId,
                    "provider" to withUuid.provider,
                    "subscriptionId" to withUuid.subscriptionId,
                    "planId" to withUuid.planId,
                    "status" to withUuid.status,
                    "currentPeriodEnd" to withUuid.currentPeriodEnd,
                    "createdAt" to withUuid.createdAt,
                    "updatedAt" to now
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ========================================
    // FULL SYNC
    // ========================================

    suspend fun syncAllFromServer(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                syncAntrenamenteFromServer(userId)
                syncExercitiiFromServer(userId)
                syncExercisesFromServer(userId)
                syncTemplatesFromServer(userId)
                syncTemplateExercisesFromServer(userId)
                syncPersonalRecordsFromServer(userId)
                syncMuscleRecoveryFromServer(userId)
                syncExerciseMetadataFromServer(userId)
                syncBiometricFromServer(userId)
                syncFoodFromServer(userId)
                syncCardioFromServer(userId)
                syncRestDaysFromServer(userId)
            } catch (e: Exception) {
                MessagesHelper.addSyncFailed(db.messageDao())
                e.printStackTrace()
            }
        }
    }

    suspend fun pushAllToServer(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                val unsyncedRecoveries = db.muscleRecoveryDao().getUnsynced().filter { it.userId == userId }
                Log.d("SyncRepository", "pushAllToServer: unsynced recoveries=${unsyncedRecoveries.size}")
                for (recovery in unsyncedRecoveries) {
                    saveMuscleRecovery(recovery)
                }
                val unsyncedMetadata = db.exerciseMetadataDao().getUnsynced().filter { it.userId == userId }
                Log.d("SyncRepository", "pushAllToServer: unsynced metadata=${unsyncedMetadata.size}")
                for (metadata in unsyncedMetadata) {
                    saveExerciseMetadata(metadata)
                }

                val workouts = db.antrenamentDao().getUnsynced().filter { it.userId == userId }
                Log.d("SyncRepository", "pushAllToServer: unsynced workouts=${workouts.size}")
                for (w in workouts) {
                    val exercises = db.exercitiuDao().getForAntrenament(w.id)
                    val uuid = w.syncUuid.ifEmpty { AppConstants.generateUuid() }
                    val now = System.currentTimeMillis()
                    db.antrenamentDao().upsertByUuid(w.copy(syncUuid = uuid, updatedAt = now))
                    api.upsertAntrenament(mapOf(
                        "uuid" to uuid, "userId" to w.userId, "grupaMusculara" to w.grupaMusculara,
                        "data" to w.data, "notes" to w.notes, "totalWeight" to w.totalWeight, "updatedAt" to now
                    ))
                    for (ex in exercises) {
                        val exUuid = ex.syncUuid.ifEmpty { AppConstants.generateUuid() }
                        db.exercitiuDao().upsertByUuid(ex.copy(syncUuid = exUuid, updatedAt = now))
                        api.upsertExercitiu(mapOf(
                            "uuid" to exUuid, "antrenamentUuid" to uuid, "numeExercitiu" to ex.numeExercitiu,
                            "setIndex" to ex.setIndex, "greutateKg" to ex.greutateKg, "repetari" to ex.repetari,
                            "notes" to ex.notes, "updatedAt" to now
                        ))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * When the user upgrades from a guest/anonymous account to a real account
     * (Google, email, Facebook), the Firebase UID changes. All local data was
     * stored under the old UID. This method reassigns every row to the new
     * userId so that [initialSync] can push & pull correctly.
     */
    suspend fun migrateLocalDataToNewUser(oldUserId: String, newUserId: String) {
        Log.d("SyncRepository", "migrateLocalDataToNewUser: $oldUserId -> $newUserId")
        if (oldUserId == newUserId || oldUserId.isBlank() || oldUserId == "local_user") {
            Log.d("SyncRepository", "migrateLocalDataToNewUser: skipped (same or invalid)")
            return
        }
        withContext(Dispatchers.IO) {
            try {
                val sql = db.openHelper.writableDatabase
                val tables = listOf(
                    "antrenamente", "templates", "personal_records",
                    "muscle_recovery", "exercise_metadata",
                    "biometric_entries", "food_entries",
                    "cardio_routes", "rest_days",
                    "ai_chat_history", "subscriptions",
                    "weight_goals", "injury_risks",
                    "friendships", "streaks", "user_badges",
                    "leaderboard_entries", "likes", "comments"
                )
                for (table in tables) {
                    try {
                        sql.execSQL("UPDATE $table SET userId = '$newUserId' WHERE userId = '$oldUserId'")
                    } catch (_: Exception) {
                        // Table may not have a userId column — skip silently
                    }
                }
                // Reset sync timestamps so initialSync re-fetches everything fresh
                preferencesManager.setLastSyncTimestamp("antrenamente", 0L)
                preferencesManager.setLastSyncTimestamp("exercitii", 0L)
                preferencesManager.setLastSyncTimestamp("exercises", 0L)
                preferencesManager.setLastSyncTimestamp("templates", 0L)
                preferencesManager.setLastSyncTimestamp("template_exercises", 0L)
                preferencesManager.setLastSyncTimestamp("personal_records", 0L)
                preferencesManager.setLastSyncTimestamp("muscle_recovery", 0L)
                preferencesManager.setLastSyncTimestamp("exercise_metadata", 0L)
                preferencesManager.setLastSyncTimestamp("biometric_entries", 0L)
                preferencesManager.setLastSyncTimestamp("food_entries", 0L)
                preferencesManager.setLastSyncTimestamp("cardio_routes", 0L)
                preferencesManager.setLastSyncTimestamp("rest_days", 0L)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun initialSync(userId: String) {
        Log.d("SyncRepository", "initialSync: starting for userId=$userId")
        pushAllToServer(userId)
        Log.d("SyncRepository", "initialSync: pushAllToServer done, starting syncAllFromServer")
        syncAllFromServer(userId)
        Log.d("SyncRepository", "initialSync: syncAllFromServer done")
        // Sincronizează statisticile agregate (volum + nr. antrenamente) în tabela
        // users — altfel prietenii și leaderboard-ul arată mereu 0, pentru că acest
        // calcul se făcea doar la salvarea unui antrenament nou / login.
        try {
            val totalVolume = db.antrenamentDao().sumVolumeForUser(userId)
            val workoutCount = db.antrenamentDao().countForUser(userId)
            if (totalVolume > 0 || workoutCount > 0) {
                api.upsertUser(mapOf(
                    "id" to userId,
                    "totalVolume" to totalVolume.toString(),
                    "workoutCount" to workoutCount.toString()
                ))
            }
        } catch (_: Exception) {}
    }

    /**
     * Clears syncUuid on all local tables so pushAllToServer re-pushes
     * everything. Safe because backend uses ON CONFLICT DO UPDATE (upsert).
     * Call this on login to handle server data loss (e.g. Render redeploy).
     */
    suspend fun forceResetSyncState(userId: String) {
        Log.d("SyncRepository", "forceResetSyncState: clearing syncUuids for userId=$userId")
        withContext(Dispatchers.IO) {
            try {
                // Use DAO methods (not raw SQL) so Room's in-memory cache is invalidated
                db.antrenamentDao().clearAllSyncUuids()
                db.exercitiuDao().clearAllSyncUuids()
                db.exerciseDefinitionDao().clearAllSyncUuids()
                db.templateDao().clearAllSyncUuids()
                db.templateExerciseDao().clearAllSyncUuids()
                db.personalRecordDao().clearAllSyncUuids()
                db.muscleRecoveryDao().clearAllSyncUuids()
                db.biometricDao().clearAllSyncUuids()
                db.foodDao().clearAllSyncUuids()
                db.cardioRouteDao().clearAllSyncUuids()
                db.restDayDao().clearAllSyncUuids()
                db.aiChatHistoryDao().clearAllSyncUuids()
                db.exerciseMetadataDao().clearAllSyncUuids()
                Log.d("SyncRepository", "forceResetSyncState: done")
            } catch (e: Exception) {
                Log.e("SyncRepository", "forceResetSyncState failed", e)
            }
        }
    }
}
