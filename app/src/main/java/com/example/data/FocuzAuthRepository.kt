package com.example.data

import android.content.Context
import android.util.Log
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

/**
 * Handles Firebase Authentication with Google Sign-In and Email/Password flows.
 */
class FocuzAuthRepository {
  private val auth: FirebaseAuth? by lazy {
    try {
      FirebaseAuth.getInstance()
    } catch (e: Exception) {
      Log.w(TAG, "FirebaseAuth not initialized in current environment: ${e.message}")
      null
    }
  }

  companion object {
    private const val TAG = "FocuzAuth"
  }

  fun getCurrentUser(): AuthUserState? {
    val firebaseAuth = auth ?: return null
    val firebaseUser = firebaseAuth.currentUser ?: return null
    return AuthUserState(
      uid = firebaseUser.uid,
      email = firebaseUser.email,
      displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@"),
      photoUrl = firebaseUser.photoUrl?.toString(),
      isAuthenticated = true
    )
  }

  suspend fun signInWithEmail(email: String, pass: String): Result<AuthUserState> {
    val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Auth not initialized"))
    return try {
      val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), pass).await()
      val u = result.user
      val userState = AuthUserState(
        uid = u?.uid ?: UUID.randomUUID().toString(),
        email = u?.email ?: email,
        displayName = u?.displayName ?: email.substringBefore("@"),
        isAuthenticated = true
      )
      Result.success(userState)
    } catch (e: Exception) {
      Log.e(TAG, "signInWithEmail failed: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun signUpWithEmail(email: String, pass: String, name: String = ""): Result<AuthUserState> {
    val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Auth not initialized"))
    return try {
      val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), pass).await()
      val u = result.user
      val chosenName = name.trim().ifBlank { email.substringBefore("@").replaceFirstChar { it.uppercase() } }
      if (u != null && chosenName.isNotBlank()) {
        try {
          val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(chosenName)
            .build()
          u.updateProfile(profileUpdates).await()
        } catch (ignored: Exception) {
          Log.w(TAG, "Profile displayName update non-critical: ${ignored.message}")
        }
      }
      val userState = AuthUserState(
        uid = u?.uid ?: UUID.randomUUID().toString(),
        email = u?.email ?: email,
        displayName = chosenName,
        isAuthenticated = true
      )
      Result.success(userState)
    } catch (e: Exception) {
      Log.e(TAG, "signUpWithEmail failed: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthUserState> {
    val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Auth not initialized"))
    return try {
      val credential = GoogleAuthProvider.getCredential(idToken, null)
      val authResult = firebaseAuth.signInWithCredential(credential).await()
      val u = authResult.user
      val userState = AuthUserState(
        uid = u?.uid ?: UUID.randomUUID().toString(),
        email = u?.email,
        displayName = u?.displayName ?: "Google User",
        photoUrl = u?.photoUrl?.toString(),
        isAuthenticated = true
      )
      Result.success(userState)
    } catch (e: Exception) {
      Log.e(TAG, "Google Auth failed: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun signInAnonymously(): Result<AuthUserState> {
    val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Auth not initialized"))
    return try {
      val result = firebaseAuth.signInAnonymously().await()
      val u = result.user
      val userState = AuthUserState(
        uid = u?.uid ?: UUID.randomUUID().toString(),
        email = null,
        displayName = "Guest User",
        isAuthenticated = true
      )
      Result.success(userState)
    } catch (e: Exception) {
      Log.e(TAG, "signInAnonymously failed: ${e.message}", e)
      Result.failure(e)
    }
  }

  fun signOut() {
    try {
      auth?.signOut()
    } catch (e: Exception) {
      Log.e(TAG, "signOut error: ${e.message}", e)
    }
  }
}
