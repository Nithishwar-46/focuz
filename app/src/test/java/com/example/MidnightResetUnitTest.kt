package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.SocialAppBlockerManager
import com.example.data.local.DailyTimerRecordDao
import com.example.data.local.DailyTimerRecordEntity
import com.example.data.local.FocuzDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MidnightResetUnitTest {

  private lateinit var database: FocuzDatabase
  private lateinit var dao: DailyTimerRecordDao
  private lateinit var context: Context

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, FocuzDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    dao = database.dailyTimerRecordDao()
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun testTodayDateKeyFormat() {
    val dateKey = SocialAppBlockerManager.getTodayDateKey()
    assertTrue("Date key should match YYYY-MM-DD pattern", dateKey.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
  }

  @Test
  fun testMillisUntilMidnightIsPositiveAndUnder24Hours() {
    val millis = SocialAppBlockerManager.getMillisUntilNextMidnight()
    val maxDayMillis = 24 * 60 * 60 * 1000L
    assertTrue("Millis until midnight should be > 0", millis > 0)
    assertTrue("Millis until midnight should be <= 24h", millis <= maxDayMillis)
  }

  @Test
  fun testFormatTimeRemainingUntilMidnight() {
    val formatted = SocialAppBlockerManager.formatTimeRemainingUntilMidnight()
    assertTrue("Formatted time should contain 'm'", formatted.contains("m"))
  }

  @Test
  fun testRoomDatabaseInsertAndQueryRecords() = runBlocking {
    val record1 = DailyTimerRecordEntity(
      date = "2026-10-04",
      intentionalFocusMinutes = 45,
      scrollMinutes = 15,
      interceptedOpens = 4,
      mindfulPausesTaken = 3,
      focusGoalMinutes = 60,
      goalAchieved = false
    )
    val record2 = DailyTimerRecordEntity(
      date = "2026-10-05",
      intentionalFocusMinutes = 75,
      scrollMinutes = 10,
      interceptedOpens = 6,
      mindfulPausesTaken = 5,
      focusGoalMinutes = 60,
      goalAchieved = true
    )

    dao.insertOrUpdate(record1)
    dao.insertOrUpdate(record2)

    val queried = dao.getRecordForDate("2026-10-05")
    assertNotNull(queried)
    assertEquals(75, queried?.intentionalFocusMinutes)
    assertTrue(queried?.goalAchieved == true)

    val all = dao.getAllRecords().first()
    assertEquals(2, all.size)
    assertEquals("2026-10-05", all[0].date) // ordered by date DESC
    assertEquals("2026-10-04", all[1].date)
  }

  @Test
  fun testMidnightResetClearsManualUsageAndTodayIntercepts() {
    SocialAppBlockerManager.addManualUsageMinutes(context, "com.instagram.android", 25)
    SocialAppBlockerManager.recordIntercept(context)

    val todayIntercepts = SocialAppBlockerManager.getTodayIntercepts(context)
    assertTrue("Today's intercepts should be >= 1", todayIntercepts >= 1)

    // Force midnight reset
    SocialAppBlockerManager.forceMidnightResetForTesting(context)

    val apps = SocialAppBlockerManager.getMonitoredApps(context)
    val instagram = apps.find { it.packageName == "com.instagram.android" }
    assertNotNull(instagram)
    // After reset, manual usage was cleared so effective usage is 0
    assertEquals(0, instagram?.usedTodayMinutes)
    assertEquals(0, SocialAppBlockerManager.getTodayIntercepts(context))
  }
}
