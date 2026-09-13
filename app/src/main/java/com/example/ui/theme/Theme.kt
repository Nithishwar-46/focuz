package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FocuzColorScheme = darkColorScheme(
  primary = OffWhitePrimary,
  onPrimary = CharcoalBackground,
  primaryContainer = CharcoalSurface,
  onPrimaryContainer = OffWhitePrimary,
  secondary = OffWhiteMuted,
  onSecondary = CharcoalBackground,
  secondaryContainer = CharcoalSurface,
  onSecondaryContainer = OffWhitePrimary,
  tertiary = OffWhitePrimary,
  onTertiary = CharcoalBackground,
  background = CharcoalBackground,
  onBackground = OffWhitePrimary,
  surface = CharcoalSurface,
  onSurface = OffWhitePrimary,
  surfaceVariant = CharcoalSurface,
  onSurfaceVariant = OffWhiteMuted,
  outline = CharcoalBorder,
  outlineVariant = OffWhiteSubtle
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = FocuzColorScheme,
    typography = FocuzTypography,
    content = content
  )
}
