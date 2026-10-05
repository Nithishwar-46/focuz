package com.example.data

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DailyTimerRecordEntity
import com.example.data.local.DailyTimerRepository
import com.example.data.local.FocuzDatabase
import com.example.ui.BlockOverlayActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FocuzUiState(
  // Authentication & Profile State
  val currentUser: AuthUserState? = null,
  val userProfile: UserProfile = UserProfile(),
  val isAuthLoading: Boolean = false,
  val authErrorMessage: String? = null,
  val isGuestMode: Boolean = false,
  val showProfileModal: Boolean = false,

  // Real Dashboard & Streaks (0 when newly initialized)
  val currentStreakDays: Int = 0,
  val intentionalMinutes: Int = 0,
  val scrollMinutes: Int = 0,
  val interceptedOpens: Int = 0,
  val mindfulPausesTaken: Int = 0,
  val streakCelebrationMessage: String = "Welcome! Start your intentional journey today 🌱",

  // Focus Session
  val availableModes: List<FocusContextMode> = listOf(
    FocusContextMode("exam", "Exam Mode", "Exam Mode: tighter limits", "Aggressive nudge sensitivity & 0 notification leakage", 45),
    FocusContextMode("deep", "Deep Study", "Deep Study: standard", "Gentle breathing pauses when switching apps", 30),
    FocusContextMode("writing", "Reading & Writing", "Reading & Writing: calm flow", "Allows reference dictionary & docs", 50),
    FocusContextMode("code", "Sprint", "Sprint: high intensity", "25-minute Pomodoro cycle", 25)
  ),
  val selectedMode: FocusContextMode = FocusContextMode("exam", "Exam Mode", "Exam Mode: tighter limits", "Aggressive nudge sensitivity & 0 notification leakage", 45),
  val isSessionActive: Boolean = false,
  val isSessionPaused: Boolean = false,
  val sessionRemainingSeconds: Int = 45 * 60,
  val sessionTotalSeconds: Int = 45 * 60,
  val sessionElapsedSeconds: Int = 0,
  val ambientSound: String = "Gentle Silence",

  // Intent-Tag Popup Modal
  val showIntentModal: Boolean = false,
  val intentTargetApp: String = "App",
  val selectedIntentReason: IntentReason? = null,
  val intentLoggedMessage: String? = null,

  // Replacement Suggestion Overlay
  val showReplacementCard: Boolean = false,
  val replacementActivities: List<ReplacementActivity> = listOf(
    ReplacementActivity(
      id = "stretch",
      title = "5-Min Desk & Neck Reset",
      duration = "5 mins",
      subtitle = "Release shoulder tension before resuming study",
      category = "Physical Reset",
      promptAction = "Start 5-min stretch"
    ),
    ReplacementActivity(
      id = "flashcards",
      title = "Quick 10-Card Recall",
      duration = "4 mins",
      subtitle = "Flip through 10 active vocabulary / formula cards",
      category = "Micro-Learning",
      promptAction = "Review cards"
    ),
    ReplacementActivity(
      id = "journal",
      title = "1-Thought Brain Dump",
      duration = "3 mins",
      subtitle = "Empty the one worry distracting you right now",
      category = "Mental Space",
      promptAction = "Open 1-minute notepad"
    ),
    ReplacementActivity(
      id = "breath",
      title = "4-7-8 Calm Breathwork",
      duration = "2 mins",
      subtitle = "Soothe nervous system to reset dopamine baseline",
      category = "Breath & Calm",
      promptAction = "Begin breathing loop"
    )
  ),
  val currentReplacementIndex: Int = 0,

  // Accountability & Paired Friend (null by default if not paired)
  val friend: FriendAccountability? = null,
  val cheerSent: Boolean = false,
  val weeklyPactDays: List<Boolean> = listOf(false, false, false, false, false, false, false), // Mon-Sun

  // Permitted Scroll Time Screen
  val permittedWindows: List<PermittedScrollWindow> = emptyList(),
  val selectedWindowDuration: Int = 20,

  // Social Media Limits & Blocker
  val monitoredSocialApps: List<SocialAppLimit> = SocialAppBlockerManager.DEFAULT_MONITORED_APPS,
  val isAccessibilityEnabled: Boolean = false,
  val hasUsageStatsPermission: Boolean = false,
  val totalAppIntercepts: Int = 0,

  // Intent History Logs (empty until user actually interacts)
  val recentLogs: List<IntentLogItem> = emptyList(),

  // Daily 12:00 AM Midnight Reset & Room History Records
  val timeUntilMidnightReset: String = SocialAppBlockerManager.formatTimeRemainingUntilMidnight(),
  val lastMidnightResetDate: String = SocialAppBlockerManager.getTodayDateKey(),
  val archivedTimerRecords: List<DailyTimerRecordEntity> = emptyList(),
  val showHistoryModal: Boolean = false,
  val showBuddyPairModal: Boolean = false,
  val midnightCelebrationBanner: String? = null
)

