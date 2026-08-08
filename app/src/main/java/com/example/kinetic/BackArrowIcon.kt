package com.example.kinetic

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BackArrowIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 28.dp,
    triangleColor: Color = Color.White,
    circleColor: Color = Color.White
) {
    Canvas(
        modifier = modifier
            .size(iconSize)
            .clickable { onClick() }
    ) {
        val w = size.width
        val h = size.height

        val circleRadius = w * 0.28f
        val circleCenterX = w * 0.75f
        val circleCenterY = h * 0.5f

        val triLeft = w * 0.05f
        val triTopY = h * 0.5f
        val triRight = w * 0.62f

        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(triLeft, triTopY)
                lineTo(triRight, h * 0.15f)
                lineTo(triRight, h * 0.85f)
                close()
            },
            color = triangleColor,
            style = Fill
        )

        drawCircle(
            color = circleColor,
            radius = circleRadius,
            center = Offset(circleCenterX, circleCenterY)
        )
    }
}
