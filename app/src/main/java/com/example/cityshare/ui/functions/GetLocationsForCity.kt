package com.example.cityshare.ui.functions

import com.google.firebase.firestore.FirebaseFirestore

fun getLocationsForCity(
    firestore: FirebaseFirestore,
    city: String,
    onResult: (List<Map<String, Any>>) -> Unit
){
    firestore
        .collection("cities")
        .whereEqualTo("name",city)
        .get()
        .addOnSuccessListener { citySnap ->
            if (citySnap.isEmpty) {
                onResult(emptyList())
                return@addOnSuccessListener
            }

            val cityDoc = citySnap.documents.first().reference

            cityDoc.collection("locations")
                .get()
                .addOnSuccessListener { locSnap ->
                    val locations = locSnap.map { it.data }
                    onResult(locations)
                }
                .addOnFailureListener {
                    println("Error getting locations: ${it.message}")
                    onResult(emptyList())
                }
        }
        .addOnFailureListener {
            print("Error getting user cities: "+it.message)
            onResult(emptyList())
        }
}