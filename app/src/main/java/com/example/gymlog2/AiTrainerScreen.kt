package com.example.gymlog2

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlog2.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTrainerScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    aiManager: AiTrainerManager,
    userId: String,
    preferencesManager: PreferencesManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val surfaceBg = if (isDark) bgColor() else LightBackground
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary
    val cardBg = if (isDark) cardColor() else LightCard
    val accent = if (isDark) accentColor() else LightPrimaryRed
    val userBubbleBg = if (isDark) AccentPurple else AccentPurple.copy(alpha = 0.8f)
    val aiBubbleBg = if (isDark) cardBg else LightCard

    var messages by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var historyLoaded by remember { mutableStateOf(false) }
    var currentSessionId by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var historySessions by remember { mutableStateOf<List<Long>>(emptyList()) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun saveMessage(role: String, text: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(context)
                db.aiChatHistoryDao().insert(
                    AiChatHistoryEntity(
                        userId = userId,
                        sessionId = currentSessionId,
                        role = role,
                        message = text
                    )
                )
            }
        }
    }

    fun startNewChat() {
        messages = emptyList()
        currentSessionId = System.currentTimeMillis()
    }

    fun loadSession(sessionId: Long) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(context)
                val history = db.aiChatHistoryDao().getForSession(userId, sessionId)
                messages = history.map { it.role to it.message }
            }
            currentSessionId = sessionId
            showHistoryDialog = false
        }
    }

    fun loadHistorySessions() {
        scope.launch {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(context)
                historySessions = db.aiChatHistoryDao().getSessionIds(userId)
            }
        }
    }

    fun clearHistory() {
        scope.launch {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(context)
                db.aiChatHistoryDao().deleteAllForUser(userId)
            }
            messages = emptyList()
            currentSessionId = System.currentTimeMillis()
        }
    }

    LaunchedEffect(userId) {
        if (!historyLoaded) {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(context)
                val allHistory = db.aiChatHistoryDao().getAllForUser(userId)
                if (allHistory.isNotEmpty()) {
                    val lastSessionId = allHistory.last().sessionId
                    val sessionMessages = allHistory.filter { it.sessionId == lastSessionId }
                    messages = sessionMessages.map { it.role to it.message }
                    currentSessionId = lastSessionId
                }
            }
            historyLoaded = true
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showHistoryDialog) {
        LaunchedEffect(showHistoryDialog) {
            loadHistorySessions()
        }

        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            containerColor = cardBg,
            titleContentColor = textPrimary,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.aiTrainerHistory, fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        startNewChat()
                        showHistoryDialog = false
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "New chat", tint = accent)
                    }
                }
            },
            text = {
                if (historySessions.isEmpty()) {
                    Text(strings.noHistoryYet, color = textSecondary, fontSize = 13.sp)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(historySessions) { sessionId ->
                            var preview by remember { mutableStateOf("") }
                            var dateStr by remember { mutableStateOf("") }

                            LaunchedEffect(sessionId) {
                                withContext(Dispatchers.IO) {
                                    val db = AppDatabase.getDatabase(context)
                                    val firstMsg = db.aiChatHistoryDao().getFirstUserMessage(userId, sessionId)
                                    preview = firstMsg?.message ?: ""
                                    if (firstMsg != null) {
                                        dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(firstMsg.timestamp))
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { loadSession(sessionId) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (sessionId == currentSessionId) accent.copy(alpha = 0.1f) else surfaceBg
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            preview,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (sessionId == currentSessionId) {
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
                                    Spacer(Modifier.height(4.dp))
                                    Text(dateStr, fontSize = 11.sp, color = textSecondary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text(strings.confirm, color = accent)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceBg)
    ) {
        TopAppBar(
            windowInsets = WindowInsets(0, 0, 0, 0),
            title = { Text(strings.aiTrainer) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = accent)
                }
            },
            actions = {
                IconButton(onClick = {
                    startNewChat()
                }) {
                    Icon(Icons.Default.Add, contentDescription = "New chat", tint = accent)
                }
                IconButton(onClick = { showHistoryDialog = true }) {
                    Icon(Icons.Default.History, contentDescription = "History", tint = accent)
                }
                if (messages.isNotEmpty()) {
                    IconButton(onClick = { clearHistory() }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Clear history", tint = accent)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = surfaceBg,
                titleContentColor = textPrimary
            )
        )

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
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

                    val suggestions = remember(strings) { listOf(
                        strings.aiSuggestion1,
                        strings.aiSuggestion2,
                        strings.aiSuggestion3,
                        strings.aiSuggestion4
                    ) }
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
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
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
                                color = if (isUser) androidx.compose.ui.graphics.Color.White else textPrimary,
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
                                    .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                                    .background(aiBubbleBg)
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
