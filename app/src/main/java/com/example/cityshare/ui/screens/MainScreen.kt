package com.example.cityshare.ui.screens


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cityshare.ui.components.BottomNavItem
import com.example.cityshare.ui.components.BottomNavigationBar

@Composable
fun MainScreen(){
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem("Home","home", Icons.Default.Home),
        BottomNavItem("AddLocation","addLocation", Icons.Default.AddCircle),
        BottomNavItem("Message","message", Icons.Default.MailOutline),
        BottomNavItem("Settings","settings", Icons.Default.Settings),
    )
    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController, items = bottomNavItems) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ){
            composable("home") {Homescreen(Modifier.fillMaxSize())}
            composable("addLocation") {AddLocationScreen(Modifier.fillMaxSize())}
            composable("message") {MessageScreen(Modifier.fillMaxSize())}
            composable("settings") {SettingScreen(Modifier.fillMaxSize())}
        }
    }
}