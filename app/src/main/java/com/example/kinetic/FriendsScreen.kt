package com.example.kinetic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.*
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.AppPalette
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    isDark: Boolean,
    isLbs: Boolean,
    strings: LanguageManager.Strings,
    onBackClick: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onRequestsChanged: (Int) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val socialRepository = remember { SocialRepository(db) }
    val userProfileManager = remember { UserProfileManager(context) }
    val currentUserId = userProfileManager.getOwnUserId()
    val scope = rememberCoroutineScope()

    val p = appPalette(isDark)

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var searchLoading by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    var friends by remember { mutableStateOf(listOf<FriendshipEntity>()) }
    var incomingRequests by remember { mutableStateOf(listOf<FriendshipEntity>()) }
    var friendsVolume by remember { mutableStateOf(mapOf<String, Pair<Double, Int>>()) }

    var loading by remember { mutableStateOf(true) }
    var requestSentMessage by remember { mutableStateOf<String?>(null) }
    // Incrementat după fiecare salvare de profil → forțează recompoziția cardurilor,
    // ca numele să apară imediat (nu doar la a doua vizită).
    var profilesVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val profile = userProfileManager.getOwnProfile()
        if (profile != null && profile.name.isNotBlank()) {
            socialRepository.syncUserProfile(currentUserId, profile.name, profile.photoUri)
        }
        friends = socialRepository.getFriends(currentUserId)
        incomingRequests = socialRepository.getIncomingRequests(currentUserId)

        val friendIds = friends.map { it.friendId }.distinct().filter { it != currentUserId }
        for (id in friendIds) {
            try {
                val user = NetworkClient.api.getUser(id)
                val name = (user["name"] as? String)?.takeIf { it.isNotBlank() }
                if (name != null) {
                    val photo = (user["photoUri"] as? String) ?: ""
                    userProfileManager.saveProfile(UserProfileManager.UserProfile(userId = id, name = name, photoUri = photo))
                    profilesVersion++
                }
            } catch (_: Exception) {}
        }

        friends.forEach { f ->
            val volData = socialRepository.getUserVolume(f.friendId)
            friendsVolume = friendsVolume + (f.friendId to volData)
        }
        loading = false
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    friends = socialRepository.getFriends(currentUserId)
                    incomingRequests = socialRepository.getIncomingRequests(currentUserId)
                    val friendIds = friends.filter { it.friendId != currentUserId }.map { it.friendId }.distinct()
                    for (id in friendIds) {
                        val volData = socialRepository.getUserVolume(id)
                        friendsVolume = friendsVolume + (id to volData)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = p.bg,
        topBar = {
            KineticAppBar(
                onBack = onBackClick,
                actions = {
                    IconButton(onClick = onOpenLeaderboard) {
                        Icon(
                            painterResource(R.drawable.ic_leaderboard_trophy),
                            contentDescription = strings.leaderboard,
                            tint = p.tp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            LaunchedEffect(searchQuery) {
                searchJob?.cancel()
                if (searchQuery.trim().length >= 2) {
                    searchLoading = true
                    searchJob = launch {
                        delay(400)
                        searchResults = withContext(Dispatchers.IO) {
                            try {
                                socialRepository.searchUsersOnline(searchQuery.trim())
                                    .filter { it.first != currentUserId }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                emptyList()
                            }
                        }
                        searchLoading = false
                    }
                } else {
                    searchResults = emptyList()
                    searchLoading = false
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = AppConstants.BOTTOM_NAV_PADDING)
            ) {

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(strings.searchByNameOrId, color = p.ts) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = p.ts) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = ""; searchResults = emptyList() }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = p.ts)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = p.ts.copy(alpha = 0.3f),
                        focusedBorderColor = p.ac,
                        cursorColor = p.ac,
                        focusedTextColor = p.tp,
                        unfocusedTextColor = p.tp
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (searchQuery.isNotBlank()) {
                item {
                    if (searchLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = p.ac, modifier = Modifier.size(32.dp))
                        }
                    }
                }

                if (searchResults.isNotEmpty()) {
                    item {
                        Text(strings.searchUsers, color = p.ts, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    }
                    items(searchResults) { (userId, userName) ->
                        SearchResultCard(
                            userId = userId,
                            userName = userName,
                            p = p,
                            currentUserId = currentUserId,
                            userProfileManager = userProfileManager,
                            socialRepository = socialRepository,
                            isLbs = isLbs,
                            strings = strings,
                            onSent = {
                                requestSentMessage = "${strings.friendRequestSent}: $userName"
                                searchQuery = ""
                                searchResults = emptyList()
                            }
                        )
                    }
                }
            }

            if (incomingRequests.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(strings.incomingRequests.uppercase(), color = p.ts, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                }
                items(incomingRequests) { req ->
                    IncomingRequestCard(
                        request = req,
                        p = p,
                        userProfileManager = userProfileManager,
                        socialRepository = socialRepository,
                        onAccepted = {
                            scope.launch {
                                val updated = socialRepository.getIncomingRequests(currentUserId)
                                incomingRequests = updated
                                friends = socialRepository.getFriends(currentUserId)
                                onRequestsChanged(updated.size)
                            }
                        },
                        onRejected = {
                            scope.launch {
                                val updated = socialRepository.getIncomingRequests(currentUserId)
                                incomingRequests = updated
                                onRequestsChanged(updated.size)
                            }
                        },
                        strings = strings
                    )
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text(strings.yourFriends.uppercase(), color = p.ts, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
            }

            val visibleFriends = friends.filter { it.friendId != currentUserId }.distinctBy { it.friendId }
            if (visibleFriends.isEmpty() && !loading && incomingRequests.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            icon = Icons.Default.People,
                            title = strings.noFriends,
                            subtitle = strings.feedEmpty,
                            textPrimary = p.tp,
                            textSecondary = p.ts
                        )
                    }
                }
            }

            items(visibleFriends) { friendship ->
                val pv = profilesVersion // re-render itemul când sosesc profilele
                val friendId = friendship.friendId
                val profile = userProfileManager.getProfile(friendId)
                val shortId = userProfileManager.getShortId(friendId)
                val vol = friendsVolume[friendId]?.first ?: 0.0
                val wc = friendsVolume[friendId]?.second ?: 0

            AppGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    p = p,
                    cornerRadius = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (profile?.photoUri?.isNotBlank() == true) {
                            coil.compose.AsyncImage(
                                model = profile.photoUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(48.dp).clip(CircleShape).border(2.dp, p.ac.copy(alpha = 0.3f), CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(48.dp).background(p.ac.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                            Text(
                                (profile?.name ?: "?").take(1).uppercase(),
                                color = p.ac,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                profile?.name ?: "#$shortId",
                                color = p.tp,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "${String.format("%.0f", if (isLbs) vol * 2.20462 else vol)} ${if (isLbs) "lbs" else "kg"} · $wc ${strings.workoutsLabel.lowercase()}",
                                color = p.ts,
                                fontSize = 12.sp
                            )
                        }
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = null, tint = p.ts, modifier = Modifier.size(20.dp))
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text(strings.removeFriend, color = Volcanico) },
                                    leadingIcon = { Icon(Icons.Default.PersonRemove, contentDescription = null, tint = Volcanico) },
                                    onClick = {
                                        showMenu = false
                                        scope.launch {
                                            socialRepository.unfollow(currentUserId, friendId)
                                            friends = socialRepository.getFriends(currentUserId)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        requestSentMessage?.let { msg ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Snackbar(
                    modifier = Modifier.padding(16.dp).padding(bottom = paddingValues.calculateBottomPadding()),
                    containerColor = p.ac,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(msg, fontWeight = FontWeight.Medium)
                }
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2500)
                    requestSentMessage = null
                }
            }
        }
    }
    }
}

