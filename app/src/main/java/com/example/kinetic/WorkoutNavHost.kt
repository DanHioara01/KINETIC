package com.example.kinetic

import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

object WorkoutRoutes {
    const val HOME = "workout/home"
    const val EXERCISE_INPUT = "workout/exercise_input/{grupa}/{exerciseName}"
    const val PROGRESS = "workout/progress/{exerciseName}"

    fun progress(exerciseName: String): String =
        "workout/progress/${Uri.encode(exerciseName)}"

    fun isActiveRoute(route: String?): Boolean =
        route != null && route != HOME
}

fun NavHostController.popToWorkoutHome() {
    popBackStack(WorkoutRoutes.HOME, inclusive = false)
}

@Composable
fun WorkoutNavHost(
    navController: NavHostController,
    isLbs: Boolean,
    isDark: Boolean,
    strings: LanguageManager.Strings,
    onWorkoutSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = WorkoutRoutes.HOME,
        modifier = modifier.fillMaxSize()
    ) {
        composable(WorkoutRoutes.HOME) { }

        composable(
            route = WorkoutRoutes.EXERCISE_INPUT,
            arguments = listOf(
                navArgument("grupa") { type = NavType.StringType },
                navArgument("exerciseName") { type = NavType.StringType }
            )
        ) { entry ->
            val grupa = Uri.decode(entry.arguments?.getString("grupa") ?: "")
            val exerciseName = Uri.decode(entry.arguments?.getString("exerciseName") ?: "")
            ExerciseInputScreen(
                exercise = ExerciseDefinition(exerciseName, grupa),
                grupaMusculara = grupa,
                isLbs = isLbs,
                isDark = isSystemInDarkTheme(),
                onBackClick = { navController.popBackStack() },
                onOpenProgress = { name ->
                    navController.navigate(WorkoutRoutes.progress(name))
                },
                onWorkoutSaved = onWorkoutSaved,
                strings = strings
            )
        }

        composable(
            route = WorkoutRoutes.PROGRESS,
            arguments = listOf(navArgument("exerciseName") { type = NavType.StringType })
        ) { entry ->
            val exerciseName = Uri.decode(entry.arguments?.getString("exerciseName") ?: "")
            CalendarScreen(
                isLbs = isLbs,
                initialExercise = exerciseName,
                isDark = isDark,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
