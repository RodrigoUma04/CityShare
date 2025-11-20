package com.example.cityshare.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cityshare.ui.components.AddCityItem
import com.example.cityshare.ui.components.SuggestionItem
import com.example.cityshare.ui.components.addCitySmart
import com.example.cityshare.ui.components.addToUserCityList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

@Composable
fun Homescreen() {

    var searchText by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(listOf<String>()) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var isSearchFocused by remember { mutableStateOf(false) }


    DisposableEffect(isSearchFocused, searchText) {
        var registration: ListenerRegistration? = null

        if (isSearchFocused && searchText.isNotBlank()) {
            registration = db.collection("cities")
                .orderBy("addedAt", Query.Direction.DESCENDING)
                .limit(4)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        suggestions = snapshot.documents.mapNotNull { it.getString("name") }
                    }
                }
        }else{
                suggestions = emptyList()
            }
        onDispose {
            registration?.remove()
            suggestions = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier= Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Antwerp",
                style = MaterialTheme.typography.headlineSmall
            )
            IconButton(onClick = {}) {Icon(Icons.Default.Place, contentDescription = "Map")
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth()){
            OutlinedTextField(
                value = searchText,
                onValueChange = {searchText = it },
                modifier = Modifier.fillMaxWidth()
                    .onFocusChanged{ focusState ->
                        isSearchFocused = focusState.isFocused

                    },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                placeholder = {Text("Add cities...")},
                trailingIcon = {
                    if (searchText.isNotEmpty()){
                        IconButton(onClick = {searchText = ""}) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        if (searchText.isNotBlank() && isSearchFocused){
            AddCityItem(searchText) { typed ->
                addCitySmart(db, auth, typed)
            }
            Spacer(Modifier.height(4.dp))

            Column(modifier = Modifier.fillMaxWidth()
                .heightIn(max= 200.dp)
            ) {
                suggestions.forEach { city ->
                    SuggestionItem(city) {
                        addToUserCityList(db, auth, city)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center){
            Text("Welcome to home")
        }
    }
}