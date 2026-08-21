package com.example.kinetic

/**
 * Identitate stabilă a exercițiilor.
 *
 * Fiecare exercițiu primește un `exerciseId` canonic (slug) calculat din nume.
 * Alias-urile mapă denumirile diferite (ex. „Barbell Bench Press", „Flat Bench Press")
 * la același exercițiu canonic (ex. „Bench Press"), ca istoricul, statisticile și
 * sync-ul să se unească — indiferent de varianta de nume folosită.
 */

private val ACCENT_MAP = mapOf(
    'ă' to 'a', 'â' to 'a', 'î' to 'i', 'ș' to 's', 'ş' to 's', 'ț' to 't', 'ţ' to 't',
    'Ă' to 'A', 'Â' to 'A', 'Î' to 'I', 'Ș' to 'S', 'Ş' to 'S', 'Ț' to 'T', 'Ţ' to 'T'
)

/** Transformă orice nume într-un slug stabil: „Barbell Bench Press" → "barbell_bench_press". */
fun slugify(raw: String): String {
    val sb = StringBuilder()
    for (c in raw.trim().lowercase()) {
        val ch = ACCENT_MAP[c] ?: c
        if (ch in 'a'..'z' || ch in '0'..'9') {
            sb.append(ch)
        } else if (sb.isNotEmpty() && sb.last() != '_') {
            sb.append('_')
        }
    }
    return sb.toString().trim('_')
}

/**
 * Alias (slug) → nume canonic (exact cum e definit în DataProvider).
 * Adaugă aici variantele comune de denumire pentru același exercițiu.
 */
