package com.example.kinetic

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.theme.*
import kotlinx.coroutines.launch

// ── Data classes for the UI ──
data class PlanExerciseUi(
    val id: Long = 0,
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val sets: Int = 3,
    val targetReps: Int = 10,
    val targetWeight: Double = 0.0,
    val orderIndex: Int = 0
)

data class PlanUi(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val daysPerWeek: Int = 3,
    val exercises: List<PlanExerciseUi> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlansScreen(
    isDark: Boolean,
    userId: String,
    onBack: () -> Unit,
    addPlanTrigger: Int = 0,
    onSharePlan: (Long, String) -> Unit = { _, _ -> }
) {
    val p = appPalette(isDark)
    val scope = rememberCoroutineScope()
    var plans by remember { mutableStateOf<List<PlanUi>>(emptyList()) }
    var selectedPlan by remember { mutableStateOf<PlanUi?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf<PlanUi?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Back button: deselect plan or go back
    BackHandler {
        if (selectedPlan != null) {
            selectedPlan = null
        } else {
            onBack()
        }
    }

    // Trigger create plan dialog from header button
    LaunchedEffect(addPlanTrigger) {
        if (addPlanTrigger > 0) showCreateDialog = true
    }

    // Load plans from DB
    LaunchedEffect(userId) {
        val db = AppDatabase.getDatabase(context)
        val planDao = db.workoutPlanDao()
        val exerciseDao = db.workoutPlanExerciseDao()
        val rawPlans = planDao.getAllForUser(userId)
        plans = rawPlans.map { plan ->
            val exercises = exerciseDao.getForPlan(plan.id).map { ex ->
                PlanExerciseUi(
                    id = ex.id,
                    name = ex.exerciseName,
                    muscleGroup = ex.muscleGroup,
                    equipment = ex.equipment,
                    sets = ex.sets,
                    targetReps = ex.targetReps,
                    targetWeight = ex.targetWeight,
                    orderIndex = ex.orderIndex
                )
            }
            PlanUi(
                id = plan.id,
                name = plan.name,
                description = plan.description,
                daysPerWeek = plan.daysPerWeek,
                exercises = exercises
            )
        }
    }

    Scaffold(
        containerColor = p.bg,
    ) { padding ->
        if (selectedPlan != null) {
            // ── Plan Detail ──
            PlanDetailContent(
                plan = selectedPlan!!,
                isDark = isDark,
                onAddExercise = { showAddExerciseDialog = true },
                modifier = Modifier.padding(padding)
            )
        } else {
            // ── Plans List ──
            if (plans.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("No plans yet", fontFamily = Varien, fontSize = 20.sp, color = p.tp)
                        Spacer(Modifier.height(8.dp))
                        Text("Create your first workout plan", color = p.ts, fontSize = 14.sp)
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = p.ac),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Create Plan", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    itemsIndexed(plans) { _, plan ->
                        PlanCard(
                            plan = plan,
                            isDark = isDark,
                            onClick = { selectedPlan = plan }
                        )
                    }
                }
            }
        }
    }

    // ── Create Plan Dialog ──
    if (showCreateDialog) {
        CreatePlanDialog(
            isDark = isDark,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, desc, days ->
                scope.launch {
                    val db = AppDatabase.getDatabase(context)
                    val id = db.workoutPlanDao().insert(
                        WorkoutPlanEntity(
                            userId = userId,
                            name = name,
                            description = desc,
                            daysPerWeek = days
                        )
                    )
                    val rawPlans = db.workoutPlanDao().getAllForUser(userId)
                    plans = rawPlans.map { plan ->
                        val exercises = db.workoutPlanExerciseDao().getForPlan(plan.id).map { ex ->
                            PlanExerciseUi(ex.id, ex.exerciseName, ex.muscleGroup, ex.equipment, ex.sets, ex.targetReps, ex.targetWeight, ex.orderIndex)
                        }
                        PlanUi(plan.id, plan.name, plan.description, plan.daysPerWeek, exercises)
                    }
                    selectedPlan = plans.find { it.id == id }
                    showCreateDialog = false
                }
            }
        )
    }

    // ── Add Exercise Dialog ──
    if (showAddExerciseDialog && selectedPlan != null) {
        AddExerciseToPlanDialog(
            isDark = isDark,
            onDismiss = { showAddExerciseDialog = false },
            onAdd = { exerciseName, muscleGroup, equipment, sets, reps, weight ->
                scope.launch {
                    val db = AppDatabase.getDatabase(context)
                    val order = selectedPlan!!.exercises.size
                    db.workoutPlanExerciseDao().insert(
                        WorkoutPlanExerciseEntity(
                            planId = selectedPlan!!.id,
                            exerciseName = exerciseName,
                            muscleGroup = muscleGroup,
                            equipment = equipment,
                            sets = sets,
                            targetReps = reps,
                            targetWeight = weight,
                            orderIndex = order
                        )
                    )
                    val exercises = db.workoutPlanExerciseDao().getForPlan(selectedPlan!!.id).map { ex ->
                        PlanExerciseUi(ex.id, ex.exerciseName, ex.muscleGroup, ex.equipment, ex.sets, ex.targetReps, ex.targetWeight, ex.orderIndex)
                    }
                    selectedPlan = selectedPlan!!.copy(exercises = exercises)
                    val rawPlans = db.workoutPlanDao().getAllForUser(userId)
                    plans = rawPlans.map { plan ->
                        val exs = db.workoutPlanExerciseDao().getForPlan(plan.id).map { ex ->
                            PlanExerciseUi(ex.id, ex.exerciseName, ex.muscleGroup, ex.equipment, ex.sets, ex.targetReps, ex.targetWeight, ex.orderIndex)
                        }
                        PlanUi(plan.id, plan.name, plan.description, plan.daysPerWeek, exs)
                    }
                    showAddExerciseDialog = false
                }
            }
        )
    }

    // ── Share Plan Dialog ──
    showShareDialog?.let { plan ->
        SharePlanDialog(
            isDark = isDark,
            planName = plan.name,
            planId = plan.id,
            userId = userId,
            onDismiss = { showShareDialog = null },
            onShare = { toUserId, toUserName ->
                scope.launch {
                    val db = AppDatabase.getDatabase(context)
                    db.sharedPlanDao().insert(
                        SharedPlanEntity(
                            planId = plan.id,
                            planName = plan.name,
                            fromUserId = userId,
                            fromUserName = "User",
                            toUserId = toUserId,
                            status = "pending"
                        )
                    )
                    db.messageDao().insert(
                        MessageEntity(
                            title = "Plan Shared",
                            body = "You shared \"${plan.name}\" with $toUserName",
                            type = "INFO"
                        )
                    )
                    showShareDialog = null
                }
            }
        )
    }
}

