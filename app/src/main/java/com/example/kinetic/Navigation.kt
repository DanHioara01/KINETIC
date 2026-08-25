package com.example.kinetic

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Workouts : Screen("workouts")
    data object Stats : Screen("stats")
    data object Water : Screen("water")
    data object Profile : Screen("profile")
}

sealed class DrawerScreen(val route: String, val icon: ImageVector, val labelKey: String) {
    data object Calendar : DrawerScreen("drawer_calendar", Icons.Default.CalendarMonth, "calendarView")
    data object FoodJournal : DrawerScreen("drawer_food_journal", Icons.Default.Restaurant, "foodJournal")
    data object AiTrainer : DrawerScreen("drawer_ai_trainer", Icons.Default.Psychology, "aiTrainer")
    data object Friends : DrawerScreen("drawer_friends", Icons.Default.People, "friends")
    data object GpsCardio : DrawerScreen("drawer_gps_cardio", Icons.Default.DirectionsRun, "gpsCardioMap")
    data object RestDays : DrawerScreen("drawer_rest_days", Icons.Default.Bedtime, "restDaysTitle")
    data object SavedExercises : DrawerScreen("drawer_saved_exercises", Icons.Default.Bookmark, "savedExercises")
    data object WeightGoal : DrawerScreen("drawer_weight_goal", Icons.Default.Flag, "weightGoal")
    data object BodyFatCalculator : DrawerScreen("drawer_body_fat_calculator", Icons.Default.MonitorWeight, "bodyFatCalculator")
    data object Messages : DrawerScreen("drawer_messages", Icons.Default.MailOutline, "messages")
}

sealed class SubScreen(val route: String) {
    data object ExerciseList : SubScreen("sub_exercise_list/{grupaMusculara}") {
        fun createRoute(grupaMusculara: String) = "sub_exercise_list/$grupaMusculara"
    }
    data object ExerciseInput : SubScreen("sub_exercise_input/{grupaMusculara}/{exerciseName}") {
        fun createRoute(grupaMusculara: String, exerciseName: String) =
            "sub_exercise_input/$grupaMusculara/$exerciseName"
    }
    data object CalendarView : SubScreen("sub_calendar")
    data object Templates : SubScreen("sub_templates")
    data object TemplateDetail : SubScreen("sub_template_detail/{templateName}") {
        fun createRoute(templateName: String) = "sub_template_detail/$templateName"
    }
    data object BiometricInput : SubScreen("sub_biometric_input")
    data object BiometricCharts : SubScreen("sub_biometric_charts")
    data object FoodJournalFull : SubScreen("sub_food_journal_full")
    data object BarcodeScanner : SubScreen("sub_barcode_scanner")
    data object AddFood : SubScreen("sub_add_food")
    data object Leaderboard : SubScreen("sub_leaderboard")
    data object GpsCardioFull : SubScreen("sub_gps_cardio")
    data object RestDaysFull : SubScreen("sub_rest_days")
    data object ExerciseProgress : SubScreen("sub_exercise_progress/{exerciseName}") {
        fun createRoute(exerciseName: String) = "sub_exercise_progress/$exerciseName"
    }
    data object Pricing : SubScreen("sub_pricing")
    data object WeightGoal : SubScreen("sub_weight_goal")
    data object BodyFatCalculator : SubScreen("sub_body_fat_calculator")
}
