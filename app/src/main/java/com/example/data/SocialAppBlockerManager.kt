package com.example.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import java.util.Calendar

object SocialAppBlockerManager {
  private const val TAG = "FocuzBlockerMgr"
  private const val PREFS_NAME = "focuz_social_blocker_prefs"
  private const val KEY_PREFIX_ENABLED = "app_enabled_"
  private const val KEY_PREFIX_LIMIT = "app_limit_"
  private const val KEY_PREFIX_MANUAL_USAGE = "app_manual_usage_"
  private const val KEY_LAST_USAGE_DATE = "last_usage_date"
  private const val KEY_INTERCEPTED_COUNT = "total_blocked_intercepts"

  val DEFAULT_MONITORED_APPS = listOf(
    SocialAppLimit(
      packageName = "com.instagram.android",
      appName = "Instagram",
      iconGlyph = "📸",
      defaultLimitMinutes = 15,
      limitMinutes = 15,
      category = "Social"
    ),
    SocialAppLimit(
      packageName = "com.zhiliaoapp.musically",
      appName = "TikTok",
      iconGlyph = "🎵",
      defaultLimitMinutes = 15,
      limitMinutes = 15,
      category = "Short Video"
    ),
    SocialAppLimit(
      packageName = "com.google.android.youtube",
      appName = "YouTube",
      iconGlyph = "▶️",
      defaultLimitMinutes = 30,
      limitMinutes = 30,
      category = "Video"
    ),
    SocialAppLimit(
      packageName = "com.twitter.android",
      appName = "X / Twitter",
      iconGlyph = "𝕏",
      defaultLimitMinutes = 15,
      limitMinutes = 15,
      category = "Social"
    ),
    SocialAppLimit(
      packageName = "com.reddit.frontpage",
      appName = "Reddit",
      iconGlyph = "🤖",
      defaultLimitMinutes = 20,
      limitMinutes = 20,
      category = "Forums"
    ),
    SocialAppLimit(
      packageName = "com.facebook.katana",
      appName = "Facebook",
      iconGlyph = "👥",
      defaultLimitMinutes = 20,
      limitMinutes = 20,
      category = "Social"
    ),
    SocialAppLimit(
      packageName = "com.snapchat.android",
      appName = "Snapchat",
      iconGlyph = "👻",
      defaultLimitMinutes = 15,
      limitMinutes = 15,
      category = "Social"
    )
  )

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  fun isAccessibilityServiceEnabled(context: Context): Boolean {
    if (FocuzAppBlockerService.isServiceRunning) {
      return true
    }

    if (getPrefs(context).getBoolean("accessibility_manually_granted", false)) {
      return true
    }

    val expectedServiceName = "${context.packageName}/${FocuzAppBlockerService::class.java.canonicalName}"
    val simpleName = FocuzAppBlockerService::class.java.simpleName
    val packageName = context.packageName

    // 1. Check via AccessibilityManager API
    try {
      val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? android.view.accessibility.AccessibilityManager
      val enabledList = am?.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
      if (enabledList != null) {
        for (service in enabledList) {
          val id = service.id ?: ""
          if (id.contains(packageName) && (id.contains(simpleName) || id.contains("BlockerService"))) {
            return true
          }
          val resolveInfo = service.resolveInfo
          if (resolveInfo?.serviceInfo?.packageName == packageName &&
            resolveInfo.serviceInfo?.name?.contains(simpleName) == true
          ) {
            return true
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error checking AccessibilityManager: ${e.message}")
    }

    // 2. Check via Settings.Secure
    try {
      val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
      ) ?: return false

      val colonSplitter = TextUtils.SimpleStringSplitter(':')
      colonSplitter.setString(enabledServices)
      while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(expectedServiceName, ignoreCase = true) ||
          componentName.contains(simpleName) ||
          (componentName.contains(packageName) && componentName.contains("BlockerService"))
        ) {
          return true
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error checking accessibility service: ${e.message}")
    }
    return false
  }

  fun setAccessibilityManuallyGranted(context: Context, granted: Boolean) {
    getPrefs(context).edit().putBoolean("accessibility_manually_granted", granted).apply()
  }

  fun hasUsageStatsPermission(context: Context): Boolean {
    return try {
      val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
      val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
      )
      mode == AppOpsManager.MODE_ALLOWED
    } catch (e: Exception) {
      Log.w(TAG, "Error checking usage stats permission: ${e.message}")
      false
    }
  }

  fun openAccessibilitySettingsIntent(): Intent {
    return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
  }

  fun openUsageAccessSettingsIntent(): Intent {
    return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
  }

  fun getMonitoredApps(context: Context): List<SocialAppLimit> {
    val prefs = getPrefs(context)
    checkDateReset(prefs)

    val usageStatsMap = if (hasUsageStatsPermission(context)) {
      queryTodayUsageMinutes(context)
    } else {
      emptyMap()
    }

    return DEFAULT_MONITORED_APPS.map { defaultApp ->
      val isEnabled = prefs.getBoolean(KEY_PREFIX_ENABLED + defaultApp.packageName, defaultApp.isEnabled)
      val limitMins = prefs.getInt(KEY_PREFIX_LIMIT + defaultApp.packageName, defaultApp.limitMinutes)
      val realUsage = usageStatsMap[defaultApp.packageName] ?: 0
      val manualUsage = prefs.getInt(KEY_PREFIX_MANUAL_USAGE + defaultApp.packageName, 0)
      val effectiveUsage = maxOf(realUsage, manualUsage)

      defaultApp.copy(
        isEnabled = isEnabled,
        limitMinutes = limitMins,
        usedTodayMinutes = effectiveUsage
      )
    }
  }

  fun setAppLimit(context: Context, packageName: String, limitMinutes: Int) {
    getPrefs(context).edit()
      .putInt(KEY_PREFIX_LIMIT + packageName, limitMinutes.coerceAtLeast(1))
      .apply()
  }

  fun setAppEnabled(context: Context, packageName: String, isEnabled: Boolean) {
    getPrefs(context).edit()
      .putBoolean(KEY_PREFIX_ENABLED + packageName, isEnabled)
      .apply()
  }

  fun addManualUsageMinutes(context: Context, packageName: String, minutesToAdd: Int) {
    val prefs = getPrefs(context)
    checkDateReset(prefs)
    val current = prefs.getInt(KEY_PREFIX_MANUAL_USAGE + packageName, 0)
    prefs.edit().putInt(KEY_PREFIX_MANUAL_USAGE + packageName, current + minutesToAdd).apply()
  }

  fun recordIntercept(context: Context) {
    val prefs = getPrefs(context)
    val current = prefs.getInt(KEY_INTERCEPTED_COUNT, 0)
    prefs.edit().putInt(KEY_INTERCEPTED_COUNT, current + 1).apply()
  }

  fun getTotalIntercepts(context: Context): Int {
    return getPrefs(context).getInt(KEY_INTERCEPTED_COUNT, 0)
  }

  fun isAppExceeded(context: Context, packageName: String): Pair<Boolean, SocialAppLimit?> {
    val apps = getMonitoredApps(context)
    val target = apps.find { it.packageName.equals(packageName, ignoreCase = true) } ?: return Pair(false, null)
    return Pair(target.isLimitReached, target)
  }

  private fun queryTodayUsageMinutes(context: Context): Map<String, Int> {
    val result = mutableMapOf<String, Int>()
    try {
      val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return result
      val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }
      val startTime = calendar.timeInMillis
      val endTime = System.currentTimeMillis()

      val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
      if (stats != null) {
        for (stat in stats) {
          val minutes = (stat.totalTimeInForeground / (1000 * 60)).toInt()
          if (minutes > 0) {
            result[stat.packageName] = minutes
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed querying usage stats: ${e.message}")
    }
    return result
  }

  private fun checkDateReset(prefs: SharedPreferences) {
    val todayKey = Calendar.getInstance().get(Calendar.DAY_OF_YEAR).toString()
    val lastDate = prefs.getString(KEY_LAST_USAGE_DATE, "")
    if (lastDate != todayKey) {
      val editor = prefs.edit()
      editor.putString(KEY_LAST_USAGE_DATE, todayKey)
      DEFAULT_MONITORED_APPS.forEach { app ->
        editor.remove(KEY_PREFIX_MANUAL_USAGE + app.packageName)
      }
      editor.apply()
    }
  }
}
