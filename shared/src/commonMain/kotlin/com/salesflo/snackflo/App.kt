package com.salesflo.snackflo

import AppPreferences
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.salesflo.snackflo.admin.AdminDashboardScreen
import com.salesflo.snackflo.auth.ForgotPasswordScreen
import com.salesflo.snackflo.auth.LoginScreen
import com.salesflo.snackflo.auth.SignUpScreen
import com.salesflo.snackflo.common.AppConstant
import com.salesflo.snackflo.common.NavigationPaths
import com.salesflo.snackflo.employee.EmployeeFoodListScreen
import com.salesflo.snackflo.employee.RestaurantExpansionScreen
import com.salesflo.snackflo.repository.AuthResult


@Composable
fun App() {
    MaterialTheme {
        StartupApp()
    }
}
@Composable
fun StartupApp() {

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
        val isLoggedIn = AppPreferences.isLoggedIn
        composable("splash") {
            LaunchedEffect(Unit) {
                val isEmployee = AppPreferences.userType == AppConstant.EMPLOYEE_TYPE
                if (isLoggedIn) {
                    if (isEmployee) {
                        navController.navigate(NavigationPaths.Home) {
                            popUpTo("splash") { inclusive = true }
                        }

                    } else {
                        navController.navigate(NavigationPaths.AdminScreen) {
                            popUpTo("splash") { inclusive = true }
                        }
                    }

                } else {
                    navController.navigate(NavigationPaths.LoginScreen) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
        }

        composable(NavigationPaths.LoginScreen) {
            LoginScreen(
                // FIX: Clear navigation stack when going to signup
                onSignUpClick = {
                    navController.navigate(NavigationPaths.CreateAccount) {
                        popUpTo(NavigationPaths.LoginScreen) { inclusive = true }
                    }
                },
                onForgotPassClick = { navController.navigate("Forgot Password") },
                onLoginSuccess = { authResult: AuthResult ->

                    AppPreferences.isLoggedIn = true
                    AppPreferences.userType = authResult.userType.toString()
                    AppPreferences.userId = authResult.userId.toString()
                    AppPreferences.userName = authResult.username.toString()

                    val isEmployee = authResult.userType == AppConstant.EMPLOYEE_TYPE

                    if (isEmployee) {
                        navController.navigate(NavigationPaths.Home) {
                            popUpTo(NavigationPaths.LoginScreen) { inclusive = true }
                            launchSingleTop = true
                        }

                    } else {
                        navController.navigate(NavigationPaths.AdminScreen) {
                            popUpTo(NavigationPaths.LoginScreen) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(NavigationPaths.CreateAccount) {
            SignUpScreen(
                // FIX: When going back to login, clear signup from stack
                onLoginClick = {
                    navController.navigate(NavigationPaths.LoginScreen) {
                        popUpTo(NavigationPaths.CreateAccount) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("Forgot Password") {
            ForgotPasswordScreen(onBackClick = { navController.popBackStack() })
        }

        composable(NavigationPaths.Home) {
            val userId = AppPreferences.userId
            EmployeeFoodListScreen(
                userId = userId.toString(),
                onFabClick = { navController.navigate(NavigationPaths.AddItemsScreen) },
                onLogOut = {
                    AppPreferences.clearAll()

                    navController.navigate(NavigationPaths.LoginScreen) {
                        popUpTo(NavigationPaths.Home) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(NavigationPaths.AdminScreen) {
            AdminDashboardScreen(
                onLogOut = {
                    AppPreferences.clearAll()

                    navController.navigate(NavigationPaths.LoginScreen) {
                        popUpTo(NavigationPaths.AdminScreen) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavigationPaths.AddItemsScreen) {
            RestaurantExpansionScreen(
                onArrowClick = { navController.popBackStack() }
            )
        }
    }
}

