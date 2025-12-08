package com.example.cityshare.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocationDetailPopup(
    location: Map<String, Any>?,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                        val images = imageUrls?.mapNotNull { it as? String }?.filter { it.isNotEmpty() } ?: emptyList()

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

                    // Scrollable content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title with category icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category icon
                            val categoryIcon = getCategoryIcon(location["category"] as? String)
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = "Category",
                                tint = getCategoryColor(location["category"]as String),
                                modifier = Modifier.size(28.dp)
                            )

                            Text(
                                text = location["name"] as? String ?: "Location",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
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
            }
        }
    }
}

@Composable
private fun InfoRowWithIcon(
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

private fun getCategoryIcon(category: String?): ImageVector {
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