val EXERCISE_ALIASES: Map<String, String> = buildMap {
    // ── Piept ───────────────────────────────
    put("barbell_bench_press", "Bench Press")
    put("flat_bench_press", "Bench Press")
    put("bench_press_barbell", "Bench Press")
    put("flat_barbell_bench_press", "Bench Press")
    put("pushup", "Push Up")
    put("press_up", "Push Up")
    put("incline_barbell_bench_press", "Incline Bench Press")
    put("incline_bench_press_barbell", "Incline Bench Press")
    put("decline_barbell_bench_press", "Decline Bench Press")
    put("decline_bench_press_barbell", "Decline Bench Press")
    put("dumbbell_bench_press", "Dumbbell Bench Press")
    put("db_bench_press", "Dumbbell Bench Press")
    put("flat_dumbbell_bench_press", "Dumbbell Bench Press")
    put("dumbbell_flat_bench_press", "Dumbbell Bench Press")
    put("cable_fly", "Cable Fly")
    put("cable_crossover", "Cable Fly")
    put("pec_deck", "Pec Deck")
    put("chest_fly_machine", "Pec Deck")
    put("dips", "Chest Dip")
    put("chest_dips", "Chest Dip")
    // ── Spate ───────────────────────────────
    put("barbell_row", "Barbell Row")
    put("bent_over_row", "Barbell Row")
    put("barbell_bent_over_row", "Barbell Row")
    put("bent_over_barbell_row", "Barbell Row")
    put("pendlay_row", "Barbell Row")
    put("deadlift", "Deadlift")
    put("barbell_deadlift", "Deadlift")
    put("conventional_deadlift", "Deadlift")
    put("romanian_deadlift", "Romanian Deadlift")
    put("rdl", "Romanian Deadlift")
    put("stiff_leg_deadlift", "Romanian Deadlift")
    put("lat_pulldown", "Lat Pulldown")
    put("lat_pull_down", "Lat Pulldown")
    put("pulldown", "Lat Pulldown")
    put("wide_grip_lat_pulldown", "Lat Pulldown")
    put("pull_up", "Pull Up")
    put("pullup", "Pull Up")
    put("seated_row", "Seated Row")
    put("cable_row", "Seated Row")
    put("seated_cable_row", "Seated Row")
    put("t_bar_row", "T-Bar Row")
    put("t_bar_rows", "T-Bar Row")
    put("face_pull", "Face Pull")
    put("face_pulls", "Face Pull")
    // ── Umeri ───────────────────────────────
    put("overhead_press", "Overhead Press")
    put("shoulder_press", "Overhead Press")
    put("barbell_overhead_press", "Overhead Press")
    put("barbell_shoulder_press", "Overhead Press")
    put("military_press", "Overhead Press")
    put("strict_press", "Overhead Press")
    put("dumbbell_shoulder_press", "Dumbbell Shoulder Press")
    put("db_shoulder_press", "Dumbbell Shoulder Press")
    put("lateral_raise", "Lateral Raise")
    put("side_lateral_raise", "Lateral Raise")
    put("dumbbell_lateral_raise", "Lateral Raise")
    put("db_lateral_raise", "Lateral Raise")
    put("front_raise", "Front Raise")
    put("dumbbell_front_raise", "Front Raise")
    put("rear_delt_fly", "Reverse Fly")
    put("reverse_fly", "Reverse Fly")
    put("rear_delt_raise", "Reverse Fly")
    // ── Picioare ────────────────────────────
    put("squat", "Squat")
    put("barbell_squat", "Squat")
    put("back_squat", "Squat")
    put("barbell_back_squat", "Squat")
    put("goblet_squat", "Goblet Squat")
    put("leg_press", "Leg Press")
    put("hack_squat", "Hack Squat")
    put("machine_hack_squat", "Hack Squat")
    put("leg_extension", "Leg Extension")
    put("leg_extensions", "Leg Extension")
    put("leg_curl", "Leg Curl")
    put("lying_leg_curl", "Leg Curl")
    put("seated_leg_curl", "Leg Curl")
    put("lunges", "Lunges")
    put("walking_lunge", "Lunges")
    put("walking_lunges", "Lunges")
    put("dumbbell_lunge", "Lunges")
    put("calf_raise", "Calf Raise")
    put("standing_calf_raise", "Calf Raise")
    put("glute_bridge", "Glute Bridge")
    put("hip_thrust", "Hip Thrust")
    put("barbell_hip_thrust", "Hip Thrust")
    // ── Biceps ──────────────────────────────
    put("barbell_curl", "Barbell Curl")
    put("barbell_bicep_curl", "Barbell Curl")
    put("bicep_curl_barbell", "Barbell Curl")
    put("ez_bar_curl", "EZ Bar Curl")
    put("ez_bar_bicep_curl", "EZ Bar Curl")
    put("dumbbell_curl", "Dumbbell Curl")
    put("dumbbell_bicep_curl", "Dumbbell Curl")
    put("db_curl", "Dumbbell Curl")
    put("hammer_curl", "Hammer Curl")
    put("dumbbell_hammer_curl", "Hammer Curl")
    put("db_hammer_curl", "Hammer Curl")
    put("preacher_curl", "Preacher Curl")
    put("concentration_curl", "Concentration Curl")
    put("dumbbell_concentration_curl", "Concentration Curl")
    // ── Triceps ─────────────────────────────
    put("triceps_pushdown", "Triceps Pushdown")
    put("pushdown", "Triceps Pushdown")
    put("cable_pushdown", "Triceps Pushdown")
    put("tricep_pushdown", "Triceps Pushdown")
    put("overhead_triceps_extension", "Overhead Triceps Extension")
    put("skull_crusher", "Skull Crusher")
    put("lying_triceps_extension", "Skull Crusher")
    put("triceps_dip", "Triceps Dip")
    put("bench_dip", "Triceps Dip")
    // ── Core ────────────────────────────────
    put("plank", "Plank")
    put("forearm_plank", "Plank")
    put("front_plank", "Plank")
    put("russian_twist", "Russian Twist")
    put("sit_up", "Sit-up")
    put("situps", "Sit-up")
    put("crunch", "Crunch")
    put("crunches", "Crunch")
    put("hanging_leg_raise", "Hanging Leg Raise")
    put("hanging_knee_raise", "Hanging Knee Raise")
    // ── Alte mișcări frecvente ──────────────
    put("farmers_walk", "Farmer's Walk")
    put("farmer_walk", "Farmer's Walk")
    put("kettlebell_swing", "Kettlebell Swing")
    put("kb_swing", "Kettlebell Swing")
    put("burpee", "Burpee")
    put("burpees", "Burpee")
    put("mountain_climber", "Mountain Climber")
    put("mountain_climbers", "Mountain Climber")
    put("jumping_jack", "Jumping Jack")
    put("jumping_jacks", "Jumping Jack")
    put("bicycle_crunch", "Bicycle Crunch")
    put("bicycle_crunches", "Bicycle Crunch")
}

/** Returnează numele canonic pentru un nume dat (alias → canonic, altfel numele însuși). */
fun canonicalExerciseName(name: String): String = EXERCISE_ALIASES[slugify(name)] ?: name.trim()

/** ID-ul canonic al exercițiului — stabil pentru orice alias al lui. */
fun exerciseIdFor(name: String): String = slugify(canonicalExerciseName(name))

/** Cheie de potrivire loose pe nume (slug), pentru interogări „după nume". */
fun exerciseKey(name: String): String = slugify(name)
