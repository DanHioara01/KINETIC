package com.example.kinetic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.AppSectionLabel
import com.example.kinetic.ui.theme.AppPalette
import com.example.kinetic.ui.theme.appPalette
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackingScreen(
    isDark: Boolean = isSystemInDarkTheme(),
    preferencesManager: PreferencesManager,
    strings: LanguageManager.Strings,
    paddingValues: PaddingValues
) {
    val p = appPalette(isDark)
    var todayWaterMl by remember { mutableIntStateOf(preferencesManager.getTodayWaterMl()) }
    val waterGoal = preferencesManager.getWaterGoalMl()
    var customMl by remember { mutableStateOf("") }
    var waterHistory by remember { mutableStateOf(preferencesManager.getWaterHistory7Days()) }

    // Re-read the stored water when the app comes back to the foreground, so water
    // added from the home-screen widget shows up here instantly (no restart needed).
    LifecycleResumeEffect(Unit) {
        todayWaterMl = preferencesManager.getTodayWaterMl()
        waterHistory = preferencesManager.getWaterHistory7Days()
        onPauseOrDispose { }
    }

    val isComplete = todayWaterMl >= waterGoal

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var alarms by remember { mutableStateOf(preferencesManager.getWaterReminders()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var editingAlarmId by remember { mutableIntStateOf(-1) }
    var pickerHour by remember { mutableIntStateOf(9) }
    var pickerMinute by remember { mutableIntStateOf(0) }

    val onAddWater: (Int) -> Unit = { ml ->
        preferencesManager.addWaterMl(ml)
        todayWaterMl = preferencesManager.getTodayWaterMl()
        waterHistory = preferencesManager.getWaterHistory7Days()
        // Refresh the water widget instantly
        scope.launch {
            try {
                KineticGlanceWidget().updateAll(context)
            } catch (_: Exception) {}
        }
    }

    val onResetWater: () -> Unit = {
        preferencesManager.resetTodayWaterMl()
        todayWaterMl = preferencesManager.getTodayWaterMl()
        waterHistory = preferencesManager.getWaterHistory7Days()
        scope.launch {
            try {
                KineticGlanceWidget().updateAll(context)
            } catch (_: Exception) {}
        }
    }

    var visibleItems by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        // Fade-in rapid: toate secțiunile apar simultan (300ms), fără stagger
        visibleItems = 7
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(p.bg),
        contentPadding = PaddingValues(
            start = 18.dp,
            top = paddingValues.calculateTopPadding() + 16.dp,
            end = 18.dp,
            bottom = paddingValues.calculateBottomPadding() + AppConstants.BOTTOM_NAV_PADDING + 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AnimatedVisibility(
                visible = visibleItems > 0,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Text(
                strings.waterIntake,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.W900,
                fontSize = 30.sp,
                color = p.tp,
                letterSpacing = (-1.5).sp
            )
            }
        }

        item {
            AnimatedVisibility(
                visible = visibleItems > 1,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WaterBottle(
                    levelFraction = if (waterGoal > 0) todayWaterMl.toFloat() / waterGoal else 0f
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            todayWaterMl.toString(),
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.W800,
                            fontSize = 22.sp,
                            color = p.ac
                        )
                        Text(
                            strings.ml,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = p.tt
                        )
                    }
                    Text(
                        "/",
                        fontSize = 20.sp,
                        color = p.tt,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$waterGoal",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.W800,
                            fontSize = 22.sp,
                            color = p.ts
                        )
                        Text(
                            strings.ml,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = p.tt
                        )
                    }
                }
            }
            }
        }

        item {
            AnimatedVisibility(
                visible = visibleItems > 2,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Column {
                AppSectionLabel(strings.addWater, p)
                QuickAddRow(strings, p, onAddWater, onResetWater)
                Spacer(Modifier.height(8.dp))
                CustomAddRow(
                    text = customMl,
                    onTextChange = { customMl = it },
                    onAdd = {
                        val v = customMl.toIntOrNull()
                        if (v != null && v in 1..5000) {
                            onAddWater(v)
                            customMl = ""
                        }
                    },
                    p = p,
                    strings = strings
                )
            }
            }
        }

        item {
            AnimatedVisibility(
                visible = visibleItems > 3,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Column {
                AppSectionLabel(strings.weeklyHistory, p)
                HistoryCard(
                    waterHistory = waterHistory,
                    waterGoal = waterGoal,
                    p = p,
                    strings = strings
                )
            }
            }
        }

        item {
            AnimatedVisibility(
                visible = visibleItems > 4,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Column {
                AppSectionLabel(strings.tips, p)
                TipsRow(p = p, strings = strings)
            }
            }
        }

        if (alarms.isNotEmpty()) {
            item {
                AnimatedVisibility(
                    visible = visibleItems > 5,
                    enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
                ) {
                Column {
                    AppSectionLabel(strings.reminder.uppercase(), p)
                    alarms.forEachIndexed { index, alarm ->
                        if (index > 0) Spacer(Modifier.height(8.dp))
                        AlarmItem(
                            alarm = alarm,
                            p = p,
                            onEdit = {
                                editingAlarmId = alarm.id
                                pickerHour = alarm.hour
                                pickerMinute = alarm.minute
                                showTimePicker = true
                            },
                            onToggle = { enabled ->
                                preferencesManager.toggleWaterReminder(alarm.id, enabled)
                                alarms = preferencesManager.getWaterReminders()
                                val receiver = WaterReminderReceiver()
                                if (enabled) receiver.scheduleAlarm(context, alarm.id)
                                else receiver.cancelAlarm(context, alarm.id)
                            },
                            onDelete = {
                                preferencesManager.deleteWaterReminder(alarm.id)
                                alarms = preferencesManager.getWaterReminders()
                                WaterReminderReceiver().cancelAlarm(context, alarm.id)
                            }
                        )
                    }
                }
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = visibleItems > 6,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            AppGlassCard(
                modifier = Modifier.fillMaxWidth().clickable {
                    editingAlarmId = -1
                    pickerHour = 9
                    pickerMinute = 0
                    showTimePicker = true
                },
                p = p,
                cornerRadius = 12.dp,
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = p.ac, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.reminder, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = p.ac)
                }
            }
            }
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = pickerHour,
            initialMinute = pickerMinute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = p.sf,
            titleContentColor = p.tp,
            textContentColor = p.ts,
            title = { Text(strings.selectTime, fontWeight = FontWeight.Bold) },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        selectorColor = p.ac,
                        containerColor = p.sf
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editingAlarmId >= 0) {
                            preferencesManager.updateWaterReminder(editingAlarmId, timePickerState.hour, timePickerState.minute)
                        } else {
                            preferencesManager.addWaterReminder(timePickerState.hour, timePickerState.minute)
                        }
                        alarms = preferencesManager.getWaterReminders()
                        WaterReminderReceiver().scheduleAllEnabledAlarms(context)
                        showTimePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = p.ac),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.confirm, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                if (editingAlarmId >= 0) {
                    TextButton(onClick = {
                        preferencesManager.deleteWaterReminder(editingAlarmId)
                        alarms = preferencesManager.getWaterReminders()
                        WaterReminderReceiver().cancelAlarm(context, editingAlarmId)
                        showTimePicker = false
                    }) {
                        Text(strings.delete, color = p.rs)
                    }
                }
                TextButton(onClick = { showTimePicker = false }) {
                    Text(strings.cancel, color = p.ts)
                }
            }
        )
    }
}

