package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocuzUiState
import com.example.ui.components.PermittedTimeBlockWidget
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary

/**
 * Permitted Scroll Time Screen.
 * Scheduled "free time" window shown as a calm, clearly-bounded time block,
 * never a countdown of shame.
 */
@Composable
fun PermittedScrollScreen(
  uiState: FocuzUiState,
  onSelectDuration: (Int) -> Unit,
  onAddWindow: (title: String, timeRange: String, durationMins: Int) -> Unit = { _, _, _ -> },
  modifier: Modifier = Modifier
) {
  var autoPauseNudges by remember { mutableStateOf(true) }
  var gentleChimeOnClose by remember { mutableStateOf(true) }
  var showAddDialog by remember { mutableStateOf(false) }
  var newWindowTitle by remember { mutableStateOf("") }
  var newWindowTime by remember { mutableStateOf("") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(CharcoalBackground)
      .padding(horizontal = 20.dp)
      .testTag("permitted_scroll_screen"),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(12.dp))

      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Permitted Scroll Windows",
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Light,
          color = OffWhitePrimary
        )
        Text(
          text = "Scheduled free time — rest is part of deep work",
          style = MaterialTheme.typography.bodySmall,
          color = OffWhiteMuted
        )
      }
    }

    // Active Guilt-Free Window or None
    item {
      Text(
        text = "ACTIVE WINDOW",
        style = MaterialTheme.typography.labelSmall,
        color = OffWhiteMuted,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      if (uiState.permittedWindows.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CharcoalSurface)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
            .padding(24.dp)
            .testTag("empty_permitted_window_card"),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              tint = OffWhiteMuted,
              modifier = Modifier.size(36.dp)
            )
            Text(
              text = "None",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Medium,
              color = OffWhitePrimary
            )
            Text(
              text = "No active scroll windows scheduled. Tap below to create your guilt-free browsing block.",
              style = MaterialTheme.typography.bodySmall,
              color = OffWhiteMuted,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      } else {
        PermittedTimeBlockWidget(window = uiState.permittedWindows[0])
      }
    }

    // Philosophy Card (Why Permitted Scrolling Works)
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
          .padding(18.dp)
          .testTag("permitted_philosophy_card")
      ) {
        Row(
          verticalAlignment = Alignment.Top,
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(CharcoalBackground),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = OffWhitePrimary,
              modifier = Modifier.size(18.dp)
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Why bounded windows work",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = OffWhitePrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Strict deprivation turns social apps into 'forbidden fruit', leading to unconscious binges. By scheduling an intentional time window, you can scroll with complete peace of mind and zero shame.",
              style = MaterialTheme.typography.bodySmall,
              color = OffWhiteMuted,
              lineHeight = 20.sp
            )
          }
        }
      }
    }

    // Upcoming Window if more than 1
    if (uiState.permittedWindows.size > 1) {
      item {
        Text(
          text = "NEXT SCHEDULED WINDOW",
          style = MaterialTheme.typography.labelSmall,
          color = OffWhiteMuted,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        PermittedTimeBlockWidget(window = uiState.permittedWindows[1])
      }
    }

    // Duration Customizer
    item {
      Text(
        text = "WINDOW DURATION PREFERENCE",
        style = MaterialTheme.typography.labelSmall,
        color = OffWhiteMuted,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        listOf(15, 20, 30, 45).forEach { mins ->
          val isSelected = uiState.selectedWindowDuration == mins
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .background(if (isSelected) OffWhitePrimary else CharcoalSurface)
              .border(
                1.dp,
                if (isSelected) OffWhitePrimary else CharcoalBorder,
                RoundedCornerShape(14.dp)
              )
              .clickable { onSelectDuration(mins) }
              .padding(vertical = 12.dp)
              .testTag("duration_chip_${mins}m"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${mins}m",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
              color = if (isSelected) CharcoalBackground else OffWhitePrimary
            )
          }
        }
      }
    }

    // Quiet Settings (Non-punitive toggles)
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
          .padding(18.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          // Setting 1: Auto pause nudges
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Pause nudges automatically",
                style = MaterialTheme.typography.labelLarge,
                color = OffWhitePrimary
              )
              Text(
                text = "Zero interrupts while your window is open",
                style = MaterialTheme.typography.bodySmall,
                color = OffWhiteMuted
              )
            }
            Switch(
              checked = autoPauseNudges,
              onCheckedChange = { autoPauseNudges = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = CharcoalBackground,
                checkedTrackColor = OffWhitePrimary,
                uncheckedThumbColor = OffWhiteMuted,
                uncheckedTrackColor = CharcoalBackground
              )
            )
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(CharcoalBorder)
          )

          // Setting 2: Gentle chime on close
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Gentle chime on completion",
                style = MaterialTheme.typography.labelLarge,
                color = OffWhitePrimary
              )
              Text(
                text = "Soft sound instead of abrupt blocking",
                style = MaterialTheme.typography.bodySmall,
                color = OffWhiteMuted
              )
            }
            Switch(
              checked = gentleChimeOnClose,
              onCheckedChange = { gentleChimeOnClose = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = CharcoalBackground,
                checkedTrackColor = OffWhitePrimary,
                uncheckedThumbColor = OffWhiteMuted,
                uncheckedTrackColor = CharcoalBackground
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}
