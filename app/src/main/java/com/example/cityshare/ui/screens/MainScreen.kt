package com.example.cityshare.ui.screens


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        BottomNavItem("Map","map", Icons.Default.LocationOn),
        BottomNavItem("AddLocation","addLocation", Icons.Default.AddCircle),
        BottomNavItem("Message","message", Icons.Default.MailOutline),
        BottomNavItem("Settings","settings", Icons.Default.Settings),
        )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = bottomNavItems
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("home") {
                    Homescreen(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                composable("map") {
                    MapScreen(
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
                composable("addLocation") {
                    AddLocationScreen(
                        Modifier
                            .fillMaxSize()
                    )
                }
                composable("message") {
                    MessageScreen(
                        Modifier.fillMaxSize()
                    )
                }
                composable("settings") {
                    SettingScreen(
                        Modifier.fillMaxSize()
                    )

                }
            }
        }
    }

}