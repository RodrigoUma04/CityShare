package com.example.cityshare.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SuggestionItem(city: String, onClick: (String) -> Unit){
    Row(Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
        .clickable{onClick(city)},
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(Icons.Default.LocationOn, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(city)
    }
}