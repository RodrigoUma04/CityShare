package com.example.cityshare.ui.screens


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cityshare.ui.components.BottomNavItem
import com.example.cityshare.ui.components.BottomNavigationBar

@Composable
fun MainScreen(){
    val rootNavController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem("Home","home", Icons.Default.Home),
        BottomNavItem("AddLocation","addLocation", Icons.Default.AddCircle),
        BottomNavItem("Message","message", Icons.Default.MailOutline),
        BottomNavItem("Settings","settings", Icons.Default.Settings),
    )

    NavHost(
        navController = rootNavController,
        startDestination = "main_with_nav"
    ){
        composable("main_with_nav"){
            val innerNavController = rememberNavController()

            Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    navController = innerNavController,
                    items = bottomNavItems
                )
            },
            contentWindowInsets = WindowInsets(0)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)) {
                NavHost(
                    navController = innerNavController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize()
                ){
                    composable("home") {
                        Homescreen(
                            onMapClicked = { rootNavController.navigate("map") },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable("addLocation") {
                        AddLocationScreen(
                            Modifier
                                .fillMaxSize()
                                .padding(bottom = 90.dp)
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
        composable("map") {
            MapScreen(
                onBackClicked = {
                    rootNavController.popBackStack()
                }
            )
        }
    }
}