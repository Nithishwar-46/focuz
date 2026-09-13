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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.layout.ContentScale
import com.example.data.FocuzUiState
import com.example.ui.components.CalmProgressRing
import com.example.ui.components.QuickStatCard
import com.example.ui.components.StreakFlameWidget
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary
import com.example.ui.theme.OffWhiteSubtle

/**
 * Home / Dashboard Screen for Focuz.
 * Displays today's streak, calm progress ring, quick stats, and interactive triggers.
 */
@Composable
fun HomeScreen(
  uiState: FocuzUiState,
  onOpenIntentPopup: (String) -> Unit,
  onOpenReplacementCard: () -> Unit,
  onNavigateToFocusSession: () -> Unit,
  onOpenProfile: () -> Unit = {},
  onSignOut: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(CharcoalBackground)
      .padding(horizontal = 20.dp)
      .testTag("home_screen"),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(12.dp))

      // Top bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Focuz",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Light,
            letterSpacing = (-1.0).sp,
            color = OffWhitePrimary
          )
          val displayName = uiState.userProfile.name.ifBlank { uiState.currentUser?.displayName ?: "" }
          val userGreeting = if (displayName.isNotBlank()) "Welcome, $displayName" else "Intent-aware wellbeing"
          Text(
            text = userGreeting,
            style = MaterialTheme.typography.bodySmall,
            color = OffWhiteMuted
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StreakFlameWidget(
            streakDays = uiState.currentStreakDays,
            showLabel = true,
            largeHero = false
          )

          // Clickable Profile Avatar
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(CharcoalSurface)
              .border(1.dp, CharcoalBorder, CircleShape)
              .clickable { onOpenProfile() }
              .testTag("btn_open_profile"),
            contentAlignment = Alignment.Center
          ) {
            val photoUrl = uiState.userProfile.photoUrl.ifBlank { uiState.currentUser?.photoUrl ?: "" }
            if (photoUrl.isNotBlank()) {
              AsyncImage(
                model = photoUrl,
                contentDescription = "User profile picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            } else {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = OffWhitePrimary,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }

    // Encouraging microcopy banner
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
          .padding(18.dp)
          .testTag("encouragement_banner")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(CharcoalBackground),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = OffWhitePrimary,
              modifier = Modifier.size(20.dp)
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = uiState.streakCelebrationMessage,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = OffWhitePrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "You paused ${uiState.interceptedOpens} unconscious opens today — that's genuine mindful progress.",
              style = MaterialTheme.typography.bodySmall,
              color = OffWhiteMuted
            )
          }
        }
      }
    }

    // Calm Progress Ring
    item {
      CalmProgressRing(
        intentionalMinutes = uiState.intentionalMinutes,
        scrollMinutes = uiState.scrollMinutes
      )
    }

    // Interactive intent simulator card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
          .padding(18.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "TRY INTENT NUDGES",
              style = MaterialTheme.typography.labelSmall,
              color = OffWhiteMuted,
              letterSpacing = 1.sp
            )

            Text(
              text = "Live preview",
              style = MaterialTheme.typography.bodySmall,
              color = OffWhiteMuted
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Experience how Focuz catches unconscious scrolling before it starts.",
            style = MaterialTheme.typography.bodyMedium,
            color = OffWhiteMuted
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { onOpenIntentPopup("Instagram") },
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("btn_trigger_intent_modal"),
              colors = ButtonDefaults.buttonColors(
                containerColor = OffWhitePrimary,
                contentColor = CharcoalBackground
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.TouchApp,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "Open App Nudge",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            OutlinedButton(
              onClick = onOpenReplacementCard,
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("btn_trigger_replacement_card"),
              border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = OffWhitePrimary
              )
            ) {
              Text(
                text = "5-Min Swap Card",
                style = MaterialTheme.typography.labelMedium
              )
            }
          }
        }
      }
    }

    // Quick Stats Grid
    item {
      Text(
        text = "TODAY'S INTENTIONALITY",
        style = MaterialTheme.typography.labelSmall,
        color = OffWhiteMuted,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        QuickStatCard(
          value = "${uiState.interceptedOpens}",
          label = "Opens Intercepted",
          microcopy = "Mindless loops redirected",
          modifier = Modifier.weight(1f),
          tag = "stat_intercepted"
        )

        QuickStatCard(
          value = "90%",
          label = "Intentional Ratio",
          microcopy = "Above weekly goal",
          modifier = Modifier.weight(1f),
          tag = "stat_ratio"
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        QuickStatCard(
          value = "${uiState.mindfulPausesTaken}",
          label = "Mindful Pauses",
          microcopy = "Took a breath first",
          modifier = Modifier.weight(1f),
          tag = "stat_pauses"
        )

        QuickStatCard(
          value = "2h 45m",
          label = "Focused Time",
          microcopy = "High-quality deep work",
          modifier = Modifier.weight(1f),
          tag = "stat_focus_time"
        )
      }
    }

    // Quick Launch Active Session Card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
          .clickable(onClick = onNavigateToFocusSession)
          .padding(20.dp)
          .testTag("card_launch_focus_session")
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Active Focus Session",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = OffWhitePrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${uiState.selectedMode.tag} • Tap to enter timer",
              style = MaterialTheme.typography.bodySmall,
              color = OffWhiteMuted
            )
          }

          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(OffWhitePrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Start session",
              tint = CharcoalBackground
            )
          }
        }
      }
    }

    // Recent Intent Timeline
    item {
      Text(
        text = "RECENT INTENT LOGS",
        style = MaterialTheme.typography.labelSmall,
        color = OffWhiteMuted,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      if (uiState.recentLogs.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalSurface)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "None",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Medium,
              color = OffWhitePrimary
            )
            Text(
              text = "No intent logs recorded yet. When you pause or open an app, your real activity will appear here.",
              style = MaterialTheme.typography.bodySmall,
              color = OffWhiteMuted,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      } else {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CharcoalSurface)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          uiState.recentLogs.forEachIndexed { index, log ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = log.appName,
                  style = MaterialTheme.typography.labelLarge,
                  color = OffWhitePrimary
                )
                Text(
                  text = "${log.reason.label} • ${log.outcome}",
                  style = MaterialTheme.typography.bodySmall,
                  color = OffWhiteMuted
                )
              }

              Text(
                text = log.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = OffWhiteMuted
              )
            }

            if (index < uiState.recentLogs.lastIndex) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(1.dp)
                  .background(CharcoalBorder)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}
