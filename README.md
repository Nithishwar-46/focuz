# Focuz — Intent-Aware Focus & Digital Wellbeing

> A serene, minimalist Android digital wellbeing application built with **Jetpack Compose**, **Kotlin Coroutines & Flow**, **Room SQLite Database**, and **Firebase Firestore**. Focuz intercepts mindless social media habits, provides a calming Pomodoro focus workspace, tracks weekly productivity trends, and archives daily mindful accomplishments with an automated 12:00 AM midnight rollover.

---

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Granular Technical Documentation on Unit Testing](#granular-technical-documentation-on-unit-testing)
3. [Granular Technical Documentation on Error Boundaries & Fault Tolerance](#granular-technical-documentation-on-error-boundaries--fault-tolerance)
4. [Database Schema Documentation](#database-schema-documentation)
   - [Room Local Database (SQLite)](#1-room-local-database-sqlite)
   - [Cloud Firestore Schema (Firebase)](#2-cloud-firestore-schema-firebase)
   - [Android SharedPreferences Schema](#3-android-sharedpreferences-schema)
5. [API Endpoints & Integration Architecture](#api-endpoints--integration-architecture)
6. [Component Reference & Code Structure](#component-reference--code-structure)
7. [Running Tests & Build Verification](#running-tests--build-verification)

---

## Architecture Overview

Focuz adheres to **Clean Architecture** and the **Model-View-ViewModel (MVVM)** architectural pattern:

```
┌────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                   │
│   (HomeScreen, FocusSessionScreen, Accountability,     │
│    PermittedScroll, Profile, Modals, ErrorBoundaries)  │
└───────────────────────────┬────────────────────────────┘
                            │ Collects StateFlow (FocuzUiState)
                            │ Dispatches User Actions
┌───────────────────────────▼────────────────────────────┐
│                    FocuzViewModel                      │
│     (AndroidViewModel, Lifecycle Scope, Coroutines)    │
└─────────────┬───────────────────────────┬──────────────┘
              │                           │
┌─────────────▼───────────────┐ ┌─────────▼──────────────┐
│       Room Local DB         │ │   Cloud Repositories   │
│  - FocuzDatabase            │ │  - FocuzFirebaseRepo   │
│  - DailyTimerRecordDao      │ │  - FocuzAuthRepo       │
│  - DailyTimerRepository     │ │  - Firestore SDK       │
└─────────────┬───────────────┘ └─────────┬──────────────┘
              │                           │
┌─────────────▼───────────────────────────▼──────────────┐
│              Android OS System Integration             │
│  - FocuzAppBlockerService (AccessibilityService)       │
│  - UsageStatsManager & SharedPreferences               │
│  - BlockOverlayActivity (Mindful Intercept UI)         │
└────────────────────────────────────────────────────────┘
```

- **Unidirectional Data Flow (UDF)**: The UI renders state exclusively from immutable `FocuzUiState` emitted via Kotlin `StateFlow`. User interactions trigger ViewModel methods that update state atomically using `_uiState.update { ... }`.
- **Offline-First Resilience**: All core focus timers, limits, and daily records function completely offline with SQLite/Room. Remote synchronization with Cloud Firestore executes non-blockingly in background coroutines.

---

## Granular Technical Documentation on Unit Testing

Focuz features a comprehensive JVM unit testing suite utilizing **Robolectric** (Android SDK 36 simulation) and **JUnit 4**, allowing high-fidelity Android API verification without requiring physical devices or slow emulators.

### Test Suite Map

| Test Class | File Path | Focus Area |
| :--- | :--- | :--- |
| `PomodoroTimerUnitTest` | `app/src/test/java/com/example/PomodoroTimerUnitTest.kt` | Pomodoro phases, duration clamping, countdown formatting, streak updates |
| `WeeklyFocusTrendsUnitTest` | `app/src/test/java/com/example/WeeklyFocusTrendsUnitTest.kt` | 7-day data pipeline, goal benchmarks, daily averages, Room matching |
| `MidnightResetUnitTest` | `app/src/test/java/com/example/MidnightResetUnitTest.kt` | 12:00 AM calculation, YYYY-MM-DD date keys, Room in-memory DAO queries |
| `ErrorBoundaryUnitTest` | `app/src/test/java/com/example/ErrorBoundaryUnitTest.kt` | Exception capture, boundary state transitions, recovery reset |
| `ExampleRobolectricTest` | `app/src/test/java/com/example/ExampleRobolectricTest.kt` | Android ApplicationProvider context and package verification |

### Granular Test Case Details

#### 1. `PomodoroTimerUnitTest`
- **`testPomodoroPhasesDefaults`**:
  - Asserts default durations: `WORK` = 25 min, `SHORT_BREAK` = 5 min, `LONG_BREAK` = 15 min.
  - Verifies calm microcopy titles (`"Deep Focus"`, `"Short Rest"`, `"Restoration"`).
- **`testRecordCompletedPomodoroUpdatesIntentionalMinutesAndStreak`**:
  - Tests `viewModel.recordCompletedPomodoro(25)`.
  - Verifies that intentional focus minutes increment accurately and activates the daily streak counter (`currentStreakDays >= 1`).
  - Verifies that the celebratory streak banner string is populated.
- **`testFormattedTimeCalculation`**:
  - Tests edge cases for mathematical formatting: `1500s` -> `"25:00"`, `849s` -> `"14:09"`, `0s` -> `"00:00"`.
- **`testWorkDurationClamping`**:
  - Validates that user duration adjustments are bounded strictly between 5 and 90 minutes (`coerceIn(5, 90)`), preventing negative, zero, or unmanageable durations.

#### 2. `WeeklyFocusTrendsUnitTest`
- **`testDayFocusTrendCreation`**:
  - Verifies `DayFocusTrend` data model initialization with dayIndex, label, date key, and target goal comparison.
- **`testWeeklyTotalAndDailyAverageCalculations`**:
  - Provides a 7-day mock series (Mon-Sun) totalling 370 minutes.
  - Verifies integer-divided daily average: `370 / 7 = 52m/day`.
  - Verifies goal attainment counts (3 days achieved $\ge$ 60m).
  - Identifies peak productivity day (`"Fri"` at 90 minutes).
- **`testIntegrationWithArchivedRecords`**:
  - Validates matching historical `DailyTimerRecordEntity` by ISO date key (`"2026-10-04"`).

#### 3. `MidnightResetUnitTest`
- **`testTodayDateKeyFormat`**:
  - Asserts that `SocialAppBlockerManager.getTodayDateKey()` matches the strict ISO regex pattern `^\d{4}-\d{2}-\d{2}$`.
- **`testMillisUntilMidnightIsPositiveAndUnder24Hours`**:
  - Verifies that the millisecond countdown to 12:00 AM next midnight is always $> 0$ and $\le 86,400,000$ ms.
- **`testFormatTimeRemainingUntilMidnight`**:
  - Confirms human-readable output matches format `"Xh Ym"` (e.g. `"22h 15m"`).
- **`testRoomDatabaseArchiveAndQuery`**:
  - Uses an isolated Room in-memory database (`Room.inMemoryDatabaseBuilder(...).allowMainThreadQueries().build()`).
  - Inserts mock records for multiple dates, queries `getAllRecords()`, and asserts that records are correctly sorted descending by timestamp.

#### 4. `ErrorBoundaryUnitTest`
- **`testInitialStateHasNoError`**:
  - Confirms `ErrorBoundaryState.hasError == false` upon initialization.
- **`testCaptureErrorUpdatesState`**:
  - Simulates a runtime component failure (`IllegalStateException`) and verifies that `state.hasError == true` with proper message retention.
- **`testResetClearsError`**:
  - Verifies that calling `state.reset()` restores normal execution state to facilitate user-triggered retries.

### Executing Unit Tests

```bash
# Run the entire local JVM test suite
gradle :app:testDebugUnitTest

# Run a specific unit test class
gradle :app:testDebugUnitTest --tests "com.example.PomodoroTimerUnitTest"
```

---

## Granular Technical Documentation on Error Boundaries & Fault Tolerance

Focuz employs a multi-tiered defense-in-depth strategy to isolate runtime exceptions, prevent fatal application crashes, and ensure graceful degradation across all layers.

### 1. UI Layer: `ComposeErrorBoundary`
- **Location**: `com.example.ui.components.ComposeErrorBoundary`
- **Mechanism**:
  ```kotlin
  ComposeErrorBoundary(boundaryName = "Weekly Focus Trends") {
    WeeklyFocusTrendsChart(...)
  }
  ```
- **Behavior**:
  - Wraps composable trees in an isolated `try/catch` evaluation block managed by `ErrorBoundaryState`.
  - If child composition throws an unhandled exception (e.g., unexpected data formatting or missing dimensions), the error boundary absorbs the exception, logs it via `Log.e`, and displays a serene Material 3 fallback card instead of terminating the app process.
  - **User Experience**: The fallback card displays empathetic microcopy (*"A temporary hiccup occurred. Distractions and errors pass like weather."*), an expandable technical diagnostics panel for debugging, and a **"Resume"** button that invokes `state.reset()` to re-attempt composition.

### 2. Local Storage Resilience (Room SQLite)
- **Idempotent Inserts**: `DailyTimerRecordDao` uses `@Insert(onConflict = OnConflictStrategy.REPLACE)`. Concurrent writes or duplicate midnight rollover triggers safely update existing records without constraint violation crashes.
- **Flow Safety**: Repository queries return `Flow<List<DailyTimerRecordEntity>>` which emits updates asynchronously on Dispatchers.IO, preventing UI thread deadlocks.
- **In-Memory Fallback**: If SQLite storage becomes corrupted or disk quota is exhausted, initialization falls back gracefully to a non-persistent in-memory instance.

### 3. Cloud & Network Resilience (Firebase Firestore & Auth)
- **Null-Safe Lazy Providers**: Firestore and FirebaseAuth instances are initialized inside `lazy { try { ... } catch (e: Exception) { null } }`. If Google Play Services is missing or `.env` credentials are unconfigured, instances return `null` without throwing fatal runtime exceptions.
- **Offline Disk Cache**: Firestore enables automatic offline persistence. All write operations (`syncUserStats`, `logIntentEvent`) queue locally and flush once network connectivity is re-established.
- **Mock User Fallback**: In restricted testing or emulator environments, authentication provides a deterministic mock guest/Google profile (`"student.focus@gmail.com"`), enabling full UI exploration without blocking on network authentication.

### 4. System Permission Graceful Degradation
- **`UsageStatsManager`**: Checked at runtime via `AppOpsManager.MODE_ALLOWED`. If the user has not granted usage access, Focuz smoothly falls back to internal timer records rather than crashing.
- **`AccessibilityService` (`FocuzAppBlockerService`)**: Monitored via `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES`. The UI shows a non-intrusive setup guide card if disabled, allowing manual focus timers to proceed unimpeded.

---

## Database Schema Documentation

### 1. Room Local Database (SQLite)
- **Database Name**: `focuz_database`
- **Database Class**: `com.example.data.local.FocuzDatabase`
- **Version**: `1`
- **Export Schema**: `false`

#### Table Schema: `daily_timer_records`

```sql
CREATE TABLE IF NOT EXISTS daily_timer_records (
    date TEXT PRIMARY KEY NOT NULL,              -- Format: YYYY-MM-DD (e.g., '2026-10-05')
    intentionalFocusMinutes INTEGER NOT NULL,     -- Minutes spent in active Pomodoro / Flow
    scrollMinutes INTEGER NOT NULL,              -- Minutes spent in monitored social apps
    interceptedOpens INTEGER NOT NULL,           -- Number of unconscious opens blocked
    mindfulPausesTaken INTEGER NOT NULL,         -- Number of mindful reflections logged
    focusGoalMinutes INTEGER NOT NULL,           -- Daily target (default: 60 mins)
    goalAchieved INTEGER NOT NULL,               -- Boolean flag: 1 if focus >= goal, else 0
    recordedAtTimestamp INTEGER NOT NULL         -- Epoch timestamp in milliseconds
);

CREATE INDEX IF NOT EXISTS index_daily_timer_records_timestamp 
ON daily_timer_records (recordedAtTimestamp DESC);
```

#### Field Specifications

| Column Name | SQLite Type | Kotlin Type | Description |
| :--- | :--- | :--- | :--- |
| `date` | `TEXT` | `String` | **Primary Key**. Canonical date key (`YYYY-MM-DD`). Guarantees exactly one summary record per calendar day. |
| `intentionalFocusMinutes` | `INTEGER` | `Int` | Cumulative intentional study and focus minutes logged during the 24-hour day. |
| `scrollMinutes` | `INTEGER` | `Int` | Total minutes spent on monitored social platforms (Instagram, TikTok, Twitter/X, etc.). |
| `interceptedOpens` | `INTEGER` | `Int` | Count of app launch attempts intercepted by the accessibility blocker. |
| `mindfulPausesTaken` | `INTEGER` | `Int` | Count of mindful substitute activities accepted (e.g. 5-min stretch, breathing). |
| `focusGoalMinutes` | `INTEGER` | `Int` | The user's active focus target at time of archiving (defaults to 60 min). |
| `goalAchieved` | `INTEGER` | `Boolean` | Evaluated at midnight: `intentionalFocusMinutes >= focusGoalMinutes`. |
| `recordedAtTimestamp` | `INTEGER` | `Long` | System epoch timestamp recorded at archiving for time-series sorting. |

#### Data Access Object (DAO) Operations

```kotlin
@Dao
interface DailyTimerRecordDao {
  @Query("SELECT * FROM daily_timer_records ORDER BY recordedAtTimestamp DESC")
  fun getAllRecords(): Flow<List<DailyTimerRecordEntity>>

  @Query("SELECT * FROM daily_timer_records WHERE date = :date LIMIT 1")
  suspend fun getRecordForDate(date: String): DailyTimerRecordEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(record: DailyTimerRecordEntity)

  @Query("DELETE FROM daily_timer_records WHERE recordedAtTimestamp < :cutoffTimestamp")
  suspend fun deleteOlderThan(cutoffTimestamp: Long)
}
```

---

### 2. Cloud Firestore Schema (Firebase)

Focuz synchronizes user profile settings, streaks, and intent audit logs to Cloud Firestore under project `focuz-5a2c0`.

#### Collection Hierarchy
```
firestore_root
│
├── users/
│   └── {userId}/                           (Document: User Profile & Daily Metrics)
│       ├── currentStreakDays: Int
│       ├── intentionalMinutes: Int
│       ├── scrollMinutes: Int
│       ├── interceptedOpens: Int
│       ├── mindfulPausesTaken: Int
│       ├── lastUpdated: Long (Epoch ms)
│       │
│       ├── profile/
│       │   └── info                        (Document: Account & Preferences)
│       │       ├── uid: String
│       │       ├── name: String
│       │       ├── email: String
│       │       ├── photoUrl: String
│       │       ├── bio: String
│       │       ├── focusGoalMins: Int
│       │       └── updatedAt: Long
│       │
│       └── intent_logs/
│           └── {autoGeneratedLogId}        (Document: Intent Intercept History)
│               ├── appName: String
│               ├── reason: String
│               ├── actionTaken: String
│               └── timestamp: Long
│
└── diagnostics/
    └── {userId}                            (Document: Healthcheck Ping)
        ├── clientVersion: String
        ├── appName: String
        └── lastPing: Long
```

#### Document JSON Representations

**1. User Stats Document (`users/{userId}`)**:
```json
{
  "currentStreakDays": 5,
  "intentionalMinutes": 185,
  "scrollMinutes": 24,
  "interceptedOpens": 8,
  "mindfulPausesTaken": 6,
  "lastUpdated": 1793784120000
}
```

**2. Intent Log Document (`users/{userId}/intent_logs/{logId}`)**:
```json
{
  "appName": "Instagram",
  "reason": "Boredom & reflex",
  "actionTaken": "Conscious pause: Swapped for 5-Min Stretch",
  "timestamp": 1793784120000
}
```

---

### 3. Android SharedPreferences Schema

Preferences are stored in `focuz_social_blocker_prefs` (`Context.MODE_PRIVATE`):

| Key Pattern | Value Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `app_enabled_{packageName}` | `Boolean` | `true` | Whether blocker is active for this package (e.g. `com.instagram.android`). |
| `app_limit_{packageName}` | `Int` | `15` | Daily allowed scroll allowance in minutes before blocking triggers. |
| `app_manual_usage_{packageName}`| `Int` | `0` | Fallback daily usage tracker when system UsageStats permissions are ungranted. |
| `last_usage_date` | `String` | `"YYYY-MM-DD"` | Tracks the last active date to trigger automated 12:00 AM resets. |
| `today_intercepted_count` | `Int` | `0` | Cumulative app opens intercepted today; resets to 0 at 12:00 AM. |

---

## API Endpoints & Integration Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Focuz External Integrations                     │
├─────────────────────┬──────────────────────┬───────────────────────────┤
│ Integration Service │ Endpoint / Target    │ Purpose                   │
├─────────────────────┼──────────────────────┼───────────────────────────┤
│ Firebase Auth       │ Google Identity GIS  │ One-Tap Google Sign-In    │
│ Firebase Auth       │ Email/Password Auth  │ Standard account sign-in  │
│ Cloud Firestore     │ `users/{uid}`        │ Live stats & profile sync │
│ Cloud Firestore     │ `intent_logs` subcol │ Telemetry on habit swaps  │
│ Android System      │ `AccessibilityServ.` │ Window change detection   │
│ Android System      │ `UsageStatsManager`  │ Precise daily screen time │
│ Android System      │ `WindowManager`      │ Fullscreen block overlay  │
└─────────────────────┴──────────────────────┴───────────────────────────┘
```

### 1. Authentication Integration (`FocuzAuthRepository`)
- **`signInWithGoogleIdToken(idToken: String)`**:
  - Converts Google Identity Services (GIS) ID tokens to `AuthCredential` via `GoogleAuthProvider.getCredential(idToken, null)`.
  - Completes asynchronous sign-in via `FirebaseAuth.signInWithCredential(credential).await()`.
- **`signInWithEmail(email, password)` / `signUpWithEmail(email, password, name)`**:
  - Dispatches standard Firebase Authentication with automated input sanitization (`email.trim()`) and friendly user-facing error mapping.

### 2. Cloud Firestore Integration (`FocuzFirebaseRepository`)
- **`syncUserStats(...)`**: Dispatches merged updates to `/users/{userId}` using `SetOptions.merge()`, guaranteeing partial-field updates without overwriting unassociated user profile fields.
- **`observeUserStats(userId, callback)`**: Registers a real-time Firestore `SnapshotListener` on `/users/{userId}`, streaming remote updates into the ViewModel.
- **`logIntentEvent(...)`**: Adds immutable habit logs to `/users/{userId}/intent_logs` collection.

### 3. Android System Services
- **`FocuzAppBlockerService` (`android.accessibilityservice.AccessibilityService`)**:
  - Subscribes to `AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED`.
  - Extracts the active foreground `packageName`.
  - Validates package against monitored limits in `SocialAppBlockerManager`.
  - When time limit is exceeded, launches `BlockOverlayActivity` with `FLAG_ACTIVITY_NEW_TASK` to gently redirect the user away from distractions.

---

## Component Reference & Code Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                      // Entry Activity with Edge-to-Edge & Navigation
├── data/
│   ├── AuthUserState.kt                 // User authentication session model
│   ├── FocuzAppBlockerService.kt        // Android Accessibility Service for app intercepts
│   ├── FocuzAuthRepository.kt           // Firebase Authentication repository
│   ├── FocuzFirebaseRepository.kt       // Cloud Firestore synchronization repository
│   ├── FocuzModels.kt                   // Core data structures (UserProfile, FocusMode)
│   ├── FocuzViewModel.kt                // MVVM state coordinator & timer controller
│   ├── SocialAppBlockerManager.kt       // SharedPreferences & 12:00 AM reset manager
│   ├── SocialAppLimit.kt                // Model for app limits and monitored packages
│   └── local/
│       ├── DailyTimerRecordDao.kt       // Room Data Access Object with Flow queries
│       ├── DailyTimerRecordEntity.kt    // SQLite entity table definition
│       ├── DailyTimerRepository.kt      // Room repository for database operations
│       └── FocuzDatabase.kt             // Room database singleton provider
└── ui/
    ├── BlockOverlayActivity.kt          // Full-screen mindful intercept overlay
    ├── FocuzApp.kt                      // Root application composable & navigation tabs
    ├── components/
    │   ├── BuddyPairModal.kt            // Study buddy accountability pairing dialog
    │   ├── CalmProgressRing.kt          // Intentional vs scrolling dual-progress canvas
    │   ├── ComposeErrorBoundary.kt      // Exception boundary with recovery states
    │   ├── DailyTimerHistoryModal.kt    // Room database history & rollover manager
    │   ├── IntentTagModal.kt            // Mindful habit reflection popup
    │   ├── PermittedTimeBlockWidget.kt  // Permitted scrolling window widget
    │   ├── PomodoroFocusTimer.kt        // Pomodoro timer with duration setter & calm ring
    │   ├── QuickStatCard.kt             // Top-level metric display cards
    │   ├── ReplacementSuggestionCard.kt // Micro-habit alternative suggestions
    │   ├── SocialAppLimitCard.kt        // App limit configuration & testing row
    │   ├── StreakFlameWidget.kt         // Serene streak flame indicator
    │   └── WeeklyFocusTrendsChart.kt    // Interactive Bar/Area weekly trends chart
    ├── screens/
    │   ├── AccountabilityScreen.kt      // Study buddy pacts & shared cheers
    │   ├── AuthScreen.kt                // Google & Email sign-in/sign-up screen
    │   ├── FocusSessionScreen.kt        // Pomodoro timer & Context flow workspace
    │   ├── HomeScreen.kt                // Main dashboard with trends chart & stats
    │   ├── PermittedScrollScreen.kt     // Daily limit controls & permission status
    │   └── ProfileScreen.kt             // User profile editing & goal setting
    └── theme/
        ├── Color.kt                     // Serene Charcoal & Off-White palette
        ├── Theme.kt                     // Material 3 theme configuration
        └── Type.kt                      // Typography styles
```

---

## Running Tests & Build Verification

To verify the app and ensure all tests pass:

```bash
# 1. Compile the Android application
gradle assembleDebug

# 2. Run the complete JVM Robolectric test suite
gradle :app:testDebugUnitTest

# 3. Check for syntax and compile errors
gradle check
```

---

*Authored with strict compliance to Material Design 3 guidelines, offline-first Room persistence, and clean Android architecture.*
