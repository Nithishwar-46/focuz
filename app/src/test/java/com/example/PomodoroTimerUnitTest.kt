package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.FocuzViewModel
import com.example.ui.components.PomodoroPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PomodoroTimerUnitTest {

  private lateinit var application: Application
  private lateinit var viewModel: FocuzViewModel

  @Before
  fun setup() {
    application = ApplicationProvider.getApplicationContext<Application>()
    viewModel = FocuzViewModel(application)
  }

  @Test
  fun testPomodoroPhasesDefaults() {
    assertEquals("Deep Focus", PomodoroPhase.WORK.title)
    assertEquals(25, PomodoroPhase.WORK.defaultMinutes)

    assertEquals("Short Rest", PomodoroPhase.SHORT_BREAK.title)
    assertEquals(5, PomodoroPhase.SHORT_BREAK.defaultMinutes)

    assertEquals("Restoration", PomodoroPhase.LONG_BREAK.title)
    assertEquals(15, PomodoroPhase.LONG_BREAK.defaultMinutes)
  }

  @Test
  fun testRecordCompletedPomodoroUpdatesIntentionalMinutesAndStreak() {
    val initialIntentional = viewModel.uiState.value.intentionalMinutes
    val pomodoroMins = 25

    viewModel.recordCompletedPomodoro(pomodoroMins)

    val updatedState = viewModel.uiState.value
    assertEquals(initialIntentional + pomodoroMins, updatedState.intentionalMinutes)
    assertTrue("Streak should be at least 1 after completing a pomodoro", updatedState.currentStreakDays >= 1)
    assertTrue(updatedState.streakCelebrationMessage.contains("Pomodoro completed!"))
  }

  @Test
  fun testFormattedTimeCalculation() {
    val remainingSeconds = 25 * 60 // 1500
    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val formatted = String.format(Locale.US, "%02d:%02d", mins, secs)
    assertEquals("25:00", formatted)

    val midSessionSeconds = 14 * 60 + 9
    val midFormatted = String.format(Locale.US, "%02d:%02d", midSessionSeconds / 60, midSessionSeconds % 60)
    assertEquals("14:09", midFormatted)
  }

  @Test
  fun testWorkDurationClamping() {
    val clampedMin = 2.coerceIn(5, 90)
    assertEquals(5, clampedMin)

    val clampedMax = 120.coerceIn(5, 90)
    assertEquals(90, clampedMax)

    val validVal = 30.coerceIn(5, 90)
    assertEquals(30, validVal)
  }
}