@Composable
private fun QuickAddRow(strings: LanguageManager.Strings, p: AppPalette, onAdd: (Int) -> Unit, onReset: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(250, 500).forEach { ml ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(p.ac, Color(0xFFFF6B4A).copy(alpha = 0.9f))))
                    .clickable { onAdd(ml) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Text(
                        "$ml ml",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(p.rs.copy(alpha = 0.15f))
                .clickable { onReset() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = strings.reset,
                tint = p.rs,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CustomAddRow(
    text: String,
    onTextChange: (String) -> Unit,
    onAdd: () -> Unit,
    p: AppPalette,
    strings: LanguageManager.Strings
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { input ->
                val filtered = input.filter { c -> c.isDigit() }
                if (filtered.length <= 4) onTextChange(filtered)
            },
            modifier = Modifier.weight(1f).height(48.dp),
            placeholder = {
                Text(strings.customMl, color = p.ts, fontSize = 14.sp)
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                color = p.tp,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = p.cr,
                focusedContainerColor = p.acs,
                unfocusedBorderColor = p.bd,
                focusedBorderColor = p.ac.copy(alpha = 0.3f),
                cursorColor = p.ac
            ),
            singleLine = true
        )

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(p.ac, Color(0xFFFF6B4A).copy(alpha = 0.9f))))
                .clickable { onAdd() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun HistoryCard(
    waterHistory: List<Pair<String, Int>>,
    waterGoal: Int,
    p: AppPalette,
    strings: LanguageManager.Strings
) {
    AppGlassCard(p = p) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val maxMl = waterHistory.maxOfOrNull { it.second }?.coerceAtLeast(waterGoal) ?: waterGoal

                waterHistory.forEachIndexed { i, (dayName, ml) ->
                    val isToday = i == waterHistory.lastIndex
                    val barPct = (ml.toFloat() / maxMl).coerceIn(0.02f, 1f)
                    val animatedBar by animateFloatAsState(
                        targetValue = barPct,
                        animationSpec = tween(1200, easing = FastOutSlowInEasing),
                        label = "bar_$i"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Zonă dedicată barelor: bara crește DOAR aici și nu poate
                        // acoperi niciodată eticheta zilei de sub ea.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .fillMaxHeight(animatedBar.coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                                    .background(
                                        if (isToday) Brush.horizontalGradient(listOf(p.ac, Color(0xFFFF6B4A).copy(alpha = 0.9f)))
                                        else if (ml >= waterGoal) Brush.horizontalGradient(listOf(p.gn.copy(alpha = 0.5f), p.gn.copy(alpha = 0.3f)))
                                        else Brush.horizontalGradient(listOf(p.ac.copy(alpha = 0.3f), p.ac.copy(alpha = 0.15f)))
                                    )
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            dayName,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.3.sp,
                            color = if (isToday) p.ac else p.ts
                        )
                    }
                }
            }

            val avg = waterHistory.sumOf { it.second } / waterHistory.size
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${strings.average}: $avg ml/zi", fontSize = 9.sp, color = p.ts)
                Text("${strings.target}: $waterGoal ml", fontSize = 9.sp, color = p.ts)
            }
        }
    }
}

