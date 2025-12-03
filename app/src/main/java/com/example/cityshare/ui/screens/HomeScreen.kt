package com.example.cityshare.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cityshare.ui.components.CitySelectionPopup
import com.example.cityshare.ui.functions.addCityToCollection
import com.example.cityshare.ui.functions.getLocationsForCity
import com.example.cityshare.ui.functions.getCities
import com.example.cityshare.ui.functions.getCurrentLocationAddress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.cityshare.ui.components.CategorySelector
import com.example.cityshare.ui.components.LocationsList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Homescreen(
    onMapClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var currentCity by remember { mutableStateOf<String?>(null) }
    var allCities by remember { mutableStateOf(listOf<String>()) }
    var selectedCity by remember { mutableStateOf("Unknown") }
    var locationsInCity by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var showCityPopup by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    fun findMatchingCity(address: String?, availableCities: List<String>): String? {
        if (address == null) {
            Log.d("Homescreen", "Address is null, cannot match city")
            return null
        }

        Log.d("Homescreen", "Checking address: '$address'")
        Log.d("Homescreen", "Against available cities: $availableCities")

        // Check if any city name appears in the address
        val matchingCity = availableCities.find { city ->
            val contains = address.contains(city, ignoreCase = true)
            Log.d("Homescreen", "Does '$address' contain '$city'? $contains")
            contains
        }

        Log.d("Homescreen", "Matching city result: ${matchingCity ?: "No match found"}")
        return matchingCity
    }

    fun updateSelectedCity(address: String?, availableCities: List<String>) {
        Log.d("Homescreen", "updateSelectedCity called")
        Log.d("Homescreen", "Address: $address")
        Log.d("Homescreen", "Available cities: $availableCities")

        val matchedCity = findMatchingCity(address, availableCities)

        selectedCity = when {
            matchedCity != null -> {
                Log.d("Homescreen", "Selected matched city: $matchedCity")
                matchedCity
            }
            availableCities.isNotEmpty() -> {
                Log.d("Homescreen", "No match, selecting first city: ${availableCities.first()}")
                availableCities.first()
            }
            else -> {
                Log.d("Homescreen", "No cities available, selecting Unknown")
                "Unknown"
            }
        }

        Log.d("Homescreen", "Final selected city: $selectedCity")
    }

    LaunchedEffect(Unit) {
        Log.d("Homescreen", "Fetching current location...")
        val city = getCurrentLocationAddress(context)
        Log.d("Homescreen", "Location fetched: $city")
        currentCity = city
    }

    LaunchedEffect(Unit) {
        Log.d("Homescreen", "Fetching cities from Firestore...")
        getCities(db) { cities ->
            Log.d("Homescreen", "Cities fetched: $cities")
            allCities = cities
        }
    }

    LaunchedEffect(currentCity, allCities) {
        Log.d("Homescreen", "LaunchedEffect triggered - currentCity: $currentCity, allCities: $allCities")
        if (currentCity != null && allCities.isNotEmpty()) {
            updateSelectedCity(currentCity, allCities)
        } else if (allCities.isNotEmpty() && selectedCity == "Unknown") {
            // Fallback: if we have cities but no location yet, select first city
            Log.d("Homescreen", "No location yet, but cities available. Selecting first.")
            selectedCity = allCities.first()
        }
    }

    LaunchedEffect(selectedCity) {
        Log.d("Homescreen", "Selected city changed to: $selectedCity")
        if (selectedCity != "Unknown" && selectedCity.isNotEmpty()) {
            getLocationsForCity(db, selectedCity) { locations ->
                Log.d("Homescreen", "Locations fetched for $selectedCity: ${locations.size} locations")
                locationsInCity = locations
            }
        }
    }

    Column(
        modifier = modifier
            .padding(horizontal = 30.dp, vertical = 20.dp)
    ) {
        Row(
            modifier= Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                Modifier
                    .padding(vertical = 4.dp)
                    .clickable { showCityPopup = true }
            ){
                Text(
                    text = selectedCity,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Choose another",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }



        Spacer(Modifier.height(30.dp))

        var searchText by remember { mutableStateOf("") }

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search by name", color = Color.LightGray) },
            trailingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.LightGray.copy(alpha = 0.3f),
                unfocusedContainerColor = Color.LightGray.copy(alpha = 0.3f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(Modifier.height(30.dp))

        Text(
            text = "Categories",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        CategorySelector(
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                selectedCategory = category
                Log.d("Homescreen", "Category selected: $category")
                // TODO: Filter locationsInCity based on selectedCategory
            }
        )

        Spacer(Modifier.height(30.dp))

        Text(
            text = "Locations",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        LocationsList(
            locations = locationsInCity,
            selectedCategory = selectedCategory,
            onLocationClick = { location ->
                Log.d("Homescreen", "Location clicked: ${location["name"]}")
                // TODO: Navigate to location details screen
            },
            modifier = Modifier.fillMaxWidth()
        )

        CitySelectionPopup(
            currentCity = currentCity ?: "Unknown",
            cities = allCities,
            showDialog = showCityPopup,
            onCitySelected = {city ->
                selectedCity = city
                showCityPopup = false
            },
            onAddCity = { city ->
                addCityToCollection(
                    firestore = db,
                    city = city,
                    onSuccess = {
                        print("Main collection updated")
                        selectedCity = city
                        allCities = allCities + city
                        },
                    onError = { print("Error updating main collection: $it") }
                )
                showCityPopup = false
            },
            onDismiss = { showCityPopup = false }
        )
    }
}