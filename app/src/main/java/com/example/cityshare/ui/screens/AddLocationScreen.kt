package com.example.cityshare.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocationScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Form state
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var priceRange by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isValidatingAddress by remember { mutableStateOf(false) }
    var addressValid by remember { mutableStateOf<Boolean?>(null) }

    // Categories
    val categories = listOf(
        "Restaurant", "Museum", "Club/Nightlife", "Park/Nature",
        "Shopping", "Entertainment", "Cafe/Bar", "Historic Site", "Sports/Recreation"
    )
    val priceRanges = listOf("€", "€€", "€€€", "€€€€")

    // Category dropdown
    var categoryExpanded by remember { mutableStateOf(false) }
    var priceExpanded by remember { mutableStateOf(false) }

    //Firebase Login
    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener { authResult ->
                    Log.d("AddLocation", "Anonymous sign-in success: ${authResult.user?.uid}")
                }
                .addOnFailureListener { e ->
                    Log.w("AddLocation", "Anonymous sign-in failed", e)
            }
        }
    }
    // Location permission
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
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    // Camera permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (selectedImages.size + uris.size <= 10) {
            selectedImages = selectedImages + uris
        } else {
            errorMessage = "Maximum 10 images allowed"
        }
    }

    fun createFileUri(): Uri{
        val timestamp = System.currentTimeMillis()
        val imageFile = File(
            context.cacheDir,
            "JPEG_${timestamp}.jpg"
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    // Camera launcher
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            if (selectedImages.size < 10) {
                selectedImages = selectedImages + tempImageUri!!
            } else {
                errorMessage = "Maximum 10 images allowed"
            }
        }
        tempImageUri = null
    }

    suspend fun validateAndGeocodeAddress(addr: String): Pair<Boolean, Triple<Double, Double, String>?> {
        return try {
            withContext(Dispatchers.IO) {
                val encodedAddress = URLEncoder.encode(addr, "UTF-8")
                val url = URL("https://nominatim.openstreetmap.org/search?q=$encodedAddress&format=json&limit=1&addressdetails=1")

                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "CityShareApp")
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("AddLocation", "HTTP error: ${connection.responseCode}")
                    return@withContext Pair(false, null)
                }
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                if (response =="[]") return@withContext Pair(false, null)

                val json = JSONObject(response.substring(1, response.length - 1))
                val addressObj = json.getJSONObject("address")
                val city = when {
                    addressObj.has("city") -> addressObj.getString("city")
                    addressObj.has("town") -> addressObj.getString("town")
                    addressObj.has("village") -> addressObj.getString("village")
                    addressObj.has("municipality") -> addressObj.getString("municipality")
                    else -> "Unknown"
                }
                val lat = json.getDouble("lat")
                val lon = json.getDouble("lon")
                Pair(true, Triple(lat, lon, city))
                }
        } catch (e: Exception) {
            Log.e("AddLocation", "Geocoding error", e)
            Pair(false, null)
        }
    }

    suspend fun getCurrentLocationAddress(): String? {
        return try {
            if (!hasLocationPermission) {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
                return null
            }

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                return null;
            }

            val location = when{
                isGpsEnabled -> locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                else -> locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } ?: return null


            withContext(Dispatchers.IO) {
                    val url = URL(
                        "https://nominatim.openstreetmap.org/reverse?lat=${location.latitude}&lon=${location.longitude}&format=json&addressdetails=1"
                    )
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

                    val road = addressObj.optString("road","")
                    val houseNumber = addressObj.optString("house_number","")
                    val postcode = addressObj.optString("postcode","")
                    val city = addressObj.optString("city",
                        addressObj.optString("town",
                        addressObj.optString("village",
                         addressObj.optString("municipality","")
                        )
                        )
                    )
                    val rawCountry = addressObj.optString("country","")
                    val country = rawCountry.split("/").first().trim()

                    listOf(
                        listOf(road, houseNumber).filter { it.isNotEmpty() }.joinToString(" "),
                        listOf(postcode, city).filter { it.isNotEmpty() }.joinToString(" "),
                        country
                    ).filter { it.isNotEmpty() }
                        .joinToString(", ")
                }
        } catch (e: Exception) {
            Log.e("AddLocation", "Reverse geocoding error", e)
            null
        }
    }

    suspend fun uploadImages(uris: List<Uri>): List<String> {
        val storage = FirebaseStorage.getInstance()
        val uploadedUrls = mutableListOf<String>()

        for (uri in uris) {
            val filename = "${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child("locations/$filename")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            uploadedUrls.add(downloadUrl)
        }

        return uploadedUrls
    }

    suspend fun submitLocation() {
        if (name.isBlank() || description.isBlank() || address.isBlank() ||
            category.isBlank() || priceRange.isBlank()) {
            errorMessage = "Please fill in all required fields"
            return
        }

        if (selectedImages.isEmpty()) {
            errorMessage = "Please add at least one image"
            return
        }

        isLoading = true
        errorMessage = null

        try {
            // Validate address and get coordinates
            val (valid, geoData) = validateAndGeocodeAddress(address)
            if (!valid || geoData == null) {
                errorMessage = "Invalid address. Please enter a valid address."
                isLoading = false
                return
            }

            val (latitude, longitude, city) = geoData

            // Upload images
            val imageUrls = uploadImages(selectedImages)

            // Create location document
            val db = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid ?: return

            val cityDoc = db.collection("cities").document(city)
            val citySnapshot = cityDoc.get().await()
            if (!citySnapshot.exists()) {
                cityDoc.set(mapOf("name" to city)).await()
            }

            val locationData = hashMapOf(
                "name" to name,
                "description" to description,
                "address" to address,
                "latitude" to latitude,
                "longitude" to longitude,
                "category" to category,
                "priceRange" to priceRange,
                "imageUrls" to imageUrls,
                "addedBy" to userId,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "averageRating" to 0.0,
                "totalRatings" to 0
            )

            db.collection("cities")
                .document(city)
                .collection("locations")
                .add(locationData)
                .await()

            successMessage = "Location added successfully!"

            // Reset form
            name = ""
            description = ""
            address = ""
            category = ""
            priceRange = ""
            selectedImages = emptyList()
            addressValid = null

        } catch (e: Exception) {
            Log.e("AddLocation", "Error submitting location", e)
            errorMessage = "Failed to add location: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            text = "Add New Location",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        // Address with validation
        Column {
            OutlinedTextField(
                value = address,
                onValueChange = {
                    address = it
                    addressValid = null
                },
                label = { Text("Address *") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (isValidatingAddress) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                },
                supportingText = {
                    when (addressValid) {
                        true -> Text("✓ Valid address", color = MaterialTheme.colorScheme.primary)
                        false -> Text("✗ Invalid address", color = MaterialTheme.colorScheme.error)
                        null -> null
                    }
                }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isValidatingAddress = true
                            val (valid, _) = validateAndGeocodeAddress(address)
                            addressValid = valid
                            if (!valid) {
                                errorMessage = "Invalid address. Please enter a valid address."
                            } else {
                                errorMessage = null
                            }
                            isValidatingAddress = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                ) {
                    Text("Validate Address")
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isValidatingAddress = true
                            val addr = getCurrentLocationAddress()
                            if (addr != null) {
                                address = addr
                                addressValid = true
                            } else {
                                errorMessage = "Could not get current location"
                            }
                            isValidatingAddress = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Use Current")
                }
            }
        }

        // Category dropdown
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it }
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            category = cat
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // Price range dropdown
        ExposedDropdownMenuBox(
            expanded = priceExpanded,
            onExpandedChange = { priceExpanded = it }
        ) {
            OutlinedTextField(
                value = priceRange,
                onValueChange = {},
                readOnly = true,
                label = { Text("Price Range *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priceExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = priceExpanded,
                onDismissRequest = { priceExpanded = false }
            ) {
                priceRanges.forEach { price ->
                    DropdownMenuItem(
                        text = { Text(price) },
                        onClick = {
                            priceRange = price
                            priceExpanded = false
                        }
                    )
                }
            }
        }

        // Images
        Column {
            Text("Images * (1-10)", style = MaterialTheme.typography.titleMedium)

            if (selectedImages.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedImages) { uri ->
                        Box {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { selectedImages = selectedImages - uri },
                                modifier = Modifier.align(Alignment.TopEnd),
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Gallery")
                }

                OutlinedButton(
                    onClick = {
                        if (!hasCameraPermission) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            try {
                                val uri = createFileUri()
                                tempImageUri = uri
                                cameraLauncher.launch(uri)
                            } catch (e: Exception) {
                                Log.e("AddLocation", "Error creating file", e)
                                errorMessage = "Failed to open camera: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                ) {
                    Text("Camera")
                }
            }
        }

        // Error/Success messages
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        successMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        // Submit button
        Button(
            onClick = { scope.launch { submitLocation() } },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Add Location")
            }
        }
        if (successMessage != null) {
            AlertDialog(
                onDismissRequest = { successMessage = null },
                title = { Text("Success") },
                text = { Text(successMessage!!) },
                confirmButton = {
                    TextButton(onClick = { successMessage = null }) {
                        Text("OK")
                    }
                }
            )
        }
        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = { Text("Error") },
                text = { Text(errorMessage!!) },
                confirmButton = {
                    TextButton(onClick = { errorMessage = null }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}