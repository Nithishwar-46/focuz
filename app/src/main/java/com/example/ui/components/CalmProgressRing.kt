package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary
import com.example.ui.theme.OffWhiteSubtle

/**
 * Reusable Calm Progress Ring component displaying time spent intentionally vs. scrolling.
 * Built strictly with the two shades of charcoal and off-white.
 */
@Composable
fun CalmProgressRing(
  intentionalMinutes: Int,
  scrollMinutes: Int,
  modifier: Modifier = Modifier
) {
  val totalMinutes = (intentionalMinutes + scrollMinutes).coerceAtLeast(1)
  val intentionalFraction = (intentionalMinutes.toFloat() / totalMinutes.toFloat()).coerceIn(0f, 1f)
  val scrollFraction = (scrollMinutes.toFloat() / totalMinutes.toFloat()).coerceIn(0f, 1f)

  val animatedIntentional by animateFloatAsState(
    targetValue = intentionalFraction,
    animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
    label = "intentional_progress"
  )

  val animatedScroll by animateFloatAsState(
    targetValue = scrollFraction,
    animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
    label = "scroll_progress"
  )

  val intentionalHours = intentionalMinutes / 60
  val intentionalRemainingMins = intentionalMinutes % 60
  val intentionalFormatted = if (intentionalHours > 0) "${intentionalHours}h ${intentionalRemainingMins}m" else "${intentionalRemainingMins}m"

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp))
      .background(CharcoalSurface)
      .padding(24.dp)
      .testTag("calm_progress_ring_container"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.size(190.dp)) {
          val strokeWidth = 14.dp.toPx()
          val diameter = size.minDimension - strokeWidth
          val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
          val arcSize = Size(diameter, diameter)

          // Background subtle track
          drawArc(
            color = CharcoalBorder,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )

          // Intentional Time Arc (Crisp Off-White Primary)
          val intentionalSweep = 360f * animatedIntentional
          if (intentionalSweep > 0f) {
            drawArc(
              color = OffWhitePrimary,
              startAngle = -90f,
              sweepAngle = intentionalSweep,
              useCenter = false,
              topLeft = topLeft,
              size = arcSize,
              style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
          }

          // Mindful Scrolling Arc (Muted Off-White Secondary)
          val scrollSweep = (360f * animatedScroll) - 8f // tiny gentle separation gap
          if (scrollSweep > 2f) {
            val scrollStart = -90f + intentionalSweep + 4f
            drawArc(
              color = OffWhiteMuted,
              startAngle = scrollStart,
              sweepAngle = scrollSweep.coerceAtLeast(0f),
              useCenter = false,
              topLeft = topLeft,
              size = arcSize,
              style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
          }
        }

        // Center Content
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = "${(intentionalFraction * 100).toInt()}%",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Light,
            color = OffWhitePrimary
          )
          Text(
            text = "Intentional",
            style = MaterialTheme.typography.labelSmall,
            color = OffWhiteMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Legend & breakdown
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Intentional stat
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(OffWhitePrimary)
          )
          Column {
            Text(
              text = intentionalFormatted,
              style = MaterialTheme.typography.labelLarge,
              color = OffWhitePrimary
            )
            Text(
              text = "Intentional",
              style = MaterialTheme.typography.bodySmall,
              color = OffWhiteMuted
            )
          }
        }

        // Subtle divider
        Box(
          modifier = Modifier
            .size(width = 1.dp, height = 24.dp)
            .background(CharcoalBorder)
        )

        // Scrolling stat
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(OffWhiteMuted)
          )
          Column {
            Text(
              text = "${scrollMinutes}m",
              style = MaterialTheme.typography.labelLarge,
              color = OffWhitePrimary
            )
            Text(
              text = "Mindful scroll",
              style = MaterialTheme.typography.bodySmall,
              color = OffWhiteMuted
            )
          }
        }
      }
    }
  }
}
