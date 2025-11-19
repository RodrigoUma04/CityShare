package com.example.cityshare.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cityshare.data.loginWithEmailPassword
import com.example.cityshare.rememberSnackbarHostState
import com.example.cityshare.GlobalSnackbarHost
import com.example.cityshare.data.registerWithEmailPassword
import com.example.cityshare.ui.screens.Homescreen
import com.example.cityshare.ui.screens.LoginScreen
import com.example.cityshare.ui.screens.RegisterScreen
import kotlinx.coroutines.launch

enum class NavigationScreen(){
    Login,
    Register,
    Home
}

@Composable
fun NavigationApp(
    navController: NavHostController = rememberNavController()
){
    val (snackbarHostState, scope) = rememberSnackbarHostState()

    Scaffold(snackbarHost = { GlobalSnackbarHost(snackbarHostState) } ) {
            innerPadding ->

        NavHost(
            navController = navController,
            startDestination = NavigationScreen.Login.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = NavigationScreen.Login.name) {
                LoginScreen(
                    onLoginButtonClicked = { email, password ->
                        loginWithEmailPassword(
                            email = email,
                            password = password,
                            onSuccess = { navController.navigate(NavigationScreen.Home.name) {
                                popUpTo(NavigationScreen.Login.name) { inclusive = true }
                            }
                            },
                            onFailure = { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Login failed: $error")
                                }
                            }
                        )
                    },
                    onRegisterButtonClicked = { navController.navigate(NavigationScreen.Register.name) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(route = NavigationScreen.Register.name) {
                RegisterScreen(
                    onRegisterButtonClicked = { email, password ->
                        registerWithEmailPassword(
                            email = email,
                            password = password,
                            onSuccess = { navController.navigate(NavigationScreen.Home.name) {
                                popUpTo(NavigationScreen.Login.name) { inclusive = true }
                            }
                            },
                            onFailure = { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Registering failed: $error")
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(route = NavigationScreen.Home.name) {
                Homescreen(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}