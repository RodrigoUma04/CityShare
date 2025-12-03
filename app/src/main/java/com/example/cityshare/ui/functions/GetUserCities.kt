package com.example.cityshare.ui.functions

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun getCities(
    firestore: FirebaseFirestore,
    onResult: (List<String>) -> Unit
){
    firestore
        .collection("cities")
        .get()
        .addOnSuccessListener { snap ->
            val cities = snap.documents.mapNotNull { it.getString("name") }
            onResult(cities)
        }
        .addOnFailureListener {
            print("Error getting user cities: "+it.message)
            onResult(emptyList())
        }
}

