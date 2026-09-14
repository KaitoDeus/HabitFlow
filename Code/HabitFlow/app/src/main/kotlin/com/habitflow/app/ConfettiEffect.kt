package com.habitflow.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class ConfettiParticle(
    val xRatio: Float,
    val yOffsetRatio: Float,
    val speed: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    val rotationSpeed: Float,
    val initialAngle: Float
)

@Composable
fun ConfettiEffect(
    visible: Boolean,
    onFinished: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val colors = remember {
        listOf(
            Color(0xFF39FF14), // Neon Green
            Color(0xFFFF3366), // Pink Red
            Color(0xFFFFD700), // Gold Yellow
            Color(0xFF00E5FF), // Cyan
            Color(0xFFFF9100), // Orange
            Color(0xFF7C4DFF), // Purple
            Color(0xFF00E676)  // Green
        )
    }

    val particles = remember {
        List(65) {
            ConfettiParticle(
                xRatio = Random.nextFloat(),
                yOffsetRatio = Random.nextFloat() * 0.25f,
                speed = Random.nextFloat() * 0.7f + 0.5f,
                color = colors.random(),
                width = Random.nextFloat() * 12f + 8f,
                height = Random.nextFloat() * 18f + 12f,
                rotationSpeed = Random.nextFloat() * 720f - 360f,
                initialAngle = Random.nextFloat() * 360f
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2500, easing = LinearEasing)
        )
        onFinished()
    }

    val animProgress = progress.value
    val alpha = if (animProgress > 0.8f) (1f - animProgress) / 0.2f else 1f

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            val totalY = (p.yOffsetRatio + animProgress * p.speed)
            val currentY = (totalY % 1.15f) * size.height
            val currentX = p.xRatio * size.width + kotlin.math.sin((animProgress * 6f + p.xRatio * 10f).toDouble()).toFloat() * 20f
            val currentRotation = p.initialAngle + p.rotationSpeed * animProgress

            rotate(degrees = currentRotation, pivot = Offset(currentX, currentY)) {
                drawRect(
                    color = p.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                    topLeft = Offset(currentX - p.width / 2, currentY - p.height / 2),
                    size = Size(p.width, p.height)
                )
            }
        }
    }
}
