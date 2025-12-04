package com.example.cityshare.ui.state

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.cityshare.ui.functions.getCities
import com.example.cityshare.ui.functions.getCurrentLocationAddress
import com.example.cityshare.ui.functions.getLocationsForCity
import com.google.firebase.firestore.FirebaseFirestore

class CityState {
    var currentCity by mutableStateOf<String?>(null)
    var allCities by mutableStateOf(listOf<String>())
    var selectedCity by mutableStateOf("Unknown")
    var locationsInCity by mutableStateOf(listOf<Map<String, Any>>())
    var selectedCategory by mutableStateOf<String?>(null)
    var showCityPopup by mutableStateOf(false)

    fun findMatchingCity(address: String?, availableCities: List<String>): String? {
        if (address == null) {
            Log.d("CityState", "Address is null, cannot match city")
            return null
        }

        Log.d("CityState", "Checking address: '$address'")
        Log.d("CityState", "Against available cities: $availableCities")

        val matchingCity = availableCities.find { city ->
            val contains = address.contains(city, ignoreCase = true)
            Log.d("CityState", "Does '$address' contain '$city'? $contains")
            contains
        }

        Log.d("CityState", "Matching city result: ${matchingCity ?: "No match found"}")
        return matchingCity
    }

    fun updateSelectedCity(address: String?, availableCities: List<String>) {
        Log.d("CityState", "updateSelectedCity called")
        Log.d("CityState", "Address: $address")
        Log.d("CityState", "Available cities: $availableCities")

        val matchedCity = findMatchingCity(address, availableCities)

        selectedCity = when {
            matchedCity != null -> {
                Log.d("CityState", "Selected matched city: $matchedCity")
                matchedCity
            }
            availableCities.isNotEmpty() -> {
                Log.d("CityState", "No match, selecting first city: ${availableCities.first()}")
                availableCities.first()
            }
            else -> {
                Log.d("CityState", "No cities available, selecting Unknown")
                "Unknown"
            }
        }

        Log.d("CityState", "Final selected city: $selectedCity")
    }

    fun getFilteredLocations(): List<Map<String, Any>> {
        return if (selectedCategory != null) {
            locationsInCity.filter { location ->
                location["category"] as? String == selectedCategory
            }
        } else {
            locationsInCity
        }
    }
}

@Composable
fun rememberCityState(
    context: Context,
    db: FirebaseFirestore
): CityState {
    val state = remember { CityState() }

    // Fetch current location
    LaunchedEffect(Unit) {
        Log.d("CityState", "Fetching current location...")
        val city = getCurrentLocationAddress(context)
        Log.d("CityState", "Location fetched: $city")
        state.currentCity = city
    }

    // Fetch cities
    LaunchedEffect(Unit) {
        Log.d("CityState", "Fetching cities from Firestore...")
        getCities(db) { cities ->
            Log.d("CityState", "Cities fetched: $cities")
            state.allCities = cities
        }
    }

    // Update selected city when data is available
    LaunchedEffect(state.currentCity, state.allCities) {
        Log.d("CityState", "LaunchedEffect triggered - currentCity: ${state.currentCity}, allCities: ${state.allCities}")
        if (state.currentCity != null && state.allCities.isNotEmpty()) {
            state.updateSelectedCity(state.currentCity, state.allCities)
        } else if (state.allCities.isNotEmpty() && state.selectedCity == "Unknown") {
            Log.d("CityState", "No location yet, but cities available. Selecting first.")
            state.selectedCity = state.allCities.first()
        }
    }

    // Fetch locations for selected city
    LaunchedEffect(state.selectedCity) {
        Log.d("CityState", "Selected city changed to: ${state.selectedCity}")
        if (state.selectedCity != "Unknown" && state.selectedCity.isNotEmpty()) {
            getLocationsForCity(db, state.selectedCity) { locations ->
                Log.d("CityState", "Locations fetched for ${state.selectedCity}: ${locations.size} locations")
                state.locationsInCity = locations
            }
        }
    }

    return state
}