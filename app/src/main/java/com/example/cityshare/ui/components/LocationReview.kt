package com.example.cityshare.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cityshare.ui.functions.getUserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LocationReview(locationId: String){
    var currentUser by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    val authUserId = FirebaseAuth.getInstance().currentUser?.uid

    var reviews by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var averageRating by remember { mutableStateOf(0.0) }
    var isLoadingReviews by remember { mutableStateOf(false) }
    var showWriteReviewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(authUserId) {
        if (authUserId != null) {
            isLoading = true
            currentUser = getUserData(authUserId)
            isLoading = false
        }
    }
    LaunchedEffect(locationId) {
    isLoadingReviews = true
    scope.launch {
        try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("locationId", locationId)
                .get()
                .await()

            reviews = snapshot.documents.mapNotNull { it.data }
            averageRating = reviews.mapNotNull { it["rating"] as? Double }
                .average()
        } catch (e: Exception) {e.printStackTrace()
        } finally {
            isLoadingReviews = false
        }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Button(onClick = {showWriteReviewDialog = true},
            Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSurface)
        )
        {
            Text("Write a Review")
        }
        Text("Reviews", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (averageRating > 0) "Average Rating: %.1f".format(averageRating) else "No reviews yet",
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            repeat(5){
                    index ->
                Icon(
                    imageVector = if (index < averageRating) Icons.Default.Star else Icons.Default.Star,
                    contentDescription = null,
                    tint = if (index < averageRating)  Color.Yellow else Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        if (isLoadingReviews) {
            CircularProgressIndicator()
        } else if (reviews.isEmpty()) {
            Text("No reviews yet")
        }  else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                reviews.forEach { review ->
                    val rating = (review["rating"] as? Number)?.toInt() ?: 0
                    val text = review["text"] as? String ?: ""
                    val username = review["username"] as? String ?: "Anonymous"

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { i ->
                                Icon(
                                    imageVector = if (i < rating) Icons.Default.Star else Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i < rating) Color.Yellow else Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(username, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                        Text(text, fontSize = 14.sp)
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
    if (showWriteReviewDialog){
        WriteReviewDialog(
            locationId = locationId,
            onDismiss = { showWriteReviewDialog = false },
            onSubmit = { rating, reviewText ->
                scope.launch {
                    try {
                        val userId = authUserId
                        val username = currentUser!!["username"] as? String ?: "Anonymous"
                        firestore.collection("reviews")
                            .add(
                                mapOf(
                                    "locationId" to locationId,
                                    "userId" to userId,
                                    "username" to username,
                                    "rating" to rating,
                                    "text" to reviewText,
                                    "timestamp" to System.currentTimeMillis()
                                )
                            )
                            .await()
                        // Refresh reviews
                        val snapshot = firestore.collection("reviews")
                            .whereEqualTo("locationId", locationId)
                            .get()
                            .await()
                        reviews = snapshot.documents.mapNotNull { it.data }
                        averageRating = reviews.mapNotNull { it["rating"] as? Number }
                            .map { it.toDouble() }
                            .average()
                    } catch (e: Exception) { e.printStackTrace() }
                }
                showWriteReviewDialog = false
            }
        )
    }
}
@Composable
fun WriteReviewDialog(
    locationId: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, reviewText: String) -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Write a Review", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                // Star rating selector
                Row {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Icon(
                                imageVector = if (index < rating) Icons.Default.Star
                                else Icons.Default.StarBorder,
                                contentDescription = "Star",
                                tint = if (index < rating) Color.Yellow
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }

                // Review text input
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("Write your review") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmit(rating, reviewText) },
                        enabled = rating > 0 && reviewText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}