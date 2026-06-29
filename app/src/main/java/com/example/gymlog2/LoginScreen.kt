package com.example.gymlog2

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlog2.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    strings: LanguageManager.Strings,
    isDark: Boolean,
    onEmailLogin: (String, String) -> Unit,
    onGoogleLogin: () -> Unit,
    onFacebookLogin: () -> Unit,
    onGuestLogin: () -> Unit,
    onSignUpClick: () -> Unit,
    error: String? = null,
    isLoadingExternal: Boolean = false
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var firebaseError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val isLoadingCombined = isLoading || isLoadingExternal

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    LaunchedEffect(error) {
        firebaseError = error
        if (error != null) isLoading = false
    }

    fun validateEmail(value: String): Boolean {
        if (value.isBlank()) {
            emailError = strings.emailError
            return false
        }
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!emailRegex.matches(value)) {
            emailError = strings.emailError
            return false
        }
        emailError = null
        return true
    }

    fun validatePassword(value: String): Boolean {
        if (value.isBlank()) {
            passwordError = strings.passwordError
            return false
        }
        if (value.length < 6) {
            passwordError = strings.passwordError
            return false
        }
        passwordError = null
        return true
    }

    val isFormValid = email.isNotBlank() && password.length >= 6

    val accent = Ember
    val glassBg = MediumTeal.copy(alpha = 0.25f)
    val glassBorder = MediumTeal.copy(alpha = 0.4f)

    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )
    val logoAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.login_bg2),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(2.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DeepTealBlack.copy(alpha = 0.85f),
                            DarkTeal.copy(alpha = 0.6f),
                            DeepTealBlack.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Ember.copy(alpha = 0.12f),
                            MediumTeal.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(initialScale = 0.3f, animationSpec = tween(800)) + fadeIn(animationSpec = tween(800))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher),
                        contentDescription = "Kinetic Logo",
                        modifier = Modifier
                            .size(72.dp)
                            .graphicsLayer {
                                scaleX = logoScale
                                scaleY = logoScale
                                alpha = logoAlpha
                            }
                            .shadow(20.dp, CircleShape, ambientColor = accent.copy(alpha = 0.4f))
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "KINETIC",
                        fontSize = 32.sp,
                        letterSpacing = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Cream
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        strings.appTagline,
                        fontSize = 11.sp,
                        letterSpacing = 4.sp,
                        color = Cream.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(600, delayMillis = 200)) + fadeIn(animationSpec = tween(600, delayMillis = 200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(glassBg)
                        .border(1.dp, glassBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailError != null) validateEmail(it)
                            firebaseError = null
                        },
                        label = { Text(strings.email) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = accent) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent,
                            unfocusedBorderColor = if (emailError != null) accent else Cream.copy(alpha = 0.2f),
                            focusedContainerColor = Color(0xFF1A1A1A).copy(alpha = 0.8f),
                            unfocusedContainerColor = Color(0xFF151515).copy(alpha = 0.8f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Cream,
                            unfocusedLabelColor = Cream.copy(alpha = 0.7f),
                            cursorColor = accent,
                            focusedLeadingIconColor = accent,
                            unfocusedLeadingIconColor = Cream.copy(alpha = 0.7f)
                        )
                    )
                    if (emailError != null) {
                        Text(emailError!!, color = accent, fontSize = 11.sp)
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordError != null) validatePassword(it)
                            firebaseError = null
                        },
                        label = { Text(strings.password) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = accent) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Cream.copy(alpha = 0.7f)
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent,
                            unfocusedBorderColor = if (passwordError != null) accent else Cream.copy(alpha = 0.2f),
                            focusedContainerColor = Color(0xFF1A1A1A).copy(alpha = 0.8f),
                            unfocusedContainerColor = Color(0xFF151515).copy(alpha = 0.8f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Cream,
                            unfocusedLabelColor = Cream.copy(alpha = 0.7f),
                            cursorColor = accent,
                            focusedLeadingIconColor = accent,
                            unfocusedLeadingIconColor = Cream.copy(alpha = 0.7f)
                        )
                    )
                    if (passwordError != null) {
                        Text(passwordError!!, color = accent, fontSize = 11.sp)
                    }
                }
            }

            if (firebaseError != null) {
                Spacer(Modifier.height(6.dp))
                Text(firebaseError!!, color = accent, fontSize = 12.sp, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(10.dp))

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(600, delayMillis = 350)) + fadeIn(animationSpec = tween(600, delayMillis = 350))
            ) {
                Button(
                    onClick = {
                        if (!isLoadingCombined) {
                            val emailValid = validateEmail(email)
                            val passwordValid = validatePassword(password)
                            if (emailValid && passwordValid) {
                                isLoading = true
                                firebaseError = null
                                onEmailLogin(email, password)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = accent.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    enabled = !isLoadingCombined && isFormValid,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (isFormValid) listOf(accent, EmberDark)
                                    else listOf(MediumTeal, MediumTeal)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoadingCombined) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Cream,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                strings.login,
                                fontSize = 15.sp,
                                letterSpacing = 3.sp,
                                color = if (isFormValid) Cream else Cream.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 450))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, glassBorder)
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 14.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(glassBg)
                            .border(1.dp, glassBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            strings.or.uppercase(),
                            color = Cream.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            letterSpacing = 3.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(glassBorder, Color.Transparent)
                                )
                            )
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(600, delayMillis = 500)) + fadeIn(animationSpec = tween(600, delayMillis = 500))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SocialLoginButton(
                        text = strings.loginWithGoogle,
                        icon = {
                            Text("G", color = GoogleBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        },
                        bgColor = glassBg,
                        borderColor = glassBorder,
                        enabled = !isLoadingCombined,
                        onClick = {
                            if (!isLoadingCombined) {
                                isLoading = true
                                firebaseError = null
                                onGoogleLogin()
                            }
                        }
                    )
                    SocialLoginButton(
                        text = strings.loginWithFacebook,
                        icon = {
                            Icon(Icons.Default.Facebook, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(22.dp))
                        },
                        bgColor = glassBg,
                        borderColor = glassBorder,
                        enabled = !isLoadingCombined,
                        onClick = {
                            if (!isLoadingCombined) {
                                isLoading = true
                                firebaseError = null
                                onFacebookLogin()
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 600))
            ) {
                Text(
                    strings.loginAsGuest,
                    color = Cream.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isLoadingCombined) {
                            if (!isLoadingCombined) {
                                isLoading = true
                                firebaseError = null
                                onGuestLogin()
                            }
                        }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 650))
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        strings.dontHaveAccount + " ",
                        color = Cream.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Text(
                        strings.signUp,
                        color = accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = !isLoadingCombined) {
                                onSignUpClick()
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        if (isLoadingCombined) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(52.dp),
                    color = accent,
                    strokeWidth = 4.dp,
                    trackColor = DarkTeal.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    icon: @Composable () -> Unit,
    bgColor: Color,
    borderColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) bgColor else bgColor.copy(alpha = 0.3f))
            .border(1.dp, if (enabled) borderColor else borderColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            color = if (enabled) Cream else Cream.copy(alpha = 0.3f),
            fontSize = 14.sp,
            letterSpacing = 2.sp
        )
    }
}
