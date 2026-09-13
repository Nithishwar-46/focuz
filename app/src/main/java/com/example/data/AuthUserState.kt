package com.example.data

data class UserProfile(
  val uid: String = "",
  val name: String = "",
  val email: String = "",
  val photoUrl: String = "",
  val bio: String = "",
  val focusGoalMins: Int = 0
)

data class AuthUserState(
  val uid: String = "",
  val email: String? = null,
  val displayName: String? = null,
  val photoUrl: String? = null,
  val isAuthenticated: Boolean = false
)
