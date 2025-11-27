package com.example.cityshare.ui.functions

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun getUserCities(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth,
    onResult: (List<String>) -> Unit
){
    val uid = auth.currentUser?.uid ?: return
    firestore
        .collection("users")
        .document(uid)
        .collection("cities")
        .get()
        .addOnSuccessListener { snap ->
            val cities = snap.documents.mapNotNull { it.getString("city") }
            onResult(cities)
        }
        .addOnFailureListener {
            print("Error getting user cities: "+it.message)
            onResult(emptyList())
        }
}