@Composable
private fun PlanCard(plan: PlanUi, isDark: Boolean, onClick: () -> Unit) {
    val p = appPalette(isDark)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = p.card),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(p.bd)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                        .background(p.ac.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = p.ac, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = p.tp)
                    if (plan.description.isNotBlank()) {
                        Text(plan.description, fontSize = 12.sp, color = p.ts, maxLines = 1)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {                        Badge(plan.exercises.size.toString() + " exercises", p.bl, p.bls)
                        Badge(plan.daysPerWeek.toString() + " days/wk", p.gn, p.gns)
            }
        }
    }
}

@Composable
private fun Badge(text: String, textColor: Color, bgColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            fontFamily = JetBrainsMono
        )
    }
}

@Composable
private fun PlanDetailContent(plan: PlanUi, isDark: Boolean, onAddExercise: () -> Unit, modifier: Modifier = Modifier) {
    val p = appPalette(isDark)
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Plan info
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = p.card),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(plan.name, fontFamily = Varien, fontSize = 22.sp, color = p.tp)
                    if (plan.description.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(plan.description, fontSize = 13.sp, color = p.ts)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Badge(plan.exercises.size.toString() + " exercises", p.bl, p.bls)
                        Badge(plan.daysPerWeek.toString() + " days/wk", p.gn, p.gns)
                    }
                }
            }
        }

        // Exercises
        itemsIndexed(plan.exercises) { idx, exercise ->
            ExerciseInPlanCard(exercise = exercise, index = idx + 1, isDark = isDark)
        }

        // Add Exercise button
        item {
            Button(
                onClick = onAddExercise,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(p.ac.copy(alpha = 0.3f))
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = p.ac, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Exercise", color = p.ac, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ExerciseInPlanCard(exercise: PlanExerciseUi, index: Int, isDark: Boolean) {
    val p = appPalette(isDark)
    val mg = when (exercise.muscleGroup.lowercase()) {
        "chest" -> MuscleGroupColor("Chest", Color(0xFFFF3C3C))
        "back" -> MuscleGroupColor("Back", Color(0xFF4E8CFF))
        "shoulders" -> MuscleGroupColor("Shoulders", Color(0xFFA855F7))
        "biceps" -> MuscleGroupColor("Biceps", Color(0xFFF5A623))
        "triceps" -> MuscleGroupColor("Triceps", Color(0xFF2DD4A0))
        "legs" -> MuscleGroupColor("Legs", Color(0xFFFB7185))
        "core" -> MuscleGroupColor("Core", Color(0xFFFF9800))
        "cardio" -> MuscleGroupColor("Cardio", Color(0xFFE91E63))
        else -> MuscleGroupColor(exercise.muscleGroup, p.ac)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = p.card),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                        .background(mg.color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$index", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = mg.color, fontFamily = JetBrainsMono)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(exercise.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = p.tp)
                    Text(exercise.equipment.replaceFirstChar { it.uppercase() }, fontSize = 11.sp, color = p.ts)
                }
                Surface(
                    color = mg.color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        mg.label,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = mg.color
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            // Sets table
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .background(p.bg).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SETS", fontSize = 9.sp, color = p.ts, letterSpacing = 1.sp)
                    Text(exercise.sets.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = p.tp, fontFamily = JetBrainsMono)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REPS", fontSize = 9.sp, color = p.ts, letterSpacing = 1.sp)
                    Text(exercise.targetReps.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = p.tp, fontFamily = JetBrainsMono)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("WEIGHT", fontSize = 9.sp, color = p.ts, letterSpacing = 1.sp)
                    Text("${exercise.targetWeight.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = p.tp, fontFamily = JetBrainsMono)
                }
            }
        }
    }
}

