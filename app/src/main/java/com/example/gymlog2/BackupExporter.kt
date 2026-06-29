package com.example.gymlog2

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object BackupExporter {

    suspend fun exportToJson(context: Context, db: AppDatabase, userId: String): String {
        return withContext(Dispatchers.IO) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val json = JSONObject()

            json.put("version", 1)
            json.put("exportDate", dateFormat.format(Date()))
            json.put("userId", userId)

            val workouts = db.antrenamentDao().getAllForUser(userId)
            val workoutsArr = JSONArray()
            for (w in workouts) {
                val exercises = db.exercitiuDao().getForAntrenament(w.id)
                val wObj = JSONObject().apply {
                    put("grupaMusculara", w.grupaMusculara)
                    put("data", w.data)
                    put("notes", w.notes)
                    put("totalWeight", w.totalWeight)
                    val exArr = JSONArray()
                    for (ex in exercises) {
                        exArr.put(JSONObject().apply {
                            put("numeExercitiu", ex.numeExercitiu)
                            put("setIndex", ex.setIndex)
                            put("greutateKg", ex.greutateKg)
                            put("repetari", ex.repetari)
                            put("notes", ex.notes)
                        })
                    }
                    put("exercises", exArr)
                }
                workoutsArr.put(wObj)
            }
            json.put("workouts", workoutsArr)

            val prs = db.personalRecordDao().getAllForUser(userId)
            val prsArr = JSONArray()
            for (pr in prs) {
                prsArr.put(JSONObject().apply {
                    put("exerciseName", pr.exerciseName)
                    put("weight", pr.weight)
                    put("reps", pr.reps)
                    put("volume", pr.volume)
                    put("date", pr.date)
                })
            }
            json.put("personalRecords", prsArr)

            val templates = db.templateDao().getAllForUser(userId)
            val templatesArr = JSONArray()
            for (t in templates) {
                val tExercises = db.templateExerciseDao().getForTemplate(t.id)
                val tObj = JSONObject().apply {
                    put("name", t.name)
                    val exArr = JSONArray()
                    for (ex in tExercises) {
                        exArr.put(JSONObject().apply {
                            put("exerciseName", ex.exerciseName)
                            put("group", ex.group)
                        })
                    }
                    put("exercises", exArr)
                }
                templatesArr.put(tObj)
            }
            json.put("templates", templatesArr)

            val biometrics = db.biometricDao().getAllForUser(userId)
            val bioArr = JSONArray()
            for (b in biometrics) {
                bioArr.put(JSONObject().apply {
                    put("timestamp", b.timestamp)
                    put("weightKg", b.weightKg)
                    put("bodyFatPercent", b.bodyFatPercent)
                    put("waistCm", b.waistCm)
                    put("hipsCm", b.hipsCm)
                    put("thighsCm", b.thighsCm)
                    put("chestCm", b.chestCm)
                    put("armsCm", b.armsCm)
                    put("notes", b.notes)
                })
            }
            json.put("biometrics", bioArr)

            val food = db.foodDao().getForDay(userId, 0, System.currentTimeMillis())
            val foodArr = JSONArray()
            for (f in food) {
                foodArr.put(JSONObject().apply {
                    put("barcode", f.barcode)
                    put("name", f.name)
                    put("brand", f.brand)
                    put("mealType", f.mealType)
                    put("servingSize", f.servingSize)
                    put("servingUnit", f.servingUnit)
                    put("calories", f.calories)
                    put("proteinG", f.proteinG)
                    put("carbsG", f.carbsG)
                    put("fatG", f.fatG)
                    put("fiberG", f.fiberG)
                    put("timestamp", f.timestamp)
                })
            }
            json.put("foodEntries", foodArr)

            val cardio = db.cardioRouteDao().getAllForUser(userId)
            val cardioArr = JSONArray()
            for (c in cardio) {
                cardioArr.put(JSONObject().apply {
                    put("name", c.name)
                    put("routePoints", c.routePoints)
                    put("distanceKm", c.distanceKm)
                    put("durationMs", c.durationMs)
                    put("avgSpeedKmh", c.avgSpeedKmh)
                    put("caloriesBurned", c.caloriesBurned)
                    put("startTime", c.startTime)
                    put("endTime", c.endTime)
                    put("activityType", c.activityType)
                })
            }
            json.put("cardioRoutes", cardioArr)

            val restDays = db.restDayDao().getAllForUser(userId)
            val restArr = JSONArray()
            for (r in restDays) {
                restArr.put(JSONObject().apply {
                    put("date", r.date)
                    put("type", r.type)
                    put("notes", r.notes)
                    put("activities", r.activities)
                    put("completed", r.completed)
                })
            }
            json.put("restDays", restArr)

            json.toString(2)
        }
    }

    suspend fun importFromJson(context: Context, db: AppDatabase, json: String, userId: String): ImportResult {
        return withContext(Dispatchers.IO) {
            var workoutsImported = 0
            var prsImported = 0
            var templatesImported = 0
            var bioImported = 0

            try {
                val root = JSONObject(json)

                if (root.has("workouts")) {
                    val workoutsArr = root.getJSONArray("workouts")
                    for (i in 0 until workoutsArr.length()) {
                        val wObj = workoutsArr.getJSONObject(i)
                        val antrenamentId = db.antrenamentDao().insert(
                            AntrenamentEntity(
                                userId = userId,
                                grupaMusculara = wObj.getString("grupaMusculara"),
                                data = wObj.getLong("data"),
                                notes = wObj.optString("notes", ""),
                                totalWeight = wObj.optDouble("totalWeight", 0.0)
                            )
                        )
                        val exercises = wObj.getJSONArray("exercises")
                        for (j in 0 until exercises.length()) {
                            val exObj = exercises.getJSONObject(j)
                            db.exercitiuDao().insert(
                                ExercitiuEntity(
                                    antrenamentId = antrenamentId,
                                    numeExercitiu = exObj.getString("numeExercitiu"),
                                    setIndex = exObj.optInt("setIndex", 0),
                                    greutateKg = exObj.optDouble("greutateKg", 0.0),
                                    repetari = exObj.optInt("repetari", 0),
                                    notes = exObj.optString("notes", "")
                                )
                            )
                        }
                        workoutsImported++
                    }
                }

                if (root.has("personalRecords")) {
                    val prsArr = root.getJSONArray("personalRecords")
                    for (i in 0 until prsArr.length()) {
                        val prObj = prsArr.getJSONObject(i)
                        db.personalRecordDao().upsert(
                            PersonalRecordEntity(
                                userId = userId,
                                exerciseName = prObj.getString("exerciseName"),
                                weight = prObj.getDouble("weight"),
                                reps = prObj.getInt("reps"),
                                volume = prObj.optDouble("volume", 0.0),
                                date = prObj.optLong("date", System.currentTimeMillis())
                            )
                        )
                        prsImported++
                    }
                }

                if (root.has("templates")) {
                    val tArr = root.getJSONArray("templates")
                    for (i in 0 until tArr.length()) {
                        val tObj = tArr.getJSONObject(i)
                        val templateId = db.templateDao().insert(
                            TemplateEntity(userId = userId, name = tObj.getString("name"))
                        )
                        val exArr = tObj.getJSONArray("exercises")
                        for (j in 0 until exArr.length()) {
                            val exObj = exArr.getJSONObject(j)
                            db.templateExerciseDao().insert(
                                TemplateExerciseEntity(
                                    templateId = templateId,
                                    exerciseName = exObj.getString("exerciseName"),
                                    group = exObj.getString("group")
                                )
                            )
                        }
                        templatesImported++
                    }
                }

                if (root.has("biometrics")) {
                    val bioArr = root.getJSONArray("biometrics")
                    for (i in 0 until bioArr.length()) {
                        val bObj = bioArr.getJSONObject(i)
                        db.biometricDao().insert(
                            BiometricEntity(
                                userId = userId,
                                timestamp = bObj.getLong("timestamp"),
                                weightKg = bObj.optDouble("weightKg", 0.0),
                                bodyFatPercent = bObj.optDouble("bodyFatPercent", 0.0),
                                waistCm = bObj.optDouble("waistCm", 0.0),
                                hipsCm = bObj.optDouble("hipsCm", 0.0),
                                thighsCm = bObj.optDouble("thighsCm", 0.0),
                                chestCm = bObj.optDouble("chestCm", 0.0),
                                armsCm = bObj.optDouble("armsCm", 0.0),
                                notes = bObj.optString("notes", "")
                            )
                        )
                        bioImported++
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            ImportResult(workoutsImported, prsImported, templatesImported, bioImported)
        }
    }

    data class ImportResult(
        val workouts: Int,
        val personalRecords: Int,
        val templates: Int,
        val biometrics: Int
    )

    fun shareBackup(context: Context, json: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, json)
            putExtra(Intent.EXTRA_SUBJECT, "Kinetic Backup")
        }
        context.startActivity(Intent.createChooser(intent, "Export backup"))
    }
}
