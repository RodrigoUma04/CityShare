package com.example.cityshare.ui.functions

import com.google.firebase.firestore.FirebaseFirestore

fun getLocationsForCity(
    firestore: FirebaseFirestore,
    city: String,
    onResult: (List<Map<String, Any>>) -> Unit
){
    firestore
        .collection("cities")
        .whereEqualTo("city",city)
        .get()
        .addOnSuccessListener { snap ->
            val locations = snap.map { it.data }
            onResult(locations)
        }
        .addOnFailureListener {
            print("Error getting user cities: "+it.message)
            onResult(emptyList())
        }
}