@Composable
private fun TipsRow(p: AppPalette, strings: LanguageManager.Strings) {
    val tipTexts = listOf(strings.waterTip1, strings.waterTip2)

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppGlassCard(modifier = Modifier.weight(1f), p = p, cornerRadius = 12.dp, contentPadding = PaddingValues(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(p.acs),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Favorite, null, tint = p.ac, modifier = Modifier.size(12.dp))
                }
                Text(tipTexts[0], fontSize = 10.sp, color = p.ts, lineHeight = 15.sp)
            }
        }

        AppGlassCard(modifier = Modifier.weight(1f), p = p, cornerRadius = 12.dp, contentPadding = PaddingValues(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(p.gns),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Schedule, null, tint = p.gn, modifier = Modifier.size(12.dp))
                }
                Text(tipTexts[1], fontSize = 10.sp, color = p.ts, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
private fun AlarmItem(
    alarm: WaterAlarm,
    p: AppPalette,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val strings = LanguageManager.getStrings(context)

    AppGlassCard(
        modifier = Modifier.fillMaxWidth(),
        p = p,
        cornerRadius = 14.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(if (alarm.enabled) p.acs else p.cr, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications, null,
                    tint = if (alarm.enabled) p.ac else p.ts.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f).clickable { onEdit() }
            ) {
                Text(
                    "${alarm.hour.toString().padStart(2, '0')}:${alarm.minute.toString().padStart(2, '0')}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.enabled) p.tp else p.ts.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
                Text(
                    strings.everyDay,
                    fontSize = 12.sp,
                    color = if (alarm.enabled) p.ac else p.ts.copy(alpha = 0.4f)
                )
            }
            Switch(
                checked = alarm.enabled,
                onCheckedChange = { onToggle(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = p.ac,
                    uncheckedThumbColor = p.ts,
                    uncheckedTrackColor = p.ts.copy(alpha = 0.3f)
                )
            )
        }
    }
}

// ═══════════════════════════════════════════
//  Sticlă sport matte black — umplere animată,
//  valuri + bule (fără logo, fără butoane)
// ═══════════════════════════════════════════

@Composable
fun WaterBottle(
    levelFraction: Float,
    modifier: Modifier = Modifier
) {
    // Nivelul țintă din aplicație — se animă ușor la fiecare adăugare de apă
    val animatedLevel by animateFloatAsState(
        targetValue = levelFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "waterLevel"
    )
    val fillPercent = animatedLevel * 100f

    // ── Wave Animation ──
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // ── Bubbles ──
    val bubbles = remember { List(8) { BubbleState() } }
    val bubblesActive = fillPercent > 5f

    // ── Floating Animation (premium feel) ──
    val infiniteTransition2 = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition2.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                0f at 0 using LinearEasing
                -5f at 1000 using FastOutSlowInEasing
                0f at 2000 using LinearEasing
                5f at 3000 using FastOutSlowInEasing
                0f at 4000 using LinearEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "floatOffset"
    )

    // ── Bubble Animation ──
    bubbles.forEach { bubble ->
        LaunchedEffect(bubblesActive) {
            while (bubblesActive) {
                if (bubble.active) {
                    bubble.y -= bubble.speed
                    if (bubble.y < 0f) bubble.reset()
                } else if (Random.nextFloat() < 0.02f) {
                    bubble.activate()
                }
                delay(16)
            }
        }
    }

    Box(
        modifier = modifier.offset(y = floatOffset.dp),
        contentAlignment = Alignment.Center
    ) {
        BottleCanvas(
            fillPercent = fillPercent,
            wavePhase = wavePhase,
            bubbles = bubbles
        )
    }
}

