package com.example.cityshare.ui.screens

import android.content.Context
import android.location.LocationManager
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
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
import com.example.cityshare.ui.components.CategorySelector
import com.example.cityshare.ui.components.CitySelectionPopup
import com.example.cityshare.ui.components.LocationDetailPopup
import com.example.cityshare.ui.components.LocationsList
import com.example.cityshare.ui.functions.addCityToCollection
import com.example.cityshare.ui.state.rememberCityState
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Homescreen(
    onNavigateToChat: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val cityState = rememberCityState(context, db)
    var selectedLocation by remember { mutableStateOf<Map<String, Any>?>(null) }
    var showLocationPopup by remember { mutableStateOf(false) }


    var userLocation by remember { mutableStateOf<android.location.Location?>(null) }

    // Add this LaunchedEffect to get user location:
    LaunchedEffect(Unit) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (isGpsEnabled || isNetworkEnabled) {
                val location = if (isGpsEnabled) {
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                } else {
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }
                userLocation = location
            }
        } catch (e: SecurityException) {
            Log.e("Homescreen", "Location permission error", e)
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
                    .clickable { cityState.showCityPopup = true }
            ){
                Text(
                    text = cityState.selectedCity,
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

        Text(
            text = "Categories",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        CategorySelector(
            selectedCategory = cityState.selectedCategory,
            onCategorySelected = { category ->
                cityState.selectedCategory = category
                Log.d("Homescreen", "Category selected: $category")
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
            locations = cityState.locationsInCity,
            userLocation = userLocation,
            selectedCategory = cityState.selectedCategory,
            onLocationClick = { location ->
                selectedLocation = location
                showLocationPopup = true
            },
            modifier = Modifier.fillMaxWidth()
        )
        CitySelectionPopup(
            currentCity = cityState.currentCity ?: "Unknown",
            cities = cityState.allCities,
            showDialog = cityState.showCityPopup,
            onCitySelected = {city ->
                cityState.selectedCity = city
                cityState.showCityPopup = false
            },
            onAddCity = { city ->
                addCityToCollection(
                    firestore = db,
                    city = city,
                    onSuccess = {
                        print("Main collection updated")
                        cityState.selectedCity = city
                        cityState.allCities = cityState.allCities + city
                        },
                    onError = { print("Error updating main collection: $it") }
                )
                cityState.showCityPopup = false
            },
            onDismiss = { cityState.showCityPopup = false }
        )

        LocationDetailPopup(
            location = selectedLocation,
            showDialog = showLocationPopup,
            onDismiss = { showLocationPopup = false },
            onChatWithUser = {userId ->
                showLocationPopup = false
                onNavigateToChat(userId)
            }
        )
    }
}