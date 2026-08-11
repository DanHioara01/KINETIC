package com.example.kinetic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.*
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.AppPalette
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun makeChatTitle(text: String): String {
    val line = text.lineSequence().firstOrNull()?.trim().orEmpty()
    return if (line.isEmpty()) "New chat"
    else if (line.length > 40) line.take(40).trimEnd() + "…"
    else line
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTrainerScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    aiManager: AiTrainerManager,
    userId: String,
    preferencesManager: PreferencesManager,
    onBack: () -> Unit,
    onOpenMenu: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val p = appPalette(isDark)
    val surfaceBg = p.bg
    val textPrimary = p.tp
    val textSecondary = p.ts
    val cardBg = p.cr
    val accent = p.ac
    val userBubbleBg = if (isDark) AccentPurple else AccentPurple.copy(alpha = 0.8f)
    val aiBubbleBg = if (isDark) p.cr else LightCard

    var messages by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to the latest message when a new one arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
    var showHistoryDrawer by remember { mutableStateOf(false) }
    var currentSessionId by remember { mutableStateOf(System.currentTimeMillis()) }
    val historySessions = remember { mutableStateListOf<Long>() }

    val historyOffsetX = animateFloatAsState(
        targetValue = if (showHistoryDrawer) 0f else 1f,
        animationSpec = tween(durationMillis = 300)
    )

    LaunchedEffect(showHistoryDrawer) {
        if (showHistoryDrawer) {
            scope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(context)
                val sessions = db.aiChatHistoryDao().getSessionIds(userId)
                historySessions.clear()
                historySessions.addAll(sessions)
            }
        }
    }

    fun saveMessage(role: String, message: String) {
        val sid = currentSessionId
        scope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            db.aiChatHistoryDao().insert(
                AiChatHistoryEntity(
                    userId = userId,
                    sessionId = sid,
                    role = role,
                    message = message
                )
            )
        }
    }
    fun loadSession(sessionId: Long) {
        currentSessionId = sessionId
        showHistoryDrawer = false
        scope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val msgs = db.aiChatHistoryDao().getForSession(userId, sessionId)
            messages = msgs.map { it.role to it.message }
        }
    }
    fun deleteSession(sessionId: Long) {
        historySessions.remove(sessionId)
        if (sessionId == currentSessionId) {
            currentSessionId = System.currentTimeMillis()
            messages = emptyList()
        }
        scope.launch(Dispatchers.IO) {
            AppDatabase.getDatabase(context).aiChatHistoryDao().deleteSession(userId, sessionId)
        }
    }
    fun startNewChat() {
        currentSessionId = System.currentTimeMillis()
        messages = emptyList()
    }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                KineticAppBar(
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = { startNewChat() }) {
                            Icon(Icons.Default.Add, contentDescription = strings.newChat, tint = accent)
                        }
                        IconButton(onClick = { showHistoryDrawer = true }) {
                            Icon(Icons.Default.History, contentDescription = strings.history, tint = accent)
                        }
                    }
                )
            },
            containerColor = surfaceBg
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(surfaceBg)
                    .padding(innerPadding)
                    .padding(bottom = AppConstants.BOTTOM_NAV_PADDING)
            ) {
                if (messages.isEmpty()) {
                    val suggestions = remember(strings) {
                        listOf(
                            strings.aiSuggestion1,
                            strings.aiSuggestion2,
                            strings.aiSuggestion3,
                            strings.aiSuggestion4
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    when {
                                        dragAmount < -30f -> showHistoryDrawer = true
                                        dragAmount > 30f -> onOpenMenu()
                                    }
                                }
                            }
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✦", fontSize = 48.sp, color = accent)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                strings.aiTrainerWelcome,
                                color = textPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                strings.aiTrainerHint,
                                color = textSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            suggestions.forEach { suggestion ->
                                OutlinedButton(
                                    onClick = {
                                        inputText = suggestion
                                        scope.launch {
                                            messages = messages + ("user" to suggestion)
                                            saveMessage("user", suggestion)
                                            isLoading = true
                                            val response = aiManager.chat(userId, suggestion, messages, preferencesManager)
                                            messages = messages + ("ai" to response)
                                            saveMessage("ai", response)
                                            isLoading = false
                                            inputText = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accent)
                                ) {
                                    Text(suggestion, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    when {
                                        dragAmount < -30f -> showHistoryDrawer = true
                                        dragAmount > 30f -> onOpenMenu()
                                    }
                                }
                            }
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                    ) {
                        items(messages) { (role, text) ->
                            val isUser = role == "user"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 300.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isUser) 16.dp else 4.dp,
                                                bottomEnd = if (isUser) 4.dp else 16.dp
                                            )
                                        )
                                        .background(if (isUser) userBubbleBg else aiBubbleBg)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text,
                                        color = if (isUser) Color.White else textPrimary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                        if (isLoading) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                                                                        .background(aiBubbleBg, RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            "...",
                                            color = textSecondary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(strings.askAiTrainer, color = textSecondary.copy(alpha = 0.5f)) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent,
                            unfocusedBorderColor = textSecondary.copy(alpha = 0.3f),
                            cursorColor = accent,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        maxLines = 3
                    )
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isLoading) {
                                val msg = inputText.trim()
                                messages = messages + ("user" to msg)
                                saveMessage("user", msg)
                                inputText = ""
                                isLoading = true
                                scope.launch {
                                    val response = aiManager.chat(userId, msg, messages, preferencesManager)
                                    messages = messages + ("ai" to response)
                                    saveMessage("ai", response)
                                    isLoading = false
                                }
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            tint = if (inputText.isNotBlank() && !isLoading) accent else textSecondary
                        )
                    }
                }
            }
        }

        // Scrim history
        if (showHistoryDrawer) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { showHistoryDrawer = false }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount > 30f) showHistoryDrawer = false
                        }
                    }
            )
        }

        // History drawer (right)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.82f)
                .fillMaxHeight()
                .offset {
                    val drawerWidthPx = with(density) { (screenWidthDp * 0.82f).dp.toPx() }
                    val offset = historyOffsetX.value * drawerWidthPx
                    IntOffset(offset.roundToInt(), 0)
                }
                .background(if (isDark) Color(0xFF1C1C1E) else Color.White)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount > 30f) showHistoryDrawer = false
                    }
                }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.aiTrainerHistory, fontWeight = FontWeight.Bold, color = textPrimary)
                    IconButton(onClick = { showHistoryDrawer = false }) {
                        Icon(Icons.Default.Close, contentDescription = strings.close, tint = textPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (historySessions.isEmpty()) {
                    Text(strings.noHistoryYet, color = textSecondary, fontSize = 13.sp)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(historySessions.toList()) { sessionId ->
                            var preview by remember { mutableStateOf("") }
                            var dateStr by remember { mutableStateOf("") }
                            LaunchedEffect(sessionId) {
                                withContext(Dispatchers.IO) {
                                    val db = AppDatabase.getDatabase(context)
                                    val firstMsg = db.aiChatHistoryDao().getFirstUserMessage(userId, sessionId)
                                    preview = firstMsg?.message ?: ""
                                    if (firstMsg != null) {
                                        dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                                            .format(Date(firstMsg.timestamp))
                                    }
                                }
                            }
                            AppGlassCard(
                                p = p,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { loadSession(sessionId) },
                                cornerRadius = 12.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 6.dp)
                                            .clickable { loadSession(sessionId) }
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                makeChatTitle(preview),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (sessionId == currentSessionId) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = accent.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        strings.current,
                                                        fontSize = 9.sp,
                                                        color = accent,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(dateStr, fontSize = 11.sp, color = textSecondary)
                                    }
                                    IconButton(onClick = { deleteSession(sessionId) }) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = strings.deleteChat,
                                            tint = textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showHistoryDrawer = false; startNewChat() }) {
                        Text(strings.newChat, color = accent)
                    }
                }
            }
        }
    }
}