private data class MuscleGroupColor(val label: String, val color: Color)

@Composable
private fun CreatePlanDialog(isDark: Boolean, onDismiss: () -> Unit, onCreate: (String, String, Int) -> Unit) {
    val p = appPalette(isDark)
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var days by remember { mutableIntStateOf(3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = p.card,
        titleContentColor = p.tp,
        title = { Text("Create Plan", fontFamily = Varien, fontSize = 20.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Plan name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = p.ac,
                        unfocusedBorderColor = p.bd,
                        cursorColor = p.ac,
                        focusedTextColor = p.tp,
                        unfocusedTextColor = p.tp
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = p.ac,
                        unfocusedBorderColor = p.bd,
                        cursorColor = p.ac,
                        focusedTextColor = p.tp,
                        unfocusedTextColor = p.tp
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Text("Days per week: $days", color = p.ts, fontSize = 13.sp)
                Slider(
                    value = days.toFloat(),
                    onValueChange = { days = it.toInt() },
                    valueRange = 1f..7f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = p.ac,
                        activeTrackColor = p.ac,
                        inactiveTrackColor = p.bd
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name, description, days) },
                colors = ButtonDefaults.buttonColors(containerColor = p.ac),
                shape = RoundedCornerShape(10.dp),
                enabled = name.isNotBlank()
            ) {
                Text("Create", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = p.ts)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExerciseToPlanDialog(isDark: Boolean, onDismiss: () -> Unit, onAdd: (String, String, String, Int, Int, Double) -> Unit) {
    val p = appPalette(isDark)
    var exerciseName by remember { mutableStateOf("") }
    var muscleGroup by remember { mutableStateOf("chest") }
    var equipment by remember { mutableStateOf("barbell") }
    var sets by remember { mutableIntStateOf(3) }
    var reps by remember { mutableIntStateOf(10) }
    var weight by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var expandedEquip by remember { mutableStateOf(false) }

    val muscleGroups = listOf("chest", "back", "shoulders", "biceps", "triceps", "legs", "core", "cardio")
    val equipmentList = listOf("barbell", "dumbbell", "cable", "machine", "bodyweight", "none")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = p.card,
        titleContentColor = p.tp,
        title = { Text("Add Exercise", fontFamily = Varien, fontSize = 20.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    label = { Text("Exercise name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = p.ac,
                        unfocusedBorderColor = p.bd,
                        cursorColor = p.ac,
                        focusedTextColor = p.tp,
                        unfocusedTextColor = p.tp
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Muscle group dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = muscleGroup.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Muscle group") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = p.ac,
                            unfocusedBorderColor = p.bd,
                            cursorColor = p.ac,
                            focusedTextColor = p.tp,
                            unfocusedTextColor = p.tp
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = p.card,
                        tonalElevation = 0.dp
                    ) {
                        muscleGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.replaceFirstChar { it.uppercase() }, color = p.tp) },
                                onClick = { muscleGroup = group; expanded = false },
                                colors = MenuDefaults.itemColors(
                                    textColor = p.tp,
                                    leadingIconColor = p.tp,
                                    trailingIconColor = p.ts
                                )
                            )
                        }
                    }
                }

                // Equipment dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedEquip,
                    onExpandedChange = { expandedEquip = !expandedEquip }
                ) {
                    OutlinedTextField(
                        value = equipment.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Equipment") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedEquip) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = p.ac,
                            unfocusedBorderColor = p.bd,
                            cursorColor = p.ac,
                            focusedTextColor = p.tp,
                            unfocusedTextColor = p.tp
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEquip,
                        onDismissRequest = { expandedEquip = false },
                        containerColor = p.card,
                        tonalElevation = 0.dp
                    ) {
                        equipmentList.forEach { equip ->
                            DropdownMenuItem(
                                text = { Text(equip.replaceFirstChar { it.uppercase() }, color = p.tp) },
                                onClick = { equipment = equip; expandedEquip = false },
                                colors = MenuDefaults.itemColors(
                                    textColor = p.tp,
                                    leadingIconColor = p.tp,
                                    trailingIconColor = p.ts
                                )
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = sets.toString(),
                        onValueChange = { sets = it.toIntOrNull() ?: 3 },
                        label = { Text("Sets") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = p.ac,
                            unfocusedBorderColor = p.bd,
                            cursorColor = p.ac,
                            focusedTextColor = p.tp,
                            unfocusedTextColor = p.tp
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = reps.toString(),
                        onValueChange = { reps = it.toIntOrNull() ?: 10 },
                        label = { Text("Reps") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = p.ac,
                            unfocusedBorderColor = p.bd,
                            cursorColor = p.ac,
                            focusedTextColor = p.tp,
                            unfocusedTextColor = p.tp
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = p.ac,
                        unfocusedBorderColor = p.bd,
                        cursorColor = p.ac,
                        focusedTextColor = p.tp,
                        unfocusedTextColor = p.tp
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (exerciseName.isNotBlank()) {
                        onAdd(exerciseName, muscleGroup, equipment, sets, reps, weight.toDoubleOrNull() ?: 0.0)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = p.ac),
                shape = RoundedCornerShape(10.dp),
                enabled = exerciseName.isNotBlank()
            ) {
                Text("Add", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = p.ts)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SharePlanDialog(
    isDark: Boolean,
    planName: String,
    planId: Long,
    userId: String,
    onDismiss: () -> Unit,
    onShare: (String, String) -> Unit
) {
    val p = appPalette(isDark)
    val context = androidx.compose.ui.platform.LocalContext.current
    var friends by remember { mutableStateOf<List<FriendshipEntity>>(emptyList()) }
    var selectedFriend by remember { mutableStateOf<FriendshipEntity?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        try {
            val db = AppDatabase.getDatabase(context)
            val all = db.friendshipDao().getFriendsFor(userId)
            friends = all.filter { it.status == "accepted" }
        } catch (_: Exception) {}
        loading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = p.card,
        titleContentColor = p.tp,
        title = { Text("Share Plan", fontFamily = Varien, fontSize = 20.sp) },
        text = {
            Column {
                Text("Share \"$planName\" with a friend", color = p.ts, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                if (loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = p.ac, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                } else if (friends.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👥", fontSize = 32.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No friends yet", color = p.ts, fontSize = 13.sp)
                            Text("Add friends first to share plans", color = p.tt, fontSize = 11.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(friends) { friend ->
                            val friendId = if (friend.userId == userId) friend.friendId else friend.userId
                            val isSelected = selectedFriend?.let {
                                (it.userId == userId && it.friendId == friendId) ||
                                (it.friendId == userId && it.userId == friendId)
                            } ?: false
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) p.ac.copy(alpha = 0.12f) else Color.Transparent)
                                    .clickable { selectedFriend = friend }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape)
                                        .background(p.ac.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(friendId.take(1).uppercase(), color = p.ac, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(friendId.take(12), color = p.tp, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, null, tint = p.ac, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedFriend?.let { f ->
                        val friendId = if (f.userId == userId) f.friendId else f.userId
                        onShare(friendId, friendId)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = p.ac),
                shape = RoundedCornerShape(10.dp),
                enabled = selectedFriend != null
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Share", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = p.ts)
            }
        }
    )
}