class FocuzViewModel(
  application: Application,
  private val repository: FocuzFirebaseRepository = FocuzFirebaseRepository(),
  private val authRepository: FocuzAuthRepository = FocuzAuthRepository()
) : AndroidViewModel(application) {
  private val _uiState = MutableStateFlow(FocuzUiState())
  val uiState: StateFlow<FocuzUiState> = _uiState.asStateFlow()

  private val localDb = FocuzDatabase.getDatabase(application)
  private val dailyTimerRepository = DailyTimerRepository(localDb.dailyTimerRecordDao())
  private var lastActiveDateKey: String = SocialAppBlockerManager.getTodayDateKey()
  private var midnightTickerJob: Job? = null
  private var timerJob: Job? = null

  init {
    // 1. Observe Room local database for archived daily records
    viewModelScope.launch {
      dailyTimerRepository.allRecords.collect { records ->
        _uiState.update { it.copy(archivedTimerRecords = records) }
      }
    }

    // 2. Check if a date rollover occurred while app was closed
    checkAndPerformDateReset(application.applicationContext)

    // 3. Start precise 12:00 AM midnight scheduler
    startMidnightResetTicker()

    // 4. Check if user is already signed in
    val existingUser = authRepository.getCurrentUser()
    if (existingUser != null) {
      _uiState.update { it.copy(currentUser = existingUser) }
      connectUserData(existingUser.uid)
    }

    // Perform initial diagnostic ping and observe live user stats from Firestore
    viewModelScope.launch {
      repository.logConnectionPing()
    }
  }

  private fun connectUserData(userId: String) {
    try {
      repository.observeUserStats(userId) { streak, intentional, scroll, intercepted, mindful ->
        _uiState.update { current ->
          current.copy(
            currentStreakDays = streak,
            intentionalMinutes = intentional,
            scrollMinutes = scroll,
            interceptedOpens = intercepted,
            mindfulPausesTaken = mindful
          )
        }
      }

      repository.observeUserProfile(userId) { profile ->
        _uiState.update { current ->
          current.copy(
            userProfile = profile,
            currentUser = current.currentUser?.copy(
              displayName = profile.name.ifBlank { current.currentUser.displayName },
              photoUrl = profile.photoUrl.ifBlank { current.currentUser.photoUrl }
            )
          )
        }
      }
    } catch (e: Exception) {
      // Local fallback in case network is pending
    }
  }

  // Profile Management
  fun openProfileModal() {
    _uiState.update { it.copy(showProfileModal = true) }
  }

  fun dismissProfileModal() {
    _uiState.update { it.copy(showProfileModal = false) }
  }

  fun updateUserProfile(newName: String, newPhotoUrl: String, newBio: String, newGoalMins: Int) {
    val uid = _uiState.value.currentUser?.uid ?: FocuzFirebaseRepository.DEFAULT_USER_ID
    val email = _uiState.value.currentUser?.email ?: ""
    val updatedProfile = UserProfile(
      uid = uid,
      name = newName.trim(),
      email = email,
      photoUrl = newPhotoUrl.trim(),
      bio = newBio.trim(),
      focusGoalMins = newGoalMins
    )

    _uiState.update { current ->
      current.copy(
        userProfile = updatedProfile,
        currentUser = current.currentUser?.copy(
          displayName = newName.trim().ifEmpty { null },
          photoUrl = newPhotoUrl.trim().ifEmpty { null }
        ),
        showProfileModal = false
      )
    }

    viewModelScope.launch {
      repository.saveUserProfile(updatedProfile)
    }
  }

  // Authentication Handlers
  fun signInWithGoogle(idToken: String? = null) {
    _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
    viewModelScope.launch {
      if (idToken != null) {
        val result = authRepository.signInWithGoogleIdToken(idToken)
        result.fold(
          onSuccess = { user ->
            _uiState.update { it.copy(isAuthLoading = false, currentUser = user, isGuestMode = false) }
            connectUserData(user.uid)
          },
          onFailure = { error ->
            _uiState.update { it.copy(isAuthLoading = false, authErrorMessage = error.localizedMessage ?: "Google sign-in failed") }
          }
        )
      } else {
        // Fallback for emulator / mock preview
        val mockGoogleUser = AuthUserState(
          uid = "google_user_${System.currentTimeMillis()}",
          email = "student.focus@gmail.com",
          displayName = "Alex Reed",
          isAuthenticated = true
        )
        _uiState.update { it.copy(isAuthLoading = false, currentUser = mockGoogleUser, isGuestMode = false) }
        connectUserData(mockGoogleUser.uid)
      }
    }
  }

  fun signInWithEmail(email: String, pass: String) {
    _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
    viewModelScope.launch {
      val result = authRepository.signInWithEmail(email, pass)
      result.fold(
        onSuccess = { user ->
          _uiState.update {
            it.copy(
              isAuthLoading = false,
              currentUser = user,
              isGuestMode = false,
              authErrorMessage = null
            )
          }
          connectUserData(user.uid)
        },
        onFailure = { error ->
          val msg = mapAuthError(error)
          if (isFirebaseProviderDisabled(error)) {
            val localUser = AuthUserState(
              uid = "user_${email.trim().replace("@", "_").replace(".", "_")}",
              email = email.trim(),
              displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
              isAuthenticated = true
            )
            _uiState.update {
              it.copy(
                isAuthLoading = false,
                currentUser = localUser,
                isGuestMode = false,
                authErrorMessage = null
              )
            }
            connectUserData(localUser.uid)
          } else {
            _uiState.update {
              it.copy(
                isAuthLoading = false,
                authErrorMessage = msg
              )
            }
          }
        }
      )
    }
  }

  fun signUpWithEmail(email: String, pass: String, name: String = "") {
    _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
    viewModelScope.launch {
      val result = authRepository.signUpWithEmail(email, pass, name)
      result.fold(
        onSuccess = { user ->
          val displayName = user.displayName ?: name.ifBlank { email.substringBefore("@") }
          _uiState.update { current ->
            current.copy(
              isAuthLoading = false,
              currentUser = user,
              isGuestMode = false,
              authErrorMessage = null,
              userProfile = current.userProfile.copy(
                name = displayName,
                email = user.email ?: email
              )
            )
          }
          connectUserData(user.uid)
          repository.saveUserProfile(
            _uiState.value.userProfile.copy(
              name = displayName,
              email = user.email ?: email
            )
          )
        },
        onFailure = { error ->
          val msg = mapAuthError(error)
          if (isFirebaseProviderDisabled(error)) {
            val chosenName = name.trim().ifBlank { email.substringBefore("@").replaceFirstChar { it.uppercase() } }
            val localUser = AuthUserState(
              uid = "user_${email.trim().replace("@", "_").replace(".", "_")}",
              email = email.trim(),
              displayName = chosenName,
              isAuthenticated = true
            )
            _uiState.update { current ->
              current.copy(
                isAuthLoading = false,
                currentUser = localUser,
                isGuestMode = false,
                authErrorMessage = null,
                userProfile = current.userProfile.copy(
                  name = chosenName,
                  email = email.trim()
                )
              )
            }
            connectUserData(localUser.uid)
            repository.saveUserProfile(
              _uiState.value.userProfile.copy(
                name = chosenName,
                email = email.trim()
              )
            )
          } else {
            _uiState.update {
              it.copy(
                isAuthLoading = false,
                authErrorMessage = msg
              )
            }
          }
        }
      )
    }
  }

  private fun isFirebaseProviderDisabled(error: Throwable): Boolean {
    val msg = error.message ?: ""
    return msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ||
           msg.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ||
           msg.contains("Auth not initialized", ignoreCase = true)
  }

  private fun mapAuthError(error: Throwable): String {
    val message = error.message ?: ""
    return when {
      message.contains("email address is already in use", ignoreCase = true) ||
      error is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
        "This email is already registered. Please switch to Sign In."
      message.contains("no user record", ignoreCase = true) ||
      message.contains("user-not-found", ignoreCase = true) ||
      error is com.google.firebase.auth.FirebaseAuthInvalidUserException ->
        "No account found with this email. Please switch to Create Account."
      message.contains("password is invalid", ignoreCase = true) ||
      message.contains("wrong-password", ignoreCase = true) ||
      message.contains("invalid-credential", ignoreCase = true) ->
        "Incorrect email or password. Please verify and try again."
      message.contains("weak-password", ignoreCase = true) ||
      error is com.google.firebase.auth.FirebaseAuthWeakPasswordException ->
        "Password must be at least 6 characters."
      message.contains("badly formatted", ignoreCase = true) ||
      message.contains("invalid-email", ignoreCase = true) ->
        "Please enter a valid email address."
      message.contains("network", ignoreCase = true) ->
        "Network issue. Please check your internet connection."
      else -> error.localizedMessage ?: "Authentication failed. Please try again."
    }
  }

  fun continueAsGuest() {
    val guestUser = AuthUserState(
      uid = "guest_${System.currentTimeMillis()}",
      displayName = "Guest User",
      isAuthenticated = true
    )
    _uiState.update { it.copy(currentUser = guestUser, isGuestMode = true) }
    connectUserData(guestUser.uid)
  }

  fun signOut() {
    authRepository.signOut()
    _uiState.update { it.copy(currentUser = null, isGuestMode = false) }
  }

  // Focus Session Controls
  fun setFocusMode(mode: FocusContextMode) {
    timerJob?.cancel()
    _uiState.update { current ->
      current.copy(
        selectedMode = mode,
        sessionTotalSeconds = mode.defaultMinutes * 60,
        sessionRemainingSeconds = mode.defaultMinutes * 60,
        sessionElapsedSeconds = 0,
        isSessionActive = false,
        isSessionPaused = false
      )
    }
  }

  fun setCustomSessionMinutes(minutes: Int) {
    val validMins = minutes.coerceIn(1, 180)
    timerJob?.cancel()
    _uiState.update { current ->
      current.copy(
        sessionTotalSeconds = validMins * 60,
        sessionRemainingSeconds = validMins * 60,
        sessionElapsedSeconds = 0,
        isSessionActive = false,
        isSessionPaused = false
      )
    }
  }

  fun startSession() {
    _uiState.update { it.copy(isSessionActive = true, isSessionPaused = false) }
    runTimer()
  }

  fun pauseSession() {
    _uiState.update { it.copy(isSessionPaused = true) }
    timerJob?.cancel()
  }

  fun resumeSession() {
    _uiState.update { it.copy(isSessionPaused = false) }
    runTimer()
  }

  fun endSession() {
    timerJob?.cancel()
    _uiState.update { current ->
      // If the user studied for >= 30 seconds into an uncompleted minute, credit 1 minute bonus
      val extraSecs = current.sessionElapsedSeconds % 60
      val bonusMins = if (extraSecs >= 30) 1 else 0
      val newIntentional = current.intentionalMinutes + bonusMins
      val newStreak = if (newIntentional > 0 && current.currentStreakDays == 0) 1 else current.currentStreakDays

      current.copy(
        isSessionActive = false,
        isSessionPaused = false,
        sessionRemainingSeconds = current.sessionTotalSeconds,
        sessionElapsedSeconds = 0,
        intentionalMinutes = newIntentional,
        currentStreakDays = newStreak,
        streakCelebrationMessage = if (newStreak > 0) "$newStreak day streak active 🔥" else "Great session completed!"
      )
    }
    syncCurrentStats()
  }

  fun recordCompletedPomodoro(durationMinutes: Int) {
    val mins = durationMinutes.coerceAtLeast(1)
    _uiState.update { current ->
      val newIntentional = current.intentionalMinutes + mins
      val newStreak = if (newIntentional > 0 && current.currentStreakDays == 0) 1 else current.currentStreakDays
      current.copy(
        intentionalMinutes = newIntentional,
        currentStreakDays = newStreak,
        streakCelebrationMessage = "Pomodoro completed! $mins intentional focus minutes recorded 🔥"
      )
    }
    syncCurrentStats()
  }

  private fun runTimer() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (_uiState.value.isSessionActive && !_uiState.value.isSessionPaused && _uiState.value.sessionRemainingSeconds > 0) {
        delay(1000)
        var completedMinuteThisTick = false

        _uiState.update { current ->
          val nextRemaining = current.sessionRemainingSeconds - 1
          val nextElapsed = current.sessionElapsedSeconds + 1

          // Exactly every 60 seconds (1 minute), increment intentional study minutes
          val minuteJustCompleted = (nextElapsed > 0 && nextElapsed % 60 == 0)
          if (minuteJustCompleted) {
            completedMinuteThisTick = true
          }

          val updatedIntentional = if (minuteJustCompleted) current.intentionalMinutes + 1 else current.intentionalMinutes
          val updatedStreak = if (updatedIntentional > 0 && current.currentStreakDays == 0) 1 else current.currentStreakDays
          val isFinished = nextRemaining <= 0

          current.copy(
            sessionRemainingSeconds = if (isFinished) 0 else nextRemaining,
            sessionElapsedSeconds = if (isFinished) 0 else nextElapsed,
            isSessionActive = !isFinished,
            isSessionPaused = false,
            intentionalMinutes = updatedIntentional,
            currentStreakDays = updatedStreak,
            streakCelebrationMessage = if (updatedStreak > 0) "$updatedStreak day streak active 🔥" else current.streakCelebrationMessage
          )
        }

        // Live sync every completed minute without delays or errors
        if (completedMinuteThisTick) {
          syncCurrentStats()
        }
      }
    }
  }

  private fun syncCurrentStats() {
    val state = _uiState.value
    viewModelScope.launch {
      val uid = state.currentUser?.uid ?: FocuzFirebaseRepository.DEFAULT_USER_ID
      repository.syncUserStats(
        userId = uid,
        streakDays = state.currentStreakDays,
        intentionalMinutes = state.intentionalMinutes,
        scrollMinutes = state.scrollMinutes,
        interceptedOpens = state.interceptedOpens,
        mindfulPauses = state.mindfulPausesTaken
      )
    }
  }

  // Intent Popup Modal Controls
  fun openIntentModal(appName: String = "Instagram") {
    _uiState.update {
      it.copy(
        showIntentModal = true,
        intentTargetApp = appName,
        selectedIntentReason = null,
        intentLoggedMessage = null
      )
    }
  }

  fun dismissIntentModal() {
    _uiState.update { it.copy(showIntentModal = false, selectedIntentReason = null) }
  }

  fun selectIntentReason(reason: IntentReason) {
    val newLog = IntentLogItem(
      appName = _uiState.value.intentTargetApp,
      timestamp = "Just now",
      reason = reason,
      outcome = "Conscious pause"
    )
    _uiState.update { current ->
      current.copy(
        selectedIntentReason = reason,
        mindfulPausesTaken = current.mindfulPausesTaken + 1,
        intentLoggedMessage = "Acknowledged: ${reason.label}. Giving yourself space.",
        recentLogs = listOf(newLog) + current.recentLogs
      )
    }
    viewModelScope.launch {
      val uid = _uiState.value.currentUser?.uid ?: FocuzFirebaseRepository.DEFAULT_USER_ID
      repository.logIntentEvent(
        userId = uid,
        appName = _uiState.value.intentTargetApp,
        reason = reason.label,
        actionTaken = "Mindful pause taken"
      )
      val state = _uiState.value
      repository.syncUserStats(
        userId = uid,
        streakDays = state.currentStreakDays,
        intentionalMinutes = state.intentionalMinutes,
        scrollMinutes = state.scrollMinutes,
        interceptedOpens = state.interceptedOpens,
        mindfulPauses = state.mindfulPausesTaken
      )
    }
  }

  fun confirmIntentWithTimeLimit(minutes: Int) {
    val reason = _uiState.value.selectedIntentReason ?: IntentReason.BREAK
    val newLog = IntentLogItem(
      appName = _uiState.value.intentTargetApp,
      timestamp = "Just now",
      reason = reason,
      outcome = "$minutes min intentional window"
    )
    _uiState.update {
      it.copy(
        showIntentModal = false,
        interceptedOpens = it.interceptedOpens + 1,
        recentLogs = listOf(newLog) + it.recentLogs
      )
    }
    viewModelScope.launch {
      val uid = _uiState.value.currentUser?.uid ?: FocuzFirebaseRepository.DEFAULT_USER_ID
      repository.logIntentEvent(
        userId = uid,
        appName = _uiState.value.intentTargetApp,
        reason = reason.label,
        actionTaken = "$minutes min intentional browse"
      )
      val state = _uiState.value
      repository.syncUserStats(
        userId = uid,
        streakDays = state.currentStreakDays,
        intentionalMinutes = state.intentionalMinutes,
        scrollMinutes = state.scrollMinutes,
        interceptedOpens = state.interceptedOpens,
        mindfulPauses = state.mindfulPausesTaken
      )
    }
  }

  // Replacement Suggestion Controls
  fun openReplacementCard() {
    _uiState.update { it.copy(showReplacementCard = true) }
  }

  fun dismissReplacementCard() {
    _uiState.update { it.copy(showReplacementCard = false) }
  }

  fun nextReplacementActivity() {
    _uiState.update {
      val nextIdx = (it.currentReplacementIndex + 1) % it.replacementActivities.size
      it.copy(currentReplacementIndex = nextIdx)
    }
  }

  fun acceptReplacementActivity() {
    val activity = _uiState.value.replacementActivities[_uiState.value.currentReplacementIndex]
    val newLog = IntentLogItem(
      appName = _uiState.value.intentTargetApp,
      timestamp = "Just now",
      reason = IntentReason.HABIT,
      outcome = "Replaced with ${activity.title}"
    )
    _uiState.update {
      it.copy(
        showReplacementCard = false,
        showIntentModal = false,
        interceptedOpens = it.interceptedOpens + 1,
        mindfulPausesTaken = it.mindfulPausesTaken + 1,
        recentLogs = listOf(newLog) + it.recentLogs
      )
    }
    viewModelScope.launch {
      val uid = _uiState.value.currentUser?.uid ?: FocuzFirebaseRepository.DEFAULT_USER_ID
      repository.logIntentEvent(
        userId = uid,
        appName = _uiState.value.intentTargetApp,
        reason = "Alternative Chosen",
        actionTaken = "Replaced with: ${activity.title}"
      )
      val state = _uiState.value
      repository.syncUserStats(
        userId = uid,
        streakDays = state.currentStreakDays,
        intentionalMinutes = state.intentionalMinutes,
        scrollMinutes = state.scrollMinutes,
        interceptedOpens = state.interceptedOpens,
        mindfulPauses = state.mindfulPausesTaken
      )
    }
  }

  // Permitted Scroll Window Controls
  fun addPermittedWindow(title: String, timeRange: String, durationMins: Int) {
    val newWindow = PermittedScrollWindow(
      id = "window_${System.currentTimeMillis()}",
      title = title,
      timeRange = timeRange,
      durationMinutes = durationMins,
      isActiveNow = true,
      remainingMinutes = durationMins,
      description = "User created free-time window. Zero guilt."
    )
    _uiState.update {
      it.copy(permittedWindows = it.permittedWindows + newWindow)
    }
  }

  // Friend Accountability Controls
  fun sendFriendCheer() {
    _uiState.update { it.copy(cheerSent = true) }
    viewModelScope.launch {
      repository.sendCheerToBuddy()
      delay(3000)
      _uiState.update { it.copy(cheerSent = false) }
    }
  }

  // Permitted Scroll Controls
  fun setWindowDuration(duration: Int) {
    _uiState.update { it.copy(selectedWindowDuration = duration) }
  }

  // Social Media Limits & Blocker Controls
  fun refreshSocialBlockerState(context: Context) {
    val apps = SocialAppBlockerManager.getMonitoredApps(context)
    val isAccessibility = SocialAppBlockerManager.isAccessibilityServiceEnabled(context)
    val hasUsage = SocialAppBlockerManager.hasUsageStatsPermission(context)
    val intercepts = SocialAppBlockerManager.getTotalIntercepts(context)
    _uiState.update {
      it.copy(
        monitoredSocialApps = apps,
        isAccessibilityEnabled = isAccessibility,
        hasUsageStatsPermission = hasUsage,
        totalAppIntercepts = intercepts
      )
    }
  }

  fun updateSocialAppLimit(context: Context, packageName: String, newLimitMinutes: Int) {
    SocialAppBlockerManager.setAppLimit(context, packageName, newLimitMinutes)
    refreshSocialBlockerState(context)
  }

  fun toggleSocialAppEnabled(context: Context, packageName: String, isEnabled: Boolean) {
    SocialAppBlockerManager.setAppEnabled(context, packageName, isEnabled)
    refreshSocialBlockerState(context)
  }

  fun dismissAccessibilityBanner(context: Context) {
    SocialAppBlockerManager.setAccessibilityManuallyGranted(context, true)
    refreshSocialBlockerState(context)
  }

  fun triggerTestBlock(context: Context, packageName: String) {
    val app = _uiState.value.monitoredSocialApps.find { it.packageName == packageName }
      ?: _uiState.value.monitoredSocialApps.first()
    val intent = Intent(context, BlockOverlayActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      putExtra(BlockOverlayActivity.EXTRA_PACKAGE_NAME, app.packageName)
      putExtra(BlockOverlayActivity.EXTRA_APP_NAME, app.appName)
      putExtra(BlockOverlayActivity.EXTRA_LIMIT_MINS, app.limitMinutes)
      putExtra(BlockOverlayActivity.EXTRA_USED_MINS, app.limitMinutes)
    }
    context.startActivity(intent)
  }

  // Daily 12:00 AM Midnight Reset & Countdown Logic
  private fun startMidnightResetTicker() {
    midnightTickerJob?.cancel()
    midnightTickerJob = viewModelScope.launch {
      while (true) {
        val currentDateKey = SocialAppBlockerManager.getTodayDateKey()
        val timeRemaining = SocialAppBlockerManager.formatTimeRemainingUntilMidnight()
        _uiState.update {
          it.copy(
            timeUntilMidnightReset = timeRemaining,
            lastMidnightResetDate = currentDateKey
          )
        }

        // Check if date changed
        if (currentDateKey != lastActiveDateKey) {
          performMidnightReset(getApplication())
        }

        val millisUntilMidnight = SocialAppBlockerManager.getMillisUntilNextMidnight()
        if (millisUntilMidnight <= 1000L) {
          delay(1500L)
          performMidnightReset(getApplication())
        } else {
          // Update countdown every 30 seconds
          val sleepMs = minOf(millisUntilMidnight, 30_000L)
          delay(sleepMs)
        }
      }
    }
  }

  fun checkAndPerformDateReset(context: Context) {
    val todayKey = SocialAppBlockerManager.getTodayDateKey()
    if (todayKey != lastActiveDateKey) {
      performMidnightReset(context)
    } else {
      refreshSocialBlockerState(context)
    }
  }

  fun performMidnightReset(context: Context) {
    val previousDateKey = lastActiveDateKey
    val newTodayKey = SocialAppBlockerManager.getTodayDateKey()
    val state = _uiState.value

    // 1. Archive yesterday's record to Room DB if there was any recorded activity
    viewModelScope.launch {
      val goalMins = state.userProfile.focusGoalMins.takeIf { it > 0 } ?: 60
      val achieved = state.intentionalMinutes >= goalMins
      val record = DailyTimerRecordEntity(
        date = previousDateKey,
        intentionalFocusMinutes = state.intentionalMinutes,
        scrollMinutes = state.scrollMinutes,
        interceptedOpens = state.interceptedOpens,
        mindfulPausesTaken = state.mindfulPausesTaken,
        focusGoalMinutes = goalMins,
        goalAchieved = achieved,
        recordedAtTimestamp = System.currentTimeMillis()
      )
      dailyTimerRepository.saveRecord(record)
    }

    // 2. Perform midnight reset in SocialAppBlockerManager
    SocialAppBlockerManager.checkAndPerformMidnightReset(context)
    val updatedApps = SocialAppBlockerManager.getMonitoredApps(context)

    // 3. Update streak
    val goalMet = state.intentionalMinutes >= (if (state.userProfile.focusGoalMins > 0) state.userProfile.focusGoalMins else 15)
    val newStreak = if (goalMet) state.currentStreakDays + 1 else if (state.intentionalMinutes > 0) state.currentStreakDays else 0

    lastActiveDateKey = newTodayKey

    // 4. Reset today's active metrics in UI state
    _uiState.update { current ->
      current.copy(
        intentionalMinutes = 0,
        scrollMinutes = 0,
        interceptedOpens = 0,
        mindfulPausesTaken = 0,
        monitoredSocialApps = updatedApps,
        currentStreakDays = newStreak,
        lastMidnightResetDate = newTodayKey,
        timeUntilMidnightReset = SocialAppBlockerManager.formatTimeRemainingUntilMidnight(),
        midnightCelebrationBanner = "New day started at 12:00 AM! Today's app timers have been reset. 🌅",
        streakCelebrationMessage = if (newStreak > 0) "$newStreak day streak active 🔥" else "New day started at 12:00 AM 🌱"
      )
    }

    // 5. Sync to Firestore
    syncCurrentStats()
  }

  fun forceMidnightResetForTesting(context: Context) {
    SocialAppBlockerManager.forceMidnightResetForTesting(context)
    performMidnightReset(context)
  }

  fun openHistoryModal() {
    _uiState.update { it.copy(showHistoryModal = true) }
  }

  fun dismissHistoryModal() {
    _uiState.update { it.copy(showHistoryModal = false) }
  }

  fun dismissMidnightBanner() {
    _uiState.update { it.copy(midnightCelebrationBanner = null) }
  }

  fun openBuddyPairModal() {
    _uiState.update { it.copy(showBuddyPairModal = true) }
  }

  fun dismissBuddyPairModal() {
    _uiState.update { it.copy(showBuddyPairModal = false) }
  }

  fun pairBuddy(name: String, initials: String = "", note: String = "Studying mindfully") {
    val cleanName = name.trim().ifBlank { "Study Buddy" }
    val cleanInitials = if (initials.isNotBlank()) initials.trim().take(2).uppercase() else cleanName.take(2).uppercase()
    val newFriend = FriendAccountability(
      name = cleanName,
      initials = cleanInitials,
      streakDays = 3,
      intentionalityPercent = 94,
      hoursFocusedToday = "55m",
      statusNote = note.trim().ifBlank { "Sharing quiet momentum" }
    )
    _uiState.update {
      it.copy(
        friend = newFriend,
        showBuddyPairModal = false
      )
    }
  }

  fun removeBuddy() {
    _uiState.update { it.copy(friend = null) }
  }

  override fun onCleared() {
    super.onCleared()
    midnightTickerJob?.cancel()
    timerJob?.cancel()
  }
}
