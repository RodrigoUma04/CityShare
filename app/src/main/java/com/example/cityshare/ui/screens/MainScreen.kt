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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cityshare.ui.components.BottomNavItem
import com.example.cityshare.ui.components.BottomNavigationBar

@Composable
fun MainScreen(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem("Home", "home", Icons.Default.Home),
        BottomNavItem("Map", "map", Icons.Default.LocationOn),
        BottomNavItem("AddLocation", "addLocation", Icons.Default.AddCircle),
        BottomNavItem("Message", "message", Icons.Default.MailOutline),
        BottomNavItem("Profile", "profile", Icons.Default.Person),
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
                        onNavigateToChat = { userId ->
                            navController.navigate("chat/$userId")
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                composable("map") {
                    MapScreen(
                        onNavigateToChat = { userId ->
                            navController.navigate("chat/$userId")
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                composable("addLocation") {
                    AddLocationScreen(
                        Modifier.fillMaxSize()
                    )
                }
                composable("message") {
                    MessagesScreen(
                        onChatClick = { userId ->
                            navController.navigate("chat/$userId")
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                composable("profile") {
                    ProfileScreen(
                        onLogout = onLogout,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Chat screen route
                composable(
                    route = "chat/{userId}",
                    arguments = listOf(
                        navArgument("userId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                    ChatScreen(
                        otherUserId = userId,
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}