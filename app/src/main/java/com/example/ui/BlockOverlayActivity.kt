package com.example.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.data.SocialAppBlockerManager
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary

class BlockOverlayActivity : ComponentActivity() {

  companion object {
    const val EXTRA_PACKAGE_NAME = "extra_package_name"
    const val EXTRA_APP_NAME = "extra_app_name"
    const val EXTRA_LIMIT_MINS = "extra_limit_mins"
    const val EXTRA_USED_MINS = "extra_used_mins"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: "com.instagram.android"
    val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "Instagram"
    val limitMins = intent.getIntExtra(EXTRA_LIMIT_MINS, 15)
    val usedMins = intent.getIntExtra(EXTRA_USED_MINS, 15)

    setContent {
      MyApplicationTheme {
        BlockOverlayScreen(
          appName = appName,
          packageName = packageName,
          limitMins = limitMins,
          usedMins = usedMins,
          onOpenFocuz = {
            val mainIntent = Intent(this, MainActivity::class.java).apply {
              addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(mainIntent)
            finish()
          },
          onCloseToHome = {
            finish()
          },
          onGrantExtension = { addedMins ->
            SocialAppBlockerManager.setAppLimit(this, packageName, limitMins + addedMins)
            finish()
          }
        )
      }
    }
  }
}

@Composable
fun BlockOverlayScreen(
  appName: String,
  packageName: String,
  limitMins: Int,
  usedMins: Int,
  onOpenFocuz: () -> Unit,
  onCloseToHome: () -> Unit,
  onGrantExtension: (Int) -> Unit
) {
  var showExtensionSection by remember { mutableStateOf(false) }
  var extensionReason by remember { mutableStateOf("") }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CharcoalBackground)
      .padding(24.dp)
      .testTag("block_overlay_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Spacer(modifier = Modifier.height(24.dp))

      // Lock Glyphed Hero Ring
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = "App Locked",
          tint = OffWhitePrimary,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "$appName Limit Reached",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Light,
        color = OffWhitePrimary,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "You set a $limitMins-minute daily ceiling.\nYou've spent $usedMins minutes today.",
        style = MaterialTheme.typography.bodyMedium,
        color = OffWhiteMuted,
        textAlign = TextAlign.Center,
        lineHeight = 22.sp
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Mindful pause suggestion card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
          .padding(18.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(CharcoalBackground),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.SelfImprovement,
              contentDescription = null,
              tint = OffWhitePrimary,
              modifier = Modifier.size(24.dp)
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Mindful Replacement",
              style = MaterialTheme.typography.labelSmall,
              color = OffWhiteMuted,
              letterSpacing = 1.sp
            )
            Text(
              text = "Take a 3-breath break or drink a glass of water before looking at another screen.",
              style = MaterialTheme.typography.bodySmall,
              color = OffWhitePrimary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      // Primary Action: Open Focuz
      Button(
        onClick = onOpenFocuz,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_block_open_focuz"),
        colors = ButtonDefaults.buttonColors(
          containerColor = androidx.compose.ui.graphics.Color(0xFF22252E),
          contentColor = androidx.compose.ui.graphics.Color.White
        ),
        border = BorderStroke(1.5.dp, OffWhitePrimary),
        shape = RoundedCornerShape(14.dp)
      ) {
        Text(
          text = "Open Focuz Dashboard",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = androidx.compose.ui.graphics.Color.White
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Secondary Action: Close to Home
      OutlinedButton(
        onClick = onCloseToHome,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_block_close"),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = OffWhitePrimary
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
      ) {
        Text("I'm Done (Close)")
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Conscious Friction Extension
      if (!showExtensionSection) {
        OutlinedButton(
          onClick = { showExtensionSection = true },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = OffWhiteMuted),
          border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("btn_request_extension")
        ) {
          Icon(Icons.Default.HourglassBottom, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Need 5 more mins? Add conscious extension",
            fontSize = 12.sp,
            color = OffWhiteMuted
          )
        }
      } else {
        AnimatedVisibility(visible = showExtensionSection) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(CharcoalSurface)
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Why do you need this extra time?",
              style = MaterialTheme.typography.labelMedium,
              color = OffWhitePrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
              value = extensionReason,
              onValueChange = { extensionReason = it },
              placeholder = { Text("e.g., replying to an urgent message", fontSize = 13.sp, color = OffWhiteMuted) },
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = OffWhitePrimary,
                unfocusedTextColor = OffWhitePrimary,
                cursorColor = OffWhitePrimary,
                focusedBorderColor = OffWhitePrimary,
                unfocusedBorderColor = CharcoalBorder
              ),
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = { onGrantExtension(5) },
              enabled = extensionReason.trim().length >= 3,
              colors = ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color(0xFF22252E),
                contentColor = androidx.compose.ui.graphics.Color.White,
                disabledContainerColor = CharcoalSurface,
                disabledContentColor = OffWhiteMuted
              ),
              border = BorderStroke(1.dp, if (extensionReason.trim().length >= 3) OffWhitePrimary else CharcoalBorder),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
              Text(
                "Grant +5 min Extension",
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
