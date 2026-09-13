package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.ReplacementActivity
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary
import com.example.ui.theme.OffWhiteSubtle

/**
 * Reusable Replacement Suggestion card/overlay proposing a quick 5-minute alternative.
 * Supportive, guilt-free phrasing, accept/dismiss actions.
 */
@Composable
fun ReplacementSuggestionCard(
  activity: ReplacementActivity,
  onAccept: () -> Unit,
  onDismiss: () -> Unit,
  onNextActivity: () -> Unit,
  modifier: Modifier = Modifier,
  isOverlay: Boolean = false
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp))
      .background(CharcoalSurface)
      .border(1.dp, CharcoalBorder, RoundedCornerShape(24.dp))
      .padding(20.dp)
      .testTag("replacement_suggestion_card")
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Top bar with category & cycle button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(OffWhiteSubtle)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = activity.category.uppercase(),
              style = MaterialTheme.typography.labelSmall,
              color = OffWhitePrimary,
              letterSpacing = 0.8.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(CharcoalBackground)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = activity.duration,
              style = MaterialTheme.typography.labelSmall,
              color = OffWhiteMuted
            )
          }
        }

        IconButton(
          onClick = onNextActivity,
          modifier = Modifier
            .size(36.dp)
            .testTag("replacement_next_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Try another alternative",
            tint = OffWhiteMuted,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Title & Subtitle
      Text(
        text = activity.title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = OffWhitePrimary
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = activity.subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = OffWhiteMuted
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Supportive microcopy
      Text(
        text = "Trade a quick unconscious scroll for a genuine energy refill.",
        style = MaterialTheme.typography.bodySmall,
        color = OffWhiteMuted.copy(alpha = 0.8f)
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Accept button
        Button(
          onClick = onAccept,
          modifier = Modifier
            .weight(1.3f)
            .height(48.dp)
            .testTag("replacement_accept_btn"),
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
              imageVector = Icons.Default.Check,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = activity.promptAction,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        // Dismiss / Continue to app
        OutlinedButton(
          onClick = onDismiss,
          modifier = Modifier
            .weight(0.9f)
            .height(48.dp)
            .testTag("replacement_dismiss_btn"),
          border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = OffWhiteMuted
          )
        ) {
          Text(
            text = "Continue",
            style = MaterialTheme.typography.labelMedium
          )
        }
      }
    }
  }
}
