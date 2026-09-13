package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary
import com.example.ui.theme.OffWhiteSubtle
import kotlinx.coroutines.launch

/**
 * Minimalist Charcoal & Off-White Auth Screen.
 * Supports Sign In & Sign Up with Google integration and direct email/password flows.
 */
@Composable
fun AuthScreen(
  isLoading: Boolean,
  errorMessage: String?,
  onGoogleSignIn: () -> Unit,
  onEmailSignIn: (String, String) -> Unit,
  onEmailSignUp: (String, String) -> Unit,
  onSkipForNow: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isSignUpMode by remember { mutableStateOf(false) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var validationError by remember { mutableStateOf<String?>(null) }

  val context = LocalContext.current
  val keyboardController = LocalSoftwareKeyboardController.current

  fun handleSubmit() {
    keyboardController?.hide()
    validationError = null
    val trimmedEmail = email.trim()
    if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
      validationError = "Please enter a valid email address"
      return
    }
    if (password.length < 6) {
      validationError = "Password must be at least 6 characters"
      return
    }
    if (isSignUpMode && password != confirmPassword) {
      validationError = "Passwords do not match"
      return
    }

    if (isSignUpMode) {
      onEmailSignUp(trimmedEmail, password)
    } else {
      onEmailSignIn(trimmedEmail, password)
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CharcoalBackground)
      .imePadding()
      .testTag("auth_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(20.dp))

      // App Logo Icon
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = "Focuz brand icon",
          tint = OffWhitePrimary,
          modifier = Modifier.size(32.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Title & Subtitle
      Text(
        text = if (isSignUpMode) "Create your space" else "Welcome to Focuz",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Light,
        letterSpacing = (-0.5).sp,
        color = OffWhitePrimary,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = if (isSignUpMode)
          "Sign up to sync your focus pacts & mindful streaks."
        else
          "Intentional screen time and shared momentum.",
        style = MaterialTheme.typography.bodyMedium,
        color = OffWhiteMuted,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(32.dp))

      // Google Sign-In Primary Action
      Button(
        onClick = onGoogleSignIn,
        enabled = !isLoading,
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .clip(RoundedCornerShape(16.dp))
          .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
          .testTag("btn_google_signin"),
        colors = ButtonDefaults.buttonColors(
          containerColor = CharcoalSurface,
          contentColor = OffWhitePrimary
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          // Clean Google 'G' glyph representation
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(OffWhitePrimary),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "G",
              color = CharcoalBackground,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = if (isSignUpMode) "Sign up with Google" else "Continue with Google",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = OffWhitePrimary
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Divider "OR"
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .height(1.dp)
            .background(CharcoalBorder)
        )
        Text(
          text = "OR WITH EMAIL",
          style = MaterialTheme.typography.labelSmall,
          color = OffWhiteMuted,
          letterSpacing = 1.sp
        )
        Box(
          modifier = Modifier
            .weight(1f)
            .height(1.dp)
            .background(CharcoalBorder)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Email Input Field
      OutlinedTextField(
        value = email,
        onValueChange = {
          email = it
          validationError = null
        },
        label = { Text("Email address", color = OffWhiteMuted) },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            tint = OffWhiteMuted
          )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Email,
          imeAction = ImeAction.Next
        ),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = OffWhitePrimary,
          unfocusedTextColor = OffWhitePrimary,
          focusedContainerColor = CharcoalSurface,
          unfocusedContainerColor = CharcoalSurface,
          cursorColor = OffWhitePrimary,
          focusedBorderColor = OffWhitePrimary,
          unfocusedBorderColor = CharcoalBorder
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_email")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Password Input Field
      OutlinedTextField(
        value = password,
        onValueChange = {
          password = it
          validationError = null
        },
        label = { Text("Password", color = OffWhiteMuted) },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = OffWhiteMuted
          )
        },
        trailingIcon = {
          IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
              imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
              contentDescription = if (passwordVisible) "Hide password" else "Show password",
              tint = OffWhiteMuted
            )
          }
        },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Password,
          imeAction = if (isSignUpMode) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
          onDone = { handleSubmit() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = OffWhitePrimary,
          unfocusedTextColor = OffWhitePrimary,
          focusedContainerColor = CharcoalSurface,
          unfocusedContainerColor = CharcoalSurface,
          cursorColor = OffWhitePrimary,
          focusedBorderColor = OffWhitePrimary,
          unfocusedBorderColor = CharcoalBorder
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_password")
      )

      // Confirm Password (Sign-Up mode only)
      AnimatedVisibility(visible = isSignUpMode) {
        Column {
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
              confirmPassword = it
              validationError = null
            },
            label = { Text("Confirm password", color = OffWhiteMuted) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = OffWhiteMuted
              )
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Password,
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
              onDone = { handleSubmit() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = OffWhitePrimary,
              unfocusedTextColor = OffWhitePrimary,
              focusedContainerColor = CharcoalSurface,
              unfocusedContainerColor = CharcoalSurface,
              cursorColor = OffWhitePrimary,
              focusedBorderColor = OffWhitePrimary,
              unfocusedBorderColor = CharcoalBorder
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_confirm_password")
          )
        }
      }

      // Validation / Error Messages
      val displayError = validationError ?: errorMessage
      if (displayError != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = displayError,
          style = MaterialTheme.typography.bodySmall,
          color = OffWhitePrimary,
          textAlign = TextAlign.Start,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CharcoalSurface)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("auth_error_banner")
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Submit Button (Sign In / Sign Up)
      Button(
        onClick = { handleSubmit() },
        enabled = !isLoading,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_submit_auth"),
        colors = ButtonDefaults.buttonColors(
          containerColor = OffWhitePrimary,
          contentColor = CharcoalBackground
        ),
        shape = RoundedCornerShape(14.dp)
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = CharcoalBackground,
            strokeWidth = 2.dp
          )
        } else {
          Text(
            text = if (isSignUpMode) "Create Account" else "Sign In",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Toggle between Sign In & Sign Up
      TextButton(
        onClick = {
          isSignUpMode = !isSignUpMode
          validationError = null
        },
        modifier = Modifier.testTag("btn_toggle_auth_mode")
      ) {
        Text(
          text = if (isSignUpMode)
            "Already have an account? Sign in"
          else
            "Don't have an account? Sign up",
          style = MaterialTheme.typography.bodyMedium,
          color = OffWhiteMuted
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Skip for now (Guest / Explore mode)
      OutlinedButton(
        onClick = onSkipForNow,
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("btn_guest_explore"),
        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = OffWhiteMuted
        )
      ) {
        Text(
          text = "Continue as Guest",
          style = MaterialTheme.typography.bodyMedium
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
