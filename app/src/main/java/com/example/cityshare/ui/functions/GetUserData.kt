package com.example.cityshare.ui.functions

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

suspend fun getUserData(userId: String): Map<String, Any>? {
    return try{
        val firestore = FirebaseFirestore.getInstance()
        val userDoc = firestore.collection("users")
            .document(userId)
            .get()
            .await()
        if (userDoc.exists()) userDoc.data else null
        } catch (e: Exception){
            e.printStackTrace()
            null
    }
}