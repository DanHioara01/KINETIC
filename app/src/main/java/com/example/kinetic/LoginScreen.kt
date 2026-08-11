package com.example.kinetic

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import com.example.kinetic.ui.theme.*
import kotlinx.coroutines.delay

enum class LoginMethod { NONE, EMAIL, GOOGLE, FACEBOOK, GUEST }

@Composable
fun LoginScreen(
    strings: LanguageManager.Strings,
    isDark: Boolean,
    onEmailLogin: (String, String) -> Unit,
    onGoogleLogin: () -> Unit,
    onFacebookLogin: () -> Unit,
    onGuestLogin: () -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPassword: ((String) -> Unit)? = null,
    onLanguageClick: (() -> Unit)? = null,
    error: String? = null,
    isLoadingExternal: Boolean = false,
    isGoogleAvailable: Boolean = true,
    isFacebookAvailable: Boolean = true,
    signUpSuccess: Boolean = false,
    onSignUpSuccessDismiss: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    var activeMethod by remember { mutableStateOf(LoginMethod.NONE) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var firebaseError by remember { mutableStateOf<String?>(null) }

    val isLoadingCombined = activeMethod != LoginMethod.NONE || isLoadingExternal

    LaunchedEffect(Unit) { delay(100); visible = true }

    LaunchedEffect(error) {
        if (error != null) {
            firebaseError = error
            activeMethod = LoginMethod.NONE
        }
    }

    fun doValidateEmail(value: String): Boolean {
        if (value.isBlank()) { emailError = strings.emailError; return false }
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!emailRegex.matches(value)) { emailError = strings.emailError; return false }
        emailError = null; return true
    }

    fun doValidatePassword(value: String): Boolean {
        if (value.isBlank()) { passwordError = strings.passwordError; return false }
        if (value.length < 6) { passwordError = strings.passwordError; return false }
        passwordError = null; return true
    }

    fun clearErrors() {
        firebaseError = null
    }

    val isFormValid = email.isNotBlank() && password.length >= 6

    val accent = Ember
    val glassBg = MediumTeal.copy(alpha = 0.25f)
    val glassBorder = MediumTeal.copy(alpha = 0.4f)

    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "logoScale"
    )
    val logoAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "logoAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.login_bg2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(2.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(DeepTealBlack.copy(alpha = 0.85f), DarkTeal.copy(alpha = 0.6f), DeepTealBlack.copy(alpha = 0.9f))
                )
            )
        )

        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    listOf(Ember.copy(alpha = 0.12f), MediumTeal.copy(alpha = 0.05f), Color.Transparent), radius = 800f
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(initialScale = 0.3f, animationSpec = tween(800)) + fadeIn(animationSpec = tween(800))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Kinetic Logo",
                        modifier = Modifier
                            .size(72.dp)
                            .graphicsLayer { scaleX = logoScale; scaleY = logoScale; alpha = logoAlpha }
                            .shadow(20.dp, CircleShape, ambientColor = accent.copy(alpha = 0.4f))
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("KINETIC", fontSize = 32.sp, letterSpacing = 10.sp, color = Cream, fontFamily = Varien)
                    Spacer(Modifier.height(4.dp))
                    Text(strings.appTagline, fontSize = 11.sp, letterSpacing = 4.sp, color = Cream.copy(alpha = 0.5f))
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Email / Password Fields ────────────────────────────────
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
                            email = it; clearErrors()
                            if (emailError != null) doValidateEmail(it)
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
                    if (emailError != null) Text(emailError!!, color = accent, fontSize = 11.sp)

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it; clearErrors()
                            if (passwordError != null) doValidatePassword(it)
                        },
                        label = { Text(strings.password) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = accent) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null, tint = Cream.copy(alpha = 0.7f)
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
                    if (passwordError != null) Text(passwordError!!, color = accent, fontSize = 11.sp)

                    if (onForgotPassword != null) {
                        Text(
                            strings.forgotPassword,
                            color = accent.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !isLoadingCombined) { onForgotPassword(email) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // ── Firebase Error ─────────────────────────────────────────
            AnimatedVisibility(visible = firebaseError != null) {
                firebaseError?.let { err ->
                    Spacer(Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.15f)),
                        border = CardDefaults.outlinedCardBorder().copy(/* default brush = accent border */)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(err, color = accent, fontSize = 12.sp, lineHeight = 16.sp)
                        }
                    }
                }
            }

            // ── Sign Up Success ─────────────────────────────────────────
            AnimatedVisibility(visible = signUpSuccess) {
                Spacer(Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = strings.signUpSuccessMessage,
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onSignUpSuccessDismiss, modifier = Modifier.size(18.dp)) {
                            Icon(Icons.Default.Close, contentDescription = strings.close, tint = Color(0xFF4CAF50).copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Login Button ───────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(600, delayMillis = 350)) + fadeIn(animationSpec = tween(600, delayMillis = 350))
            ) {
                Button(
                    onClick = {
                        if (!isLoadingCombined) {
                            val emailValid = doValidateEmail(email)
                            val passValid = doValidatePassword(password)
                            if (emailValid && passValid) {
                                activeMethod = LoginMethod.EMAIL
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
                        modifier = Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(
                                if (isFormValid) listOf(accent, EmberDark) else listOf(MediumTeal, MediumTeal)
                            )
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (activeMethod == LoginMethod.EMAIL) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Cream, strokeWidth = 2.dp)
                        } else {
                            Text(strings.login, fontSize = 15.sp, letterSpacing = 3.sp, color = if (isFormValid) Cream else Cream.copy(alpha = 0.4f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── OR Divider + Social Login (only if any social is available) ──
            if (isGoogleAvailable || isFacebookAvailable) {
                AnimatedVisibility(visible = visible, enter = fadeIn(animationSpec = tween(600, delayMillis = 450))) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, glassBorder))))
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 14.dp)
                                .background(glassBg, RoundedCornerShape(20.dp))
                                .border(1.dp, glassBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Text(strings.or.uppercase(), color = Cream.copy(alpha = 0.6f), fontSize = 11.sp, letterSpacing = 3.sp)
                        }
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(glassBorder, Color.Transparent))))
                    }
                }

                Spacer(Modifier.height(10.dp))

                // ── Social Login Buttons ───────────────────────────────────
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(600, delayMillis = 500)) + fadeIn(animationSpec = tween(600, delayMillis = 500))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isGoogleAvailable) {
                            SocialLoginButton(
                                text = strings.loginWithGoogle,
                                icon = {
                                    if (activeMethod == LoginMethod.GOOGLE) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GoogleBlue, strokeWidth = 2.dp)
                                    } else {
                                        Text("G", color = GoogleBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                },
                                bgColor = glassBg, borderColor = glassBorder,
                                enabled = !isLoadingCombined,
                                onClick = {
                                    activeMethod = LoginMethod.GOOGLE
                                    firebaseError = null
                                    onGoogleLogin()
                                }
                            )
                        }
                        if (isFacebookAvailable) {
                            SocialLoginButton(
                                text = strings.loginWithFacebook,
                                icon = {
                                    if (activeMethod == LoginMethod.FACEBOOK) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = FacebookBlue, strokeWidth = 2.dp)
                                    } else {
                                        Text("f", color = FacebookBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                },
                                bgColor = glassBg, borderColor = glassBorder,
                                enabled = !isLoadingCombined,
                                onClick = {
                                    activeMethod = LoginMethod.FACEBOOK
                                    firebaseError = null
                                    onFacebookLogin()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Guest Login ────────────────────────────────────────────
            AnimatedVisibility(visible = visible, enter = fadeIn(animationSpec = tween(600, delayMillis = 600))) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isLoadingCombined) {
                            activeMethod = LoginMethod.GUEST
                            firebaseError = null
                            onGuestLogin()
                        }
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeMethod == LoginMethod.GUEST) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Cream.copy(alpha = 0.6f), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        strings.loginAsGuest,
                        color = Cream.copy(alpha = if (activeMethod == LoginMethod.GUEST) 1f else 0.6f),
                        fontSize = 13.sp,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // ── Sign Up Link ───────────────────────────────────────────
            AnimatedVisibility(visible = visible, enter = fadeIn(animationSpec = tween(600, delayMillis = 650))) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.dontHaveAccount + " ", color = Cream.copy(alpha = 0.7f), fontSize = 13.sp)
                    Text(
                        strings.signUp,
                        color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = !isLoadingCombined) { onSignUpClick() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // ── Full-screen loading overlay ────────────────────────────────
        if (isLoadingCombined) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(52.dp), color = accent, strokeWidth = 4.dp,
                    trackColor = DarkTeal.copy(alpha = 0.4f)
                )
            }
        }

        // ── Language Button (top right) ────────────────────────────────
        if (onLanguageClick != null && !isLoadingCombined) {
            Box(            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
                .background(glassBg, RoundedCornerShape(12.dp))
                .border(1.dp, glassBorder, RoundedCornerShape(12.dp))
                .clickable { onLanguageClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = null,
                        tint = Cream.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        LanguageManager.getLanguage().uppercase(),
                        color = Cream.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
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
    Row(            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(if (enabled) bgColor else bgColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .border(1.dp, if (enabled) borderColor else borderColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(Modifier.width(10.dp))
        Text(text, color = if (enabled) Cream else Cream.copy(alpha = 0.3f), fontSize = 14.sp, letterSpacing = 2.sp)
    }
}
