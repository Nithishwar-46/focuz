package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted record of daily focus time, scrolling, pauses, and intercepts.
 * A new record is archived for every completed day right at 12:00 AM (midnight),
 * and a fresh clean record begins for the new day.
 */
@Entity(tableName = "daily_timer_records")
data class DailyTimerRecordEntity(
  @PrimaryKey
  val date: String, // Format: "YYYY-MM-DD" e.g. "2026-10-04"
  val intentionalFocusMinutes: Int = 0,
  val scrollMinutes: Int = 0,
  val interceptedOpens: Int = 0,
  val mindfulPausesTaken: Int = 0,
  val focusGoalMinutes: Int = 60,
  val goalAchieved: Boolean = false,
  val recordedAtTimestamp: Long = System.currentTimeMillis()
)
