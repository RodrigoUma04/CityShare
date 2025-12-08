package com.example.cityshare.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.cityshare.ui.functions.User
import com.example.cityshare.ui.functions.getOrCreateUser
import com.example.cityshare.ui.functions.logout
import com.example.cityshare.ui.functions.updateUsername
import com.example.cityshare.ui.functions.uploadProfileImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val context = LocalContext.current

    var user by remember { mutableStateOf<User?>(null) }
    var showEditUsernameDialog by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempUsername by remember { mutableStateOf("") }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Camera permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            uploadProfileImage(
                auth = auth,
                firestore = firestore,
                storage = storage,
                imageUri = uri,
                onSuccess = { imageUrl ->
                    user = user?.copy(profileImageUrl = imageUrl)
                    Log.d("ProfileScreen", "Image uploaded successfully: $imageUrl")
                },
                onError = { error ->
                    Log.e("ProfileScreen", "Error uploading image: $error")
                }
            )
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            uploadProfileImage(
                auth = auth,
                firestore = firestore,
                storage = storage,
                imageUri = tempImageUri!!,
                onSuccess = { imageUrl ->
                    user = user?.copy(profileImageUrl = imageUrl)
                    Log.d("ProfileScreen", "Camera image uploaded successfully: $imageUrl")
                },
                onError = { error ->
                    Log.e("ProfileScreen", "Error uploading camera image: $error")
                }
            )
        }
        tempImageUri = null
    }

    LaunchedEffect(Unit) {
        getOrCreateUser(
            auth = auth,
            firestore = firestore,
            onSuccess = { loadedUser -> user = loadedUser },
            onError = { error -> Log.e("ProfileScreen", "Error loading user: $error") }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            if (user?.profileImageUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                        .border(3.dp, Color.LightGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        modifier = Modifier.size(75.dp),
                        tint = Color.Gray
                    )
                }
            } else {
                AsyncImage(
                    model = user?.profileImageUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.LightGray, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            IconButton(
                onClick = { showImageSourceDialog = true },
                modifier = Modifier
                    .size(45.dp)
                    .background(Color.White, CircleShape)
                    .border(2.dp, Color.LightGray, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change photo",
                    tint = Color.DarkGray
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = user?.username ?: "Unknown",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {
                tempUsername = user?.username ?: ""
                showEditUsernameDialog = true
            }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit username",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(text = user?.email ?: "", fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray.copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            Text(
                text = "Favorite locations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(275.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No favorites yet", color = Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { logout(auth) { onLogout() } },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Logout", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }

    // Username dialog
    if (showEditUsernameDialog) {
        AlertDialog(
            onDismissRequest = { showEditUsernameDialog = false },
            title = { Text("Edit username") },
            text = {
                OutlinedTextField(
                    value = tempUsername,
                    onValueChange = { tempUsername = it },
                    label = { Text("Username", color = Color.Gray) },
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.LightGray.copy(alpha = 0.3f),
                        unfocusedContainerColor = Color.LightGray.copy(alpha = 0.3f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempUsername.isNotBlank()) {
                        updateUsername(
                            auth, firestore, tempUsername,
                            onSuccess = {
                                user = user?.copy(username = tempUsername)
                                showEditUsernameDialog = false
                            },
                            onError = { error ->
                                Log.e("ProfileScreen", "Error updating username: $error")
                            }
                        )
                    }
                }) { Text("Save", color = Color.Gray) }
            },
            dismissButton = {
                TextButton(onClick = { showEditUsernameDialog = false }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    // Image source dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Choose photo source") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                            showImageSourceDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Gray)
                        Spacer(Modifier.size(8.dp))
                        Text("Choose from gallery", color = Color.Gray)
                    }

                    OutlinedButton(
                        onClick = {
                            if (!hasCameraPermission) {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            } else {
                                tempImageUri = createFileUri(context)
                                cameraLauncher.launch(tempImageUri!!)
                            }
                            showImageSourceDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Gray)
                        Spacer(Modifier.size(8.dp))
                        Text("Take photo", color = Color.Gray)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }
}

private fun createFileUri(context: android.content.Context): Uri {
    val timestamp = System.currentTimeMillis()
    val imageFile = File(context.cacheDir, "JPEG_$timestamp.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}