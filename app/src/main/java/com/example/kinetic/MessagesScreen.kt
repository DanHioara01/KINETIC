@file:Suppress("NewApi")
package com.example.kinetic

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.platform.LocalContext
import com.example.kinetic.LanguageManager
import com.example.kinetic.ui.theme.DarkBackground
import com.example.kinetic.ui.theme.LightBackground
import com.example.kinetic.ui.theme.DarkCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MessageFilter { ALL, UNREAD, READ }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    messageDao: MessageDao,
    accent: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    isDark: Boolean,
    strings: LanguageManager.Strings = LanguageManager.getStrings(androidx.compose.ui.platform.LocalContext.current)
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    var filter by remember { mutableStateOf(MessageFilter.ALL) }
    val allMessages by messageDao.observeAll().collectAsState(initial = emptyList())
    val unreadMessages by messageDao.observeUnread().collectAsState(initial = emptyList())
    val readMessages by messageDao.observeRead().collectAsState(initial = emptyList())
    val unreadCount by messageDao.observeUnreadCount().collectAsState(initial = 0)
    val messages = when (filter) { MessageFilter.ALL -> allMessages; MessageFilter.UNREAD -> unreadMessages; MessageFilter.READ -> readMessages }

    LaunchedEffect(Unit) { messageDao.deleteOlderThan(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) }

    @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = if (isDark) DarkBackground else LightBackground) { _ ->
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.statusBarsPadding())
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Messages", fontSize = 28.sp, fontWeight = FontWeight.Black, color = textPrimary)
                if (unreadCount > 0) Text("$unreadCount unread", fontSize = 13.sp, color = accent)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (unreadCount > 0) {
                    TextButton(onClick = { scope.launch { messageDao.markAllAsRead() } }) {
                        Text(strings.markAllRead, color = accent, fontSize = 13.sp)
                    }
                }
                if (allMessages.isNotEmpty()) {
                    TextButton(onClick = { scope.launch { messageDao.deleteAll() } }) {
                        Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(16.dp), tint = Color(0xFFCC3333))
                        Spacer(Modifier.width(4.dp))
                        Text(strings.clearAll, color = Color(0xFFCC3333), fontSize = 13.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MessageFilter.values().forEach { f ->
                val label = when (f) { MessageFilter.ALL -> "All"; MessageFilter.UNREAD -> "Unread"; MessageFilter.READ -> "Read" }
                val count = when (f) { MessageFilter.ALL -> allMessages.size; MessageFilter.UNREAD -> unreadMessages.size; MessageFilter.READ -> readMessages.size }
                FilterChip(selected = filter == f, onClick = { filter = f },
                    label = { Text("$label ($count)", fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(containerColor = cardBg, selectedContainerColor = accent, labelColor = textSecondary, selectedLabelColor = Color.White),
                    shape = RoundedCornerShape(20.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        if (messages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MailOutline, null, modifier = Modifier.size(64.dp), tint = textSecondary.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))
                    Text(when (filter) { MessageFilter.ALL -> "No messages yet"; MessageFilter.UNREAD -> "No unread messages"; MessageFilter.READ -> "No read messages" },
                        color = textSecondary.copy(alpha = 0.5f), fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                items(messages, key = { it.id }) { msg ->
                    var expanded by remember { mutableStateOf(false) }
                    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { v ->
                        if (v == SwipeToDismissBoxValue.EndToStart) { scope.launch { if (android.os.Build.VERSION.SDK_INT >= 26) { val vib = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator; vib?.vibrate(android.os.VibrationEffect.createOneShot(30, android.os.VibrationEffect.DEFAULT_AMPLITUDE)) }; messageDao.deleteById(msg.id); snackbarHostState.showSnackbar(strings.notificationDeleted) }; true } else false
                    })
                    SwipeToDismissBox(state = dismissState,
                        backgroundContent = { Box(modifier = Modifier.fillMaxSize().background(Color(0xFFCC3333), RoundedCornerShape(12.dp)).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) { Icon(Icons.Default.Delete, "Delete", tint = Color.White) } },
                        enableDismissFromStartToEnd = false) {
                        MsgCard(msg, accent, textPrimary, textSecondary, expanded, { expanded = !expanded }) {
                            if (!msg.isRead) scope.launch { messageDao.markAsRead(msg.id) }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun MsgCard(msg: MessageEntity, accent: Color, textPrimary: Color, textSecondary: Color, expanded: Boolean, onToggleExpand: () -> Unit, onMarkRead: () -> Unit) {
    val (ico, icoBg) = when (msg.type) {
        "SUCCESS" -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        "WARNING" -> Icons.Default.Warning to Color(0xFFFF9800)
        "REMINDER" -> Icons.Default.Alarm to Color(0xFF2196F3)
        else -> Icons.Default.Info to accent
    }
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) Color(0xFF1C1416) else Color.White
        )
    ) {
        Column(modifier = Modifier.padding(14.dp).clickable { onToggleExpand() }) {
        Row(verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(icoBg.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(ico, null, tint = icoBg, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(msg.title, fontWeight = if (msg.isRead) FontWeight.Normal else FontWeight.Bold,
                        fontSize = 15.sp, color = if (msg.isRead) textSecondary else textPrimary,
                        maxLines = if (expanded) Int.MAX_VALUE else 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    if (!msg.isRead) Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accent))
                }
                Spacer(Modifier.height(4.dp))
                Text(msg.body, fontSize = 13.sp, color = textSecondary.copy(alpha = 0.7f), maxLines = if (expanded) Int.MAX_VALUE else 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Text(fmtTime(msg.timestamp), fontSize = 11.sp, color = textSecondary.copy(alpha = 0.4f))
            }
            }
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (!msg.isRead) {
                        TextButton(onClick = onMarkRead) {
                            Icon(Icons.Default.MarkEmailRead, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Mark as read", fontSize = 12.sp)
                        }
                    } else {
                        Text("Read", fontSize = 11.sp, color = textSecondary.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

private fun fmtTime(ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    val m = diff / 60000; val h = diff / 3600000; val d = diff / 86400000
    return when { m < 1 -> "Just now"; m < 60 -> "${m}m ago"; h < 24 -> "${h}h ago"; d < 7 -> "${d}d ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(ts))
    }
}
