package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.ui.graphics.vector.ImageVector

enum class IntentReason(val label: String, val prompt: String, val subtitle: String) {
  BORED("Bored", "🥱 Bored", "Looking for quick stimulation"),
  BREAK("Break", "☕ Break", "Taking a well-earned breather"),
  HABIT("Habit", "🔄 Habit", "Opened phone without realizing"),
  NEED_INFO("Need info", "🔍 Need info", "Looking for something specific")
}

data class ReplacementActivity(
  val id: String,
  val title: String,
  val duration: String,
  val subtitle: String,
  val category: String,
  val promptAction: String
)

data class FocusContextMode(
  val id: String,
  val title: String,
  val tag: String,
  val subtitle: String,
  val defaultMinutes: Int
)

data class FriendAccountability(
  val name: String,
  val initials: String,
  val streakDays: Int,
  val intentionalityPercent: Int,
  val hoursFocusedToday: String,
  val statusNote: String
)

data class PermittedScrollWindow(
  val id: String,
  val title: String,
  val timeRange: String,
  val durationMinutes: Int,
  val isActiveNow: Boolean,
  val remainingMinutes: Int,
  val description: String
)

data class IntentLogItem(
  val appName: String,
  val timestamp: String,
  val reason: IntentReason,
  val outcome: String
)
