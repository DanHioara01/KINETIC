package com.example.kinetic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    isDark: Boolean
) {
    val scope = rememberCoroutineScope()
    var filter by remember { mutableStateOf(MessageFilter.ALL) }
    val allMessages by messageDao.observeAll().collectAsState(initial = emptyList())
    val unreadMessages by messageDao.observeUnread().collectAsState(initial = emptyList())
    val readMessages by messageDao.observeRead().collectAsState(initial = emptyList())
    val unreadCount by messageDao.observeUnreadCount().collectAsState(initial = 0)
    val messages = when (filter) { MessageFilter.ALL -> allMessages; MessageFilter.UNREAD -> unreadMessages; MessageFilter.READ -> readMessages }

    LaunchedEffect(Unit) { messageDao.deleteOlderThan(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) }

    Column(modifier = Modifier.fillMaxSize().background(if (isDark) Color(0xFF0E0E12) else Color(0xFFF5F5F5)).padding(horizontal = 16.dp)) {
        Spacer(Modifier.statusBarsPadding())
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Messages", fontSize = 28.sp, fontWeight = FontWeight.Black, color = textPrimary)
                if (unreadCount > 0) Text("$unreadCount unread", fontSize = 13.sp, color = accent)
            }
            if (unreadCount > 0) {
                TextButton(onClick = { scope.launch { messageDao.markAllAsRead() } }) {
                    Text("Mark all read", color = accent, fontSize = 13.sp)
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
                    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { v ->
                        if (v == SwipeToDismissBoxValue.EndToStart) { scope.launch { messageDao.deleteById(msg.id) }; true } else false
                    })
                    SwipeToDismissBox(state = dismissState,
                        backgroundContent = { Box(modifier = Modifier.fillMaxSize().background(Color(0xFFCC3333), RoundedCornerShape(12.dp)).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) { Icon(Icons.Default.Delete, "Delete", tint = Color.White) } },
                        enableDismissFromStartToEnd = false) {
                        MsgCard(msg, accent, textPrimary, textSecondary, if (isDark) Color(0xFF1E1E24) else Color.White) {
                            if (!msg.isRead) scope.launch { messageDao.markAsRead(msg.id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MsgCard(msg: MessageEntity, accent: Color, textPrimary: Color, textSecondary: Color, bg: Color, onClick: () -> Unit) {
    val (ico, icoBg) = when (msg.type) {
        "SUCCESS" -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        "WARNING" -> Icons.Default.Warning to Color(0xFFFF9800)
        "REMINDER" -> Icons.Default.Alarm to Color(0xFF2196F3)
        else -> Icons.Default.Info to accent
    }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = bg)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(icoBg.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(ico, null, tint = icoBg, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(msg.title, fontWeight = if (msg.isRead) FontWeight.Normal else FontWeight.Bold,
                        fontSize = 15.sp, color = if (msg.isRead) textSecondary else textPrimary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    if (!msg.isRead) Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accent))
                }
                Spacer(Modifier.height(4.dp))
                Text(msg.body, fontSize = 13.sp, color = textSecondary.copy(alpha = 0.7f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Text(fmtTime(msg.timestamp), fontSize = 11.sp, color = textSecondary.copy(alpha = 0.4f))
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
