package com.example.cityshare.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CitySelectionPopup(
    currentCity: String,
    cities: List<String>,
    onCitySelected: (String) -> Unit,
    onAddCity: (String) -> Unit,
    showDialog: Boolean,
    onDismiss: () -> Unit
){
    var newCity by remember { mutableStateOf("") }

    if (showDialog){
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text("Your current city: $currentCity",
                    fontWeight = FontWeight.Bold
                ) },
            text = {
                Column{
                    Text(
                        "Choose another city or add new one:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    //Existing cities
                    LazyColumn(
                        modifier = Modifier.height(150.dp)
                    ) {
                        items(cities) { city ->
                            Text(
                                text = city,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCitySelected(city)
                                        onDismiss()
                                    }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // New city input
                    OutlinedTextField(
                        value = newCity,
                        onValueChange = { newCity = it },
                        placeholder = { Text("Add new city") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCity.isNotBlank()) {
                            onAddCity(newCity)
                            newCity = ""
                            onDismiss()
                        }
                    }
                ) {
                    Text("Add", color = Color.Gray)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}