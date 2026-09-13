package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary
import com.example.ui.theme.OffWhiteSubtle

/**
 * Visual Duolingo-style streak flame & focus ring widget.
 * Designed with a minimalist monochromatic aesthetic using charcoal and off-white.
 */
@Composable
fun StreakFlameWidget(
  streakDays: Int,
  modifier: Modifier = Modifier,
  showLabel: Boolean = true,
  largeHero: Boolean = false
) {
  val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.96f,
    targetValue = 1.04f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  if (largeHero) {
    Column(
      modifier = modifier
        .clip(RoundedCornerShape(24.dp))
        .background(CharcoalSurface)
        .border(1.dp, CharcoalBorder, RoundedCornerShape(24.dp))
        .padding(28.dp)
        .testTag("streak_flame_hero"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
      ) {
        // Outer concentric wave ring
        Canvas(modifier = Modifier.size(110.dp)) {
          drawCircle(
            color = OffWhiteSubtle,
            radius = size.minDimension / 2f * pulseScale,
            style = Stroke(width = 1.5.dp.toPx())
          )
        }

        // Flame drawing
        Canvas(modifier = Modifier.size(64.dp)) {
          val w = size.width
          val h = size.height

          // Outer Flame Path
          val outerFlame = Path().apply {
            moveTo(w * 0.5f, h * 0.05f)
            cubicTo(w * 0.72f, h * 0.28f, w * 0.95f, h * 0.55f, w * 0.85f, h * 0.80f)
            cubicTo(w * 0.75f, h * 0.98f, w * 0.25f, h * 0.98f, w * 0.15f, h * 0.80f)
            cubicTo(w * 0.05f, h * 0.55f, w * 0.28f, h * 0.28f, w * 0.5f, h * 0.05f)
            close()
          }
          drawPath(outerFlame, color = OffWhitePrimary, style = Fill)

          // Inner Flame Core
          val innerFlame = Path().apply {
            moveTo(w * 0.5f, h * 0.40f)
            cubicTo(w * 0.65f, h * 0.55f, w * 0.72f, h * 0.70f, w * 0.65f, h * 0.85f)
            cubicTo(w * 0.58f, h * 0.93f, w * 0.42f, h * 0.93f, w * 0.35f, h * 0.85f)
            cubicTo(w * 0.28f, h * 0.70f, w * 0.35f, h * 0.55f, w * 0.5f, h * 0.40f)
            close()
          }
          drawPath(innerFlame, color = CharcoalSurface, style = Fill)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "$streakDays Day Streak",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.SemiBold,
        color = OffWhitePrimary
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Nice, $streakDays days of intentional focus 🔥",
        style = MaterialTheme.typography.bodyMedium,
        color = OffWhiteMuted
      )
    }
  } else {
    // Compact pill badge
    Row(
      modifier = modifier
        .clip(RoundedCornerShape(20.dp))
        .background(CharcoalSurface)
        .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
        .padding(horizontal = 14.dp, vertical = 8.dp)
        .testTag("streak_flame_pill"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height

        val flamePath = Path().apply {
          moveTo(w * 0.5f, h * 0.05f)
          cubicTo(w * 0.72f, h * 0.28f, w * 0.95f, h * 0.55f, w * 0.85f, h * 0.80f)
          cubicTo(w * 0.75f, h * 0.98f, w * 0.25f, h * 0.98f, w * 0.15f, h * 0.80f)
          cubicTo(w * 0.05f, h * 0.55f, w * 0.28f, h * 0.28f, w * 0.5f, h * 0.05f)
          close()
        }
        drawPath(flamePath, color = OffWhitePrimary, style = Fill)
      }

      Text(
        text = "$streakDays Days",
        style = MaterialTheme.typography.labelLarge,
        color = OffWhitePrimary
      )

      if (showLabel) {
        Box(
          modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(OffWhiteMuted)
        )
        Text(
          text = "Active",
          style = MaterialTheme.typography.labelSmall,
          color = OffWhiteMuted
        )
      }
    }
  }
}
