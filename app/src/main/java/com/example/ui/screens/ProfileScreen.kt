package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.FocuzUiState
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary
import com.example.ui.theme.OffWhiteSubtle
import java.io.File
import java.io.FileOutputStream

/**
 * Clean, minimalist Profile Screen allowing users to view and edit their name,
 * profile picture (with gallery photo upload and URL support), bio, and daily focus goal.
 * Directly updates and saves to Firebase Firestore and local storage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  uiState: FocuzUiState,
  onSaveProfile: (name: String, photoUrl: String, bio: String, goalMins: Int) -> Unit,
  onBack: () -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val initialName = uiState.userProfile.name.ifBlank {
    uiState.currentUser?.displayName ?: ""
  }
  val initialPhoto = uiState.userProfile.photoUrl.ifBlank {
    uiState.currentUser?.photoUrl ?: ""
  }
  val initialBio = uiState.userProfile.bio
  val initialGoal = if (uiState.userProfile.focusGoalMins > 0) uiState.userProfile.focusGoalMins.toString() else ""

  var nameInput by remember(initialName) { mutableStateOf(initialName) }
  var photoUrlInput by remember(initialPhoto) { mutableStateOf(initialPhoto) }
  var bioInput by remember(initialBio) { mutableStateOf(initialBio) }
  var goalInput by remember(initialGoal) { mutableStateOf(initialGoal) }

  var isEditingPhoto by remember { mutableStateOf(false) }
  var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

  // Photo Picker to choose user's own image from device gallery
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let { selectedUri ->
      try {
        val inputStream = context.contentResolver.openInputStream(selectedUri)
        val file = File(context.filesDir, "user_avatar_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
          outputStream.use { output ->
            input.copyTo(output)
          }
        }
        val localPhotoUri = file.toURI().toString()
        photoUrlInput = localPhotoUri
        onSaveProfile(nameInput, localPhotoUri, bioInput, goalInput.toIntOrNull() ?: 60)
        saveSuccessMessage = "Profile picture updated from device photos!"
      } catch (e: Exception) {
        val fallbackUri = selectedUri.toString()
        photoUrlInput = fallbackUri
        onSaveProfile(nameInput, fallbackUri, bioInput, goalInput.toIntOrNull() ?: 60)
        saveSuccessMessage = "Profile picture updated!"
      }
    }
  }

  // Curated minimalist avatar presets if user wants quick 1-tap pfp
  val presetAvatars = listOf(
    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200&auto=format&fit=crop&q=80"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(CharcoalBackground)
      .padding(horizontal = 20.dp)
      .testTag("profile_screen"),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // Top Bar with Back Button
    item {
      Spacer(modifier = Modifier.height(12.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("btn_profile_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = OffWhitePrimary
          )
        }

        Text(
          text = "Profile",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = OffWhitePrimary
        )

        OutlinedButton(
          onClick = onSignOut,
          modifier = Modifier.testTag("btn_profile_signout"),
          border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(
            text = "Log Out",
            style = MaterialTheme.typography.labelSmall,
            color = OffWhiteMuted
          )
        }
      }
    }

    // Avatar Section
    item {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(108.dp)
            .clip(CircleShape)
            .background(CharcoalSurface)
            .border(2.dp, CharcoalBorder, CircleShape)
            .clickable {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
          contentAlignment = Alignment.Center
        ) {
          if (photoUrlInput.isNotBlank()) {
            AsyncImage(
              model = photoUrlInput,
              contentDescription = "Profile photo",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          } else {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Default avatar",
              modifier = Modifier.size(54.dp),
              tint = OffWhiteMuted
            )
          }

          // Camera edit overlay badge
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .size(32.dp)
              .clip(CircleShape)
              .background(OffWhitePrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CameraAlt,
              contentDescription = "Change photo",
              tint = CharcoalBackground,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = if (nameInput.isNotBlank()) nameInput else "None (Tap Edit)",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Normal,
          color = OffWhitePrimary
        )

        val emailDisplay = uiState.currentUser?.email ?: if (uiState.isGuestMode) "Guest Mode" else "No email"
        Text(
          text = emailDisplay,
          style = MaterialTheme.typography.bodySmall,
          color = OffWhiteMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Direct Action Buttons for Photo
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            modifier = Modifier.testTag("btn_upload_photo_gallery"),
            border = androidx.compose.foundation.BorderStroke(1.dp, OffWhitePrimary),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = CharcoalSurface,
              contentColor = OffWhitePrimary
            )
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Upload,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = OffWhitePrimary
              )
              Text(
                text = "Upload Image from Gallery",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          OutlinedButton(
            onClick = { isEditingPhoto = !isEditingPhoto },
            modifier = Modifier.testTag("btn_toggle_photo_options"),
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = if (isEditingPhoto) "Close Options" else "More Options",
              style = MaterialTheme.typography.labelSmall,
              color = OffWhiteMuted
            )
          }
        }
      }
    }

    // Photo URL / Preset Selector (Collapsible)
    if (isEditingPhoto) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalSurface)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Option 1: Gallery Upload Banner
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CharcoalBackground)
                .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
                .clickable {
                  photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                  )
                }
                .padding(14.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.PhotoLibrary,
                  contentDescription = null,
                  tint = OffWhitePrimary,
                  modifier = Modifier.size(20.dp)
                )
                Column {
                  Text(
                    text = "Pick From Photos & Gallery",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = OffWhitePrimary
                  )
                  Text(
                    text = "Upload your personal avatar directly from device storage",
                    style = MaterialTheme.typography.bodySmall,
                    color = OffWhiteMuted
                  )
                }
              }
            }

            Text(
              text = "OR CHOOSE A MINIMALIST PRESET",
              style = MaterialTheme.typography.labelSmall,
              color = OffWhiteMuted,
              letterSpacing = 1.sp
            )

            // Preset Avatars Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              presetAvatars.forEach { url ->
                Box(
                  modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(
                      width = if (photoUrlInput == url) 2.dp else 1.dp,
                      color = if (photoUrlInput == url) OffWhitePrimary else CharcoalBorder,
                      shape = CircleShape
                    )
                    .clickable { photoUrlInput = url }
                ) {
                  AsyncImage(
                    model = url,
                    contentDescription = "Preset avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                  )
                }
              }
            }

            OutlinedTextField(
              value = photoUrlInput,
              onValueChange = { photoUrlInput = it },
              label = { Text("Image URL") },
              placeholder = { Text("https://example.com/avatar.jpg") },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("input_profile_photo_url"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = OffWhitePrimary,
                unfocusedTextColor = OffWhitePrimary,
                focusedBorderColor = OffWhitePrimary,
                unfocusedBorderColor = CharcoalBorder,
                focusedLabelColor = OffWhitePrimary,
                unfocusedLabelColor = OffWhiteMuted,
                focusedContainerColor = CharcoalBackground,
                unfocusedContainerColor = CharcoalBackground
              ),
              shape = RoundedCornerShape(12.dp),
              singleLine = true
            )
          }
        }
      }
    }

    // Edit Name
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "YOUR NAME",
          style = MaterialTheme.typography.labelSmall,
          color = OffWhiteMuted,
          letterSpacing = 1.sp
        )

        OutlinedTextField(
          value = nameInput,
          onValueChange = { nameInput = it },
          placeholder = { Text("Enter your real name") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_profile_name"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = OffWhitePrimary,
            unfocusedTextColor = OffWhitePrimary,
            focusedBorderColor = OffWhitePrimary,
            unfocusedBorderColor = CharcoalBorder,
            focusedLabelColor = OffWhitePrimary,
            unfocusedLabelColor = OffWhiteMuted,
            focusedContainerColor = CharcoalSurface,
            unfocusedContainerColor = CharcoalSurface
          ),
          shape = RoundedCornerShape(14.dp),
          singleLine = true
        )
      }
    }

    // Bio / Intention
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "INTENTION / BIO",
          style = MaterialTheme.typography.labelSmall,
          color = OffWhiteMuted,
          letterSpacing = 1.sp
        )

        OutlinedTextField(
          value = bioInput,
          onValueChange = { bioInput = it },
          placeholder = { Text("e.g. Preparing for exams, reclaiming mindful focus") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_profile_bio"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = OffWhitePrimary,
            unfocusedTextColor = OffWhitePrimary,
            focusedBorderColor = OffWhitePrimary,
            unfocusedBorderColor = CharcoalBorder,
            focusedLabelColor = OffWhitePrimary,
            unfocusedLabelColor = OffWhiteMuted,
            focusedContainerColor = CharcoalSurface,
            unfocusedContainerColor = CharcoalSurface
          ),
          shape = RoundedCornerShape(14.dp),
          maxLines = 3
        )
      }
    }

    // Daily Focus Goal Minutes
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "DAILY FOCUS GOAL (MINUTES)",
          style = MaterialTheme.typography.labelSmall,
          color = OffWhiteMuted,
          letterSpacing = 1.sp
        )

        OutlinedTextField(
          value = goalInput,
          onValueChange = { if (it.all { char -> char.isDigit() }) goalInput = it },
          placeholder = { Text("e.g. 60 or leave empty for None") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_profile_goal"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = OffWhitePrimary,
            unfocusedTextColor = OffWhitePrimary,
            focusedBorderColor = OffWhitePrimary,
            unfocusedBorderColor = CharcoalBorder,
            focusedLabelColor = OffWhitePrimary,
            unfocusedLabelColor = OffWhiteMuted,
            focusedContainerColor = CharcoalSurface,
            unfocusedContainerColor = CharcoalSurface
          ),
          shape = RoundedCornerShape(14.dp),
          singleLine = true
        )
      }
    }

    // Save Button
    item {
      Spacer(modifier = Modifier.height(6.dp))

      Button(
        onClick = {
          val goalVal = goalInput.toIntOrNull() ?: 0
          onSaveProfile(nameInput, photoUrlInput, bioInput, goalVal)
          saveSuccessMessage = "Profile updated & synced to Cloud Firestore!"
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_save_profile"),
        colors = ButtonDefaults.buttonColors(
          containerColor = androidx.compose.ui.graphics.Color(0xFF22252E),
          contentColor = androidx.compose.ui.graphics.Color.White
        ),
        border = BorderStroke(1.5.dp, OffWhitePrimary),
        shape = RoundedCornerShape(14.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = androidx.compose.ui.graphics.Color.White
          )
          Text(
            text = "Save Profile to Firebase",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = androidx.compose.ui.graphics.Color.White
          )
        }
      }

      if (saveSuccessMessage != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = saveSuccessMessage!!,
          style = MaterialTheme.typography.bodySmall,
          color = OffWhitePrimary,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }

      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}
