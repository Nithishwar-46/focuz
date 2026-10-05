package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary
import com.example.ui.theme.OffWhiteSubtle
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Phases of a Pomodoro cycle.
 */
enum class PomodoroPhase(
  val title: String,
  val badgeLabel: String,
  val defaultMinutes: Int,
  val calmTip: String
) {
  WORK(
    title = "Deep Focus",
    badgeLabel = "WORK FOCUS",
    defaultMinutes = 25,
    calmTip = "Single-task with full presence. The outside world is safely paused."
  ),
  SHORT_BREAK(
    title = "Short Rest",
    badgeLabel = "SHORT BREAK",
    defaultMinutes = 5,
    calmTip = "Unclench your jaw, soften your shoulders, and rest your eyes 20ft away."
  ),
  LONG_BREAK(
    title = "Restoration",
    badgeLabel = "LONG REST",
    defaultMinutes = 15,
    calmTip = "Four cycles complete. Step away from screens, hydrate, and stretch freely."
  )
}

/**
 * A Pomodoro-style focus timer component that allows users to set a work duration
 * and displays a calm, serene countdown interface.
 *
 * Can run self-contained or notify external callers on session completion.
 */
@Composable
fun PomodoroFocusTimer(
  modifier: Modifier = Modifier,
  initialWorkDurationMinutes: Int = 25,
  initialBreakDurationMinutes: Int = 5,
  initialLongBreakDurationMinutes: Int = 15,
  onWorkCompleted: (durationMinutes: Int) -> Unit = {},
  onBreakCompleted: (phase: PomodoroPhase) -> Unit = {},
  onTick: (phase: PomodoroPhase, remainingSeconds: Int, totalSeconds: Int) -> Unit = { _, _, _ -> }
) {
  var workMinutes by remember { mutableIntStateOf(initialWorkDurationMinutes.coerceIn(5, 90)) }
  var shortBreakMinutes by remember { mutableIntStateOf(initialBreakDurationMinutes.coerceIn(1, 30)) }
  var longBreakMinutes by remember { mutableIntStateOf(initialLongBreakDurationMinutes.coerceIn(5, 45)) }

  var currentPhase by remember { mutableStateOf(PomodoroPhase.WORK) }
  var completedPomodorosInCycle by remember { mutableIntStateOf(0) }
  val totalPomodorosPerCycle = 4

  var isRunning by remember { mutableStateOf(false) }
  var isPaused by remember { mutableStateOf(false) }

  // Total seconds for current phase
  val currentPhaseTotalSeconds = remember(currentPhase, workMinutes, shortBreakMinutes, longBreakMinutes) {
    when (currentPhase) {
      PomodoroPhase.WORK -> workMinutes * 60
      PomodoroPhase.SHORT_BREAK -> shortBreakMinutes * 60
      PomodoroPhase.LONG_BREAK -> longBreakMinutes * 60
    }
  }

  var remainingSeconds by remember { mutableIntStateOf(currentPhaseTotalSeconds) }

  // Sync remainingSeconds if user adjusts work duration while timer is not running
  LaunchedEffect(currentPhase, workMinutes, shortBreakMinutes, longBreakMinutes) {
    if (!isRunning && !isPaused) {
      remainingSeconds = currentPhaseTotalSeconds
    }
  }

  // Active Timer Tick Coroutine
  LaunchedEffect(isRunning, isPaused, remainingSeconds) {
    if (isRunning && !isPaused && remainingSeconds > 0) {
      delay(1000L)
      val next = remainingSeconds - 1
      remainingSeconds = next
      onTick(currentPhase, next, currentPhaseTotalSeconds)

      if (next <= 0) {
        // Phase completed!
        isRunning = false
        isPaused = false

        if (currentPhase == PomodoroPhase.WORK) {
          val nextCount = completedPomodorosInCycle + 1
          completedPomodorosInCycle = nextCount
          onWorkCompleted(workMinutes)

          // If reached 4 cycles -> Long break, otherwise short break
          currentPhase = if (nextCount >= totalPomodorosPerCycle) {
            completedPomodorosInCycle = 0
            PomodoroPhase.LONG_BREAK
          } else {
            PomodoroPhase.SHORT_BREAK
          }
          remainingSeconds = when (currentPhase) {
            PomodoroPhase.SHORT_BREAK -> shortBreakMinutes * 60
            PomodoroPhase.LONG_BREAK -> longBreakMinutes * 60
            else -> workMinutes * 60
          }
        } else {
          // Break finished -> back to WORK
          onBreakCompleted(currentPhase)
          currentPhase = PomodoroPhase.WORK
          remainingSeconds = workMinutes * 60
        }
      }
    }
  }

  // Breathing pulse animation for active calm state
  val infiniteTransition = rememberInfiniteTransition(label = "pomodoro_breathing")
  val breathScale by infiniteTransition.animateFloat(
    initialValue = 0.98f,
    targetValue = 1.02f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "breath_scale"
  )

  val activePulseScale = if (isRunning && !isPaused) breathScale else 1f

  // Circular progress calculation
  val progressFraction = if (currentPhaseTotalSeconds > 0) {
    (remainingSeconds.toFloat() / currentPhaseTotalSeconds.toFloat()).coerceIn(0f, 1f)
  } else 0f

  val animatedProgress by animateFloatAsState(
    targetValue = progressFraction,
    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
    label = "pomodoro_arc_progress"
  )

  val mins = remainingSeconds / 60
  val secs = remainingSeconds % 60
  val formattedTime = String.format(Locale.US, "%02d:%02d", mins, secs)

  // Color accents based on phase
  val phaseAccentColor by animateColorAsState(
    targetValue = when (currentPhase) {
      PomodoroPhase.WORK -> OffWhitePrimary
      PomodoroPhase.SHORT_BREAK -> Color(0xFF81C784) // Gentle Sage Green
      PomodoroPhase.LONG_BREAK -> Color(0xFF90CAF9)  // Calm Sky Blue
    },
    animationSpec = tween(500),
    label = "phase_color"
  )

  // Ambient sound selection
  var selectedSoundscape by remember { mutableStateOf("Silent Calm") }
  val soundscapes = listOf("Silent Calm", "Gentle Rain", "White Noise", "Forest Birds")

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp))
      .background(CharcoalSurface)
      .border(1.dp, CharcoalBorder, RoundedCornerShape(24.dp))
      .padding(20.dp)
      .testTag("pomodoro_focus_timer_card"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Header with Phase Selector / Cycle Dots
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
          tint = phaseAccentColor,
          modifier = Modifier.size(18.dp)
        )
        Text(
          text = "POMODORO FOCUS",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold,
          color = OffWhiteMuted,
          letterSpacing = 1.2.sp
        )
      }

      // Pomodoro 4-Cycle Dots (● ● ○ ○)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.testTag("pomodoro_cycle_dots")
      ) {
        Text(
          text = "Set: ",
          style = MaterialTheme.typography.labelSmall,
          color = OffWhiteMuted
        )
        for (i in 0 until totalPomodorosPerCycle) {
          val isFilled = i < completedPomodorosInCycle
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(if (isFilled) phaseAccentColor else CharcoalBorder)
          )
        }
      }
    }

    // 2. Phase Tab Badges (Work / Short Break / Long Break)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      PomodoroPhase.values().forEach { phase ->
        val isSelected = currentPhase == phase
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF22252E) else CharcoalBackground)
            .border(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) phaseAccentColor else CharcoalBorder,
              shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isRunning) {
              currentPhase = phase
              remainingSeconds = when (phase) {
                PomodoroPhase.WORK -> workMinutes * 60
                PomodoroPhase.SHORT_BREAK -> shortBreakMinutes * 60
                PomodoroPhase.LONG_BREAK -> longBreakMinutes * 60
              }
              isPaused = false
            }
            .padding(vertical = 8.dp)
            .testTag("pomodoro_phase_${phase.name.lowercase()}"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = phase.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else OffWhiteMuted,
            maxLines = 1
          )
        }
      }
    }

    // 3. Work Duration Setter (Active when not running, or can toggle)
    AnimatedVisibility(visible = !isRunning && currentPhase == PomodoroPhase.WORK) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(CharcoalBackground)
          .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
          .padding(14.dp)
          .testTag("pomodoro_duration_selector"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "WORK DURATION",
            style = MaterialTheme.typography.labelSmall,
            color = OffWhiteMuted,
            letterSpacing = 1.sp
          )

          // Stepper +/- controls
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            IconButton(
              onClick = {
                val next = (workMinutes - 5).coerceAtLeast(5)
                workMinutes = next
                remainingSeconds = next * 60
              },
              modifier = Modifier
                .size(32.dp)
                .testTag("pomodoro_duration_minus")
            ) {
              Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease duration by 5 minutes",
                tint = OffWhitePrimary,
                modifier = Modifier.size(16.dp)
              )
            }

            Text(
              text = "${workMinutes} min",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = OffWhitePrimary,
              modifier = Modifier.padding(horizontal = 4.dp)
            )

            IconButton(
              onClick = {
                val next = (workMinutes + 5).coerceAtMost(90)
                workMinutes = next
                remainingSeconds = next * 60
              },
              modifier = Modifier
                .size(32.dp)
                .testTag("pomodoro_duration_plus")
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase duration by 5 minutes",
                tint = OffWhitePrimary,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        // Quick Preset Duration Chips (15m, 20m, 25m, 30m, 45m, 50m)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(15, 20, 25, 30, 45, 50).forEach { minsPreset ->
            val isPresetSelected = workMinutes == minsPreset
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isPresetSelected) Color(0xFF2B303C) else CharcoalSurface)
                .border(
                  width = if (isPresetSelected) 1.5.dp else 1.dp,
                  color = if (isPresetSelected) OffWhitePrimary else CharcoalBorder,
                  shape = RoundedCornerShape(8.dp)
                )
                .clickable {
                  workMinutes = minsPreset
                  remainingSeconds = minsPreset * 60
                }
                .padding(vertical = 6.dp)
                .testTag("pomodoro_preset_${minsPreset}m"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${minsPreset}m",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isPresetSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isPresetSelected) Color.White else OffWhiteMuted
              )
            }
          }
        }

        // Fine slider for granular adjustment
        Slider(
          value = workMinutes.toFloat(),
          onValueChange = { newVal ->
            val minsInt = newVal.toInt()
            workMinutes = minsInt
            remainingSeconds = minsInt * 60
          },
          valueRange = 5f..90f,
          steps = 16,
          colors = SliderDefaults.colors(
            thumbColor = OffWhitePrimary,
            activeTrackColor = OffWhitePrimary,
            inactiveTrackColor = CharcoalBorder
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("pomodoro_duration_slider")
        )
      }
    }

    // 4. Calm Countdown Interface (Circular Ambient Ring + Countdown Typography)
    Box(
      modifier = Modifier
        .size(240.dp)
        .scale(activePulseScale)
        .testTag("pomodoro_countdown_display"),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.size(220.dp)) {
        val strokeWidth = 10.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        val arcSize = Size(diameter, diameter)

        // Calm subtle background track
        drawArc(
          color = CharcoalBorder.copy(alpha = 0.6f),
          startAngle = -90f,
          sweepAngle = 360f,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Animated active sweep arc
        val sweepAngle = 360f * animatedProgress
        if (sweepAngle > 0f) {
          drawArc(
            color = phaseAccentColor,
            startAngle = -90f,
            sweepAngle = sweepAngle,
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
        // Phase status pill
        val statusText = when {
          !isRunning && !isPaused -> "READY"
          isPaused -> "PAUSED"
          currentPhase == PomodoroPhase.WORK -> "IN FLOW"
          else -> "RESTING"
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CharcoalBackground)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
            .testTag("pomodoro_status_badge")
        ) {
          Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = phaseAccentColor,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Large calm time display
        Text(
          text = formattedTime,
          style = MaterialTheme.typography.displayMedium.copy(fontSize = 50.sp),
          fontWeight = FontWeight.Light,
          letterSpacing = (-1.0).sp,
          color = OffWhitePrimary,
          modifier = Modifier.testTag("pomodoro_formatted_time")
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = currentPhase.title,
          style = MaterialTheme.typography.bodySmall,
          color = OffWhiteMuted
        )
      }
    }

    // 5. Calm Microcopy / Mindful Tip
    Text(
      text = if (isPaused) "Paused peacefully. Take a breath and resume when centered." else currentPhase.calmTip,
      style = MaterialTheme.typography.bodySmall,
      color = OffWhiteMuted,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp)
    )

    // 6. Action Control Buttons (Start, Pause, Resume, Reset, Skip)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (!isRunning && !isPaused) {
        // Start Button
        Button(
          onClick = {
            isRunning = true
            isPaused = false
          },
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .testTag("pomodoro_start_btn"),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF22252E),
            contentColor = Color.White
          ),
          border = BorderStroke(1.5.dp, phaseAccentColor),
          shape = RoundedCornerShape(14.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Start Pomodoro session",
              tint = phaseAccentColor,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "Start ${currentPhase.title}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = Color.White
            )
          }
        }
      } else {
        // Pause / Resume Button
        Button(
          onClick = {
            if (isPaused) {
              isPaused = false
              isRunning = true
            } else {
              isPaused = true
            }
          },
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .testTag("pomodoro_pause_resume_btn"),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF22252E),
            contentColor = Color.White
          ),
          border = BorderStroke(1.5.dp, phaseAccentColor),
          shape = RoundedCornerShape(14.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
              contentDescription = if (isPaused) "Resume timer" else "Pause timer",
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = if (isPaused) "Resume" else "Pause",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              color = Color.White
            )
          }
        }

        // Reset Button
        OutlinedButton(
          onClick = {
            isRunning = false
            isPaused = false
            remainingSeconds = currentPhaseTotalSeconds
          },
          modifier = Modifier
            .size(52.dp)
            .testTag("pomodoro_reset_btn"),
          border = BorderStroke(1.dp, CharcoalBorder),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CharcoalBackground,
            contentColor = OffWhitePrimary
          ),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Reset timer",
            tint = OffWhiteMuted,
            modifier = Modifier.size(20.dp)
          )
        }

        // Skip to next phase Button
        OutlinedButton(
          onClick = {
            isRunning = false
            isPaused = false
            if (currentPhase == PomodoroPhase.WORK) {
              val nextCount = completedPomodorosInCycle + 1
              completedPomodorosInCycle = nextCount
              currentPhase = if (nextCount >= totalPomodorosPerCycle) {
                completedPomodorosInCycle = 0
                PomodoroPhase.LONG_BREAK
              } else {
                PomodoroPhase.SHORT_BREAK
              }
            } else {
              currentPhase = PomodoroPhase.WORK
            }
            remainingSeconds = when (currentPhase) {
              PomodoroPhase.WORK -> workMinutes * 60
              PomodoroPhase.SHORT_BREAK -> shortBreakMinutes * 60
              PomodoroPhase.LONG_BREAK -> longBreakMinutes * 60
            }
          },
          modifier = Modifier
            .size(52.dp)
            .testTag("pomodoro_skip_btn"),
          border = BorderStroke(1.dp, CharcoalBorder),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CharcoalBackground,
            contentColor = OffWhitePrimary
          ),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
          Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = "Skip to next phase",
            tint = OffWhiteMuted,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    // 7. Ambient Soundscape Selector
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(CharcoalBackground)
        .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.GraphicEq,
          contentDescription = null,
          tint = OffWhiteMuted,
          modifier = Modifier.size(16.dp)
        )
        Text(
          text = "Soundscape",
          style = MaterialTheme.typography.bodySmall,
          color = OffWhiteMuted
        )
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        soundscapes.take(3).forEach { sound ->
          val isSelected = selectedSoundscape == sound
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (isSelected) Color(0xFF22252E) else Color.Transparent)
              .border(
                width = 1.dp,
                color = if (isSelected) OffWhitePrimary.copy(alpha = 0.6f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
              )
              .clickable { selectedSoundscape = sound }
              .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = sound.replace(" ", "\u00A0"),
              style = MaterialTheme.typography.labelSmall,
              color = if (isSelected) Color.White else OffWhiteMuted,
              fontSize = 11.sp
            )
          }
        }
      }
    }
  }
}
