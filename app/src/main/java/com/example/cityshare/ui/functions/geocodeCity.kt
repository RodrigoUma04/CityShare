package com.example.cityshare.ui.functions

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import java.net.URLEncoder
suspend fun geocodeCity(cityName: String): Pair<Double, Double>? {
    return withContext(Dispatchers.IO) {
        try {
            val encodedCity = URLEncoder.encode(cityName, "UTF-8")
            val urlString = "https://nominatim.openstreetmap.org/search?q=$encodedCity&format=json&limit=1"
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.setRequestProperty("User-Agent", "CityShareApp/1.0")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(response)

            if (jsonArray.length() > 0) {
                val result = jsonArray.getJSONObject(0)
                val lat = result.getDouble("lat")
                val lon = result.getDouble("lon")
                Log.d("geocodeCity", "Successfully geocoded $cityName to: $lat, $lon")
                Pair(lat, lon)
            } else {
                Log.w("geocodeCity", "No results found for city: $cityName")
                null
            }
        } catch (e: Exception) {
            Log.e("geocodeCity", "Error geocoding city: $cityName", e)
            null
        }
    }
}