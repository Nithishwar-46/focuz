package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

/**
 * Minimalist, high-craft Email Authentication Screen for Focuz.
 * Provides clean Email & Password Sign In and Sign Up flows with tactile dark-accent button styling.
 */
@Composable
fun AuthScreen(
  isLoading: Boolean,
  errorMessage: String?,
  onEmailSignIn: (String, String) -> Unit,
  onEmailSignUp: (String, String, String) -> Unit,
  onSkipForNow: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isSignUpMode by remember { mutableStateOf(false) }
  var name by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var confirmPasswordVisible by remember { mutableStateOf(false) }
  var validationError by remember { mutableStateOf<String?>(null) }

  val keyboardController = LocalSoftwareKeyboardController.current

  fun handleSubmit() {
    keyboardController?.hide()
    validationError = null
    val trimmedEmail = email.trim()
    val trimmedPassword = password.trim()

    if (isSignUpMode && name.trim().isEmpty()) {
      validationError = "Please enter your name"
      return
    }
    if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
      validationError = "Please enter a valid email address"
      return
    }
    if (trimmedPassword.length < 6) {
      validationError = "Password must be at least 6 characters"
      return
    }
    if (isSignUpMode && trimmedPassword != confirmPassword.trim()) {
      validationError = "Passwords do not match"
      return
    }

    if (isSignUpMode) {
      onEmailSignUp(trimmedEmail, trimmedPassword, name.trim())
    } else {
      onEmailSignIn(trimmedEmail, trimmedPassword)
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
      Spacer(modifier = Modifier.height(16.dp))

      // App Logo Icon
      Box(
        modifier = Modifier
          .size(60.dp)
          .clip(CircleShape)
          .background(CharcoalSurface)
          .border(1.5.dp, CharcoalBorder, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = "Focuz logo",
          tint = OffWhitePrimary,
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Title & Subtitle
      Text(
        text = if (isSignUpMode) "Create Account" else "Welcome Back",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Normal,
        letterSpacing = (-0.5).sp,
        color = OffWhitePrimary,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = if (isSignUpMode)
          "Sign up to track intentional study habits & streaks."
        else
          "Sign in to access your focus pacts & study history.",
        style = MaterialTheme.typography.bodyMedium,
        color = OffWhiteMuted,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(26.dp))

      // Segmented Mode Switcher (Sign In vs Create Account)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder, RoundedCornerShape(14.dp))
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(if (!isSignUpMode) OffWhitePrimary else Color.Transparent)
            .clickable {
              isSignUpMode = false
              validationError = null
            }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Sign In",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (!isSignUpMode) FontWeight.Bold else FontWeight.Medium,
            color = if (!isSignUpMode) CharcoalBackground else OffWhiteMuted
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSignUpMode) OffWhitePrimary else Color.Transparent)
            .clickable {
              isSignUpMode = true
              validationError = null
            }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Create Account",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (isSignUpMode) FontWeight.Bold else FontWeight.Medium,
            color = if (isSignUpMode) CharcoalBackground else OffWhiteMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Name Input (Only in Sign Up Mode)
      AnimatedVisibility(visible = isSignUpMode) {
        Column {
          OutlinedTextField(
            value = name,
            onValueChange = {
              name = it
              validationError = null
            },
            label = { Text("Full Name", color = OffWhiteMuted) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = OffWhiteMuted
              )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Text,
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
              .testTag("input_name")
          )
          Spacer(modifier = Modifier.height(12.dp))
        }
      }

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
          onDone = { if (!isSignUpMode) handleSubmit() }
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
            trailingIcon = {
              IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                Icon(
                  imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                  tint = OffWhiteMuted
                )
              }
            },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

      // Validation / Firebase Error Banner
      val displayError = validationError ?: errorMessage
      if (displayError != null) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF241C1C))
            .border(1.dp, Color(0xFF5E2B2B), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("auth_error_banner"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFFF8B8B),
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = displayError,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFFFD4D4),
            textAlign = TextAlign.Start
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Distinct, tactile action button (charcoal container + crisp off-white border + clear text & icon)
      Button(
        onClick = { handleSubmit() },
        enabled = !isLoading,
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("btn_submit_auth"),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF22252E),
          contentColor = Color.White,
          disabledContainerColor = Color(0xFF1B1D24),
          disabledContentColor = Color(0xFF6B7280)
        ),
        border = BorderStroke(1.5.dp, OffWhitePrimary),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
          defaultElevation = 2.dp,
          pressedElevation = 0.dp
        )
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = Color.White,
            strokeWidth = 2.dp
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = if (isSignUpMode) "Creating account..." else "Signing in...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
        } else {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = if (isSignUpMode) Icons.Default.PersonAdd else Icons.AutoMirrored.Filled.Login,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = if (isSignUpMode) "Create Account" else "Sign In",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              letterSpacing = 0.3.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = Color(0xFFA1A3AC),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Toggle text button below
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

      // Guest explore button
      OutlinedButton(
        onClick = onSkipForNow,
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("btn_guest_explore"),
        border = BorderStroke(1.dp, CharcoalBorder),
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
