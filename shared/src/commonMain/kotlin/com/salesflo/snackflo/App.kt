package com.salesflo.snackflo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cmppreference.LocalPreference
import com.example.cmppreference.LocalPreferenceProvider
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

    LocalPreferenceProvider {
        val preference = LocalPreference.current
        val navController = rememberNavController()


        NavHost(navController = navController, startDestination = "splash") {
            val isLoggedIn = preference.getInt(NavigationPaths.LoginScreen, 0) == 1
            //  val isEmployeeType = (preference.getString("userType") ?: "") == AppConstant.EMPLOYEE_TYPE


            composable("splash") {
                LaunchedEffect(Unit) {
                    val isEmployee = (preference.getString("userType") ?: "") == AppConstant.EMPLOYEE_TYPE
                    if (isLoggedIn) {
                        if(isEmployee){
                            navController.navigate(NavigationPaths.Home) {
                                popUpTo("splash") { inclusive = true }
                            }

                        }
                        else{
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
                    onSignUpClick = { navController.navigate(NavigationPaths.CreateAccount) },
                    onForgotPassClick = { navController.navigate("Forgot Password")},
                    onLoginSuccess = {   authResult: AuthResult ->

                        preference.put(NavigationPaths.LoginScreen, 1)
                        preference.put("userType", authResult.userType.toString())
                        preference.put("username" , authResult.username.toString())
                        preference.put("userId" , authResult.userId.toString())
                        val isEmployee = authResult.userType == AppConstant.EMPLOYEE_TYPE

                        if(isEmployee){
                            navController.navigate(NavigationPaths.Home) {
                                popUpTo(NavigationPaths.LoginScreen) { inclusive = true }
                            }

                        }
                        else{
                            navController.navigate(NavigationPaths.AdminScreen) {
                                popUpTo(NavigationPaths.LoginScreen) { inclusive = true }
                            }
                        }

                    }
                )
            }

            composable(NavigationPaths.CreateAccount) {
                SignUpScreen(
                    onLoginClick = { navController.navigate(NavigationPaths.LoginScreen) },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable ("Forgot Password"){
                ForgotPasswordScreen( onBackClick = { navController.popBackStack() })
            }

            composable(NavigationPaths.Home) {
                val userId = preference.getString("userId") ?: ""
                EmployeeFoodListScreen(
                    userId = userId.toString(),
                    onFabClick = { navController.navigate(NavigationPaths.AddItemsScreen) },
                    onLogOut = {
                        preference.put(NavigationPaths.LoginScreen, 0)
                        preference.put("userType", "")
                        preference.put("username", "")
                        preference.put("userId", "")

//                        while (navController.popBackStack()) {
//                            // keep popping
//                        }
                        navController.navigate(NavigationPaths.LoginScreen) {
                            popUpTo(NavigationPaths.Home) { inclusive = true }
                            launchSingleTop = true
                        }

//                        navController.navigate(NavigationPaths.LoginScreen) {
//                            popUpTo(navController.graph.startDestinationRoute!!) {
//                                inclusive = true
//                            }
//                        }
                    },

                    )
            }
            composable(NavigationPaths.AdminScreen) {
                AdminDashboardScreen(
                    onLogOut = {
                        preference.put(NavigationPaths.LoginScreen, 0)
                        preference.put("userType", "")
                        preference.put("username", "")
                        preference.put("userId", "")

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
}