@Composable
private fun SearchResultCard(
    userId: String,
    userName: String,
    p: AppPalette,
    currentUserId: String,
    userProfileManager: UserProfileManager,
    socialRepository: SocialRepository,
    isLbs: Boolean,
    strings: LanguageManager.Strings,
    onSent: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var sending by remember { mutableStateOf(false) }
    var alreadyFriend by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        val friends = socialRepository.getFriends(currentUserId)
        alreadyFriend = friends.any { it.friendId == userId }
    }

    AppGlassCard(
        modifier = Modifier.fillMaxWidth(),
        p = p,
        cornerRadius = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(p.acs, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(userName.take(1).uppercase(), color = p.ac, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(userName, color = p.tp, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("#${userProfileManager.getShortId(userId)}", color = p.ts, fontSize = 11.sp)
            }
            if (!alreadyFriend) {
                Button(
                    onClick = {
                        sending = true
                        scope.launch {
                            val ownProfile = userProfileManager.getOwnProfile()
                            socialRepository.sendFriendRequest(currentUserId, userId, ownProfile?.name ?: currentUserId, ownProfile?.photoUri ?: "")
                            sending = false
                            onSent()
                        }
                    },
                    enabled = !sending,
                    modifier = Modifier.background(RedButtonGradient, RoundedCornerShape(10.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    if (sending) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(strings.sendRequest, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Text(strings.friendRequestSent, color = p.ts, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun IncomingRequestCard(
    request: FriendshipEntity,
    p: AppPalette,
    userProfileManager: UserProfileManager,
    socialRepository: SocialRepository,
    onAccepted: () -> Unit,
    onRejected: () -> Unit,
    strings: LanguageManager.Strings
) {
    val scope = rememberCoroutineScope()
    val shortId = userProfileManager.getShortId(request.userId)
    var profile by remember { mutableStateOf(userProfileManager.getProfile(request.userId)) }

    // Fetch reactiv: dacă profilul expeditorului nu e încă local, îl încărcăm de pe
    // backend chiar în card, ca numele să apară imediat (nu la a doua vizită).
    LaunchedEffect(request.userId) {
        if (profile == null) {
            try {
                val user = NetworkClient.api.getUser(request.userId)
                val name = (user["name"] as? String)?.takeIf { it.isNotBlank() }
                if (name != null) {
                    userProfileManager.saveProfile(
                        UserProfileManager.UserProfile(
                            userId = request.userId,
                            name = name,
                            photoUri = (user["photoUri"] as? String) ?: ""
                        )
                    )
                    profile = userProfileManager.getProfile(request.userId)
                }
            } catch (_: Exception) {}
        }
    }

    AppGlassCard(
        modifier = Modifier.fillMaxWidth(),
        p = p,
        cornerRadius = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val photoUri = profile?.photoUri
            if (photoUri?.isNotBlank() == true) {
                coil.compose.AsyncImage(
                    model = photoUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(42.dp).clip(CircleShape).border(2.dp, p.ac.copy(alpha = 0.3f), CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier.size(42.dp).background(p.acs, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (profile?.name ?: "?").take(1).uppercase(),
                        color = p.ac,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val profileName = profile?.name
                if (profileName != null) {
                    Text(profileName, color = p.tp, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("#$shortId", color = p.ts, fontSize = 11.sp)
                } else {
                    Text("#$shortId", color = p.tp, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            socialRepository.acceptFriendRequest(request.id)
                            onAccepted()
                        }
                    },
                    modifier = Modifier.background(RedButtonGradient, RoundedCornerShape(10.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.accept, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            socialRepository.rejectFriendRequest(request.id)
                            onRejected()
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = p.ts),
                    border = ButtonDefaults.outlinedButtonBorder,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = p.ts, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.reject, color = p.ts, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
