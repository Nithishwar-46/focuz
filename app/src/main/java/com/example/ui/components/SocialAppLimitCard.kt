package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SocialAppLimit
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary

@Composable
fun SocialAppLimitCard(
  appLimit: SocialAppLimit,
  onLimitChanged: (Int) -> Unit,
  onToggleEnabled: (Boolean) -> Unit,
  onTestBlock: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }

  val presetLimits = listOf(5, 15, 30, 45, 60)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(CharcoalSurface)
      .border(
        width = 1.dp,
        color = if (appLimit.isLimitReached) Color(0xFFE57373) else CharcoalBorder,
        shape = RoundedCornerShape(16.dp)
      )
      .clickable { isExpanded = !isExpanded }
      .padding(16.dp)
      .testTag("app_limit_card_${appLimit.appName.lowercase().replace(" ", "_")}")
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Header: Icon, Name, Category, Switch
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // App Icon / Glyph
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(CharcoalBackground)
              .border(1.dp, CharcoalBorder, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = appLimit.iconGlyph,
              fontSize = 20.sp
            )
          }

          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = appLimit.appName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = OffWhitePrimary
              )
              if (appLimit.isLimitReached) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF3E1E22))
                    .border(1.dp, Color(0xFFE57373).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(
                      Icons.Default.Lock,
                      contentDescription = null,
                      tint = Color(0xFFE57373),
                      modifier = Modifier.size(10.dp)
                    )
                    Text(
                      text = "LOCKED",
                      color = Color(0xFFE57373),
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }

            Text(
              text = if (appLimit.isEnabled) {
                "Limit: ${appLimit.limitMinutes} mins/day"
              } else {
                "Tracking paused"
              },
              style = MaterialTheme.typography.bodySmall,
              color = OffWhiteMuted
            )
          }
        }

        // Toggle Switch
        Switch(
          checked = appLimit.isEnabled,
          onCheckedChange = onToggleEnabled,
          colors = SwitchDefaults.colors(
            checkedThumbColor = CharcoalBackground,
            checkedTrackColor = OffWhitePrimary,
            uncheckedThumbColor = OffWhiteMuted,
            uncheckedTrackColor = CharcoalBackground,
            uncheckedBorderColor = CharcoalBorder
          ),
          modifier = Modifier.testTag("switch_${appLimit.appName.lowercase()}")
        )
      }

      if (appLimit.isEnabled) {
        Spacer(modifier = Modifier.height(14.dp))

        // Progress Bar
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "${appLimit.usedTodayMinutes}m used today",
              fontSize = 12.sp,
              color = if (appLimit.isLimitReached) Color(0xFFE57373) else OffWhiteMuted
            )
            Text(
              text = if (appLimit.isLimitReached) "Limit reached" else "${appLimit.remainingMinutes}m remaining",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = if (appLimit.isLimitReached) Color(0xFFE57373) else OffWhitePrimary
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          LinearProgressIndicator(
            progress = { appLimit.usageRatio },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = if (appLimit.isLimitReached) Color(0xFFE57373) else OffWhitePrimary,
            trackColor = CharcoalBackground,
          )
        }
      }

      // Expanded controls for quick limit adjustments
      AnimatedVisibility(visible = isExpanded && appLimit.isEnabled) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(CharcoalBorder)
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "SET DAILY LIMIT",
            style = MaterialTheme.typography.labelSmall,
            color = OffWhiteMuted,
            letterSpacing = 1.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Stepper + Preset Chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              IconButton(
                onClick = { if (appLimit.limitMinutes > 5) onLimitChanged(appLimit.limitMinutes - 5) },
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(CharcoalBackground)
                  .border(1.dp, CharcoalBorder, CircleShape)
              ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease limit", tint = OffWhitePrimary, modifier = Modifier.size(16.dp))
              }

              Text(
                text = "${appLimit.limitMinutes}m",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OffWhitePrimary,
                modifier = Modifier.padding(horizontal = 4.dp)
              )

              IconButton(
                onClick = { onLimitChanged(appLimit.limitMinutes + 5) },
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(CharcoalBackground)
                  .border(1.dp, CharcoalBorder, CircleShape)
              ) {
                Icon(Icons.Default.Add, contentDescription = "Increase limit", tint = OffWhitePrimary, modifier = Modifier.size(16.dp))
              }
            }

            // Quick Preset Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              presetLimits.take(3).forEach { mins ->
                val isSelected = appLimit.limitMinutes == mins
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Color(0xFF22252E) else CharcoalBackground)
                    .border(
                      width = if (isSelected) 1.5.dp else 1.dp,
                      color = if (isSelected) OffWhitePrimary else CharcoalBorder,
                      shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onLimitChanged(mins) }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                  Text(
                    text = "${mins}m",
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else OffWhiteMuted
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Test Blocker Action Button
          OutlinedButton(
            onClick = onTestBlock,
            modifier = Modifier
              .fillMaxWidth()
              .height(40.dp)
              .testTag("btn_test_block_${appLimit.appName.lowercase()}"),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = OffWhitePrimary),
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
            shape = RoundedCornerShape(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp), tint = OffWhitePrimary)
              Text(
                text = "Simulate & Preview Lockout Screen",
                fontSize = 12.sp,
                color = OffWhitePrimary
              )
            }
          }
        }
      }
    }
  }
}
