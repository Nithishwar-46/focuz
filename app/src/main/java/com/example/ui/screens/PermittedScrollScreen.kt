package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.delay
import com.example.data.FocuzUiState
import com.example.data.SocialAppBlockerManager
import com.example.ui.components.PermittedTimeBlockWidget
import com.example.ui.components.SocialAppLimitCard
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary

@Composable
fun PermittedScrollScreen(
  uiState: FocuzUiState,
  onSelectDuration: (Int) -> Unit,
  onUpdateAppLimit: (packageName: String, limitMins: Int) -> Unit = { _, _ -> },
  onToggleAppEnabled: (packageName: String, isEnabled: Boolean) -> Unit = { _, _ -> },
  onTestBlock: (packageName: String) -> Unit = {},
  onRefreshPermissions: () -> Unit = {},
  onDismissAccessibilityBanner: () -> Unit = {},
  onAddWindow: (title: String, timeRange: String, durationMins: Int) -> Unit = { _, _, _ -> },
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedSubTab by remember { mutableIntStateOf(0) } // 0: App Limits & Blocker, 1: Permitted Windows
  var autoPauseNudges by remember { mutableStateOf(true) }
  var gentleChimeOnClose by remember { mutableStateOf(true) }

  // Check and refresh permissions whenever this screen is displayed or resumed
  LifecycleResumeEffect(Unit) {
    onRefreshPermissions()
    onPauseOrDispose { }
  }

  // Actively poll every 1.5s if not yet granted, so it disappears immediately upon user return
  LaunchedEffect(uiState.isAccessibilityEnabled) {
    if (!uiState.isAccessibilityEnabled) {
      while (!uiState.isAccessibilityEnabled) {
        delay(1500)
        onRefreshPermissions()
      }
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(CharcoalBackground)
      .padding(horizontal = 20.dp)
      .testTag("permitted_scroll_screen"),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(12.dp))

      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Limits & Windows",
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Light,
          color = OffWhitePrimary
        )
        Text(
          text = "Block social apps upon reaching limits or schedule guilt-free time",
          style = MaterialTheme.typography.bodySmall,
          color = OffWhiteMuted
        )
      }
    }

    // Top Sub-Tabs: [ App Limits & Blocker ] | [ Permitted Windows ]
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder, RoundedCornerShape(14.dp))
          .padding(4.dp)
      ) {
        // Tab 0: App Limits
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selectedSubTab == 0) OffWhitePrimary else CharcoalSurface)
            .clickable { selectedSubTab = 0 }
            .padding(vertical = 10.dp)
            .testTag("subtab_app_limits"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "App Limits & Blocker",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal,
            color = if (selectedSubTab == 0) CharcoalBackground else OffWhiteMuted
          )
        }

        // Tab 1: Permitted Windows
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selectedSubTab == 1) OffWhitePrimary else CharcoalSurface)
            .clickable { selectedSubTab = 1 }
            .padding(vertical = 10.dp)
            .testTag("subtab_permitted_windows"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Guilt-Free Windows",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal,
            color = if (selectedSubTab == 1) CharcoalBackground else OffWhiteMuted
          )
        }
      }
    }

    // SECTION 0: APP LIMITS & BLOCKER
    if (selectedSubTab == 0) {
      // 1. Accessibility Service Banner (Only shown if permission is NOT yet granted; disappears once granted)
      if (!uiState.isAccessibilityEnabled) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(18.dp))
              .background(Color(0xFF261D1A))
              .border(1.dp, Color(0xFFFFB74D).copy(alpha = 0.6f), RoundedCornerShape(18.dp))
              .padding(18.dp)
              .testTag("accessibility_banner")
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF422718)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(20.dp)
                  )
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Enable Accessibility Blocker",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = OffWhitePrimary
                  )
                  Text(
                    text = "Android requires turning on the Focuz service to detect Instagram and kick you back to Home.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OffWhiteMuted
                  )
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Button(
                  onClick = {
                    try {
                      context.startActivity(SocialAppBlockerManager.openAccessibilitySettingsIntent())
                    } catch (e: Exception) {
                      // Fallback
                    }
                  },
                  modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_enable_accessibility"),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB74D),
                    contentColor = Color(0xFF1B1B1B)
                  ),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(
                      text = "Open Settings",
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 13.sp
                    )
                  }
                }

                OutlinedButton(
                  onClick = {
                    onRefreshPermissions()
                  },
                  modifier = Modifier
                    .height(44.dp)
                    .testTag("btn_check_accessibility"),
                  border = BorderStroke(1.dp, CharcoalBorder),
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = OffWhitePrimary)
                ) {
                  Text(
                    text = "Check Status",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                  )
                }

                OutlinedButton(
                  onClick = {
                    onDismissAccessibilityBanner()
                  },
                  modifier = Modifier
                    .height(44.dp)
                    .testTag("btn_dismiss_accessibility"),
                  border = BorderStroke(1.dp, CharcoalBorder),
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = OffWhiteMuted)
                ) {
                  Text(
                    text = "Dismiss",
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp
                  )
                }
              }
            }
          }
        }
      }

      // 2. Usage Stats Status Banner (Optional / Recommended for accurate sync)
      item {
        if (!uiState.hasUsageStatsPermission) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(CharcoalSurface)
              .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
              .padding(14.dp)
              .testTag("usage_access_banner")
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Precise Screen Time Sync",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = OffWhitePrimary
                )
                Text(
                  text = "Allow usage access to sync exact daily minutes from Android OS.",
                  style = MaterialTheme.typography.bodySmall,
                  color = OffWhiteMuted,
                  fontSize = 11.sp
                )
              }

              Spacer(modifier = Modifier.width(8.dp))

              OutlinedButton(
                onClick = {
                  try {
                    context.startActivity(SocialAppBlockerManager.openUsageAccessSettingsIntent())
                  } catch (e: Exception) {
                    // ignore
                  }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OffWhitePrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_enable_usage_access")
              ) {
                Text("Grant", fontSize = 12.sp)
              }
            }
          }
        }
      }

      // 3. Section Title
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "TARGET SOCIAL APPS",
            style = MaterialTheme.typography.labelSmall,
            color = OffWhiteMuted,
            letterSpacing = 1.sp
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (uiState.isAccessibilityEnabled) Color(0xFF81C784) else Color(0xFFFFB74D))
            )
            Text(
              text = if (uiState.isAccessibilityEnabled)
                "${uiState.monitoredSocialApps.count { it.isEnabled }} active • Blocker On"
              else
                "${uiState.monitoredSocialApps.count { it.isEnabled }} active",
              style = MaterialTheme.typography.labelSmall,
              color = if (uiState.isAccessibilityEnabled) Color(0xFF81C784) else OffWhiteMuted
            )
          }
        }
      }

      // 4. List of Monitored Social Media Apps
      items(uiState.monitoredSocialApps, key = { it.packageName }) { appLimit ->
        SocialAppLimitCard(
          appLimit = appLimit,
          onLimitChanged = { newMins ->
            onUpdateAppLimit(appLimit.packageName, newMins)
          },
          onToggleEnabled = { isEnabled ->
            onToggleAppEnabled(appLimit.packageName, isEnabled)
          },
          onTestBlock = {
            onTestBlock(appLimit.packageName)
          }
        )
      }

      // 5. How It Works Explainer Card
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CharcoalSurface)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(18.dp))
            .padding(18.dp)
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
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = OffWhitePrimary,
                modifier = Modifier.size(18.dp)
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "How Focuz closes Instagram",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = OffWhitePrimary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "When you open Instagram and your daily limit has been exceeded, Focuz's background accessibility engine instantly fires the system Home command to minimize Instagram, followed by displaying your mindful pause screen.",
                style = MaterialTheme.typography.bodySmall,
                color = OffWhiteMuted,
                lineHeight = 18.sp
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }

    // SECTION 1: PERMITTED SCROLL WINDOWS
    if (selectedSubTab == 1) {
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
                text = "No active scroll windows scheduled. Rest is part of deep work.",
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

      // Philosophy Card
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
                text = "Prohibition triggers rebound bingeing. Setting scheduled guilt-free windows lets you enjoy downtime without shame, protected by clear start and end boundaries.",
                style = MaterialTheme.typography.bodySmall,
                color = OffWhiteMuted,
                lineHeight = 18.sp
              )
            }
          }
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
                .background(if (isSelected) Color(0xFF22252E) else CharcoalSurface)
                .border(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) OffWhitePrimary else CharcoalBorder,
                  shape = RoundedCornerShape(14.dp)
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
                color = if (isSelected) Color.White else OffWhitePrimary
              )
            }
          }
        }
      }

      // Settings toggles
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
}