@Composable
private fun BottleCanvas(
    fillPercent: Float,
    wavePhase: Float,
    bubbles: List<BubbleState>
) {
    Canvas(modifier = Modifier.size(126.dp, 364.dp)) {
        val w = size.width
        val h = size.height

        // ── Dimensions ──
        val bodyLeft = 20f
        val bodyRight = w - 20f
        val bodyWidth = bodyRight - bodyLeft
        val bodyTop = 120f
        val bodyBottom = h - 20f
        val bodyHeight = bodyBottom - bodyTop
        val cornerRadius = 22f

        // ── 0. GLASSMORPHISM GLOW (subtil, în spatele sticlei) ──
        val glassCenterX = bodyLeft + bodyWidth / 2f
        val glassCenterY = bodyTop + bodyHeight / 2f
        val glassRadius = bodyWidth * 0.9f
        // Glow principal — albastru deschis, difuz
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1E8CD6).copy(alpha = 0.08f),
                    Color(0xFF0A50A0).copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(glassCenterX, glassCenterY),
                radius = glassRadius
            ),
            topLeft = Offset(glassCenterX - glassRadius, glassCenterY - glassRadius),
            size = Size(glassRadius * 2, glassRadius * 2)
        )
        // Glow secundar — mai larg, mai subtil
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.04f),
                    Color.White.copy(alpha = 0.02f),
                    Color.Transparent
                ),
                center = Offset(glassCenterX - bodyWidth * 0.15f, glassCenterY - bodyHeight * 0.1f),
                radius = glassRadius * 1.2f
            ),
            topLeft = Offset(glassCenterX - glassRadius * 1.2f, glassCenterY - glassRadius * 1.2f),
            size = Size(glassRadius * 2.4f, glassRadius * 2.4f)
        )

        // ── 1. CAP ASSEMBLY ──
        drawCapAssembly(w, bodyTop)




        drawRoundRect(
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1A1A1A), Color(0xFF2A2A2A), Color(0xFF1E1E1E),
                    Color(0xFF151515), Color(0xFF0F0F0F), Color(0xFF1A1A1A),
                    Color(0xFF222222), Color(0xFF1A1A1A), Color(0xFF111111),
                    Color(0xFF0D0D0D), Color(0xFF151515)
                ),
                start = Offset(bodyLeft, bodyTop),
                end = Offset(bodyRight, bodyBottom)
            )
        )

        // ══════════════════════════════════
        //  8. WATER FILL (clipped to body, behind edges)
        // ══════════════════════════════════
        if (fillPercent > 0f) {
            // Apa rămâne strict în interiorul conturului sticlei — fundul exact la bodyBottom
            val waterBottom = bodyBottom
            val waterHeight = (bodyHeight * fillPercent / 100f)
            // La 100% umplere, apa nu ajunge până sus — se oprește cu 5px sub vârful corpului
            val maxWaterTop = bodyTop + 5f
            val waterTop = (waterBottom - waterHeight).coerceAtLeast(maxWaterTop)
            val waterHeightEffective = waterBottom - waterTop

            // Clip path pentru apă — ușor mai sus ca să ascundă colțurile de jos
            val waterClipPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = bodyLeft + 1f,
                        top = bodyTop + 1f,
                        right = bodyRight - 1f,
                        bottom = bodyBottom - 2f,
                        cornerRadius = CornerRadius(cornerRadius - 1f, cornerRadius - 1f)
                    )
                )
            }

            clipPath(path = waterClipPath) {
                // ── Water body — până la bottomul sticlei ──
                val waterBodyPath = Path().apply {
                    moveTo(bodyLeft, waterTop)
                    lineTo(bodyLeft, bodyBottom - cornerRadius)
                    quadraticTo(bodyLeft, bodyBottom, bodyLeft + cornerRadius, bodyBottom)
                    lineTo(bodyRight - cornerRadius, bodyBottom)
                    quadraticTo(bodyRight, bodyBottom, bodyRight, bodyBottom - cornerRadius)
                    lineTo(bodyRight, waterTop)
                    close()
                }
                drawPath(
                    path = waterBodyPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E8CD6).copy(alpha = 0.30f),
                            Color(0xFF1478C8).copy(alpha = 0.40f),
                            Color(0xFF0F64B4).copy(alpha = 0.50f),
                            Color(0xFF0A50A0).copy(alpha = 0.60f),
                            Color(0xFF053C8C).copy(alpha = 0.70f),
                            Color(0xFF053278).copy(alpha = 0.80f)
                        ),
                        start = Offset(0f, waterTop),
                        end = Offset(0f, waterBottom)
                    )
                )

                // ── Caustic light effect ──
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF64C8FF).copy(alpha = 0.08f), Color.Transparent
                        )
                    ),
                    topLeft = Offset(bodyLeft + bodyWidth * 0.2f, waterTop + waterHeightEffective * 0.2f),
                    size = Size(bodyWidth * 0.4f, waterHeightEffective * 0.5f)
                )

                // ── Water highlight streak ──
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF96DCFF).copy(alpha = 0.08f),
                            Color(0xFF96DCFF).copy(alpha = 0.15f),
                            Color(0xFF96DCFF).copy(alpha = 0.1f),
                            Color(0xFF96DCFF).copy(alpha = 0.05f)
                        ),
                        start = Offset(0f, waterTop),
                        end = Offset(0f, bodyBottom)
                    ),
                    topLeft = Offset(bodyLeft + 26f, waterTop),
                    size = Size(10f, waterBottom - waterTop),
                    blendMode = BlendMode.Plus
                )

                // ══ WAVE SURFACE ══
                val wavePath = Path().apply {
                    moveTo(bodyLeft, waterTop)

                    // Wave curve (top, left-to-right for proper fill)
                    val steps = 40
                    val stepWidth = bodyWidth / steps
                    for (i in 0..steps) {
                        val x = bodyLeft + i * stepWidth
                        val normalizedX = i.toFloat() / steps
                        val wave1 = sin(normalizedX * 4f * Math.PI + wavePhase * 2) * 3f
                        val wave2 = sin(normalizedX * 6f * Math.PI + wavePhase * 3 + 1f) * 2f
                        val y = waterTop + wave1.toFloat() + wave2.toFloat()
                        lineTo(x, y)
                    }

                    lineTo(bodyRight, bodyBottom - cornerRadius)
                    quadraticTo(bodyRight, bodyBottom, bodyRight - cornerRadius, bodyBottom)
                    lineTo(bodyLeft + cornerRadius, bodyBottom)
                    quadraticTo(bodyLeft, bodyBottom, bodyLeft, bodyBottom - cornerRadius)
                    lineTo(bodyLeft, waterTop)
                    close()
                }

                drawPath(
                    path = wavePath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E8CD6).copy(alpha = 0.35f),
                            Color(0xFF1478C8).copy(alpha = 0.50f),
                            Color(0xFF0F64B4).copy(alpha = 0.65f)
                        ),
                        start = Offset(0f, waterTop),
                        end = Offset(0f, waterTop + 30f)
                    )
                )

                // Second wave layer
                val wavePath2 = Path().apply {
                    moveTo(bodyLeft, waterTop + 4f)

                    val steps = 40
                    val stepWidth = bodyWidth / steps
                    for (i in 0..steps) {
                        val x = bodyLeft + i * stepWidth
                        val normalizedX = i.toFloat() / steps
                        val wave1 = sin(normalizedX * 5f * Math.PI + wavePhase * 2.5f + 1f) * 2.5f
                        val wave2 = sin(normalizedX * 7f * Math.PI + wavePhase * 1.8f + 2f) * 1.5f
                        val y = waterTop + 4f + wave1.toFloat() + wave2.toFloat()
                        lineTo(x, y)
                    }

                    lineTo(bodyRight, bodyBottom - cornerRadius)
                    quadraticTo(bodyRight, bodyBottom, bodyRight - cornerRadius, bodyBottom)
                    lineTo(bodyLeft + cornerRadius, bodyBottom)
                    quadraticTo(bodyLeft, bodyBottom, bodyLeft, bodyBottom - cornerRadius)
                    lineTo(bodyLeft, waterTop + 4f)
                    close()
                }

                drawPath(
                    path = wavePath2,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF3AA8F0).copy(alpha = 0.1f),
                            Color(0xFF2890E0).copy(alpha = 0.22f)
                        ),
                        start = Offset(0f, waterTop + 4f),
                        end = Offset(0f, waterTop + 34f)
                    )
                )

                // ── BUBBLES ──
                bubbles.forEach { bubble ->
                    if (bubble.active && bubble.x in 0f..1f) {
                        val bx = bodyLeft + bubble.x * bodyWidth
                        val by = waterTop + bubble.y * waterHeightEffective

                        if (by in waterTop..waterBottom) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFB4E6FF).copy(alpha = 0.4f),
                                        Color(0xFF64B4F0).copy(alpha = 0.15f),
                                        Color(0xFF3C96DC).copy(alpha = 0.05f)
                                    ),
                                    center = Offset(bx - bubble.radius * 0.3f, by - bubble.radius * 0.3f),
                                    radius = bubble.radius * 1.2f
                                ),
                                radius = bubble.radius,
                                center = Offset(bx, by)
                            )
                            drawCircle(
                                color = Color(0xFFC8F0FF).copy(alpha = 0.15f),
                                radius = bubble.radius,
                                center = Offset(bx, by),
                                style = Stroke(width = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // ── 2b. BOTTLE BODY EDGE (groasă, bordură vizibilă) ──
        drawRoundRect(
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF3A3A3A), Color(0xFF4A4A4A), Color(0xFF3A3A3A),
                    Color(0xFF333333), Color(0xFF444444), Color(0xFF3A3A3A)
                ),
                start = Offset(bodyLeft, bodyTop),
                end = Offset(bodyRight, bodyBottom)
            ),
            style = Stroke(width = 4.5f)
        )
        // Inner shadow edge (subtil, adâncime)
        drawRoundRect(
            topLeft = Offset(bodyLeft + 1f, bodyTop + 1f),
            size = Size(bodyWidth - 2f, bodyHeight - 2f),
            cornerRadius = CornerRadius(cornerRadius - 1f, cornerRadius - 1f),
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.06f), Color.Transparent,
                    Color.Transparent, Color.White.copy(alpha = 0.03f)
                ),
                start = Offset(bodyLeft, bodyTop),
                end = Offset(bodyRight, bodyBottom)
            ),
            style = Stroke(width = 1.5f)
        )

        // ── 3. BRUSHED ALUMINUM TEXTURE ──
        for (y in bodyTop.toInt()..bodyBottom.toInt() step 2) {
            drawLine(
                color = Color.White.copy(alpha = 0.008f),
                start = Offset(bodyLeft, y.toFloat()),
                end = Offset(bodyRight, y.toFloat()),
                strokeWidth = 1f
            )
        }

        // ── 4. MAIN HIGHLIGHT STRIPE ──
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent, Color.White.copy(alpha = 0.06f),
                    Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                start = Offset(0f, bodyTop),
                end = Offset(0f, bodyBottom)
            ),
            topLeft = Offset(bodyLeft + 28f, bodyTop),
            size = Size(18f, bodyHeight),
            blendMode = BlendMode.Plus
        )

        // ── 5. CRIMSON ACCENT STRIPE (clipat la forma sticlei) ──
        val stripeBodyPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = bodyLeft,
                    top = bodyTop,
                    right = bodyRight,
                    bottom = bodyBottom,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            )
        }
        clipPath(path = stripeBodyPath) {
            // Bara roșie — se unește perfect cu colțurile sticlei
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFB41414).copy(alpha = 0.5f),
                        Color(0xFFDC2828).copy(alpha = 0.9f),
                        Color(0xFFFF3C32),
                        Color(0xFFDC2828).copy(alpha = 0.9f),
                        Color(0xFFB41414).copy(alpha = 0.5f)
                    ),
                    start = Offset(bodyLeft, 0f),
                    end = Offset(bodyRight, 0f)
                ),
                topLeft = Offset(bodyLeft, bodyTop - 4f),
                size = Size(bodyWidth, 10f)
            )
            // Crimson glow — mai larg, tot clipat
            drawRect(
                color = Color(0xFFDC2828).copy(alpha = 0.15f),
                topLeft = Offset(bodyLeft, bodyTop - 10f),
                size = Size(bodyWidth, 18f),
                blendMode = BlendMode.Plus
            )
        }

        // ── 6. RIM LIGHTING (mai groase) ──
        // Left rim (warm)
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent, Color(0xFFFF7864).copy(alpha = 0.6f),
                    Color(0xFFFFC8B4).copy(alpha = 0.9f), Color(0xFFFF7864).copy(alpha = 0.6f),
                    Color.Transparent
                ),
                start = Offset(0f, bodyTop),
                end = Offset(0f, bodyBottom)
            ),
            start = Offset(bodyLeft - 2f, bodyTop + bodyHeight * 0.03f),
            end = Offset(bodyLeft - 2f, bodyTop + bodyHeight * 0.97f),
            strokeWidth = 5f
        )
        // Right rim (cool)
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent, Color(0xFFC8B4FF).copy(alpha = 0.4f),
                    Color.White.copy(alpha = 0.6f), Color(0xFFC8B4FF).copy(alpha = 0.4f),
                    Color.Transparent
                ),
                start = Offset(0f, bodyTop),
                end = Offset(0f, bodyBottom)
            ),
            start = Offset(bodyRight + 2f, bodyTop + bodyHeight * 0.03f),
            end = Offset(bodyRight + 2f, bodyTop + bodyHeight * 0.97f),
            strokeWidth = 5f
        )
        // Top rim highlight (subtil)
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent, Color.White.copy(alpha = 0.12f),
                    Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                startX = bodyLeft,
                endX = bodyRight
            ),
            start = Offset(bodyLeft + cornerRadius, bodyTop),
            end = Offset(bodyRight - cornerRadius, bodyTop),
            strokeWidth = 2f
        )

        // ── 9. GRIP BAND (deasupra apei, mai înalt, mai groase marginile) ──
        val gripTop = bodyTop + bodyHeight * 0.45f
        val gripHeight = 120f
        drawRoundRect(
            topLeft = Offset(bodyLeft - 4f, gripTop),
            size = Size(bodyWidth + 8f, gripHeight),
            cornerRadius = CornerRadius(4f, 4f),
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1F1F1F), Color(0xFF292929), Color(0xFF1C1C1C),
                    Color(0xFF222222), Color(0xFF1A1A1A), Color(0xFF1E1E1E)
                ),
                start = Offset(bodyLeft, gripTop),
                end = Offset(bodyRight, gripTop + gripHeight)
            )
        )
        // Grip band border (mai gros)
        drawRoundRect(
            topLeft = Offset(bodyLeft - 4f, gripTop),
            size = Size(bodyWidth + 8f, gripHeight),
            cornerRadius = CornerRadius(4f, 4f),
            color = Color(0xFF444444),
            style = Stroke(width = 3f)
        )
        // Knurl pattern
        for (y in gripTop.toInt()..(gripTop + gripHeight).toInt() step 5) {
            drawLine(
                color = Color.Black.copy(alpha = 0.3f),
                start = Offset(bodyLeft - 4f, y.toFloat()),
                end = Offset(bodyRight + 4f, y.toFloat()),
                strokeWidth = 1f
            )
        }
        // ── Logo circle (centrat pe grip band, mai gros) ──
        val logoCenterX = bodyLeft + bodyWidth / 2f
        val logoCenterY = gripTop + gripHeight / 2f
        drawCircle(
            color = Color(0xFF444444).copy(alpha = 0.5f),
            radius = 14f,
            center = Offset(logoCenterX, logoCenterY),
            style = Stroke(width = 3f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.15f),
            radius = 12f,
            center = Offset(logoCenterX, logoCenterY),
            style = Stroke(width = 1.5f)
        )

        // ── 10. DROP SHADOW ──
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f))
            ),
            topLeft = Offset(bodyLeft, bodyBottom - 20f),
            size = Size(bodyWidth, 20f)
        )
    }
}

