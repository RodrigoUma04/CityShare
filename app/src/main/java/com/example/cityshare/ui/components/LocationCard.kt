package com.example.cityshare.ui.components

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.roundToInt

@Composable
fun LocationsList(
    locations: List<Map<String, Any>>,
    selectedCategory: String?,
    userLocation: android.location.Location?,
    onLocationClick: (Map<String, Any>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter locations by category if one is selected
    val filteredLocations = if (selectedCategory != null) {
        locations.filter { location ->
            location["category"] as? String == selectedCategory
        }
    } else {
        locations
    }

    if (filteredLocations.isEmpty()) {
        // Show empty state
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (selectedCategory != null) {
                    "No locations found for $selectedCategory"
                } else {
                    "No locations found for this city"
                },
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredLocations) { location ->
                LocationCard(
                    location = location,
                    userLocation = userLocation,
                    onClick = { onLocationClick(location) }
                )
            }
        }
    }
}

@Composable
fun LocationCard(
    location: Map<String, Any>,
    userLocation: android.location.Location?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = location["name"] as? String ?: "Unknown"
    val description = location["description"] as? String ?: ""
    val category = location["category"] as? String ?: ""
    val address = location["address"] as? String ?: ""
    val averageRating = (location["averageRating"] as? Number)?.toDouble() ?: 0.0
    val totalRatings = (location["totalRatings"] as? Number)?.toInt() ?: 0
    val imageUrls = location["imageUrls"] as? List<*>
    val firstImage = imageUrls?.firstOrNull() as? String

    val latitude = (location["latitude"] as? Number)?.toDouble()
    val longitude = (location["longitude"] as? Number)?.toDouble()

    val distanceText = if (userLocation != null && latitude != null && longitude != null) {
        val locationPoint = Location("").apply {
            this.latitude = latitude
            this.longitude = longitude
        }
        val distanceInMeters = userLocation.distanceTo(locationPoint)
        val distanceInKm = distanceInMeters / 1000

        if (distanceInKm < 1) {
            "${distanceInMeters.roundToInt()} m"
        } else {
            String.format("%.1f km", distanceInKm)
        }
    } else {
        "?"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Image section
            if (firstImage != null) {
                AsyncImage(
                    model = firstImage,
                    contentDescription = name,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder when no image
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Details section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section: Name and category
                Column {
                    Text(
                        text = name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = distanceText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.LightGray
                    )

                    if (category.isNotEmpty()) {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            color = getCategoryColor(category),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Middle section: Description
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom section: Rating and address
                Column {
                    // Rating
                    if (totalRatings > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFA000),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", averageRating),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = " ($totalRatings)",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        Text(
                            text = "No ratings yet",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Restaurant" -> Color(0xFFFF6B6B)
        "Museum" -> Color(0xFF4ECDC4)
        "Club/Nightlife" -> Color(0xFF9B59B6)
        "Park/Nature" -> Color(0xFF2ECC71)
        "Shopping" -> Color(0xFFF39C12)
        "Entertainment" -> Color(0xFFE74C3C)
        "Cafe/Bar" -> Color(0xFF8B4513)
        "Historic Site" -> Color(0xFF34495E)
        "Sports/Recreation" -> Color(0xFF3498DB)
        else -> Color.Gray
    }
}