package com.example.data

data class SocialAppLimit(
  val packageName: String,
  val appName: String,
  val iconGlyph: String,
  val defaultLimitMinutes: Int = 15,
  val limitMinutes: Int = 15,
  val usedTodayMinutes: Int = 0,
  val isEnabled: Boolean = true,
  val category: String = "Social Media"
) {
  val isLimitReached: Boolean
    get() = isEnabled && usedTodayMinutes >= limitMinutes

  val remainingMinutes: Int
    get() = if (limitMinutes > usedTodayMinutes) limitMinutes - usedTodayMinutes else 0

  val usageRatio: Float
    get() = if (limitMinutes > 0) (usedTodayMinutes.toFloat() / limitMinutes.toFloat()).coerceIn(0f, 1f) else 0f
}
