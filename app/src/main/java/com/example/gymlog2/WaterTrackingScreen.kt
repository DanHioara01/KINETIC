package com.example.gymlog2

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.gymlog2.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackingScreen(
    preferencesManager: PreferencesManager,
    strings: LanguageManager.Strings,
    accent: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    surfaceBg: Color,
    paddingValues: PaddingValues
) {
    var todayWaterMl by remember { mutableIntStateOf(preferencesManager.getTodayWaterMl()) }
    val waterGoal = preferencesManager.getWaterGoalMl()
    val progress = if (waterGoal > 0) (todayWaterMl.toFloat() / waterGoal).coerceIn(0f, 1f) else 0f
    var customMl by remember { mutableStateOf("") }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "water_progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val dividerBg = dividerColor()

    val waterCardBg = cardBg

    var waterHistory by remember { mutableStateOf(preferencesManager.getWaterHistory7Days()) }

    val context = LocalContext.current
    var alarms by remember { mutableStateOf(preferencesManager.getWaterReminders()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var editingAlarmId by remember { mutableIntStateOf(-1) }
    var pickerHour by remember { mutableIntStateOf(9) }
    var pickerMinute by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, accent, RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = strings.waterIntake.uppercase(),
                    color = accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.water_bottle),
                        contentDescription = "Water bottle",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    if (animatedProgress > 0f) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val bottleWidth = size.width * 0.72f
                            val bottleHeight = size.height * 0.76f
                            val bottleLeft = (size.width - bottleWidth) / 2f
                            val bottleTop = size.height * 0.13f
                            val cornerRadius = 8.dp.toPx()
                            val waveAmplitude = 2.dp.toPx()

                            val waterTop = bottleTop + bottleHeight * (1f - animatedProgress)
                            val waterColor = lerp(Color(0xFF2196F3), Color(0xFF00BCD4), animatedProgress)

                            val innerPath = Path().apply {
                                addRoundRect(
                                    RoundRect(
                                        left = bottleLeft,
                                        top = bottleTop,
                                        right = bottleLeft + bottleWidth,
                                        bottom = bottleTop + bottleHeight,
                                        topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                                        topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                                        bottomLeftCornerRadius = CornerRadius(cornerRadius * 1.5f, cornerRadius * 1.5f),
                                        bottomRightCornerRadius = CornerRadius(cornerRadius * 1.5f, cornerRadius * 1.5f)
                                    )
                                )
                            }

                            clipPath(innerPath) {
                                drawRect(
                                    color = waterColor.copy(alpha = 0.4f),
                                    topLeft = Offset(bottleLeft, waterTop),
                                    size = Size(bottleWidth, bottleTop + bottleHeight - waterTop)
                                )

                                val wavePath = Path().apply {
                                    val w = bottleWidth
                                    val l = bottleLeft
                                    moveTo(l, waterTop)
                                    for (x in 0..100) {
                                        val xR = x / 100f
                                        val xPos = l + w * xR
                                        val yOff = kotlin.math.sin(
                                            Math.toRadians((xR * 360 + wavePhase).toDouble())
                                        ).toFloat() * waveAmplitude
                                        lineTo(xPos, waterTop + yOff)
                                    }
                                    lineTo(l + w, bottleTop + bottleHeight)
                                    lineTo(l, bottleTop + bottleHeight)
                                    close()
                                }

                                drawPath(wavePath, color = waterColor.copy(alpha = 0.55f))
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "$todayWaterMl",
                        color = accent,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "/ ${waterGoal} ${strings.ml}",
                        color = textSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val percent = ((progress * 100).toInt()).coerceIn(0, 100)
                    Text(
                        "${percent}%",
                        color = textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = accent,
                        trackColor = textSecondary.copy(alpha = 0.2f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        preferencesManager.addWaterMl(250)
                        todayWaterMl = preferencesManager.getTodayWaterMl()
                        waterHistory = preferencesManager.getWaterHistory7Days()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("+250 ${strings.ml}", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        preferencesManager.addWaterMl(500)
                        todayWaterMl = preferencesManager.getTodayWaterMl()
                        waterHistory = preferencesManager.getWaterHistory7Days()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("+500 ${strings.ml}", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customMl,
                    onValueChange = { customMl = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("${strings.ml} personalizat", color = textSecondary.copy(alpha = 0.5f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = textSecondary.copy(alpha = 0.3f),
                        cursorColor = accent,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )
                Button(
                    onClick = {
                        val ml = customMl.toIntOrNull() ?: 0
                        if (ml > 0) {
                            preferencesManager.addWaterMl(ml)
                            todayWaterMl = preferencesManager.getTodayWaterMl()
                            waterHistory = preferencesManager.getWaterHistory7Days()
                            customMl = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    shape = RoundedCornerShape(12.dp),
                    enabled = customMl.toIntOrNull()?.let { it > 0 } == true,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    preferencesManager.addWaterMl(100)
                    todayWaterMl = preferencesManager.getTodayWaterMl()
                    waterHistory = preferencesManager.getWaterHistory7Days()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text("+100 ${strings.ml}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            OutlinedButton(
                onClick = {
                    editingAlarmId = -1
                    pickerHour = 9
                    pickerMinute = 0
                    showTimePicker = true
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textSecondary),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.reminder, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (waterHistory.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.waterHistory,
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.last7Days,
                        color = textSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val maxMl = waterHistory.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
                    val todayIndex = waterHistory.size - 1

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            waterHistory.forEachIndexed { idx, (dayName, ml) ->
                                val isToday = idx == todayIndex
                                val barColor = if (isToday) accent else accent.copy(alpha = 0.5f)
                                val barHeightFraction = if (maxMl > 0) (ml.toFloat() / maxMl) else 0f

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (ml > 0) {
                                        Text(
                                            "$ml",
                                            color = if (isToday) accent else textSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(if (isToday) 22.dp else 18.dp)
                                            .heightIn(min = 6.dp)
                                            .fillMaxHeight(fraction = barHeightFraction.coerceIn(0.06f, 1f))
                                            .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                            .background(
                                                if (isToday) Brush.verticalGradient(
                                                    colors = listOf(accent, accent.copy(alpha = 0.6f))
                                                ) else SolidColor(barColor)
                                            )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            waterHistory.forEachIndexed { idx, (dayName, _) ->
                                val isToday = idx == todayIndex
                                Text(
                                    dayName,
                                    color = if (isToday) accent else textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        if (alarms.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.reminder.uppercase(),
                        color = textSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    alarms.forEachIndexed { index, alarm ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (alarm.enabled) accent.copy(alpha = 0.08f) else cardBg
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (alarm.enabled) accent.copy(alpha = 0.15f)
                                            else textSecondary.copy(alpha = 0.1f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AccessAlarm,
                                        contentDescription = null,
                                        tint = if (alarm.enabled) accent else textSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            editingAlarmId = alarm.id
                                            pickerHour = alarm.hour
                                            pickerMinute = alarm.minute
                                            showTimePicker = true
                                        }
                                ) {
                                    Text(
                                        "${alarm.hour.toString().padStart(2, '0')}:${alarm.minute.toString().padStart(2, '0')}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (alarm.enabled) textPrimary else textSecondary.copy(alpha = 0.5f),
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        strings.everyDay,
                                        fontSize = 12.sp,
                                        color = if (alarm.enabled) accent else textSecondary.copy(alpha = 0.4f)
                                    )
                                }

                                Switch(
                                    checked = alarm.enabled,
                                    onCheckedChange = { enabled ->
                                        preferencesManager.toggleWaterReminder(alarm.id, enabled)
                                        alarms = preferencesManager.getWaterReminders()
                                        val receiver = WaterReminderReceiver()
                                        if (enabled) receiver.scheduleAlarm(context, alarm.id)
                                        else receiver.cancelAlarm(context, alarm.id)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = accent,
                                        uncheckedThumbColor = textSecondary,
                                        uncheckedTrackColor = textSecondary.copy(alpha = 0.3f)
                                    )
                                )
                            }
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
                containerColor = cardBg,
                title = { Text(strings.selectTime, color = textPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            selectorColor = accent,
                            containerColor = surfaceBg
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
                        colors = ButtonDefaults.buttonColors(containerColor = accent),
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
                            Text(strings.delete, color = Color.Red)
                        }
                    }
                    TextButton(onClick = { showTimePicker = false }) {
                        Text(strings.cancel, color = textSecondary)
                    }
                }
            )
        }
    }
}
