package com.example.cityshare.ui.functions

import com.google.firebase.firestore.FirebaseFirestore

fun getAllLocations(
    firestore: FirebaseFirestore,
    onResult: (List<Map<String, Any>>) -> Unit
) {
    firestore
        .collection("cities")
        .get()
        .addOnSuccessListener { citySnap ->
            if (citySnap.isEmpty) {
                onResult(emptyList())
                return@addOnSuccessListener
            }

            val allLocations = mutableListOf<Map<String, Any>>()
            var remaining = citySnap.size()

            for (cityDoc in citySnap.documents) {
                cityDoc.reference
                    .collection("locations")
                    .get()
                    .addOnSuccessListener { locSnap ->
                        locSnap.forEach { allLocations.add(it.data) }
                    }
                    .addOnFailureListener {
                        println("Error loading locations: ${it.message}")
                    }
                    .addOnCompleteListener {
                        remaining--
                        if (remaining == 0) {
                            onResult(allLocations)
                        }
                    }
            }
        }
        .addOnFailureListener {
            println("Error loading cities: ${it.message}")
            onResult(emptyList())
        }
}
