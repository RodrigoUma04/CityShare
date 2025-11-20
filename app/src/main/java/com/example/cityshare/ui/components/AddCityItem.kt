package com.example.cityshare.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AddCityItem(city:String, onClick:(String) -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable{onClick(city)},
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(Icons.Default.Add, null)
        Spacer(Modifier.width(8.dp))
        Text("Add \"$city\"")
    }
}

fun addCitySmart(firestore: FirebaseFirestore, auth: FirebaseAuth, city: String){
    val cityRef = firestore.collection("cities")

    cityRef.whereEqualTo("name",city)
        .get()
        .addOnSuccessListener { snap ->
            if (snap.isEmpty){
                val newCity = mapOf("name" to city,
                    "addedAt" to Timestamp.now() )
                cityRef.add(newCity)
            }
            addToUserCityList( firestore, auth,city)
        }
}

fun addToUserCityList(firestore: FirebaseFirestore, auth: FirebaseAuth, city: String){
    val uid = auth.currentUser?.uid ?: return
    val userCity = mapOf("name" to city,
        "addedAt" to Timestamp.now() )
    firestore.collection("users")
        .document(uid)
        .collection("cities")
        .add(userCity)
        .addOnFailureListener {
            print("Error adding user city: "+it.message)
        }
}