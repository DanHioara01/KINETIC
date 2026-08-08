package com.example.kinetic

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.AppPalette
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedExercisesScreen(
    userId: String,
    isDark: Boolean,
    isLbs: Boolean,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit = {},
    onOpenProgress: (String) -> Unit = {},
    onWorkoutSaved: () -> Unit = {},
    strings: LanguageManager.Strings
) {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel()

    var favoriteExercises by remember { mutableStateOf<Map<String, List<ExerciseListItem>>>(emptyMap()) }
    var reloadToken by remember { mutableIntStateOf(0) }
    var selectedExercise by remember { mutableStateOf<ExerciseDefinition?>(null) }
    var selectedGrupa by remember { mutableStateOf("") }
    val exerciseSummaries by viewModel.exerciseSummaries.collectAsState()

    LaunchedEffect(userId, reloadToken) {
        viewModel.getFavoriteExercises(userId) { favoriteExercises = it }
    }

    LaunchedEffect(favoriteExercises) {
        val allNames = favoriteExercises.values.flatten().map { it.exercise.nume }
        if (allNames.isNotEmpty()) {
            viewModel.loadExerciseSummaries(userId, allNames)
        }
    }

    BackHandler {
        when {
            selectedExercise != null -> selectedExercise = null
            else -> onBackClick()
        }
    }

    if (selectedExercise != null) {
        ExerciseInputScreen(
            exercise = selectedExercise!!,
            grupaMusculara = selectedGrupa,
            isLbs = isLbs,
            isDark = isDark,
            onBackClick = { selectedExercise = null },
            onOpenProgress = onOpenProgress,
            onWorkoutSaved = onWorkoutSaved,
            strings = strings
        )
        return
    }

    val p = appPalette(isDark)

    Scaffold(
        topBar = {
            KineticAppBar(onBack = onBackClick)
        },
        containerColor = p.bg
    ) { paddingValues ->
        if (favoriteExercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = p.ts.copy(alpha = 0.5f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        strings.noFavorites.ifBlank { "No saved exercises yet" },
                        style = MaterialTheme.typography.titleMedium,
                        color = p.ts,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        strings.tapStarToSave.ifBlank { "Tap the star on any exercise to save it here" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = p.ts.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = AppConstants.BOTTOM_NAV_PADDING),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                favoriteExercises.forEach { (grupa, exercises) ->
                    item {
                        Text(
                            text = LanguageManager.translateMuscleGroup(grupa, strings),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = p.ac,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(exercises) { item ->
                        val exercitiu = item.exercise
                        AppGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    selectedGrupa = exercitiu.group
                                    selectedExercise = exercitiu
                                },
                            p = p,
                            cornerRadius = 16.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                                                                 .background(Color.Black, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val gifUrl = ExerciseGifs.getGif(exercitiu.nume)
                                    if (gifUrl != null) {
                                        AsyncImage(
                                            model = gifUrl,
                                            contentDescription = exercitiu.nume,
                                            modifier = Modifier
                                                .size(80.dp)
                                                .padding(4.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.DirectionsBike,
                                            contentDescription = null,
                                            tint = p.ac.copy(alpha = 0.6f),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exercitiu.nume,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = p.tp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = LanguageManager.translateMuscleGroup(exercitiu.group, strings),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = p.ts
                                    )
                                    val summary = exerciseSummaries[exercitiu.nume]
                                    if (summary != null && summary.bestWeight > 0) {
                                        Text(
                                            text = "PR: ${summary.bestWeight.toInt()}kg × ${summary.bestReps}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFD94848)
                                        )
                                    }
                                    if (item.equipment.isNotBlank()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = item.equipment,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = p.ts.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.setFavorite(
                                            userId = userId,
                                            grupa = exercitiu.group,
                                            numeExercitiu = exercitiu.nume,
                                            isFavorite = false
                                        ) {
                                            reloadToken++
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = strings.removeFavorite,
                                        tint = RecoveryYellow
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
