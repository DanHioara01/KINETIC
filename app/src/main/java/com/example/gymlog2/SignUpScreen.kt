package com.example.gymlog2

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
import com.example.gymlog2.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SignUpScreen(
    strings: LanguageManager.Strings,
    isDark: Boolean,
    onSignUp: (name: String, email: String, password: String, weight: Double?, height: Double?) -> Unit,
    onGoogleSignUp: () -> Unit,
    onFacebookSignUp: () -> Unit,
    onLoginClick: () -> Unit,
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
    var isLoading by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var firebaseError by remember { mutableStateOf<String?>(null) }
    var isEmailInUse by remember { mutableStateOf(false) }

    val isLoadingCombined = isLoading || isLoadingExternal

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    LaunchedEffect(error) {
        firebaseError = error
        if (error != null) {
            isLoading = false
            isEmailInUse = error.contains("email") || error.contains("Ã®nregistrat") || error.contains("registered")
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
        return when {
            score <= 2 -> 1
            score <= 4 -> 2
            else -> 3
        }
    }

    fun validateName(value: String): Boolean {
        if (value.isBlank() || value.length < 2) {
            nameError = strings.nameError
            return false
        }
        nameError = null
        return true
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

    fun validateConfirmPassword(value: String): Boolean {
        if (value != password) {
            confirmPasswordError = strings.passwordMismatch
            return false
        }
        confirmPasswordError = null
        return true
    }

    val isFormValid = name.length >= 2 &&
            email.isNotBlank() &&
            password.length >= 6 &&
            confirmPassword == password &&
            acceptedTerms

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

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accent,
        unfocusedBorderColor = Cream.copy(alpha = 0.3f),
        focusedContainerColor = Color(0xFF1A1A1A).copy(alpha = 0.8f),
        unfocusedContainerColor = Color(0xFF151515).copy(alpha = 0.8f),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Cream,
        unfocusedLabelColor = Cream.copy(alpha = 0.9f),
        cursorColor = accent,
        focusedLeadingIconColor = accent,
        unfocusedLeadingIconColor = Cream.copy(alpha = 0.8f),
        focusedTrailingIconColor = Cream.copy(alpha = 0.8f),
        unfocusedTrailingIconColor = Cream.copy(alpha = 0.6f),
        focusedPlaceholderColor = Cream.copy(alpha = 0.5f),
        unfocusedPlaceholderColor = Cream.copy(alpha = 0.5f)
    )

    val errorFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accent,
        unfocusedBorderColor = accent,
        focusedContainerColor = Color(0xFF1A1A1A).copy(alpha = 0.8f),
        unfocusedContainerColor = Color(0xFF151515).copy(alpha = 0.8f),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Cream,
        unfocusedLabelColor = Cream.copy(alpha = 0.9f),
        cursorColor = accent,
        focusedLeadingIconColor = accent,
        unfocusedLeadingIconColor = Cream.copy(alpha = 0.8f),
        focusedTrailingIconColor = Cream.copy(alpha = 0.8f),
        unfocusedTrailingIconColor = Cream.copy(alpha = 0.6f),
        focusedPlaceholderColor = Cream.copy(alpha = 0.5f),
        unfocusedPlaceholderColor = Cream.copy(alpha = 0.5f)
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
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(40.dp))

            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(initialScale = 0.3f, animationSpec = tween(800)) + fadeIn(animationSpec = tween(800))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher),
                        contentDescription = "Kinetic Logo",
                        modifier = Modifier
                            .size(56.dp)
                            .graphicsLayer {
                                scaleX = logoScale
                                scaleY = logoScale
                                alpha = logoAlpha
                            }
                            .shadow(20.dp, CircleShape, ambientColor = accent.copy(alpha = 0.4f))
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        strings.createAccountTitle,
                        fontSize = 24.sp,
                        letterSpacing = 3.sp,
                        color = Cream
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (nameError != null) validateName(it)
                        },
                        label = { Text(strings.nameField) },
                        placeholder = { Text(strings.nameField, color = Cream.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = accent) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = if (nameError != null) errorFieldColors else fieldColors
                    )
                    if (nameError != null) {
                        Text(nameError!!, color = accent, fontSize = 11.sp)
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailError != null) validateEmail(it)
                            firebaseError = null
                            isEmailInUse = false
                        },
                        label = { Text(strings.email) },
                        placeholder = { Text(strings.email, color = Cream.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = accent) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(14.dp),
                        colors = if (emailError != null) errorFieldColors else fieldColors
                    )
                    if (emailError != null) {
                        Text(emailError!!, color = accent, fontSize = 11.sp)
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordError != null) validatePassword(it)
                            if (confirmPassword.isNotBlank()) validateConfirmPassword(confirmPassword)
                        },
                        label = { Text(strings.password) },
                        placeholder = { Text(strings.password, color = Cream.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = accent) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Cream.copy(alpha = 0.5f)
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = if (passwordError != null) errorFieldColors else fieldColors
                    )
                    if (passwordError != null) {
                        Text(passwordError!!, color = accent, fontSize = 11.sp)
                    }

                    if (password.length >= 6) {
                        val strength = getPasswordStrength(password)
                        val strengthText = when (strength) {
                            1 -> strings.passwordStrengthWeak
                            2 -> strings.passwordStrengthMedium
                            3 -> strings.passwordStrengthStrong
                            else -> ""
                        }
                        val strengthColor = when (strength) {
                            1 -> RecoveryRed
                            2 -> RecoveryYellow
                            3 -> RecoveryGreen
                            else -> Cream.copy(alpha = 0.3f)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MediumTeal)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = strength / 3f)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(strengthColor)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(strengthText, color = strengthColor, fontSize = 10.sp)
                        }
                    }

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (confirmPasswordError != null) validateConfirmPassword(it)
                        },
                        label = { Text(strings.confirmPassword) },
                        placeholder = { Text(strings.confirmPassword, color = Cream.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = accent) },
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Cream.copy(alpha = 0.5f)
                                )
                            }
                        },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = if (confirmPasswordError != null) errorFieldColors else fieldColors
                    )
                    if (confirmPasswordError != null) {
                        Text(confirmPasswordError!!, color = accent, fontSize = 11.sp)
                    }

                    HorizontalDivider(color = glassBorder, modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text(strings.weightKg) },
                            placeholder = { Text(strings.weightKg, color = Cream.copy(alpha = 0.4f)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors
                        )
                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = { heightInput = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text(strings.heightCm) },
                            placeholder = { Text(strings.heightCm, color = Cream.copy(alpha = 0.4f)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors
                        )
                    }
                    Text(
                        strings.optional,
                        color = Cream.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            if (firebaseError != null) {
                Spacer(Modifier.height(6.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(firebaseError!!, color = accent, fontSize = 12.sp, textAlign = TextAlign.Center)
                    if (isEmailInUse && emailAlreadyInUseAction != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            strings.loginInstead,
                            color = accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { emailAlreadyInUseAction() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 300))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { acceptedTerms = !acceptedTerms }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = acceptedTerms,
                        onCheckedChange = { acceptedTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = accent,
                            uncheckedColor = Cream.copy(alpha = 0.4f),
                            checkmarkColor = Cream
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        buildString {
                            append(strings.termsPrefix)
                        },
                        color = Cream.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            if (acceptedTerms) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        strings.termsAndConditions,
                        color = accent,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { }
                            .padding(horizontal = 2.dp)
                    )
                    Text(" & ", color = Cream.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(
                        strings.privacyPolicyLink,
                        color = accent,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { }
                            .padding(horizontal = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(600, delayMillis = 400)) + fadeIn(animationSpec = tween(600, delayMillis = 400))
            ) {
                Button(
                    onClick = {
                        if (!isLoadingCombined) {
                            val nameValid = validateName(name)
                            val emailValid = validateEmail(email)
                            val passwordValid = validatePassword(password)
                            val confirmValid = validateConfirmPassword(confirmPassword)
                            if (nameValid && emailValid && passwordValid && confirmValid && acceptedTerms) {
                                isLoading = true
                                firebaseError = null
                                val weight = weightInput.toDoubleOrNull()
                                val height = heightInput.toDoubleOrNull()
                                onSignUp(name, email, password, weight, height)
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
                                strings.signUp,
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
                    SocialSignUpButton(
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
                                onGoogleSignUp()
                            }
                        }
                    )
                    SocialSignUpButton(
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
                                onFacebookSignUp()
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 550))
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        strings.alreadyHaveAccount + " ",
                        color = Cream.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Text(
                        strings.login,
                        color = accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = !isLoadingCombined) {
                                onLoginClick()
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
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
private fun SocialSignUpButton(
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
