package com.example.cityshare.ui.functions

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun normalizeCityName(cityName: String): String {
    val cleaned = cityName.trim()

    val firstPart = cleaned.split("-", "/", "|")
        .firstOrNull()?.trim() ?: cleaned

    val cityNameMap = mapOf(
        "bruxelles" to "Brussels",
        "brussel" to "Brussels",
        "antwerpen" to "Antwerp",
        "anvers" to "Antwerp",
        "gent" to "Ghent",
        "gand" to "Ghent",
        "liège" to "Liege",
        "luik" to "Liege",
        "mons" to "Mons",
        "bergen" to "Mons",

        // Other common European cities
        "münchen" to "Munich",
        "wien" to "Vienna",
        "köln" to "Cologne",
        "firenze" to "Florence",
        "milano" to "Milan",
        "roma" to "Rome",
        "venezia" to "Venice",
        "lisboa" to "Lisbon",
        "warszawa" to "Warsaw",
        "praha" to "Prague",
        "moskva" to "Moscow"
    )
    val normalized = firstPart.lowercase()
    return cityNameMap[normalized] ?: normalized
}


fun addCityToCollection(
    firestore: FirebaseFirestore,
    city: String,
    onSuccess: () -> Unit = {},
    onError: (String?) -> Unit = {}
){

    val normalizedCity = normalizeCityName(city)

    val cityDocRef = firestore.collection("cities")
        .document(normalizedCity)
    cityDocRef.get()
        .addOnSuccessListener { doc ->
            if (!doc.exists()) {
                cityDocRef.set(mapOf("name" to normalizedCity,
                    "originalName" to city))
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