package com.example.kinetic

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.kinetic.ui.theme.*

@Composable
fun LineChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    lineColor: Color = accentColor(),
    dotColor: Color = accentColor()
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.second }
    val minValue = data.minOf { it.second }.coerceAtMost(maxValue * 0.8)
    val range = (maxValue - minValue).coerceAtLeast(1.0)
    val topPadding = 32.dp
    val bottomPadding = 40.dp
    val startPadding = 48.dp
    val endPadding = 16.dp

    val gridLines = 4
    val resolvedDivider = dividerColor()
    val resolvedCard = cardColor()

    val textPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 24f
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val chartLeft = startPadding.toPx()
        val chartRight = w - endPadding.toPx()
        val chartTop = topPadding.toPx()
        val chartBottom = h - bottomPadding.toPx()
        val chartW = chartRight - chartLeft
        val chartH = chartBottom - chartTop

        for (i in 0..gridLines) {
            val y = chartTop + chartH * i / gridLines
            drawLine(
                color = resolvedDivider,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
            val value = maxValue - (range * i / gridLines)
            drawContext.canvas.nativeCanvas.apply {
                textPaint.color = 0xFFB0B0B0.toInt()
                textPaint.textSize = 24f
                textPaint.textAlign = android.graphics.Paint.Align.RIGHT
                drawText("%.0f".format(value), chartLeft - 8f, y + 6f, textPaint)
            }
        }

        if (data.size == 1) {
            val x = chartLeft + chartW / 2
            val y = chartBottom - ((data[0].second - minValue) / range * chartH).toFloat()
            drawCircle(color = dotColor, radius = 8f, center = Offset(x, y))
            drawContext.canvas.nativeCanvas.apply {
                textPaint.color = 0xFFFFFFFF.toInt()
                textPaint.textSize = 22f
                textPaint.textAlign = android.graphics.Paint.Align.CENTER
                drawText("%.1f".format(data[0].second), x, y - 16f, textPaint)
                textPaint.color = 0xFFB0B0B0.toInt()
                drawText(data[0].first, x, chartBottom + 30f, textPaint)
            }
            return@Canvas
        }

        val points = data.mapIndexed { index, (label, value) ->
            val x = chartLeft + chartW * index / (data.size - 1)
            val y = chartBottom - ((value - minValue) / range * chartH).toFloat()
            Offset(x, y) to label
        }

        val path = Path()
        path.moveTo(points.first().first.x, chartBottom)
        points.forEach { (pt, _) -> path.lineTo(pt.x, pt.y) }
        path.lineTo(points.last().first.x, chartBottom)
        path.close()

        drawPath(
            path,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.3f), lineColor.copy(alpha = 0.0f)),
                startY = chartTop,
                endY = chartBottom
            )
        )

        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i].first,
                end = points[i + 1].first,
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        for (index in points.indices) {
            val (pt, label) = points[index]
            drawCircle(color = resolvedCard, radius = 7f, center = pt)
            drawCircle(color = dotColor, radius = 5f, center = pt)

            drawContext.canvas.nativeCanvas.apply {
                textPaint.color = 0xFFFFFFFF.toInt()
                textPaint.textSize = 20f
                textPaint.textAlign = android.graphics.Paint.Align.CENTER
                drawText("%.1f".format(data[index].second), pt.x, pt.y - 14f, textPaint)
                textPaint.color = 0xFFB0B0B0.toInt()
                drawText(label, pt.x, chartBottom + 28f, textPaint)
            }
        }
    }
}
