package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusContextMode
import com.example.data.FocuzUiState
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary
import com.example.ui.theme.OffWhiteSubtle

/**
 * Focus Session Screen.
 * Features active session timer, context label ("Exam Mode: tighter limits"), pause/end controls,
 * and encouraging calm microcopy.
 */
@Composable
fun FocusSessionScreen(
  uiState: FocuzUiState,
  onStartSession: () -> Unit,
  onPauseSession: () -> Unit,
  onResumeSession: () -> Unit,
  onEndSession: () -> Unit,
  onSelectMode: (FocusContextMode) -> Unit,
  onSetDuration: (Int) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val minutes = uiState.sessionRemainingSeconds / 60
  val seconds = uiState.sessionRemainingSeconds % 60
  val timeFormatted = String.format("%02d:%02d", minutes, seconds)

  val progressFraction = if (uiState.sessionTotalSeconds > 0) {
    (uiState.sessionRemainingSeconds.toFloat() / uiState.sessionTotalSeconds.toFloat()).coerceIn(0f, 1f)
  } else 0f

  val animatedProgress by animateFloatAsState(
    targetValue = progressFraction,
    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
    label = "session_timer_progress"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(CharcoalBackground)
      .padding(horizontal = 20.dp)
      .testTag("focus_session_screen"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(12.dp))

      // Top title
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
      ) {
        Text(
          text = "Focus Session",
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Light,
          color = OffWhitePrimary
        )
        Text(
          text = "Protected headspace without pressure",
          style = MaterialTheme.typography.bodySmall,
          color = OffWhiteMuted
        )
      }
    }

    // Context label & mode selector
    item {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
      ) {
        Text(
          text = "CONTEXT MODE",
          style = MaterialTheme.typography.labelSmall,
          color = OffWhiteMuted,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          uiState.availableModes.take(3).forEach { mode ->
            val isSelected = uiState.selectedMode.id == mode.id
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isSelected) Color(0xFF22252E) else CharcoalSurface)
                .border(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) OffWhitePrimary else CharcoalBorder,
                  shape = RoundedCornerShape(14.dp)
                )
                .clickable { onSelectMode(mode) }
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .testTag("mode_chip_${mode.id}"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = mode.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else OffWhiteMuted,
                maxLines = 1
              )
            }
          }
        }
      }
    }

    // Timer Duration & Reminder Interval Selector
    if (!uiState.isSessionActive) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CharcoalSurface)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
            .testTag("timer_duration_reminder_box")
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Timer,
                  contentDescription = null,
                  tint = OffWhitePrimary,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "DURATION & REMINDER",
                  style = MaterialTheme.typography.labelSmall,
                  color = OffWhiteMuted,
                  letterSpacing = 1.sp
                )
              }
              Text(
                text = "${uiState.sessionTotalSeconds / 60}m timer",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = OffWhitePrimary
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              listOf(15, 25, 45, 60).forEach { presetMins ->
                val isSelected = (uiState.sessionTotalSeconds / 60) == presetMins
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFF22252E) else CharcoalBackground)
                    .border(
                      width = if (isSelected) 1.5.dp else 1.dp,
                      color = if (isSelected) OffWhitePrimary else CharcoalBorder,
                      shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSetDuration(presetMins) }
                    .padding(vertical = 10.dp)
                    .testTag("preset_duration_${presetMins}m"),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "${presetMins}m",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else OffWhiteMuted
                  )
                }
              }
            }
          }
        }
      }
    }

    // Active context banner with "Exam Mode: tighter limits" label
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder, RoundedCornerShape(18.dp))
          .padding(16.dp)
          .testTag("context_label_card")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(CharcoalBackground),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = null,
              tint = OffWhitePrimary,
              modifier = Modifier.size(18.dp)
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = uiState.selectedMode.tag,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = OffWhitePrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = uiState.selectedMode.subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = OffWhiteMuted
            )
          }
        }
      }
    }

    // Circular Timer Display
    item {
      Box(
        modifier = Modifier
          .size(260.dp)
          .testTag("session_timer_display"),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.size(240.dp)) {
          val strokeWidth = 12.dp.toPx()
          val diameter = size.minDimension - strokeWidth
          val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
          val arcSize = Size(diameter, diameter)

          // Background track
          drawArc(
            color = CharcoalSurface,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
          )

          // Active timer arc
          val sweep = 360f * animatedProgress
          if (sweep > 0f) {
            drawArc(
              color = OffWhitePrimary,
              startAngle = -90f,
              sweepAngle = sweep,
              useCenter = false,
              topLeft = topLeft,
              size = arcSize,
              style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
          }
        }

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = timeFormatted,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Light,
            letterSpacing = (-1.0).sp,
            color = OffWhitePrimary
          )

          Spacer(modifier = Modifier.height(4.dp))

          val statusLabel = when {
            !uiState.isSessionActive -> "READY"
            uiState.isSessionPaused -> "PAUSED"
            else -> "IN FLOW"
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(CharcoalSurface)
              .padding(horizontal = 12.dp, vertical = 4.dp)
          ) {
            Text(
              text = statusLabel,
              style = MaterialTheme.typography.labelSmall,
              color = OffWhitePrimary,
              letterSpacing = 1.sp
            )
          }
        }
      }
    }

    // Encouraging Ambient Microcopy
    item {
      val encouragementText = when {
        !uiState.isSessionActive -> "Press start whenever you are ready. No countdown pressure."
        uiState.isSessionPaused -> "Paused gently. Take a breath and resume when ready."
        else -> "In the flow — distracting apps are softly held at the door."
      }

      Text(
        text = encouragementText,
        style = MaterialTheme.typography.bodyMedium,
        color = OffWhiteMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
    }

    // Pause / Resume & End Controls
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!uiState.isSessionActive) {
          Button(
            onClick = onStartSession,
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("session_start_btn"),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF22252E),
              contentColor = Color.White
            ),
            border = BorderStroke(1.5.dp, OffWhitePrimary),
            shape = RoundedCornerShape(16.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.White
              )
              Text(
                text = "Start Timer • ${uiState.selectedMode.title}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
              )
            }
          }
        } else {
          // Pause / Resume Button
          Button(
            onClick = {
              if (uiState.isSessionPaused) onResumeSession() else onPauseSession()
            },
            modifier = Modifier
              .weight(1f)
              .height(54.dp)
              .testTag("session_pause_resume_btn"),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF22252E),
              contentColor = Color.White
            ),
            border = BorderStroke(1.5.dp, OffWhitePrimary),
            shape = RoundedCornerShape(14.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = if (uiState.isSessionPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
              )
              Text(
                text = if (uiState.isSessionPaused) "Resume" else "Pause",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
              )
            }
          }

          // End Session Button
          OutlinedButton(
            onClick = onEndSession,
            modifier = Modifier
              .weight(1f)
              .height(54.dp)
              .testTag("session_end_btn"),
            border = BorderStroke(1.5.dp, CharcoalBorder),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = CharcoalSurface,
              contentColor = OffWhitePrimary
            )
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color(0xFFEF5350)
              )
              Text(
                text = "Stop Session",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = OffWhitePrimary
              )
            }
          }
        }
      }
    }

    // Ambient sound bar
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.GraphicEq,
              contentDescription = null,
              tint = OffWhiteMuted,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "Ambient soundscape",
              style = MaterialTheme.typography.bodyMedium,
              color = OffWhitePrimary
            )
          }

          Text(
            text = uiState.ambientSound,
            style = MaterialTheme.typography.labelMedium,
            color = OffWhiteMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}
