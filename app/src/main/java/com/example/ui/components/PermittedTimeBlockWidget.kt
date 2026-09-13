package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PermittedScrollWindow
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary
import com.example.ui.theme.OffWhiteSubtle

/**
 * Reusable Permitted Time Block widget.
 * Shows a calm, clearly-bounded "free time" window rather than a countdown of shame.
 */
@Composable
fun PermittedTimeBlockWidget(
  window: PermittedScrollWindow,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp))
      .background(CharcoalSurface)
      .border(1.dp, CharcoalBorder, RoundedCornerShape(24.dp))
      .padding(22.dp)
      .testTag("permitted_time_block_${window.id}")
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Header status
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
              .size(8.dp)
              .clip(CircleShape)
              .background(if (window.isActiveNow) OffWhitePrimary else OffWhiteMuted)
          )
          Text(
            text = if (window.isActiveNow) "CURRENTLY ACTIVE" else "SCHEDULED WINDOW",
            style = MaterialTheme.typography.labelSmall,
            color = if (window.isActiveNow) OffWhitePrimary else OffWhiteMuted,
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CharcoalBackground)
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = "${window.durationMinutes} mins",
            style = MaterialTheme.typography.labelMedium,
            color = OffWhitePrimary
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = window.title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = OffWhitePrimary
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = window.timeRange,
        style = MaterialTheme.typography.titleMedium,
        color = OffWhitePrimary
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Calm bounded time bar visualization
      Column(modifier = Modifier.fillMaxWidth()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(CharcoalBackground)
        ) {
          if (window.isActiveNow) {
            val fraction = (window.remainingMinutes.toFloat() / window.durationMinutes.toFloat()).coerceIn(0f, 1f)
            Box(
              modifier = Modifier
                .fillMaxWidth(fraction)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(OffWhitePrimary)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = if (window.isActiveNow) "${window.remainingMinutes} mins remaining" else "Starts at 12:30 PM",
            style = MaterialTheme.typography.bodySmall,
            color = OffWhitePrimary
          )
          Text(
            text = if (window.isActiveNow) "Guilt-free window" else "Nudges pause automatically",
            style = MaterialTheme.typography.bodySmall,
            color = OffWhiteMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Calm supportive note
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(CharcoalBackground)
          .padding(12.dp)
      ) {
        Text(
          text = window.description,
          style = MaterialTheme.typography.bodySmall,
          color = OffWhiteMuted
        )
      }
    }
  }
}
