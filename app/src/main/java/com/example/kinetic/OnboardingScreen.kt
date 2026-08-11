package com.example.kinetic

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.theme.*
import com.example.kinetic.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    strings: LanguageManager.Strings,
    onProfileComplete: (UserOnboardingProfile) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var goal by remember { mutableStateOf("") }
    var age by remember { mutableIntStateOf(25) }
    var gender by remember { mutableStateOf("") }
    var weight by remember { mutableFloatStateOf(70f) }
    var height by remember { mutableFloatStateOf(170f) }
    var activityLevel by remember { mutableStateOf("sedentary") }
    var experience by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("") }
    var sessionsPerWeek by remember { mutableIntStateOf(3) }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    var limitations by remember { mutableStateOf("") }
    var selectedGroups by remember { mutableStateOf(setOf<String>()) }
    var showValidationError by remember { mutableStateOf(false) }

    val accent = AccentRed
    val bg = DarkBackground
    val cardBg = DarkCard
    val textPrimary = TextWarmWhite
    val textSecondary = TextGrayRed
    val border = DarkDivider

    val totalSteps = 7

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Text(
                "KINETIC",
                fontSize = 28.sp,
                letterSpacing = 8.sp,
                color = textPrimary,
                fontFamily = Varien
            )
            Spacer(Modifier.height(6.dp))
            Text(
                strings.appTagline,
                fontSize = 11.sp,
                letterSpacing = 3.sp,
                color = textSecondary
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 0 until totalSteps) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                                                        .background(if (i <= step) accent else border, RoundedCornerShape(2.dp))
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                },
                label = "step"
            ) { currentStep ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentStep) {
                        0 -> GoalStep(
                            selectedGoal = goal,
                            onSelect = { goal = it },
                            strings = strings,
                            accent = accent,
                            cardBg = cardBg,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            border = border
                        )
                        1 -> BodyStep(
                            age = age,
                            onAgeChange = { age = it },
                            selectedGender = gender,
                            onGenderSelect = { gender = it },
                            weight = weight,
                            onWeightChange = { weight = it },
                            height = height,
                            onHeightChange = { height = it },
                            strings = strings,
                            accent = accent,
                            cardBg = cardBg,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            border = border
                        )
                        2 -> ActivityStep(
                            selected = activityLevel,
                            onSelect = { activityLevel = it },
                            strings = strings,
                            accent = accent,
                            cardBg = cardBg,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            border = border
                        )
                        3 -> ExperienceStep(
                            selected = experience,
                            onSelect = { experience = it },
                            strings = strings,
                            accent = accent,
                            cardBg = cardBg,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            border = border
                        )
                        4 -> EquipmentStep(
                            selected = equipment,
                            onSelect = { equipment = it },
                            strings = strings,
                            accent = accent,
                            cardBg = cardBg,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            border = border
                        )
                        5 -> FrequencyStep(
                            sessions = sessionsPerWeek,
                            onSessionsChange = { newSessions ->
                                sessionsPerWeek = newSessions
                                if (selectedDays.size > newSessions) {
                                    selectedDays = selectedDays.take(newSessions).toSet()
                                }
                            },
                            selectedDays = selectedDays,
                            onDaysChange = { selectedDays = it },
                            limitations = limitations,
                            onLimitationsChange = { limitations = it },
                            strings = strings,
                            accent = accent,
                            cardBg = cardBg,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            border = border
                        )
                        6 -> MuscleGroupStep(
                            selectedGroups = selectedGroups,
                            onToggle = { group ->
                                val allIndividual = setOf("chest", "back", "legs", "shoulders", "arms", "glutes", "core", "cardio")
                                if (group == "fullbody") {
                                    selectedGroups = if (selectedGroups.containsAll(allIndividual)) { showValidationError = false; emptySet() }
                                    else allIndividual
                                } else {
                                    val newGroups = if (group in selectedGroups) selectedGroups - group else selectedGroups + group
                                    selectedGroups = newGroups; showValidationError = false
                                }
                            },
                            strings = strings,
                            accent = accent,
                            cardBg = cardBg,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            border = border
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { step--; showValidationError = false },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, border)
                    ) {
                        Text(strings.back, letterSpacing = 2.sp)
                    }
                }

                Button(
                    onClick = {
                        val isValid = when (step) {
                            0 -> goal.isNotEmpty()
                            1 -> gender.isNotEmpty() && age in 16..70
                            2 -> activityLevel.isNotEmpty()
                            3 -> experience.isNotEmpty()
                            4 -> equipment.isNotEmpty()
                            5 -> true
                            6 -> selectedGroups.isNotEmpty()
                            else -> true
                        }
                        if (!isValid) {
                            showValidationError = true
                            return@Button
                        }
                        showValidationError = false
                        if (step < totalSteps - 1) {
                            step++
                        } else {
                            onProfileComplete(
                                UserOnboardingProfile(
                                    goal = goal,
                                    experience = experience,
                                    equipment = equipment,
                                    sessionsPerWeek = sessionsPerWeek,
                                    selectedDays = selectedDays.toList(),
                                    limitations = limitations,
                                    selectedGroups = selectedGroups.toList(),
                                    age = age,
                                    gender = gender,
                                    activityLevel = activityLevel,
                                    weight = weight,
                                    height = height
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .then(if (step > 0) Modifier.weight(1f) else Modifier.fillMaxWidth())
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    enabled = true
                ) {
                    Text(
                        if (step == totalSteps - 1) strings.finish else strings.next,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                }
            }

            if (showValidationError) {
                Spacer(Modifier.height(8.dp))
                Text(
                    strings.pleaseSelectOption,
                    color = AccentRed,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (step > 0 || step == 0) {
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = {
                    onProfileComplete(
                        UserOnboardingProfile(
                            goal = goal.ifEmpty { "maintenance" },
                            experience = experience.ifEmpty { "beginner" },
                            equipment = equipment.ifEmpty { "full_gym" },
                            sessionsPerWeek = sessionsPerWeek,
                            selectedDays = selectedDays.toList(),
                            limitations = limitations,
                            selectedGroups = selectedGroups.toList().ifEmpty { listOf("chest", "back", "legs") },
                            age = age,
                            gender = gender.ifEmpty { "male" },
                            activityLevel = activityLevel.ifEmpty { "sedentary" },
                            weight = weight,
                            height = height
                        )
                    )
                }) {
                    Text(strings.skip, color = textSecondary, fontSize = 13.sp, letterSpacing = 1.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GoalStep(
    selectedGoal: String,
    onSelect: (String) -> Unit,
    strings: LanguageManager.Strings,
    accent: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    border: Color
) {
    data class Goal(val key: String, val label: String, val iconRes: Int)
    val goals = remember(strings) { listOf(
        Goal("strength", strings.goalStrength, R.drawable.onboarding_strength),
        Goal("mass", strings.goalMass, R.drawable.onboarding_mass),
        Goal("weight_loss", strings.goalWeightLoss, R.drawable.onboarding_cardio),
        Goal("maintenance", strings.goalMaintenance, R.drawable.onboarding_maintenance)
    ) }

    Text(
        strings.selectGoal,
        style = MaterialTheme.typography.headlineMedium,
        color = textPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(6.dp))
    Text(String.format(strings.stepOf, 1), fontSize = 12.sp, color = textSecondary)
    Spacer(Modifier.height(20.dp))

    goals.forEach { goal ->
        val isSelected = selectedGoal == goal.key
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                                .background(if (isSelected) accent.copy(alpha = 0.15f) else cardBg, RoundedCornerShape(14.dp))
                .border(
                    1.dp,
                    if (isSelected) accent else border,
                    RoundedCornerShape(14.dp)
                )
                .clickable { onSelect(goal.key) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = goal.iconRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(if (isSelected) accent else textSecondary)
            )
            Spacer(Modifier.width(14.dp))
            Text(
                goal.label,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) accent else textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun BodyStep(
    age: Int,
    onAgeChange: (Int) -> Unit,
    selectedGender: String,
    onGenderSelect: (String) -> Unit,
    weight: Float,
    onWeightChange: (Float) -> Unit,
    height: Float,
    onHeightChange: (Float) -> Unit,
    strings: LanguageManager.Strings,
    accent: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    border: Color
) {
    Text(
        strings.whatsYourAge,
        style = MaterialTheme.typography.headlineMedium,
        color = textPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(6.dp))
    Text(String.format(strings.stepOf, 2), fontSize = 12.sp, color = textSecondary)
    Spacer(Modifier.height(20.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { if (age > 16) onAgeChange(age - 1) }) {
            Icon(Icons.Default.Remove, contentDescription = strings.decrease, tint = textPrimary)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                                .background(cardBg, RoundedCornerShape(14.dp))
                .border(1.dp, border, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("$age", fontSize = 28.sp, color = accent, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = { if (age < 70) onAgeChange(age + 1) }) {
            Icon(Icons.Default.Add, contentDescription = strings.increase, tint = textPrimary)
        }
    }

    Spacer(Modifier.height(20.dp))
    Text(strings.whatsYourGender, color = textSecondary, fontSize = 13.sp)
    Spacer(Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val genders = listOf("male" to strings.male, "female" to strings.female)
        genders.forEach { (key, label) ->
            val isSelected = selectedGender == key
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                                        .background(if (isSelected) accent else cardBg, RoundedCornerShape(14.dp))
                    .border(1.dp, if (isSelected) accent else border, RoundedCornerShape(14.dp))
                    .clickable { onGenderSelect(key) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (isSelected) Color.White else textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    Spacer(Modifier.height(20.dp))
    Text(strings.weight, color = textSecondary, fontSize = 13.sp)
    Spacer(Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { if (weight > 30f) onWeightChange(weight - 1f) }) {
            Icon(Icons.Default.Remove, contentDescription = strings.decrease, tint = textPrimary)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                                .background(cardBg, RoundedCornerShape(14.dp))
                .border(1.dp, border, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(String.format("%.1f kg", weight), fontSize = 24.sp, color = accent, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = { if (weight < 200f) onWeightChange(weight + 1f) }) {
            Icon(Icons.Default.Add, contentDescription = strings.increase, tint = textPrimary)
        }
    }

    Spacer(Modifier.height(20.dp))
    Text(strings.height, color = textSecondary, fontSize = 13.sp)
    Spacer(Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { if (height > 100f) onHeightChange(height - 1f) }) {
            Icon(Icons.Default.Remove, contentDescription = strings.decrease, tint = textPrimary)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                                .background(cardBg, RoundedCornerShape(14.dp))
                .border(1.dp, border, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(String.format("%.0f cm", height), fontSize = 24.sp, color = accent, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = { if (height < 230f) onHeightChange(height + 1f) }) {
            Icon(Icons.Default.Add, contentDescription = strings.increase, tint = textPrimary)
        }
    }
}

@Composable
private fun ActivityStep(
    selected: String,
    onSelect: (String) -> Unit,
    strings: LanguageManager.Strings,
    accent: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    border: Color
) {
    data class Level(val key: String, val label: String, val icon: ImageVector, val desc: String)
    val levels = remember(strings) { listOf(
        Level("sedentary", strings.sedentary, Icons.Default.EventSeat, strings.sedentaryDesc),
        Level("active", strings.active, Icons.Default.DirectionsWalk, strings.activeDesc),
        Level("very_active", strings.veryActive, Icons.Default.DirectionsRun, strings.veryActiveDesc)
    ) }

    Text(
        strings.whatsYourActivityLevel,
        style = MaterialTheme.typography.headlineMedium,
        color = textPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(6.dp))
    Text(String.format(strings.stepOf, 3), fontSize = 12.sp, color = textSecondary)
    Spacer(Modifier.height(20.dp))

    levels.forEach { level ->
        val isSelected = selected == level.key
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                                .background(if (isSelected) accent.copy(alpha = 0.15f) else cardBg, RoundedCornerShape(14.dp))
                .border(1.dp, if (isSelected) accent else border, RoundedCornerShape(14.dp))
                .clickable { onSelect(level.key) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(level.icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = if (isSelected) accent else textSecondary)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(level.label, style = MaterialTheme.typography.titleMedium, color = if (isSelected) accent else textPrimary)
                Text(level.desc, fontSize = 11.sp, color = textSecondary)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun ExperienceStep(
    selected: String,
    onSelect: (String) -> Unit,
    strings: LanguageManager.Strings,
    accent: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    border: Color
) {
    data class Level(val key: String, val label: String, val icon: ImageVector, val desc: String)
    val levels = remember(strings) { listOf(
        Level("beginner", strings.beginnerLabel, Icons.Default.ChildCare, strings.beginnerDesc),
        Level("intermediate", strings.intermediateLabel, Icons.Default.Person, strings.intermediateDesc),
        Level("advanced", strings.advancedLabel, Icons.Default.EmojiEvents, strings.advancedDesc)
    ) }

    Text(
        strings.whatsYourExperience,
        style = MaterialTheme.typography.headlineMedium,
        color = textPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(6.dp))
    Text(String.format(strings.stepOf, 4), fontSize = 12.sp, color = textSecondary)
    Spacer(Modifier.height(20.dp))

    levels.forEach { level ->
        val isSelected = selected == level.key
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                                .background(if (isSelected) accent.copy(alpha = 0.15f) else cardBg, RoundedCornerShape(14.dp))
                .border(1.dp, if (isSelected) accent else border, RoundedCornerShape(14.dp))
                .clickable { onSelect(level.key) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(level.icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = if (isSelected) accent else textSecondary)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(level.label, style = MaterialTheme.typography.titleMedium, color = if (isSelected) accent else textPrimary)
                Text(level.desc, fontSize = 11.sp, color = textSecondary)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun EquipmentStep(
    selected: String,
    onSelect: (String) -> Unit,
    strings: LanguageManager.Strings,
    accent: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    border: Color
) {
    data class Equip(val key: String, val label: String, @androidx.annotation.DrawableRes val iconRes: Int, val desc: String)
    val options = remember(strings) { listOf(
        Equip("home_no_equipment", strings.homeNoEquip, R.drawable.onboarding_strength, strings.homeNoEquipDesc),
        Equip("home_dumbbells", strings.homeDumbbells, R.drawable.onboarding_dumbbells, strings.homeDumbbellsDesc),
        Equip("full_gym", strings.fullGym, R.drawable.onboarding_full_gym, strings.fullGymDesc)
    ) }

    Text(
        strings.whatEquipment,
        style = MaterialTheme.typography.headlineMedium,
        color = textPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(6.dp))
    Text(String.format(strings.stepOf, 5), fontSize = 12.sp, color = textSecondary)
    Spacer(Modifier.height(20.dp))

    options.forEach { equip ->
        val isSelected = selected == equip.key
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                                .background(if (isSelected) accent.copy(alpha = 0.15f) else cardBg, RoundedCornerShape(14.dp))
                .border(1.dp, if (isSelected) accent else border, RoundedCornerShape(14.dp))
                .clickable { onSelect(equip.key) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = equip.iconRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                colorFilter = if (isSelected) ColorFilter.tint(accent) else ColorFilter.tint(textSecondary)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(equip.label, style = MaterialTheme.typography.titleMedium, color = if (isSelected) accent else textPrimary)
                Text(equip.desc, fontSize = 11.sp, color = textSecondary)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun FrequencyStep(
    sessions: Int,
    onSessionsChange: (Int) -> Unit,
    selectedDays: Set<String>,
    onDaysChange: (Set<String>) -> Unit,
    limitations: String,
    onLimitationsChange: (String) -> Unit,
    strings: LanguageManager.Strings,
    accent: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    border: Color
) {
    Text(
        strings.trainingFrequency,
        style = MaterialTheme.typography.headlineMedium,
        color = textPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(6.dp))
    Text(String.format(strings.stepOf, 6), fontSize = 12.sp, color = textSecondary)
    Spacer(Modifier.height(20.dp))

    Text(strings.sessionsPerWeek, color = textSecondary, fontSize = 13.sp)
    Spacer(Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 1..6) {
            val isSelected = sessions == i
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                                        .background(if (isSelected) accent else cardBg, RoundedCornerShape(12.dp))
                    .border(1.dp, if (isSelected) accent else border, RoundedCornerShape(12.dp))
                    .clickable { onSessionsChange(i) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$i",
                    color = if (isSelected) Color.White else textPrimary,
                    fontSize = 18.sp
                )
            }
        }
    }

    Spacer(Modifier.height(20.dp))
    Text(strings.selectTrainingDays, color = textSecondary, fontSize = 13.sp)
    Spacer(Modifier.height(4.dp))
    Text(
        "${selectedDays.size}/$sessions",
        color = if (selectedDays.size == sessions) accent else textSecondary,
        fontSize = 12.sp
    )
    Spacer(Modifier.height(8.dp))

    val dayKeys = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")
    val dayLabels = listOf(strings.monday, strings.tuesday, strings.wednesday, strings.thursday, strings.friday, strings.saturday, strings.sunday)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        dayKeys.forEachIndexed { index, key ->
            val isSelected = key in selectedDays
            val canSelect = isSelected || selectedDays.size < sessions
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                                        .background(
                        when {
                            isSelected -> accent
                            canSelect -> cardBg
                            else -> cardBg.copy(alpha = 0.4f)
                        }
                    , RoundedCornerShape(10.dp))
                    .border(
                        1.5.dp,
                        if (isSelected) accent else if (canSelect) accent.copy(alpha = 0.6f) else border.copy(alpha = 0.3f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = canSelect) {
                        onDaysChange(
                            if (isSelected) selectedDays - key
                            else selectedDays + key
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    dayLabels[index].take(3),
                    color = when {
                        isSelected -> Color.White
                        canSelect -> textPrimary
                        else -> textSecondary.copy(alpha = 0.4f)
                    },
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }

    Spacer(Modifier.height(20.dp))
    Text(strings.physicalLimitations, color = textSecondary, fontSize = 13.sp)
    Spacer(Modifier.height(8.dp))

    OutlinedTextField(
        value = limitations,
        onValueChange = onLimitationsChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(strings.physicalLimitationsPlaceholder, color = textSecondary, fontSize = 12.sp) },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accent,
            unfocusedBorderColor = border,
            focusedContainerColor = cardBg,
            unfocusedContainerColor = cardBg,
            focusedTextColor = textPrimary,
            unfocusedTextColor = textPrimary,
            cursorColor = accent
        ),
        minLines = 2
    )
}

@Composable
private fun MuscleGroupStep(
    selectedGroups: Set<String>,
    onToggle: (String) -> Unit,
    strings: LanguageManager.Strings,
    accent: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    border: Color
) {
    data class MuscleGroup(val key: String, val label: String, val iconRes: Int)
    val groups = remember(strings) { listOf(
        MuscleGroup("fullbody", strings.fullBody, R.drawable.onboarding_full_body),
        MuscleGroup("chest", strings.chest, R.drawable.onboarding_chest),
        MuscleGroup("back", strings.back, R.drawable.onboarding_back),
        MuscleGroup("legs", strings.legs, R.drawable.onboarding_legs),
        MuscleGroup("shoulders", strings.shoulders, R.drawable.onboarding_shoulders),
        MuscleGroup("arms", strings.arms, R.drawable.onboarding_arms),
        MuscleGroup("glutes", strings.glutes, R.drawable.onboarding_glutes),
        MuscleGroup("core", strings.core, R.drawable.onboarding_core),
        MuscleGroup("cardio", strings.cardio, R.drawable.onboarding_cardio)
    ) }

    Text(
        strings.whichMuscleGroups,
        style = MaterialTheme.typography.headlineMedium,
        color = textPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(6.dp))
    Text(strings.selectAtLeastOne, fontSize = 12.sp, color = textSecondary)
    Spacer(Modifier.height(20.dp))

    val allIndividualKeys = setOf("chest", "back", "legs", "shoulders", "arms", "glutes", "core", "cardio")
    groups.forEach { group ->
        val isSelected = if (group.key == "fullbody") selectedGroups.containsAll(allIndividualKeys)
        else group.key in selectedGroups
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                                .background(if (isSelected) accent.copy(alpha = 0.15f) else cardBg, RoundedCornerShape(14.dp))
                .border(1.dp, if (isSelected) accent else border, RoundedCornerShape(14.dp))
                .clickable { onToggle(group.key) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.ui.platform.LocalContext.current.let { ctx ->
                androidx.compose.foundation.Image(
                    painter = painterResource(id = group.iconRes),
                    contentDescription = group.label,
                    modifier = Modifier.size(26.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(if (isSelected) accent else textSecondary)
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                group.label,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) accent else textPrimary,
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle(group.key) },
                colors = CheckboxDefaults.colors(checkedColor = accent, uncheckedColor = border)
            )
        }
    }
}
