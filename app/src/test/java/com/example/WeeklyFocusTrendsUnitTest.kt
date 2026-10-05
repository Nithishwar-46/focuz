package com.example

import com.example.data.local.DailyTimerRecordEntity
import com.example.ui.components.DayFocusTrend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WeeklyFocusTrendsUnitTest {

  @Test
  fun testDayFocusTrendCreation() {
    val trend = DayFocusTrend(
      dayIndex = 0,
      dayLabel = "Mon",
      dateFormatted = "Oct 5",
      dateKey = "2026-10-05",
      focusMinutes = 65,
      scrollMinutes = 15,
      goalMinutes = 60,
      isToday = true
    )

    assertEquals("Mon", trend.dayLabel)
    assertEquals(65, trend.focusMinutes)
    assertEquals(15, trend.scrollMinutes)
    assertTrue("Should exceed 60m goal", trend.focusMinutes >= trend.goalMinutes)
    assertTrue(trend.isToday)
  }

  @Test
  fun testWeeklyTotalAndDailyAverageCalculations() {
    val days = listOf(
      DayFocusTrend(0, "Mon", "Oct 5", "2026-10-05", 45, 10, 60),
      DayFocusTrend(1, "Tue", "Oct 6", "2026-10-06", 60, 20, 60),
      DayFocusTrend(2, "Wed", "Oct 7", "2026-10-07", 75, 15, 60),
      DayFocusTrend(3, "Thu", "Oct 8", "2026-10-08", 50, 10, 60),
      DayFocusTrend(4, "Fri", "Oct 9", "2026-10-09", 90, 25, 60),
      DayFocusTrend(5, "Sat", "Oct 10", "2026-10-10", 30, 30, 60),
      DayFocusTrend(6, "Sun", "Oct 11", "2026-10-11", 20, 10, 60)
    )

    val totalMinutes = days.sumOf { it.focusMinutes }
    assertEquals(370, totalMinutes)

    val dailyAverage = totalMinutes / days.size
    assertEquals(52, dailyAverage)

    val goalsMet = days.count { it.focusMinutes >= it.goalMinutes }
    assertEquals(3, goalsMet) // Tue (60), Wed (75), Fri (90)

    val bestDay = days.maxByOrNull { it.focusMinutes }
    assertNotNull(bestDay)
    assertEquals("Fri", bestDay?.dayLabel)
    assertEquals(90, bestDay?.focusMinutes)
  }

  @Test
  fun testIntegrationWithArchivedRecords() {
    val archived = listOf(
      DailyTimerRecordEntity(
        date = "2026-10-04",
        intentionalFocusMinutes = 80,
        scrollMinutes = 20,
        interceptedOpens = 5,
        mindfulPausesTaken = 3,
        focusGoalMinutes = 60,
        goalAchieved = true
      )
    )

    val matched = archived.find { it.date == "2026-10-04" }
    assertNotNull(matched)
    assertEquals(80, matched?.intentionalFocusMinutes)
    assertTrue(matched?.goalAchieved == true)
  }
}
