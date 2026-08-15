package com.example.kinetic

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    onBarcodeScanned: (String) -> Unit,
    onBack: () -> Unit
) {
    val p = appPalette(isDark)
    val surfaceBg = p.bg
    val textPrimary = p.tp
    val textSecondary = p.ts
    val accent = p.ac
    val context = LocalContext.current

    var isScanning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun checkGooglePlayServices(): Boolean {
        val availability = GoogleApiAvailability.getInstance()
        val result = availability.isGooglePlayServicesAvailable(context)
        if (result != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(result)) {
                val activity = context.findActivity()
                if (activity != null) {
                    availability.getErrorDialog(activity, result, 0)?.show()
                }
            }
            return false
        }
        return true
    }

    fun startScan() {
        if (!checkGooglePlayServices()) {
            errorMessage = strings.scanBarcodeHelp
            return
        }
        isScanning = true
        errorMessage = null
        try {
            val scanner = GmsBarcodeScanning.getClient(context)
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    isScanning = false
                    barcode.rawValue?.let { onBarcodeScanned(it) }
                }
                .addOnCanceledListener {
                    isScanning = false
                    errorMessage = strings.cancel
                }
                .addOnFailureListener { e: Exception ->
                    isScanning = false
                    errorMessage = e.message ?: strings.error
                }
        } catch (e: Exception) {
            isScanning = false
            errorMessage = e.message ?: strings.error
        }
    }

    LaunchedEffect(Unit) {
        startScan()
    }

    Scaffold(
        topBar = {
            KineticAppBar(onBack = onBack)
        },
        containerColor = surfaceBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = accent)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(strings.scanning, color = textSecondary)
                }
            } else if (errorMessage != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = textSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage!!,
                        color = textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.scanBarcodeHelp,
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { startScan() },
                        modifier = Modifier.background(RedButtonGradient, RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.retry, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
