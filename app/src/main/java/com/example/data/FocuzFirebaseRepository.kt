package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Repository to persist Focuz stats, user streak data, intent logs, and buddy cheers
 * in Firebase Firestore under project focuz-5a2c0.
 */
class FocuzFirebaseRepository {
  private val firestore: FirebaseFirestore? by lazy {
    try {
      FirebaseFirestore.getInstance()
    } catch (e: Exception) {
      Log.w(TAG, "Firebase not initialized in current environment: ${e.message}")
      null
    }
  }

  companion object {
    private const val TAG = "FocuzFirebase"
    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_LOGS = "intent_logs"
    private const val COLLECTION_DIAGNOSTICS = "diagnostics"
    const val DEFAULT_USER_ID = "focuz_user_primary"
  }

  /**
   * Test write to confirm database connection and collection setup.
   */
  suspend fun logConnectionPing(userId: String = DEFAULT_USER_ID): Boolean {
    val db = firestore ?: return false
    return try {
      val pingData = hashMapOf(
        "lastPing" to System.currentTimeMillis(),
        "clientVersion" to "1.0.0",
        "appName" to "Focuz"
      )
      db.collection(COLLECTION_DIAGNOSTICS)
        .document(userId)
        .set(pingData, SetOptions.merge())
        .await()
      Log.d(TAG, "Firestore connection ping successful for $userId")
      true
    } catch (e: Exception) {
      Log.e(TAG, "Firestore ping failed: ${e.message}", e)
      false
    }
  }

  /**
   * Save user's focus metrics and streak counters.
   */
  suspend fun syncUserStats(
    userId: String = DEFAULT_USER_ID,
    streakDays: Int,
    intentionalMinutes: Int,
    scrollMinutes: Int,
    interceptedOpens: Int,
    mindfulPauses: Int
  ) {
    val db = firestore ?: return
    try {
      val statsData = hashMapOf(
        "currentStreakDays" to streakDays,
        "intentionalMinutes" to intentionalMinutes,
        "scrollMinutes" to scrollMinutes,
        "interceptedOpens" to interceptedOpens,
        "mindfulPausesTaken" to mindfulPauses,
        "lastUpdated" to System.currentTimeMillis()
      )
      db.collection(COLLECTION_USERS)
        .document(userId)
        .set(statsData, SetOptions.merge())
        .await()
      Log.d(TAG, "Synced user stats for $userId")
    } catch (e: Exception) {
      Log.e(TAG, "Error syncing stats: ${e.message}", e)
    }
  }

  /**
   * Log an intercepted intent event (e.g. Opening Instagram with reason "Boredom" or "Habit").
   */
  suspend fun logIntentEvent(
    userId: String = DEFAULT_USER_ID,
    appName: String,
    reason: String,
    actionTaken: String
  ) {
    val db = firestore ?: return
    try {
      val logEntry = hashMapOf(
        "appName" to appName,
        "reason" to reason,
        "actionTaken" to actionTaken,
        "timestamp" to System.currentTimeMillis()
      )
      db.collection(COLLECTION_USERS)
        .document(userId)
        .collection(COLLECTION_LOGS)
        .add(logEntry)
        .await()
      Log.d(TAG, "Logged intent event for app: $appName")
    } catch (e: Exception) {
      Log.e(TAG, "Error logging intent: ${e.message}", e)
    }
  }

  /**
   * Record a cheer sent to study buddy.
   */
  suspend fun sendCheerToBuddy(
    senderId: String = DEFAULT_USER_ID,
    receiverName: String = "Maya L."
  ) {
    val db = firestore ?: return
    try {
      val cheerData = hashMapOf(
        "senderId" to senderId,
        "receiverName" to receiverName,
        "timestamp" to System.currentTimeMillis()
      )
      db.collection("buddy_cheers")
        .add(cheerData)
        .await()
      Log.d(TAG, "Cheer recorded in Firestore for $receiverName")
    } catch (e: Exception) {
      Log.e(TAG, "Error sending cheer: ${e.message}", e)
    }
  }

  /**
   * Realtime listener to sync live updates from Firestore.
   */
  fun observeUserStats(
    userId: String = DEFAULT_USER_ID,
    onStatsUpdate: (streak: Int, intentionalMins: Int, scrollMins: Int, intercepted: Int, mindful: Int) -> Unit
  ): ListenerRegistration? {
    val db = firestore ?: return null
    return try {
      db.collection(COLLECTION_USERS)
        .document(userId)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w(TAG, "Firestore listen failed: ${error.message}")
            return@addSnapshotListener
          }

          if (snapshot != null && snapshot.exists()) {
            val streak = snapshot.getLong("currentStreakDays")?.toInt() ?: 3
            val intentional = snapshot.getLong("intentionalMinutes")?.toInt() ?: 165
            val scroll = snapshot.getLong("scrollMinutes")?.toInt() ?: 18
            val intercepted = snapshot.getLong("interceptedOpens")?.toInt() ?: 14
            val mindful = snapshot.getLong("mindfulPausesTaken")?.toInt() ?: 8

            onStatsUpdate(streak, intentional, scroll, intercepted, mindful)
          } else {
            // First time user: initialize with zero
            onStatsUpdate(0, 0, 0, 0, 0)
          }
        }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to register snapshot listener: ${e.message}", e)
      null
    }
  }

  /**
   * Save or update user profile (name, photo URL, bio, focus goal).
   */
  suspend fun saveUserProfile(profile: UserProfile): Boolean {
    val db = firestore ?: return false
    return try {
      val data = hashMapOf(
        "name" to profile.name,
        "email" to profile.email,
        "photoUrl" to profile.photoUrl,
        "bio" to profile.bio,
        "focusGoalMins" to profile.focusGoalMins,
        "updatedAt" to System.currentTimeMillis()
      )
      db.collection(COLLECTION_USERS)
        .document(profile.uid)
        .set(data, SetOptions.merge())
        .await()
      Log.d(TAG, "User profile saved for ${profile.uid}")
      true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to save profile: ${e.message}", e)
      false
    }
  }

  /**
   * Observe user profile in Firestore.
   */
  fun observeUserProfile(
    userId: String,
    onProfileUpdate: (UserProfile) -> Unit
  ): ListenerRegistration? {
    val db = firestore ?: return null
    return try {
      db.collection(COLLECTION_USERS)
        .document(userId)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w(TAG, "Failed to listen to profile: ${error.message}")
            return@addSnapshotListener
          }
          if (snapshot != null && snapshot.exists()) {
            val profile = UserProfile(
              uid = userId,
              name = snapshot.getString("name") ?: "",
              email = snapshot.getString("email") ?: "",
              photoUrl = snapshot.getString("photoUrl") ?: "",
              bio = snapshot.getString("bio") ?: "",
              focusGoalMins = snapshot.getLong("focusGoalMins")?.toInt() ?: 0
            )
            onProfileUpdate(profile)
          }
        }
    } catch (e: Exception) {
      Log.e(TAG, "Error in observeUserProfile: ${e.message}", e)
      null
    }
  }
}
