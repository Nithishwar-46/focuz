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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary

@Composable
fun BuddyPairModal(
  onDismiss: () -> Unit,
  onPair: (name: String, initials: String, note: String) -> Unit
) {
  var friendName by remember { mutableStateOf("") }
  var noteText by remember { mutableStateOf("Studying mindfully together") }

  val presetBuddies = listOf(
    Pair("Maya L.", "Pre-Med student • 45m daily"),
    Pair("Leo K.", "Software Engineering sprint"),
    Pair("Alex R.", "Deep reading & focus flow")
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .clip(RoundedCornerShape(24.dp))
        .background(CharcoalSurface)
        .border(1.dp, CharcoalBorder, RoundedCornerShape(24.dp))
        .padding(20.dp)
        .testTag("buddy_pair_modal")
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
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
                imageVector = Icons.Default.GroupAdd,
                contentDescription = null,
                tint = OffWhitePrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Column {
              Text(
                text = "Pair Study Buddy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Light,
                color = OffWhitePrimary
              )
              Text(
                text = "Shared quiet momentum",
                style = MaterialTheme.typography.bodySmall,
                color = OffWhiteMuted
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(36.dp).testTag("btn_close_buddy_modal")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = OffWhiteMuted)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "CHOOSE OR ENTER A STUDY PARTNER",
          style = MaterialTheme.typography.labelSmall,
          color = OffWhiteMuted,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Preset buddy suggestions
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          presetBuddies.forEach { (name, note) ->
            val isSelected = friendName == name
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) Color(0xFF22252E) else CharcoalBackground)
                .border(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) OffWhitePrimary else CharcoalBorder,
                  shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                  friendName = name
                  noteText = note
                }
                .padding(12.dp)
                .testTag("preset_buddy_${name.take(4).lowercase()}")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CharcoalSurface),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = name.take(2),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OffWhitePrimary
                  )
                }

                Column {
                  Text(
                    text = name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else OffWhitePrimary
                  )
                  Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = OffWhiteMuted
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = friendName,
          onValueChange = { friendName = it },
          label = { Text("Friend's Name") },
          placeholder = { Text("e.g. Jordan M.") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_friend_name"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OffWhitePrimary,
            unfocusedBorderColor = CharcoalBorder,
            focusedTextColor = OffWhitePrimary,
            unfocusedTextColor = OffWhitePrimary,
            focusedLabelColor = OffWhitePrimary,
            unfocusedLabelColor = OffWhiteMuted,
            focusedContainerColor = CharcoalBackground,
            unfocusedContainerColor = CharcoalBackground
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = noteText,
          onValueChange = { noteText = it },
          label = { Text("Shared Focus Note") },
          placeholder = { Text("e.g. Crushing exam week together") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_friend_note"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OffWhitePrimary,
            unfocusedBorderColor = CharcoalBorder,
            focusedTextColor = OffWhitePrimary,
            unfocusedTextColor = OffWhitePrimary,
            focusedLabelColor = OffWhitePrimary,
            unfocusedLabelColor = OffWhiteMuted,
            focusedContainerColor = CharcoalBackground,
            unfocusedContainerColor = CharcoalBackground
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = {
            val chosenName = friendName.ifBlank { "Maya L." }
            val initials = chosenName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase()
            onPair(chosenName, initials, noteText)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("btn_confirm_pair_buddy"),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF22252E),
            contentColor = Color.White
          ),
          border = BorderStroke(1.5.dp, OffWhitePrimary),
          shape = RoundedCornerShape(14.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Text(
              text = "Confirm & Start 7-Day Pact",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              color = Color.White
            )
          }
        }
      }
    }
  }
}
