package com.example.cityshare.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.cityshare.R
import com.example.cityshare.ui.components.CitySelectionPopup
import com.example.cityshare.ui.components.LocationCard
import com.example.cityshare.ui.components.LocationDetailPopup
import com.example.cityshare.ui.components.MapCategoryChips
import com.example.cityshare.ui.components.MapCitySelector
import com.example.cityshare.ui.functions.addCityToCollection
import com.example.cityshare.ui.state.rememberCityState
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val cityState = rememberCityState(context, db)

    var showLocationPopup by remember { mutableStateOf(false) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var selectedLocation by remember { mutableStateOf<Map<String, Any>?>(null) }

    val filteredLocations = cityState.getFilteredLocations()

    DisposableEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        onDispose {
            mapView?.onDetach()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)

                    if (hasLocationPermission) {
                        val gpsProvider = GpsMyLocationProvider(ctx)
                        val myLocationOverlay = MyLocationNewOverlay(gpsProvider, this)
                        myLocationOverlay.enableMyLocation()
                        overlays.add(myLocationOverlay)
                        locationOverlay = myLocationOverlay

                        try {
                            val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                            if (isGpsEnabled || isNetworkEnabled) {
                                val location = if (isGpsEnabled) {
                                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                } else {
                                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                }

                                location?.let {
                                    val userLocation = GeoPoint(it.latitude, it.longitude)
                                    controller.setCenter(userLocation)
                                    controller.setZoom(15.0)
                                    myLocationOverlay.enableFollowLocation()
                                }

                                if (location == null) {
                                    gpsProvider.startLocationProvider { loc, _ ->
                                        loc?.let {
                                            val userLocation = GeoPoint(it.latitude, it.longitude)
                                            controller.animateTo(userLocation)
                                        }
                                    }
                                }
                            }
                        } catch (e: SecurityException) {
                            Log.e("MapScreen", "Location permission error", e)
                        }
                    }

                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                val markerIcon = ContextCompat.getDrawable(context, R.drawable.pin)

                view.overlays.removeAll { it is Marker }

                filteredLocations.forEach { loc ->
                    val lat = loc["latitude"] as? Double ?: return@forEach
                    val lng = loc["longitude"] as? Double ?: return@forEach
                    val name = loc["name"] as? String ?: "Unknown"

                    val marker = Marker(view).apply {
                        position = GeoPoint(lat, lng)
                        title = name
                        icon = markerIcon
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            selectedLocation = loc
                            true
                        }
                    }
                    view.overlays.add(marker)
                }
                view.invalidate()
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MapCitySelector(
                selectedCity = cityState.selectedCity,
                onClick = { cityState.showCityPopup = true }
            )

            Spacer(Modifier.height(12.dp))

            MapCategoryChips(
                selectedCategory = cityState.selectedCategory,
                onCategorySelected = { category ->
                    cityState.selectedCategory = category
                    Log.d("MapScreen", "Category selected: $category")
                }
            )
        }

        if (hasLocationPermission) {
            FloatingActionButton(
                onClick = {
                    locationOverlay?.let { overlay ->
                        overlay.myLocation?.let { location ->
                            mapView?.controller?.animateTo(location)

                            cityState.currentCity?.let { currentCity ->
                                cityState.updateSelectedCity(currentCity, cityState.allCities)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Center on location"
                )
            }
        }

        selectedLocation?.let { loc ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            selectedLocation = null
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { }
                        }
                ) {
                    LocationCard(
                        location = loc,
                        onClick = {
                                selectedLocation = loc
                                showLocationPopup = true
                        }
                    )
                }
            }
        }

        if (cityState.showCityPopup) {
            CitySelectionPopup(
                currentCity = cityState.currentCity ?: "Unknown",
                cities = cityState.allCities,
                showDialog = cityState.showCityPopup,
                onCitySelected = { city ->
                    cityState.selectedCity = city
                    cityState.showCityPopup = false
                },
                onAddCity = { city ->
                    addCityToCollection(
                        firestore = db,
                        city = city,
                        onSuccess = {
                            Log.d("MapScreen", "City added successfully: $city")
                            cityState.allCities = cityState.allCities + city
                            cityState.selectedCity = city
                        },
                        onError = { error ->
                            Log.e("MapScreen", "Error adding city: $error")
                        }
                    )
                    cityState.showCityPopup = false
                },
                onDismiss = { cityState.showCityPopup = false }
            )
        }
        LocationDetailPopup(
            location = selectedLocation,
            showDialog = showLocationPopup,
            onDismiss = { showLocationPopup = false }
        )
    }
}