package com.example.cityshare.ui.functions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

suspend fun getCurrentLocationAddress(context: Context): String? {
    return try {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED &&
            coarseLocationPermission != PackageManager.PERMISSION_GRANTED
        ){
            Log.w("LocationUtils", "Location permissions not granted")
            return null
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)

        var location: Location? = null
        for (provider in providers) {
            location = try {
                locationManager.getLastKnownLocation(provider)
            } catch (e: SecurityException){
                Log.e("LocationUtils", "Security exception", e)
                null
            }
            if (location != null) break
        }

        location ?: run{
            Log.w("LocationUtils", "No location found")
            return "Unknown"
        }

        withContext(Dispatchers.IO) {
            val url = URL("https://nominatim.openstreetmap.org/reverse?lat=${location.latitude}&lon=${location.longitude}&format=json&addressdetails=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "CityShareApp")
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("AddLocation", "HTTP error: ${connection.responseCode}")
                return@withContext null
            }

            val response = connection.inputStream.bufferedReader().use { it.readText()  }
            val json = JSONObject(response)
            val addressObj = json.getJSONObject("address")
            when {
                addressObj.has("city") -> addressObj.getString("city")
                addressObj.has("town") -> addressObj.getString("town")
                addressObj.has("village") -> addressObj.getString("village")
                addressObj.has("municipality") -> addressObj.getString("municipality")
                else -> "Unknown"
            }
        }
    } catch (e: Exception) {
        Log.e("AddLocation", "Reverse geocoding error", e)
        null
    }
}