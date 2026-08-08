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

enum class SignUpMethod { NONE, EMAIL, GOOGLE, FACEBOOK }

@Composable
fun SignUpScreen(
    strings: LanguageManager.Strings,
    isDark: Boolean,
    onSignUp: (name: String, email: String, password: String, weight: Double?, height: Double?) -> Unit,
    onGoogleSignUp: () -> Unit,
    onFacebookSignUp: () -> Unit,
    onLoginClick: () -> Unit,
    onLanguageClick: (() -> Unit)? = null,
    error: String? = null,
    isLoadingExternal: Boolean = false,
    emailAlreadyInUseAction: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    var activeMethod by remember { mutableStateOf(SignUpMethod.NONE) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var firebaseError by remember { mutableStateOf<String?>(null) }
    var isEmailInUse by remember { mutableStateOf(false) }

    val isLoadingCombined = activeMethod != SignUpMethod.NONE || isLoadingExternal

    LaunchedEffect(Unit) { delay(100); visible = true }

    LaunchedEffect(error) {
        if (error != null) {
            firebaseError = error
            activeMethod = SignUpMethod.NONE
            isEmailInUse = error.contains("email", ignoreCase = true) || error.contains("exists", ignoreCase = true)
        }
    }

    fun getPasswordStrength(pw: String): Int {
        if (pw.length < 6) return 0
        var score = 0
        if (pw.length >= 8) score++
        if (pw.length >= 12) score++
        if (pw.any { it.isUpperCase() }) score++
        if (pw.any { it.isLowerCase() }) score++
        if (pw.any { it.isDigit() }) score++
        if (pw.any { !it.isLetterOrDigit() }) score++
        return when { score <= 2 -> 1; score <= 4 -> 2; else -> 3 }
    }

    fun doValidateName(value: String): Boolean {
        if (value.isBlank() || value.trim().length < 2) { nameError = strings.nameError; return false }
        nameError = null; return true
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

    fun doValidateConfirmPassword(value: String): Boolean {
        if (value != password) { confirmPasswordError = strings.passwordMismatch; return false }
        confirmPasswordError = null
        return true
    }

    fun clearErrors() { firebaseError = null; isEmailInUse = false }

    val isFormValid = name.length >= 2 && email.isNotBlank() && password.length >= 6 && confirmPassword == password && acceptedTerms

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

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accent, unfocusedBorderColor = Cream.copy(alpha = 0.3f),
        focusedContainerColor = Color(0xFF1A1A1A).copy(alpha = 0.8f),
        unfocusedContainerColor = Color(0xFF151515).copy(alpha = 0.8f),
        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
        focusedLabelColor = Cream, unfocusedLabelColor = Cream.copy(alpha = 0.9f),
        cursorColor = accent, focusedLeadingIconColor = accent,
        unfocusedLeadingIconColor = Cream.copy(alpha = 0.8f),
        focusedTrailingIconColor = Cream.copy(alpha = 0.8f),
        unfocusedTrailingIconColor = Cream.copy(alpha = 0.6f),
        focusedPlaceholderColor = Cream.copy(alpha = 0.5f),
        unfocusedPlaceholderColor = Cream.copy(alpha = 0.5f)
    )

    val errorFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accent, unfocusedBorderColor = accent,
        focusedContainerColor = Color(0xFF1A1A1A).copy(alpha = 0.8f),
        unfocusedContainerColor = Color(0xFF151515).copy(alpha = 0.8f),
        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
        focusedLabelColor = Cream, unfocusedLabelColor = Cream.copy(alpha = 0.9f),
        cursorColor = accent, focusedLeadingIconColor = accent,
        unfocusedLeadingIconColor = Cream.copy(alpha = 0.8f),
        focusedTrailingIconColor = Cream.copy(alpha = 0.8f),
        unfocusedTrailingIconColor = Cream.copy(alpha = 0.6f),
        focusedPlaceholderColor = Cream.copy(alpha = 0.5f),
        unfocusedPlaceholderColor = Cream.copy(alpha = 0.5f)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.login_bg2), contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(2.dp), contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(DeepTealBlack.copy(alpha = 0.85f), DarkTeal.copy(alpha = 0.6f), DeepTealBlack.copy(alpha = 0.9f)))
        ))
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(Ember.copy(alpha = 0.12f), MediumTeal.copy(alpha = 0.05f), Color.Transparent), radius = 800f)
        ))

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(40.dp))

            AnimatedVisibility(visible = visible, enter = scaleIn(initialScale = 0.3f, animationSpec = tween(800)) + fadeIn(animationSpec = tween(800))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.logo), contentDescription = "Kinetic Logo",
                        modifier = Modifier
                            .size(56.dp)
                            .graphicsLayer { scaleX = logoScale; scaleY = logoScale; alpha = logoAlpha }
                            .shadow(20.dp, CircleShape, ambientColor = accent.copy(alpha = 0.4f))
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(strings.createAccountTitle, fontSize = 24.sp, letterSpacing = 3.sp, color = Cream)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Form Fields ────────────────────────────────────────────
            AnimatedVisibility(visible = visible, enter = slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(600, delayMillis = 200)) + fadeIn(animationSpec = tween(600, delayMillis = 200))) {
                Column(
                    modifier = Modifier.fillMaxWidth().background(glassBg, RoundedCornerShape(20.dp))
                        .border(1.dp, glassBorder, RoundedCornerShape(20.dp)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it; clearErrors(); if (nameError != null) doValidateName(it) },
                        label = { Text(strings.nameField) },
                        placeholder = { Text(strings.nameField, color = Cream.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = accent) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp),
                        colors = if (nameError != null) errorFieldColors else fieldColors
                    )
                    if (nameError != null) Text(nameError!!, color = accent, fontSize = 11.sp)

                    OutlinedTextField(
                        value = email, onValueChange = { email = it; clearErrors(); if (emailError != null) doValidateEmail(it) },
                        label = { Text(strings.email) },
                        placeholder = { Text(strings.email, color = Cream.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = accent) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(14.dp),
                        colors = if (emailError != null) errorFieldColors else fieldColors
                    )
                    if (emailError != null) Text(emailError!!, color = accent, fontSize = 11.sp)

                    OutlinedTextField(
                            value = password, onValueChange = {
                                password = it
                                clearErrors()
                                if (passwordError != null) doValidatePassword(it)
                                if (confirmPassword.isNotBlank()) doValidateConfirmPassword(confirmPassword)
                            },
                        label = { Text(strings.password) },
                        placeholder = { Text(strings.password, color = Cream.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = accent) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = Cream.copy(alpha = 0.5f))
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp),
                        colors = if (passwordError != null) errorFieldColors else fieldColors
                    )
                    if (passwordError != null) Text(passwordError!!, color = accent, fontSize = 11.sp)

                    if (password.length >= 6) {
                        val strength = getPasswordStrength(password)
                        val strengthText = when (strength) { 1 -> strings.passwordStrengthWeak; 2 -> strings.passwordStrengthMedium; 3 -> strings.passwordStrengthStrong; else -> "" }
                        val strengthColor = when (strength) { 1 -> RecoveryRed; 2 -> RecoveryYellow; 3 -> RecoveryGreen; else -> Cream.copy(alpha = 0.3f) }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f).height(4.dp).background(MediumTeal, RoundedCornerShape(2.dp))) {
                                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction = strength / 3f).background(strengthColor, RoundedCornerShape(2.dp)))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(strengthText, color = strengthColor, fontSize = 10.sp)
                        }
                    }

                    OutlinedTextField(
                        value = confirmPassword, onValueChange = { confirmPassword = it; if (confirmPasswordError != null) doValidateConfirmPassword(it) },
                        label = { Text(strings.confirmPassword) },
                        placeholder = { Text(strings.confirmPassword, color = Cream.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = accent) },
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = Cream.copy(alpha = 0.5f))
                            }
                        },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp),
                        colors = if (confirmPasswordError != null) errorFieldColors else fieldColors
                    )
                    if (confirmPasswordError != null) Text(confirmPasswordError!!, color = accent, fontSize = 11.sp)

                    HorizontalDivider(color = glassBorder, modifier = Modifier.padding(vertical = 4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weightInput, onValueChange = { weightInput = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text(strings.weightKg) }, placeholder = { Text(strings.weightKg, color = Cream.copy(alpha = 0.4f)) },
                            modifier = Modifier.weight(1f), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp), colors = fieldColors
                        )
                        OutlinedTextField(
                            value = heightInput, onValueChange = { heightInput = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text(strings.heightCm) }, placeholder = { Text(strings.heightCm, color = Cream.copy(alpha = 0.4f)) },
                            modifier = Modifier.weight(1f), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp), colors = fieldColors
                        )
                    }
                    Text(strings.optional, color = Cream.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }

            // ── Error Card ─────────────────────────────────────────────
            AnimatedVisibility(visible = firebaseError != null) {
                firebaseError?.let { err ->
                    Spacer(Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(err, color = accent, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                            if (isEmailInUse && emailAlreadyInUseAction != null) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    strings.loginInstead, color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { emailAlreadyInUseAction() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Terms Checkbox ─────────────────────────────────────────
            AnimatedVisibility(visible = visible, enter = fadeIn(animationSpec = tween(600, delayMillis = 300))) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { acceptedTerms = !acceptedTerms }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = acceptedTerms, onCheckedChange = { acceptedTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = accent, uncheckedColor = Cream.copy(alpha = 0.4f), checkmarkColor = Cream))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.termsPrefix, color = Cream.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }

            if (acceptedTerms) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(strings.termsAndConditions, color = accent, fontSize = 11.sp,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { }.padding(horizontal = 2.dp))
                    Text(" & ", color = Cream.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(strings.privacyPolicyLink, color = accent, fontSize = 11.sp,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { }.padding(horizontal = 2.dp))
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Sign Up Button ─────────────────────────────────────────
            AnimatedVisibility(visible = visible, enter = slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(600, delayMillis = 400)) + fadeIn(animationSpec = tween(600, delayMillis = 400))) {
                Button(
                    onClick = {
                        if (!isLoadingCombined) {
                            val nameValid = doValidateName(name)
                            val emailValid = doValidateEmail(email)
                            val passValid = doValidatePassword(password)
                            val confirmValid = doValidateConfirmPassword(confirmPassword)
                            if (nameValid && emailValid && passValid && confirmValid && acceptedTerms) {
                                activeMethod = SignUpMethod.EMAIL
                                firebaseError = null
                                onSignUp(name, email, password, weightInput.toDoubleOrNull(), heightInput.toDoubleOrNull())
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = accent.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    enabled = !isLoadingCombined && isFormValid, contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(if (isFormValid) listOf(accent, EmberDark) else listOf(MediumTeal, MediumTeal))
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (activeMethod == SignUpMethod.EMAIL) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Cream, strokeWidth = 2.dp)
                        } else {
                            Text(strings.signUp, fontSize = 15.sp, letterSpacing = 3.sp, color = if (isFormValid) Cream else Cream.copy(alpha = 0.4f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── OR Divider ─────────────────────────────────────────────
            AnimatedVisibility(visible = visible, enter = fadeIn(animationSpec = tween(600, delayMillis = 450))) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, glassBorder))))
                    Box(modifier = Modifier.padding(horizontal = 14.dp).background(glassBg, RoundedCornerShape(20.dp))
                        .border(1.dp, glassBorder, RoundedCornerShape(20.dp)).padding(horizontal = 14.dp, vertical = 5.dp)) {
                        Text(strings.or.uppercase(), color = Cream.copy(alpha = 0.6f), fontSize = 11.sp, letterSpacing = 3.sp)
                    }
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(glassBorder, Color.Transparent))))
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Social Sign-Up Buttons ─────────────────────────────────
            AnimatedVisibility(visible = visible, enter = slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(600, delayMillis = 500)) + fadeIn(animationSpec = tween(600, delayMillis = 500))) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SocialSignUpButton(
                        text = strings.loginWithGoogle,
                        icon = {
                            if (activeMethod == SignUpMethod.GOOGLE) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GoogleBlue, strokeWidth = 2.dp)
                            } else {
                                Text("G", color = GoogleBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        },
                        bgColor = glassBg, borderColor = glassBorder, enabled = !isLoadingCombined,
                        onClick = { activeMethod = SignUpMethod.GOOGLE; firebaseError = null; onGoogleSignUp() }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Login Link ─────────────────────────────────────────────
            AnimatedVisibility(visible = visible, enter = fadeIn(animationSpec = tween(600, delayMillis = 550))) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.alreadyHaveAccount + " ", color = Cream.copy(alpha = 0.7f), fontSize = 13.sp)
                    Text(
                        strings.login, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(enabled = !isLoadingCombined) { onLoginClick() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // ── Full-screen loading overlay ────────────────────────────────
        if (isLoadingCombined) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(52.dp), color = accent, strokeWidth = 4.dp, trackColor = DarkTeal.copy(alpha = 0.4f))
            }
        }

        // ── Language Button (top right) ────────────────────────────────
        if (onLanguageClick != null && !isLoadingCombined) {
            Box(
                modifier = Modifier
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
private fun SocialSignUpButton(
    text: String, icon: @Composable () -> Unit, bgColor: Color, borderColor: Color,
    enabled: Boolean = true, onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp)            .background(if (enabled) bgColor else bgColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .border(1.dp, if (enabled) borderColor else borderColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
    ) {
        icon(); Spacer(Modifier.width(10.dp))
        Text(text, color = if (enabled) Cream else Cream.copy(alpha = 0.3f), fontSize = 14.sp, letterSpacing = 2.sp)
    }
}
