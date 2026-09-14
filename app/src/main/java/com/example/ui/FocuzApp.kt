package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.FocuzViewModel
import com.example.ui.components.IntentTagModal
import com.example.ui.components.ReplacementSuggestionCard
import com.example.ui.screens.AccountabilityScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.FocusSessionScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PermittedScrollScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary
import com.example.ui.theme.OffWhiteSubtle
import kotlinx.coroutines.launch

sealed class FocuzTab(
  val index: Int,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String
) {
  object Home : FocuzTab(0, "Dashboard", Icons.Filled.PieChart, Icons.Outlined.PieChart, "tab_home")
  object Session : FocuzTab(1, "Focus", Icons.Filled.HourglassTop, Icons.Outlined.HourglassTop, "tab_session")
  object Streaks : FocuzTab(2, "Streaks", Icons.Filled.LocalFireDepartment, Icons.Outlined.LocalFireDepartment, "tab_streaks")
  object Permitted : FocuzTab(3, "Limits", Icons.Filled.Schedule, Icons.Outlined.Schedule, "tab_permitted")
  object Profile : FocuzTab(4, "Profile", Icons.Filled.Person, Icons.Outlined.Person, "tab_profile")
}

@Composable
fun FocuzApp(
  viewModel: FocuzViewModel = viewModel()
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  LifecycleResumeEffect(Unit) {
    viewModel.refreshSocialBlockerState(context)
    onPauseOrDispose { }
  }

  val tabs = listOf(
    FocuzTab.Home,
    FocuzTab.Session,
    FocuzTab.Streaks,
    FocuzTab.Permitted,
    FocuzTab.Profile
  )

  // Show Auth Screen if user is not logged in and hasn't chosen guest mode
  val isUserSignedIn = uiState.currentUser != null || uiState.isGuestMode
  if (!isUserSignedIn) {
    AuthScreen(
      isLoading = uiState.isAuthLoading,
      errorMessage = uiState.authErrorMessage,
      onEmailSignIn = { email, pass -> viewModel.signInWithEmail(email, pass) },
      onEmailSignUp = { email, pass, name -> viewModel.signUpWithEmail(email, pass, name) },
      onSkipForNow = { viewModel.continueAsGuest() }
    )
    return
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = CharcoalBackground,
    contentColor = OffWhitePrimary,
    contentWindowInsets = WindowInsets.safeDrawing,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      // Bottom navigation strictly adhering to Charcoal & Off-White aesthetic
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(CharcoalSurface)
          .border(1.dp, CharcoalBorder)
          .windowInsetsPadding(WindowInsets.navigationBars)
      ) {
        NavigationBar(
          containerColor = CharcoalSurface,
          contentColor = OffWhitePrimary,
          tonalElevation = 0.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          tabs.forEach { tab ->
            val isSelected = selectedTabIndex == tab.index
            NavigationBarItem(
              selected = isSelected,
              onClick = { selectedTabIndex = tab.index },
              icon = {
                Icon(
                  imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                  contentDescription = tab.label,
                  modifier = Modifier.size(22.dp)
                )
              },
              label = {
                Text(
                  text = tab.label,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CharcoalBackground,
                selectedTextColor = OffWhitePrimary,
                indicatorColor = OffWhitePrimary,
                unselectedIconColor = OffWhiteMuted,
                unselectedTextColor = OffWhiteMuted
              ),
              modifier = Modifier.testTag(tab.testTag)
            )
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTabIndex) {
        0 -> HomeScreen(
          uiState = uiState,
          onOpenIntentPopup = { appName -> viewModel.openIntentModal(appName) },
          onOpenReplacementCard = { viewModel.openReplacementCard() },
          onNavigateToFocusSession = { selectedTabIndex = 1 },
          onNavigateToLimits = { selectedTabIndex = 3 },
          onOpenProfile = { selectedTabIndex = 4 },
          onSignOut = { viewModel.signOut() }
        )
        1 -> FocusSessionScreen(
          uiState = uiState,
          onStartSession = { viewModel.startSession() },
          onPauseSession = { viewModel.pauseSession() },
          onResumeSession = { viewModel.resumeSession() },
          onEndSession = {
            viewModel.endSession()
            coroutineScope.launch {
              snackbarHostState.showSnackbar("Nice session! Real focus minutes recorded.")
            }
          },
          onSelectMode = { mode -> viewModel.setFocusMode(mode) },
          onSetDuration = { mins -> viewModel.setCustomSessionMinutes(mins) }
        )
        2 -> AccountabilityScreen(
          uiState = uiState,
          onSendCheer = {
            viewModel.sendFriendCheer()
            coroutineScope.launch {
              snackbarHostState.showSnackbar("Cheer sent! Quiet momentum shared.")
            }
          }
        )
        3 -> PermittedScrollScreen(
          uiState = uiState,
          onSelectDuration = { duration -> viewModel.setWindowDuration(duration) },
          onUpdateAppLimit = { pkg, limitMins -> viewModel.updateSocialAppLimit(context, pkg, limitMins) },
          onToggleAppEnabled = { pkg, isEnabled -> viewModel.toggleSocialAppEnabled(context, pkg, isEnabled) },
          onTestBlock = { pkg -> viewModel.triggerTestBlock(context, pkg) },
          onRefreshPermissions = { viewModel.refreshSocialBlockerState(context) },
          onDismissAccessibilityBanner = { viewModel.dismissAccessibilityBanner(context) }
        )
        4 -> ProfileScreen(
          uiState = uiState,
          onSaveProfile = { name, photoUrl, bio, goalMins ->
            viewModel.updateUserProfile(name, photoUrl, bio, goalMins)
            coroutineScope.launch {
              snackbarHostState.showSnackbar("Profile saved successfully!")
            }
          },
          onBack = { selectedTabIndex = 0 },
          onSignOut = { viewModel.signOut() }
        )
      }

      // Profile Modal overlay if opened via modal action
      if (uiState.showProfileModal) {
        Dialog(
          onDismissRequest = { viewModel.dismissProfileModal() },
          properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(CharcoalBackground)
          ) {
            ProfileScreen(
              uiState = uiState,
              onSaveProfile = { name, photoUrl, bio, goalMins ->
                viewModel.updateUserProfile(name, photoUrl, bio, goalMins)
                viewModel.dismissProfileModal()
                coroutineScope.launch {
                  snackbarHostState.showSnackbar("Profile updated!")
                }
              },
              onBack = { viewModel.dismissProfileModal() },
              onSignOut = {
                viewModel.dismissProfileModal()
                viewModel.signOut()
              }
            )
          }
        }
      }

      // Lightweight Intent-Tag Modal (Bottom sheet dialog)
      IntentTagModal(
        isVisible = uiState.showIntentModal,
        appName = uiState.intentTargetApp,
        selectedReason = uiState.selectedIntentReason,
        onReasonSelected = { reason ->
          viewModel.selectIntentReason(reason)
          coroutineScope.launch {
            snackbarHostState.showSnackbar("Intent noted: ${reason.label}. Breathe easy.")
          }
        },
        onMindfulMinutesSelected = { minutes ->
          viewModel.confirmIntentWithTimeLimit(minutes)
          coroutineScope.launch {
            snackbarHostState.showSnackbar("5 mindful minutes activated. Enjoy with awareness.")
          }
        },
        onSuggestAlternative = {
          viewModel.dismissIntentModal()
          viewModel.openReplacementCard()
        },
        onCloseAppConsciously = {
          viewModel.dismissIntentModal()
          coroutineScope.launch {
            snackbarHostState.showSnackbar("Moment won! Phone placed face-down with zero guilt 🔥")
          }
        },
        onDismiss = { viewModel.dismissIntentModal() }
      )

      // Reusable Replacement Suggestion Card Overlay
      if (uiState.showReplacementCard) {
        val currentActivity = uiState.replacementActivities[uiState.currentReplacementIndex]

        Dialog(
          onDismissRequest = { viewModel.dismissReplacementCard() },
          properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(CharcoalBackground.copy(alpha = 0.85f))
              .clickable { viewModel.dismissReplacementCard() }
              .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier.clickable(enabled = false, onClick = {})
            ) {
              ReplacementSuggestionCard(
                activity = currentActivity,
                onAccept = {
                  viewModel.acceptReplacementActivity()
                  coroutineScope.launch {
                    snackbarHostState.showSnackbar("Great choice! Swapped doomscroll for a quick recharge.")
                  }
                },
                onDismiss = { viewModel.dismissReplacementCard() },
                onNextActivity = { viewModel.nextReplacementActivity() },
                isOverlay = true
              )
            }
          }
        }
      }
    }
  }
}
