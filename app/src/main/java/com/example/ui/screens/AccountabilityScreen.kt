package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.WavingHand
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocuzUiState
import com.example.ui.components.StreakFlameWidget
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary
import com.example.ui.theme.OffWhiteSubtle

/**
 * Accountability & Streaks Screen.
 * Side-by-side streak comparison with paired friend, Duolingo-style flame widget,
 * and 7-day shared focus pact.
 */
@Composable
fun AccountabilityScreen(
  uiState: FocuzUiState,
  onSendCheer: () -> Unit,
  modifier: Modifier = Modifier
) {
  val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(CharcoalBackground)
      .padding(horizontal = 20.dp)
      .testTag("accountability_screen"),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(12.dp))

      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Streaks & Buddies",
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.Light,
          color = OffWhitePrimary
        )
        Text(
          text = "Quiet shared momentum — no public leaderboards",
          style = MaterialTheme.typography.bodySmall,
          color = OffWhiteMuted
        )
      }
    }

    // Hero Duolingo-style Flame Widget
    item {
      StreakFlameWidget(
        streakDays = uiState.currentStreakDays,
        largeHero = true
      )
    }

    // Side-by-Side Comparison with Paired Friend
    item {
      Text(
        text = "PAIRED FOCUS BUDDY",
        style = MaterialTheme.typography.labelSmall,
        color = OffWhiteMuted,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      val currentFriend = uiState.friend
      if (currentFriend == null) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CharcoalSurface)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
            .padding(24.dp)
            .testTag("side_by_side_comparison_card"),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.People,
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
              text = "No focus buddy paired yet. Link an accountability partner to share quiet momentum.",
              style = MaterialTheme.typography.bodySmall,
              color = OffWhiteMuted,
              textAlign = TextAlign.Center
            )
          }
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CharcoalSurface)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
            .testTag("side_by_side_comparison_card")
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Shared Momentum",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = OffWhitePrimary
              )

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(CharcoalBackground)
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "Active Pact",
                  style = MaterialTheme.typography.labelSmall,
                  color = OffWhitePrimary
                )
              }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Two-column side by side
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              // YOU Column
              Column(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(16.dp))
                  .background(CharcoalBackground)
                  .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(OffWhitePrimary),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "YOU",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalBackground
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = "${uiState.currentStreakDays} Days",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = OffWhitePrimary
                )

                val userIntentionalHours = uiState.intentionalMinutes / 60
                val userIntentionalRem = uiState.intentionalMinutes % 60
                val formattedUserFocus = if (userIntentionalHours > 0) "${userIntentionalHours}h ${userIntentionalRem}m" else "${userIntentionalRem}m"
                Text(
                  text = "$formattedUserFocus focused",
                  style = MaterialTheme.typography.labelSmall,
                  color = OffWhitePrimary
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              // FRIEND Column
              Column(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(16.dp))
                  .background(CharcoalBackground)
                  .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CharcoalSurface)
                    .border(1.dp, CharcoalBorder, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = currentFriend.initials,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = OffWhitePrimary
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = "${currentFriend.streakDays} Days",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = OffWhitePrimary
                )

                Text(
                  text = "${currentFriend.intentionalityPercent}% intentional",
                  style = MaterialTheme.typography.bodySmall,
                  color = OffWhiteMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = "${currentFriend.hoursFocusedToday} focused",
                  style = MaterialTheme.typography.labelSmall,
                  color = OffWhitePrimary
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Friend's current note
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CharcoalBackground)
                .padding(12.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = "${currentFriend.name}:",
                  style = MaterialTheme.typography.labelMedium,
                  color = OffWhitePrimary
                )
                Text(
                  text = "\"${currentFriend.statusNote}\"",
                  style = MaterialTheme.typography.bodySmall,
                  color = OffWhiteMuted
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cheer action button
            Button(
              onClick = onSendCheer,
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("send_cheer_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.cheerSent) CharcoalBackground else OffWhitePrimary,
                contentColor = if (uiState.cheerSent) OffWhitePrimary else CharcoalBackground
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = if (uiState.cheerSent) Icons.Default.Favorite else Icons.Default.WavingHand,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = if (uiState.cheerSent) "Cheer sent to ${currentFriend.name}!" else "Send gentle cheer 👋",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }
      }
    }

    // 7-Day Shared Focus Pact Tracker
    item {
      Text(
        text = "7-DAY FOCUS PACT",
        style = MaterialTheme.typography.labelSmall,
        color = OffWhiteMuted,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
          .padding(20.dp)
          .testTag("weekly_pact_card")
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "This Week's Pact",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = OffWhitePrimary
            )
            Text(
              text = "5 / 7 Days Done",
              style = MaterialTheme.typography.labelMedium,
              color = OffWhitePrimary
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // 7 Days Circles
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            uiState.weeklyPactDays.forEachIndexed { index, isCompleted ->
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) OffWhitePrimary else CharcoalBackground)
                    .border(
                      1.dp,
                      if (isCompleted) OffWhitePrimary else CharcoalBorder,
                      CircleShape
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  if (isCompleted) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "Completed day",
                      tint = CharcoalBackground,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }

                Text(
                  text = daysOfWeek[index],
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isCompleted) OffWhitePrimary else OffWhiteMuted
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Both of you are holding space for each other this week. No shame if a day is missed — just pick up tomorrow.",
            style = MaterialTheme.typography.bodySmall,
            color = OffWhiteMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}
