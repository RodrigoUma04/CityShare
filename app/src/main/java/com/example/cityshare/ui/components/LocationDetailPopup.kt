package com.example.cityshare.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.cityshare.ui.functions.getUserData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocationDetailPopup(
    location: Map<String, Any>?,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoadingUser by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var averageRating by remember { mutableStateOf(0.0) }
    var reviewCount by remember { mutableStateOf(0) }
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    // Fetch user data and reviews when dialog opens
    LaunchedEffect(showDialog, location) {
        if (showDialog && location != null) {
            val addedBy = location["addedBy"] as? String
            if (addedBy != null && userData == null) {
                isLoadingUser = true
                userData = getUserData(addedBy)
                isLoadingUser = false
            }

            // Fetch average rating
            val locationId = location["id"] as? String
            if (locationId != null) {
                scope.launch {
                    try {
                        val snapshot = firestore.collection("reviews")
                            .whereEqualTo("locationId", locationId)
                            .get()
                            .await()

                        val reviews = snapshot.documents.mapNotNull { it.data }
                        reviewCount = reviews.size
                        averageRating = reviews.mapNotNull { it["rating"] as? Number }
                            .map { it.toDouble() }
                            .average()
                            .takeIf { !it.isNaN() } ?: 0.0
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            // Reset data when dialog closes
            userData = null
            selectedTabIndex = 0
            averageRating = 0.0
            reviewCount = 0
        }
    }

    if (showDialog && location != null) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Image carousel at the top
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .padding(20.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        val imageUrls = location["imageUrls"] as? List<*>
                        val images =
                            imageUrls?.mapNotNull { it as? String }?.filter { it.isNotEmpty() }
                                ?: emptyList()

                        if (images.isNotEmpty()) {
                            val pagerState = rememberPagerState(pageCount = { images.size })

                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                AsyncImage(
                                    model = images[page],
                                    contentDescription = "Location image ${page + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Page indicator
                            if (images.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    repeat(images.size) { index ->
                                        Box(
                                            modifier = Modifier
                                                .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (pagerState.currentPage == index)
                                                        Color.White
                                                    else
                                                        Color.White.copy(alpha = 0.5f)
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        // Close button overlay
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(36.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }

                    // Title with category icon and rating
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category icon
                        val categoryIcon = getCategoryIcon(location["category"] as? String)
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = "Category",
                            tint = getCategoryColor(location["category"] as? String ?: ""),
                            modifier = Modifier.size(28.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = location["name"] as? String ?: "Location",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Rating display
                            if (averageRating > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = String.format("%.1f", averageRating),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating",
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "($reviewCount)",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tab Row
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("Overview") }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Reviews") }
                        )
                    }

                    // Tab content
                    when (selectedTabIndex) {
                        0 -> OverviewTab(
                            location = location,
                            userData = userData,
                            isLoadingUser = isLoadingUser
                        )
                        1 -> ReviewsTab(
                            locationId = location["id"] as? String ?: ""
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewTab(
    location: Map<String, Any>,
    userData: Map<String, Any>?,
    isLoadingUser: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Added by user section
        if (isLoadingUser) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 3.dp
                )
                Text(
                    text = "Loading user...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (userData != null) {
            val username = userData["username"] as? String ?: "Unknown User"
            val profileImageUrl = userData["profileImageUrl"] as? String

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile picture
                if (profileImageUrl != null && profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile picture of $username",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Default profile icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Username
                Column {
                    Text(
                        text = "Added by",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = username,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (userData != null || isLoadingUser) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }

        // Description with read more
        (location["description"] as? String)?.let { description ->
            if (description.isNotEmpty()) {
                var isExpanded by remember { mutableStateOf(false) }
                val maxLines = if (isExpanded) Int.MAX_VALUE else 3

                Column {
                    Text(
                        text = description,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (description.length > 150 || description.count { it == '\n' } > 2) {
                        TextButton(
                            onClick = { isExpanded = !isExpanded },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (isExpanded) "Read less" else "Read more",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Address
        (location["address"] as? String)?.let { address ->
            if (address.isNotEmpty()) {
                InfoRowWithIcon(
                    icon = Icons.Default.LocationOn,
                    text = address
                )
            }
        }
    }
}

@Composable
fun ReviewsTab(locationId: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        LocationReview(locationId = locationId)
    }
}

@Composable
fun InfoRowWithIcon(
    icon: ImageVector,
    text: String,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp)
        )
        Column {
            if (label != null) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(2.dp))
            }
            Text(
                text = text,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getCategoryIcon(category: String?): ImageVector {
    return when (category?.lowercase()) {
        "restaurant", "food" -> Icons.Default.Restaurant
        "cafe", "coffee", "cafe/bar" -> Icons.Default.Info
        "park", "nature", "park/nature" -> Icons.Default.Star
        "museum", "culture" -> Icons.Default.Home
        "shopping", "mall" -> Icons.Default.ShoppingCart
        "hotel", "accommodation" -> Icons.Default.Home
        "bar", "nightlife", "club/nightlife" -> Icons.Default.Star
        "entertainment" -> Icons.Default.Star
        "historic site" -> Icons.Default.Home
        "sports/recreation" -> Icons.Default.Star
        else -> Icons.Default.Place
    }
}