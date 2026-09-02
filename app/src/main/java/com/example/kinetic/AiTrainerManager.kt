package com.example.kinetic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AiTrainerManager(private val db: AppDatabase) {

    companion object {
        var serverUrl: String = "https://ai-server-7tqx.onrender.com"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun getWorkoutContext(userId: String, preferencesManager: PreferencesManager): String = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()

        val profile = preferencesManager.getOnboardingProfile()
        val weight = preferencesManager.getUserWeight()
        val height = preferencesManager.getUserHeight()

        cal.add(Calendar.DAY_OF_YEAR, -30)
        val monthAgo = cal.timeInMillis
        val recentWorkouts = db.antrenamentDao().getWorkoutsInPeriod(userId, monthAgo, System.currentTimeMillis())

        cal.timeInMillis = System.currentTimeMillis()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayStart = cal.timeInMillis
        cal.timeInMillis = todayStart
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val weekStart = cal.timeInMillis
        val weekWorkouts = db.antrenamentDao().getWorkoutsInPeriod(userId, weekStart, System.currentTimeMillis())

        val weekVolume = db.antrenamentDao().getTotalVolume(userId, weekStart, System.currentTimeMillis()) ?: 0.0
        cal.timeInMillis = todayStart
        cal.add(Calendar.MONTH, -1)
        val monthVolume = db.antrenamentDao().getTotalVolume(userId, cal.timeInMillis, System.currentTimeMillis()) ?: 0.0

        val recovery = AntrenamentRepository(db).getToateRecuperarile(userId)
        val prs = db.personalRecordDao().getAllForUser(userId).take(10)
        val topExercise = db.exercitiuDao().getMostFrequentExercise(userId, weekStart, System.currentTimeMillis())

        StringBuilder().apply {
            appendLine("=== USER PROFILE ===")
            appendLine("Goal: ${profile.goal.ifBlank { "not set" }}")
            appendLine("Experience: ${profile.experience.ifBlank { "not set" }}")
            appendLine("Equipment: ${profile.equipment.ifBlank { "not set" }}")
            appendLine("Sessions/week target: ${profile.sessionsPerWeek}")
            appendLine("Limitations: ${profile.limitations.ifBlank { "none" }}")
            appendLine("Weight: ${weight}kg, Height: ${height}cm")
            appendLine()
            appendLine("=== THIS WEEK ===")
            appendLine("Workouts this week: ${weekWorkouts.size}/${profile.sessionsPerWeek}")
            appendLine("Weekly volume: ${String.format(java.util.Locale.ROOT, "%.0f", weekVolume)} kg")
            if (topExercise != null) {
                appendLine("Most trained exercise: ${topExercise.numeExercitiu} (${topExercise.cnt}x)")
            }
            appendLine()
            appendLine("=== LAST 30 DAYS ===")
            appendLine("Total workouts: ${recentWorkouts.size}")
            appendLine("Monthly volume: ${String.format(java.util.Locale.ROOT, "%.0f", monthVolume)} kg")
            val groups = recentWorkouts.groupBy { it.grupaMusculara }
            appendLine("Muscle groups trained: ${groups.keys.joinToString(", ")}")
            appendLine()
            appendLine("=== MUSCLE RECOVERY (0=fresh, 1=fatigued) ===")
            recovery.forEach { (group, level) ->
                val status = when {
                    level < 0.2 -> "Ready"
                    level < 0.5 -> "Moderate"
                    level < 0.8 -> "Tired"
                    else -> "Needs rest"
                }
                appendLine("$group: ${String.format(java.util.Locale.ROOT, "%.0f", level * 100)}% - $status")
            }
            appendLine()
            if (prs.isNotEmpty()) {
                appendLine("=== PERSONAL RECORDS ===")
                prs.forEach { pr ->
                    appendLine("${pr.exerciseName}: ${pr.weight}kg x ${pr.reps} reps (${dateFormat.format(Date(pr.date))})")
                }
                appendLine()
            }
            if (recentWorkouts.isNotEmpty()) {
                appendLine("=== RECENT WORKOUTS ===")
                recentWorkouts.take(5).forEach { workout ->
                    val exercises = db.exercitiuDao().getForAntrenament(workout.id)
                    appendLine("${dateFormat.format(Date(workout.data))} - ${workout.grupaMusculara}: ${exercises.size} exercises, ${String.format(java.util.Locale.ROOT, "%.0f", workout.totalWeight)} kg total")
                }
            }
        }.toString()
    }

    suspend fun chat(
        userId: String,
        message: String,
        conversationHistory: List<Pair<String, String>>,
        preferencesManager: PreferencesManager
    ): String = withContext(Dispatchers.IO) {
        try {
            val context = getWorkoutContext(userId, preferencesManager)

            val systemPrompt = """You are an expert AI personal trainer called "Kinetic Trainer" with deep knowledge of exercise science, anatomy, sports-specific training, weight loss, and health. You help users with their fitness journey.

You have access to the user's workout data, recovery status, personal records, and profile. Use this data to give personalized, actionable advice.

=== MUSCLE GROUPS AND HOW TO TRAIN THEM (authoritative knowledge) ===
- Chest: push-ups, bench press, incline press, dips, flyes. Prioritize compound presses + one fly variation.
- Back: pull-ups, rows, lat pulldown, deadlift (trap/erector focus), face pulls for rear delts/rotator cuff.
- Shoulders: overhead press, lateral raises (mid delt), rear delt flyes, face pulls. Don't overtrain shoulders after chest day.
- Biceps: chin-ups, curls (barbell, dumbbell, hammer, incline). Hammer curls target brachialis and forearm.
- Triceps: close-grip bench, dips, overhead extensions, pushdowns. Triceps = 2/3 of arm mass.
- Forearms: wrist curls, reverse wrist curls, reverse curls, farmer carries, dead hangs, grip work (crush/hold/pinch). Forearms are built with high-frequency, high-volume, static holds and wrist movements.
- Core: planks, hanging leg raises, cable crunches, ab wheel, side planks, pallof press. Core is for stability, not for arm strength.
- Legs: squats, leg press, lunges, Romanian deadlifts (hamstrings), leg curls, calf raises (seated + standing).
- Glutes: hip thrusts, Bulgarian split squats, RDLs, glute kickbacks, step-ups.
- Traps/Neck: shrugs, farmer carries, rack pulls, neck harness work, face pulls.

=== SPORTS-SPECIFIC TRAINING (always match the sport's actual demands) ===
- ARM WRESTLING: the primary muscles are the FOREARM (wrist flexors, pronators, brachioradialis), GRIP and WRIST strength, plus biceps and side shoulder (deltoid). Training must include: wrist curls, reverse wrist curls, hammer curls, pronation/supination work (with a sledgehammer or dumbbell), wrist roller, thick-grip work, heavy static holds, towel pull-ups, and biceps work. Core/abs are NOT the priority for arm wrestling strength — forearm, grip and wrist are. If the user asks about arm wrestling, emphasize forearm + grip + wrist + biceps, NOT core.
- RUNNING: glutes, hamstrings, calves, core stability, hip flexors; strength work = split squats, RDL, calf raises, single-leg work.
- BASKETBALL/VOLLEYBALL (jumping): quad-dominant legs (squats, jumps), calves, glutes, core.
- SWIMMING: lats, shoulders, core; pull-ups, rows, rotator cuff, landmine presses.
- BOXING/MARTIAL ARTS: legs, core rotational power, shoulders, neck; medicine-ball throws, cable rotations, jump rope.
- TENNIS/BADMINTON: rotator cuff, shoulders, forearms, legs, core rotation; external rotation, wrist curls, split squats.
- FOOTBALL/SOCCER: quads, hamstrings, glutes, calves, core; nordic curls, Bulgarian split squats, calf raises.
- ROCK CLIMBING: forearms, grip, lats, core; dead hangs, hangboard, rows, pull-ups.
- DEADLIFT/POWERLIFTING: posterior chain, glutes, hamstrings, erectors, grip; RDLs, good mornings, farmer carries.

=== WEIGHT LOSS (fat loss) ===
- Calorie deficit is the #1 factor (approx 300-500 kcal below maintenance, not extreme cuts).
- Protein: 1.6-2.2 g per kg of bodyweight to preserve muscle.
- Strength training 3-4x/week + 8-12k steps daily + protein at every meal.
- Don't recommend crash diets, detoxes, or skipping meals. Sustainable > fast.
- Weight loss is not spot-reduction: you cannot lose fat from one area by training it.

=== HEALTH & SAFETY ===
- Never give medical diagnoses. If something sounds like an injury (sharp pain, swelling, numbness), advise rest and seeing a doctor/physiotherapist.
- Form before load: correct technique prevents injury.
- Sleep 7-9h and rest days are when muscles grow — never skip them.
- Warm up before heavy lifting; cool down and stretch after.

Rules:
- Be concise and motivational
- Give specific exercise suggestions when appropriate
- Consider muscle recovery status before suggesting workouts
- If a muscle group is fatigued (>70%), suggest alternatives or rest
- Adjust advice based on the user's experience level and goals
- Use the metric system (kg, cm)
- If the user hasn't trained enough this week, encourage them
- Suggest deload when appropriate (volume too high, recovery too low)
- Respond in the same language the user writes in
- Keep responses under 200 words

Workout Data:
$context"""

            val historyArray = JSONArray()
            conversationHistory.forEach { (role, content) ->
                val msg = JSONObject().apply {
                    put("role", role)
                    put("content", content)
                }
                historyArray.put(msg)
            }

            val body = JSONObject().apply {
                put("message", message)
                put("system_prompt", systemPrompt)
                put("history", historyArray)
            }

            val apiKey = preferencesManager.getAiApiKey()

            val requestBuilder = Request.Builder()
                .url("$serverUrl/chat")
                .post(body.toString().toRequestBody("application/json".toMediaType()))

            if (apiKey.isNotBlank()) {
                requestBuilder.addHeader("X-API-Key", apiKey)
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                cleanReply(json.getString("reply"))
            } else {
                when (response.code) {
                    401 -> "Authentication failed. Check your AI API key in Settings."
                    429 -> "AI provider rate limited. Please wait a moment and try again."
                    502, 504 -> "AI provider is unavailable. Please try again later."
                    503 -> "Service temporarily unavailable. Please try again later."
                    else -> "Error: Server returned ${response.code}. Check your server URL and API key."
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            "Error: AI server timed out. The request took too long."
        } catch (e: java.net.ConnectException) {
            "Error: Cannot connect to AI server at $serverUrl"
        } catch (e: Exception) {
            e.printStackTrace()
            "Error: ${e.message ?: "Unknown error"}. Make sure the AI server is running at $serverUrl"
        }
    }

    /**
     * Removes <think>...</think> reasoning blocks some models (e.g. Qwen)
     * prepend to the answer, so the user only sees the clean final response.
     * Handles both complete blocks and truncated ones (server cuts at max_tokens,
     * so the closing </think> tag may be missing).
     */
    private fun cleanReply(reply: String): String {
        var r = reply
        // 1) Complete <think>...</think> blocks
        r = r.replace(Regex("<think>.*?</think>", RegexOption.DOT_MATCHES_ALL), "")
        // 2) Truncated block: <think> opened but never closed (response cut off)
        val thinkStart = r.indexOf("<think>")
        if (thinkStart >= 0) {
            r = r.substring(0, thinkStart)
        }
        // 3) Qwen sometimes labels the block "Here's a thinking process:" instead
        val marker = r.indexOf("Here's a thinking process:")
        if (marker >= 0) {
            r = r.substring(0, marker)
        }
        // 4) Some models use "Thinking:\n" as a marker instead of <think> tags
        if (r.startsWith("Thinking:", ignoreCase = true)) {
            val idx = r.indexOf("\n")
            if (idx > 0) r = r.substring(idx + 1)
        }
        r = r.trim()
        // 5) If everything was reasoning and nothing real remains, ask for a retry
        return r.ifEmpty { "The trainer got cut off. Please try again." }
    }
}
