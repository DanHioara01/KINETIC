package com.example.kinetic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyState(
    icon: ImageVector,
    iconPainter: Painter? = null,
    title: String,
    subtitle: String,
    textPrimary: Color,
    textSecondary: Color,
    accent: Color? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon container
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = (accent ?: textSecondary).copy(alpha = 0.1f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (iconPainter != null) {
                    Icon(
                    painter = iconPainter,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                    modifier = Modifier.size(34.dp)
                )
                } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent ?: textSecondary,
                    modifier = Modifier.size(32.dp)
                )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            subtitle,
            fontSize = 14.sp,
            color = textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent ?: textSecondary
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    actionLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
