package com.example.cityshare.ui.functions

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val profileImageUrl: String = "",
    val favoriteLocations: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

fun getOrCreateUser(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    onSuccess: (User) -> Unit,
    onError: (String) -> Unit
) {
    val currentUser = auth.currentUser
    if (currentUser == null) {
        onError("No user logged in")
        return
    }

    val uid = currentUser.uid
    val userRef = firestore.collection("users").document(uid)

    userRef.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                // User exists
                val user = document.toObject(User::class.java)
                if (user != null) {
                    onSuccess(user)
                } else {
                    onError("Error parsing user data")
                }
            } else {
                // User doesn't exist
                val newUser = User(
                    uid = uid,
                    email = currentUser.email ?: "",
                    username = currentUser.email?.substringBefore("@") ?: "User",
                    profileImageUrl = "",
                    favoriteLocations = emptyList()
                )

                userRef.set(newUser)
                    .addOnSuccessListener {
                        Log.d("UserFunctions", "User created successfully")
                        onSuccess(newUser)
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserFunctions", "Error creating user", e)
                        onError(e.message ?: "Unknown error")
                    }
            }
        }
        .addOnFailureListener { e ->
            Log.e("UserFunctions", "Error getting user", e)
            onError(e.message ?: "Unknown error")
        }
}

fun updateUsername(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    newUsername: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val uid = auth.currentUser?.uid
    if (uid == null) {
        onError("No user logged in")
        return
    }

    firestore.collection("users").document(uid)
        .update("username", newUsername)
        .addOnSuccessListener {
            Log.d("UserFunctions", "Username updated successfully")
            onSuccess()
        }
        .addOnFailureListener { e ->
            Log.e("UserFunctions", "Error updating username", e)
            onError(e.message ?: "Unknown error")
        }
}

fun uploadProfileImage(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    storage: FirebaseStorage,
    imageUri: Uri,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val uid = auth.currentUser?.uid
    if (uid == null) {
        onError("No user logged in")
        return
    }

    val imageRef = storage.reference
        .child("profile_images")
        .child("$uid.jpg")

    imageRef.putFile(imageUri)
        .addOnSuccessListener {
            imageRef.downloadUrl
                .addOnSuccessListener { downloadUri ->
                    firestore.collection("users").document(uid)
                        .update("profileImageUrl", downloadUri.toString())
                        .addOnSuccessListener {
                            Log.d("UserFunctions", "Profile image updated successfully")
                            onSuccess(downloadUri.toString())
                        }
                        .addOnFailureListener { e ->
                            Log.e("UserFunctions", "Error updating profile image URL", e)
                            onError(e.message ?: "Unknown error")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("UserFunctions", "Error getting download URL", e)
                    onError(e.message ?: "Unknown error")
                }
        }
        .addOnFailureListener { e ->
            Log.e("UserFunctions", "Error uploading image", e)
            onError(e.message ?: "Unknown error")
        }
}

fun logout(
    auth: FirebaseAuth,
    onSuccess: () -> Unit
) {
    auth.signOut()
    Log.d("UserFunctions", "User logged out")
    onSuccess()
}