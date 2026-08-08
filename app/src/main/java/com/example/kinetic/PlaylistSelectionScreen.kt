package com.example.kinetic

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.GlassCard
import com.example.kinetic.ui.theme.DarkBackground
import com.example.kinetic.ui.theme.LightBackground
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSelectionScreen(
    isDark: Boolean,
    onBack: () -> Unit,
    onPlaylistSelected: (SpotifyPlaylist) -> Unit
) {
    val surfaceBg = if (isDark) DarkBackground else LightBackground
    val textPrimary = if (isDark) Color.White else Color(0xFF1A1A1A)
    val textSecondary = if (isDark) Color(0xFFB3B3B3) else Color(0xFF6A6A6A)
    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color.White
    val accent = Color(0xFF1DB954)

    var playlists by remember { mutableStateOf<List<SpotifyPlaylist>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedPlaylistId by remember { mutableStateOf<String?>(null) }
    var isAuthorized by remember { mutableStateOf(false) }
    var authChecked by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        SpotifyManager.loadTokens(context)
        isAuthorized = SpotifyManager.isAuthorized()
        authChecked = true
        if (isAuthorized) {
            try {
                isLoading = true
                playlists = withContext(Dispatchers.IO) { SpotifyManager.fetchPlaylists(context) }
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (uri != null && uri.toString().startsWith("kinetic://callback")) {
            coroutineScope.launch {
                val success = withContext(Dispatchers.IO) { SpotifyManager.handleRedirect(context, uri) }
                if (success) {
                    isAuthorized = true
                    isLoading = true
                    try {
                        playlists = withContext(Dispatchers.IO) { SpotifyManager.fetchPlaylists(context) }
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        isLoading = false
                    }
                } else {
                    error = "Authentication failed"
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val pending = SpotifyManager.pendingCallbackUri
        if (pending != null) {
            SpotifyManager.pendingCallbackUri = null
            coroutineScope.launch {
                val success = withContext(Dispatchers.IO) { SpotifyManager.handleRedirect(context, pending) }
                if (success) {
                    isAuthorized = true
                    isLoading = true
                    try {
                        playlists = withContext(Dispatchers.IO) { SpotifyManager.fetchPlaylists(context) }
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        isLoading = false
                    }
                } else {
                    error = "Authentication failed"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select Playlist",
                        color = textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceBg
                )
            )
        },
        containerColor = surfaceBg
    ) { paddingValues ->
        when {
            authChecked && !isAuthorized -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MusicOff,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            "Connect to Spotify",
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            "Access your playlists and set the perfect workout soundtrack",
                            color = textSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val authIntent = SpotifyManager.buildAuthIntent(context)
                                authLauncher.launch(authIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = R.drawable.ic_spotify
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Login with Spotify",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "You'll be redirected to Spotify to authorize",
                            color = textSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                openSpotifyApp(context)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = R.drawable.ic_open_spotify
                                ),
                                contentDescription = LanguageManager.getStrings(LocalContext.current).openSpotifyLabel,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                LanguageManager.getStrings(LocalContext.current).openSpotifyLabel,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = accent)
                        Spacer(Modifier.height(16.dp))
                        Text("Loading playlists...", color = textSecondary)
                    }
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Link,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Failed to load playlists", color = textPrimary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(error ?: "Unknown error", color = textSecondary)
                    }
                }
            }
            playlists.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No playlists found", color = textPrimary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Create a playlist on Spotify first", color = textSecondary)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    item {
                        Text(
                            "Choose a playlist for your workout",
                            color = textSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(playlists) { playlist ->
                        PlaylistItem(
                            playlist = playlist,
                            isSelected = playlist.id == selectedPlaylistId,
                            cardBg = cardBg,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            accent = accent,
                            onClick = {
                                selectedPlaylistId = playlist.id
                                onPlaylistSelected(playlist)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistItem(
    playlist: SpotifyPlaylist,
    isSelected: Boolean,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accent.copy(alpha = 0.15f) else cardBg
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (playlist.imageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(playlist.imageUrl),
                    contentDescription = playlist.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                                                .background(accent.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    playlist.name,
                    color = textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                if (playlist.description.isNotEmpty()) {
                    Text(
                        playlist.description,
                        color = textSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                Text(
                    "${playlist.trackCount} tracks",
                    color = textSecondary,
                    fontSize = 12.sp
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}