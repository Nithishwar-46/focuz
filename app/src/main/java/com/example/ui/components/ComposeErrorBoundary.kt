package com.example.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary

/**
 * State holder for capturing runtime component exceptions and rendering fallback UI.
 */
class ErrorBoundaryState {
  var error: Throwable? by mutableStateOf(null)
    private set

  val hasError: Boolean
    get() = error != null

  fun captureError(throwable: Throwable) {
    Log.e("ComposeErrorBoundary", "Caught exception in Compose boundary: ${throwable.message}", throwable)
    error = throwable
  }

  fun reset() {
    error = null
  }
}

@Composable
fun rememberErrorBoundaryState(): ErrorBoundaryState {
  return remember { ErrorBoundaryState() }
}

/**
 * Compose Error Boundary Component.
 *
 * Wraps composable sections to provide graceful error isolation. When a runtime error occurs
 * in child composition or async operations, instead of crashing the entire activity or process,
 * this component displays a tranquil, calm fallback card with recovery/retry affordances.
 *
 * @param modifier Modifier for root container
 * @param boundaryName Human-readable label for the boundary (e.g. "Focus Trends", "Auth Flow")
 * @param state State object holding error state
 * @param onRetry Callback invoked when the user taps "Retry"
 * @param fallbackContent Optional custom fallback composable
 * @param content Target composable content to protect
 */
@Composable
fun ComposeErrorBoundary(
  modifier: Modifier = Modifier,
  boundaryName: String = "Component",
  state: ErrorBoundaryState = rememberErrorBoundaryState(),
  onRetry: () -> Unit = {},
  fallbackContent: (@Composable (Throwable, () -> Unit) -> Unit)? = null,
  content: @Composable () -> Unit
) {
  var showDetails by remember { mutableStateOf(false) }

  if (state.hasError) {
    val currentError = state.error ?: Exception("Unknown error occurred")

    if (fallbackContent != null) {
      fallbackContent(currentError) {
        state.reset()
        onRetry()
      }
    } else {
      // Default Serene Fallback Card adhering to M3 and Focuz aesthetic
      Box(
        modifier = modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
          .padding(20.dp)
          .testTag("error_boundary_fallback_${boundaryName.lowercase().replace(" ", "_")}"),
        contentAlignment = Alignment.Center
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(Color(0xFF2A1C1C))
              .border(1.dp, Color(0xFFEF5350).copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = "Error indicator",
              tint = Color(0xFFEF5350),
              modifier = Modifier.size(22.dp)
            )
          }

          Text(
            text = "$boundaryName Paused",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = OffWhitePrimary
          )

          Text(
            text = "A temporary hiccup occurred. Distractions and errors pass like weather.",
            style = MaterialTheme.typography.bodySmall,
            color = OffWhiteMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
          )

          // Toggleable technical details for review & debugging
          if (showDetails) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CharcoalBackground)
                .padding(10.dp)
            ) {
              Text(
                text = currentError.localizedMessage ?: currentError.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFFB4AB),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
              )
            }
          }

          Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = { showDetails = !showDetails },
              border = BorderStroke(1.dp, CharcoalBorder),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = CharcoalBackground,
                contentColor = OffWhiteMuted
              )
            ) {
              Text(
                text = if (showDetails) "Hide Info" else "Diagnostics",
                style = MaterialTheme.typography.labelSmall
              )
            }

            Button(
              onClick = {
                state.reset()
                onRetry()
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF22252E),
                contentColor = Color.White
              ),
              border = BorderStroke(1.5.dp, OffWhitePrimary)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "Resume",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }
      }
    }
  } else {
    // Normal content composition when no error has been captured
    content()
  }
}
