package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FocuzUiState
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary

@Composable
fun DailyTimerHistoryModal(
  uiState: FocuzUiState,
  onDismiss: () -> Unit,
  onForceResetTest: () -> Unit
) {
  var historyTab by remember { mutableIntStateOf(0) } // 0 = Weekly Trends Chart, 1 = Past Days List
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.85f)
        .clip(RoundedCornerShape(24.dp))
        .background(CharcoalSurface)
        .border(1.dp, CharcoalBorder, RoundedCornerShape(24.dp))
        .padding(20.dp)
        .testTag("daily_timer_history_modal")
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Modal Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CharcoalBackground)
                .border(1.dp, CharcoalBorder, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = OffWhitePrimary,
                modifier = Modifier.size(20.dp)
              )
            }

            Column {
              Text(
                text = "Timer Records & Reset",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Light,
                color = OffWhitePrimary
              )
              Text(
                text = "Automated 12:00 AM daily rollover",
                style = MaterialTheme.typography.bodySmall,
                color = OffWhiteMuted
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(36.dp)
              .testTag("btn_close_history_modal")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = OffWhiteMuted
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset rule callout card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E2129))
            .border(1.dp, Color(0xFF2E3342), RoundedCornerShape(16.dp))
            .padding(14.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AccessTime,
              contentDescription = null,
              tint = Color(0xFF81C784),
              modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Daily 12:00 AM Midnight Reset Active",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
              )
              Text(
                text = "Next reset in ${uiState.timeUntilMidnightReset}. At 12:00 AM, today's session & social app timers cleanly reset to 0m and archive to history below.",
                style = MaterialTheme.typography.bodySmall,
                color = OffWhiteMuted
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "TODAY'S LIVE METRICS (${uiState.lastMidnightResetDate})",
          style = MaterialTheme.typography.labelSmall,
          color = OffWhiteMuted,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Current Live Summary
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CharcoalBackground)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "${uiState.intentionalMinutes}m", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OffWhitePrimary)
              Text(text = "Focus", style = MaterialTheme.typography.labelSmall, color = OffWhiteMuted)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "${uiState.scrollMinutes}m", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OffWhitePrimary)
              Text(text = "Scroll", style = MaterialTheme.typography.labelSmall, color = OffWhiteMuted)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "${uiState.interceptedOpens}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OffWhitePrimary)
              Text(text = "Paused", style = MaterialTheme.typography.labelSmall, color = OffWhiteMuted)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "${uiState.mindfulPausesTaken}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OffWhitePrimary)
              Text(text = "Pauses", style = MaterialTheme.typography.labelSmall, color = OffWhiteMuted)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalBackground)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
            .padding(3.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          listOf("Weekly Trends Chart", "Past Days Records").forEachIndexed { index, title ->
            val isSelected = historyTab == index
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) Color(0xFF22252E) else Color.Transparent)
                .border(
                  width = if (isSelected) 1.dp else 0.dp,
                  color = if (isSelected) OffWhitePrimary.copy(alpha = 0.4f) else Color.Transparent,
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable { historyTab = index }
                .padding(vertical = 8.dp)
                .testTag(if (index == 0) "modal_tab_trends_chart" else "modal_tab_past_records"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else OffWhiteMuted
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (historyTab == 0) {
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
          ) {
            WeeklyFocusTrendsChart(
              todayFocusMinutes = uiState.intentionalMinutes,
              todayScrollMinutes = uiState.scrollMinutes,
              dailyGoalMinutes = uiState.userProfile.focusGoalMins,
              archivedRecords = uiState.archivedTimerRecords
            )
          }
        } else {
        Text(
          text = "ARCHIVED PAST DAYS (ROOM DATABASE)",
          style = MaterialTheme.typography.labelSmall,
          color = OffWhiteMuted,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Archived List
        if (uiState.archivedTimerRecords.isEmpty()) {
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(CharcoalBackground)
              .padding(20.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "First day in progress",
                style = MaterialTheme.typography.titleSmall,
                color = OffWhitePrimary
              )
              Text(
                text = "When 12:00 AM strikes tonight, your first full day of focus will be automatically archived here.",
                style = MaterialTheme.typography.bodySmall,
                color = OffWhiteMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(uiState.archivedTimerRecords) { record ->
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(CharcoalBackground)
                  .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
                  .padding(12.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      Text(
                        text = record.date,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OffWhitePrimary
                      )
                      if (record.goalAchieved) {
                        Box(
                          modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E3A24))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                          ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(10.dp))
                            Text("Goal Met", color = Color(0xFF81C784), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                          }
                        }
                      }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                      text = "${record.intentionalFocusMinutes}m focused • ${record.scrollMinutes}m scroll • ${record.interceptedOpens} paused",
                      style = MaterialTheme.typography.bodySmall,
                      color = OffWhiteMuted
                    )
                  }

                  Text(
                    text = "${record.mindfulPausesTaken} pauses",
                    style = MaterialTheme.typography.labelSmall,
                    color = OffWhiteMuted
                  )
                }
              }
            }
          }
        }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onForceResetTest,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("btn_test_midnight_reset"),
            border = BorderStroke(1.dp, CharcoalBorder),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = CharcoalBackground,
              contentColor = OffWhitePrimary
            )
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
              Text("Test 12 AM Reset", style = MaterialTheme.typography.labelMedium)
            }
          }

          Button(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("btn_done_history_modal"),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF22252E),
              contentColor = Color.White
            ),
            border = BorderStroke(1.5.dp, OffWhitePrimary),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Done", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }
}