private fun DrawScope.drawCapAssembly(w: Float, bodyTop: Float) {
    val centerX = w / 2f

    // ── Cap Neck (latimea corpului, mai mare) ──
    drawRoundRect(
        topLeft = Offset(centerX - 78f, bodyTop - 34f),
        size = Size(156f, 34f),
        cornerRadius = CornerRadius(7f, 7f),
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF1D1D1D), Color(0xFF2D2D2D), Color(0xFF1F1F1F), Color(0xFF161616), Color(0xFF1A1A1A)),
            start = Offset(centerX - 78f, bodyTop - 34f),
            end = Offset(centerX + 78f, bodyTop)
        )
    )
    // Cap Neck border (mai gros)
    drawRoundRect(
        topLeft = Offset(centerX - 78f, bodyTop - 34f),
        size = Size(156f, 34f),
        cornerRadius = CornerRadius(7f, 7f),
        color = Color(0xFF444444),
        style = Stroke(width = 3f)
    )

    // ── Cap Top (latimea corpului, mai mare) ──
    drawRoundRect(
        topLeft = Offset(centerX - 78f, bodyTop - 62f),
        size = Size(156f, 28f),
        cornerRadius = CornerRadius(12f, 12f),
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF1A1A1A), Color(0xFF2A2A2A), Color(0xFF1C1C1C), Color(0xFF141414), Color(0xFF181818)),
            start = Offset(centerX - 78f, bodyTop - 62f),
            end = Offset(centerX + 78f, bodyTop - 34f)
        )
    )
    // Cap Top border (mai gros)
    drawRoundRect(
        topLeft = Offset(centerX - 78f, bodyTop - 62f),
        size = Size(156f, 28f),
        cornerRadius = CornerRadius(12f, 12f),
        color = Color(0xFF444444),
        style = Stroke(width = 3f)
    )

    // ── Cap Flip (tilted, mai mare) ──
    val flipPath = Path().apply {
        val fx = centerX - 46f
        val fy = bodyTop - 80f
        moveTo(fx, fy + 22f)
        lineTo(fx, fy + 4f)
        quadraticTo(fx + 46f, fy - 6f, fx + 92f, fy + 4f)
        lineTo(fx + 92f, fy + 22f)
        close()
    }
    drawPath(
        path = flipPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF1D1D1D), Color(0xFF2B2B2B), Color(0xFF1E1E1E), Color(0xFF151515)),
            start = Offset(centerX - 46f, bodyTop - 80f),
            end = Offset(centerX + 46f, bodyTop - 58f)
        )
    )

    // ── Carry Loop (mai gros) ──
    val loopPath = Path().apply {
        val lx = centerX + 46f
        val ly = bodyTop - 72f
        arcTo(
            rect = Rect(left = lx, top = ly, right = lx + 32f, bottom = ly + 46f),
            startAngleDegrees = -90f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
    }
    drawPath(
        path = loopPath,
        color = Color(0xFF3A3A3A),
        style = Stroke(width = 6f, cap = StrokeCap.Round)
    )
    drawPath(
        path = loopPath,
        color = Color(0xFF555555).copy(alpha = 0.4f),
        style = Stroke(width = 2f, cap = StrokeCap.Round)
    )
}

// ═══════════════════════════════════════════
//  Bubble State
// ═══════════════════════════════════════════

private class BubbleState {
    var x: Float by mutableFloatStateOf(Random.nextFloat())
    var y: Float by mutableFloatStateOf(1f)
    var radius: Float by mutableFloatStateOf(Random.nextFloat() * 3f + 2f)
    var speed: Float by mutableFloatStateOf(Random.nextFloat() * 0.015f + 0.005f)
    var active: Boolean by mutableStateOf(false)

    fun activate() {
        x = Random.nextFloat() * 0.6f + 0.2f
        y = 1f
        radius = Random.nextFloat() * 3f + 2f
        speed = Random.nextFloat() * 0.015f + 0.005f
        active = true
    }

    fun reset() {
        y = 1f
        x = Random.nextFloat() * 0.6f + 0.2f
        active = true
    }
}
