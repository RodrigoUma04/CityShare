package com.example.cityshare.ui.functions

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun addCityToMainCollection(
    firestore: FirebaseFirestore,
    city: String,
    onSuccess: () -> Unit = {},
    onError: (String?) -> Unit = {}
){
    val cityDocRef = firestore.collection("cities")
        .document(city)
    cityDocRef.get()
        .addOnSuccessListener { doc ->
            if (!doc.exists()) {
                cityDocRef.set(mapOf("name" to city))
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener {
                      onError(it.message)
                    }
            } else {
              onSuccess()
            }
        }
        .addOnFailureListener {
            onError(it.message) }
}
fun addCityToUserCollection(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth,
    city: String,
    onSuccess: () -> Unit = {},
    onError: (String?) -> Unit = {}
){
    val uid = auth.currentUser?.uid ?: return
    val userCityRef = firestore.collection("users")
        .document(uid)
        .collection("cities")
        .document(city)
    userCityRef.get()
        .addOnSuccessListener { doc ->
            if (!doc.exists()) {
                userCityRef.set(mapOf("city" to city))
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener {
                        onError(it.message)
                    }
            } else {
                onSuccess()
            }
        }
        .addOnFailureListener {
            onError(it.message)
        }
}
fun addCitySmart(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth,
    city: String
) {
    addCityToMainCollection(
        firestore,
        city,
        onSuccess = {print("Main collection updated")},
        onError = {print("Error updating main collection: $it")}
    )
    addCityToUserCollection(
        firestore,
        auth,
        city,
        onSuccess = {print("User collection updated")},
        onError = {print("Error updating user collection: $it")}
    )
}