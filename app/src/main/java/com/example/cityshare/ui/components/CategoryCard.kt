package com.example.cityshare.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Category(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

val categories = listOf(
    Category("Restaurant", Icons.Default.Restaurant, Color(0xFFFF6B6B)),
    Category("Museum", Icons.Default.Museum, Color(0xFF4ECDC4)),
    Category("Club/Nightlife", Icons.Default.MusicNote, Color(0xFF9B59B6)),
    Category("Park/Nature", Icons.Default.Park, Color(0xFF2ECC71)),
    Category("Shopping", Icons.Default.ShoppingBag, Color(0xFFF39C12)),
    Category("Entertainment", Icons.Default.Movie, Color(0xFFE74C3C)),
    Category("Cafe/Bar", Icons.Default.LocalCafe, Color(0xFF8B4513)),
    Category("Historic Site", Icons.Default.Place, Color(0xFF34495E)),
    Category("Sports/Recreation", Icons.Default.Sports, Color(0xFF3498DB))
)

@Composable
fun CategorySelector(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Show clear button when a category is selected
        if (selectedCategory != null) {
            item {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                        .clickable { onCategorySelected(null) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear filter",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Show only selected category or all categories
        val categoriesToShow = if (selectedCategory != null) {
            categories.filter { it.name == selectedCategory }
        } else {
            categories
        }

        items(categoriesToShow) { category ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                CategoryCard(
                    category = category,
                    isSelected = category.name == selectedCategory,
                    onClick = { onCategorySelected(category.name) }
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) category.color else category.color.copy(alpha = 0.15f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = category.name,
            tint = if (isSelected) Color.White else category.color,
            modifier = Modifier.size(32.dp)
        )

        Text(
            text = category.name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else category.color,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}