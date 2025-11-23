package com.example.cityshare.ui.screens

import android.Manifest
import android.R
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapScreen(
    onBackClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
                        // Use osmdroid's built-in location provider (works without Google Services)
                        val gpsProvider = GpsMyLocationProvider(ctx)
                        val myLocationOverlay = MyLocationNewOverlay(gpsProvider, this)
                        myLocationOverlay.enableMyLocation()
                        overlays.add(myLocationOverlay)
                        locationOverlay = myLocationOverlay

                        // Get current location using Android's LocationManager
                        try {
                            val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                            // Check if GPS is enabled
                            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                            if (isGpsEnabled || isNetworkEnabled) {
                                // Try GPS first, then network
                                val location = if (isGpsEnabled) {
                                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                } else {
                                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                }

                                location?.let {
                                    val userLocation = GeoPoint(it.latitude, it.longitude)
                                    controller.setCenter(userLocation)
                                    controller.setZoom(15.0)

                                    // Enable follow mode after centering
                                    myLocationOverlay.enableFollowLocation()
                                }

                                // If no last known location, request location updates
                                if (location == null) {
                                    gpsProvider.startLocationProvider { loc, _ ->
                                        loc?.let {
                                            val userLocation = GeoPoint(it.latitude, it.longitude)
                                            controller.animateTo(userLocation)
                                            // Only center once, don't follow
                                        }
                                    }
                                }
                            }
                        } catch (e: SecurityException) {
                            // Permission not granted
                        }
                    }

                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Back button
        IconButton(
            onClick = onBackClicked,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        // Center on location button
        if (hasLocationPermission) {
            FloatingActionButton(
                onClick = {
                    locationOverlay?.let { overlay ->
                        overlay.myLocation?.let { location ->
                            mapView?.controller?.setZoom(15.0)
                            mapView?.controller?.animateTo(location)
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
    }
}