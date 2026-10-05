package com.example

import com.example.ui.components.ErrorBoundaryState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ErrorBoundaryUnitTest {

  @Test
  fun testInitialStateHasNoError() {
    val state = ErrorBoundaryState()
    assertFalse(state.hasError)
    assertNull(state.error)
  }

  @Test
  fun testCaptureErrorUpdatesState() {
    val state = ErrorBoundaryState()
    val testException = IllegalStateException("Test database disconnection failure")

    state.captureError(testException)

    assertTrue(state.hasError)
    assertNotNull(state.error)
    assertEquals("Test database disconnection failure", state.error?.message)
  }

  @Test
  fun testResetClearsError() {
    val state = ErrorBoundaryState()
    state.captureError(RuntimeException("Transient network timeout"))

    assertTrue(state.hasError)

    state.reset()

    assertFalse(state.hasError)
    assertNull(state.error)
  }
}
