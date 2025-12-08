package com.example.cityshare.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.HorizontalDivider
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
fun LocationReview(locationId: String) {
    var currentUser by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    val authUserId = FirebaseAuth.getInstance().currentUser?.uid

    var reviews by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
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
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingReviews = false
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = { showWriteReviewDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text("Write a Review")
        }

        if (isLoadingReviews) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (reviews.isEmpty()) {
            Text(
                text = "No reviews yet. Be the first to review!",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                reviews.forEach { review ->
                    val rating = (review["rating"] as? Number)?.toInt() ?: 0
                    val text = review["text"] as? String ?: ""
                    val username = review["username"] as? String ?: "Anonymous"

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = username,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(5) { i ->
                                    Icon(
                                        imageVector = if (i < rating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (i < rating) Color(0xFFFFC107) else Color.LightGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = text,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }

    if (showWriteReviewDialog) {
        WriteReviewDialog(
            locationId = locationId,
            onDismiss = { showWriteReviewDialog = false },
            onSubmit = { rating, reviewText ->
                scope.launch {
                    try {
                        val userId = authUserId
                        val username = currentUser?.get("username") as? String ?: "Anonymous"
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Write a Review", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                // Star rating selector
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Icon(
                                imageVector = if (index < rating) Icons.Default.Star
                                else Icons.Default.StarBorder,
                                contentDescription = "Star ${index + 1}",
                                tint = if (index < rating) Color(0xFFFFC107)
                                else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(32.dp)
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
                    minLines = 3,
                    maxLines = 5
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )) { Text("Cancel") }
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