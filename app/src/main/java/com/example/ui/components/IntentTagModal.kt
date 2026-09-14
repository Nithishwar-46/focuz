package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.IntentReason
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary
import com.example.ui.theme.OffWhiteSubtle

/**
 * Lightweight Intent-Tag modal appearing when opening a distracting app.
 * Non-punitive, highly supportive microcopy, 4 quick-tap reason chips.
 */
@Composable
fun IntentTagModal(
  isVisible: Boolean,
  appName: String,
  selectedReason: IntentReason?,
  onReasonSelected: (IntentReason) -> Unit,
  onMindfulMinutesSelected: (Int) -> Unit,
  onSuggestAlternative: () -> Unit,
  onCloseAppConsciously: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (!isVisible) return

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(CharcoalBackground.copy(alpha = 0.85f))
        .clickable(onClick = onDismiss),
      contentAlignment = Alignment.BottomCenter
    ) {
      Box(
        modifier = modifier
          .clickable(enabled = false, onClick = {}) // prevent tap-through
          .fillMaxWidth()
          .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
          .padding(24.dp)
          .testTag("intent_tag_modal")
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.Start
        ) {
          // Top pill indicator
          Box(
            modifier = Modifier
              .align(Alignment.CenterHorizontally)
              .size(width = 36.dp, height = 4.dp)
              .clip(CircleShape)
              .background(OffWhiteMuted.copy(alpha = 0.4f))
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Header Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Opening $appName",
                style = MaterialTheme.typography.labelMedium,
                color = OffWhiteMuted
              )
              Text(
                text = "What are you here for?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = OffWhitePrimary
              )
            }
            IconButton(
              onClick = onDismiss,
              modifier = Modifier.testTag("intent_modal_close_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss modal",
                tint = OffWhiteMuted
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Take a breath. No guilt — just bringing conscious awareness to your time.",
            style = MaterialTheme.typography.bodyMedium,
            color = OffWhiteMuted
          )

          Spacer(modifier = Modifier.height(20.dp))

          // 4 Quick-tap reason chips
          Text(
            text = "REASON FOR OPENING",
            style = MaterialTheme.typography.labelSmall,
            color = OffWhiteMuted,
            letterSpacing = 1.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            IntentReason.values().forEach { reason ->
              val isSelected = selectedReason == reason
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(16.dp))
                  .background(if (isSelected) androidx.compose.ui.graphics.Color(0xFF22252E) else CharcoalBackground)
                  .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) OffWhitePrimary else CharcoalBorder,
                    shape = RoundedCornerShape(16.dp)
                  )
                  .clickable { onReasonSelected(reason) }
                  .padding(horizontal = 16.dp, vertical = 14.dp)
                  .testTag("reason_chip_${reason.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  Text(
                    text = reason.prompt,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) androidx.compose.ui.graphics.Color.White else OffWhitePrimary
                  )
                }

                Text(
                  text = reason.subtitle,
                  style = MaterialTheme.typography.bodySmall,
                  color = if (isSelected) OffWhitePrimary.copy(alpha = 0.9f) else OffWhiteMuted
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Action choices
          Text(
            text = "HOW WOULD YOU LIKE TO PROCEED?",
            style = MaterialTheme.typography.labelSmall,
            color = OffWhiteMuted,
            letterSpacing = 1.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Option 1: 5 mindful minutes
            Button(
              onClick = { onMindfulMinutesSelected(5) },
              modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .testTag("action_5_mins"),
              colors = ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color(0xFF22252E),
                contentColor = androidx.compose.ui.graphics.Color.White
              ),
              border = BorderStroke(1.5.dp, OffWhitePrimary),
              shape = RoundedCornerShape(14.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Timer,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = androidx.compose.ui.graphics.Color.White
                )
                Text(
                  text = "5 Mindful Mins",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = androidx.compose.ui.graphics.Color.White
                )
              }
            }

            // Option 2: Suggest 5-min alternative
            OutlinedButton(
              onClick = onSuggestAlternative,
              modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .testTag("action_suggest_alternative"),
              border = BorderStroke(1.5.dp, CharcoalBorder),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = CharcoalSurface,
                contentColor = OffWhitePrimary
              )
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = OffWhitePrimary
                )
                Text(
                  text = "5-Min Reset",
                  style = MaterialTheme.typography.labelLarge,
                  color = OffWhitePrimary
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Option 3: Close phone consciously
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .clickable(onClick = onCloseAppConsciously)
              .padding(vertical = 12.dp)
              .testTag("action_close_consciously"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Put phone face down (win this moment)",
              style = MaterialTheme.typography.bodySmall,
              color = OffWhiteMuted,
              textAlign = TextAlign.Center
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }
  }
}
