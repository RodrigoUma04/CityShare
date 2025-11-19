package com.example.cityshare.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector
)
@Composable
fun BottomNavigationBar(navController: NavController,items: List<BottomNavItem>){
    NavigationBar{
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.name) },
                label = { Text(item.name) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route){
                        navController.navigate(item.route){
                            popUpTo ( navController.graph.startDestinationId) {saveState= true }
                            launchSingleTop = true
                            restoreState= true
                        }
                    }
                }
            )
        }
    }
}