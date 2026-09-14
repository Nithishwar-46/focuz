package com.example.data

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.ui.BlockOverlayActivity

class FocuzAppBlockerService : AccessibilityService() {

  companion object {
    private const val TAG = "FocuzBlockerService"
    var isServiceRunning = false
      private set
  }

  private var activePackageName: String? = null
  private var activePackageStartTime: Long = 0L
  private val handler = Handler(Looper.getMainLooper())
  private var monitoringRunnable: Runnable? = null

  override fun onServiceConnected() {
    super.onServiceConnected()
    isServiceRunning = true
    Log.d(TAG, "FocuzAppBlockerService connected and listening for app events")
  }

  override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
      return
    }

    val currentPackage = event.packageName?.toString() ?: return
    if (currentPackage == packageName || currentPackage.contains("launcher", ignoreCase = true) ||
      currentPackage == "com.android.systemui"
    ) {
      stopActiveMonitoring()
      return
    }

    handlePackageChanged(currentPackage)
  }

  private fun handlePackageChanged(currentPackage: String) {
    // Record elapsed time for previous package if monitored
    activePackageName?.let { prevPkg ->
      if (activePackageStartTime > 0L) {
        val elapsedMins = ((System.currentTimeMillis() - activePackageStartTime) / (1000 * 60)).toInt()
        if (elapsedMins > 0) {
          SocialAppBlockerManager.addManualUsageMinutes(this, prevPkg, elapsedMins)
        }
      }
    }

    val (isExceeded, appLimit) = SocialAppBlockerManager.isAppExceeded(this, currentPackage)

    if (appLimit != null && appLimit.isEnabled) {
      activePackageName = currentPackage
      activePackageStartTime = System.currentTimeMillis()

      if (isExceeded) {
        // App limit reached -> Kick out to Home immediately and open mindful overlay
        triggerBlock(appLimit)
      } else {
        // Start minute-by-minute active timer check in case user reaches limit during this session
        startActiveMonitoring(appLimit)
      }
    } else {
      stopActiveMonitoring()
      activePackageName = null
      activePackageStartTime = 0L
    }
  }

  private fun startActiveMonitoring(appLimit: SocialAppLimit) {
    stopActiveMonitoring()
    val checkIntervalMs = 15_000L // check every 15 seconds

    monitoringRunnable = object : Runnable {
      override fun run() {
        val pkg = activePackageName ?: return
        val currentSessionMins = ((System.currentTimeMillis() - activePackageStartTime) / (1000 * 60)).toInt()
        val totalUsed = appLimit.usedTodayMinutes + currentSessionMins

        if (totalUsed >= appLimit.limitMinutes) {
          SocialAppBlockerManager.addManualUsageMinutes(this@FocuzAppBlockerService, pkg, currentSessionMins)
          triggerBlock(appLimit.copy(usedTodayMinutes = totalUsed))
        } else {
          handler.postDelayed(this, checkIntervalMs)
        }
      }
    }
    handler.postDelayed(monitoringRunnable!!, checkIntervalMs)
  }

  private fun stopActiveMonitoring() {
    monitoringRunnable?.let { handler.removeCallbacks(it) }
    monitoringRunnable = null
  }

  private fun triggerBlock(appLimit: SocialAppLimit) {
    stopActiveMonitoring()
    SocialAppBlockerManager.recordIntercept(this)

    // 1. Kick user back to launcher home immediately
    try {
      performGlobalAction(GLOBAL_ACTION_HOME)
    } catch (e: Exception) {
      Log.e(TAG, "Error performing global home action: ${e.message}")
    }

    // 2. Open mindful lock overlay
    try {
      val intent = Intent(this, BlockOverlayActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        putExtra(BlockOverlayActivity.EXTRA_PACKAGE_NAME, appLimit.packageName)
        putExtra(BlockOverlayActivity.EXTRA_APP_NAME, appLimit.appName)
        putExtra(BlockOverlayActivity.EXTRA_LIMIT_MINS, appLimit.limitMinutes)
        putExtra(BlockOverlayActivity.EXTRA_USED_MINS, appLimit.usedTodayMinutes)
      }
      startActivity(intent)
    } catch (e: Exception) {
      Log.e(TAG, "Failed launching BlockOverlayActivity: ${e.message}")
    }
  }

  override fun onInterrupt() {
    stopActiveMonitoring()
  }

  override fun onDestroy() {
    super.onDestroy()
    isServiceRunning = false
    stopActiveMonitoring()
  }
}
