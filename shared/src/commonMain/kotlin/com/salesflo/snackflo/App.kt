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
import com.salesflo.snackflo.auth.LoginScreen
import com.salesflo.snackflo.auth.SignUpScreen
import com.salesflo.snackflo.employee.EmployeeFoodListScreen
import com.salesflo.snackflo.employee.ItemScreenList
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
            val isLoggedIn = preference.getInt("login", 0) == 1

            composable("splash") {
                LaunchedEffect(Unit) {

                    if (isLoggedIn) {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }

            }

            composable("login") {
                LoginScreen(
                    onSignUpClick = { navController.navigate("signup") },
                    onLoginSuccess = { authResult: AuthResult ->
                        preference.put("login", 1)
                        preference.put("userType", authResult.userType.toString())
                        preference.put("username", authResult.username.toString())
                        preference.put("userId", authResult.userId.toString())
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("signup") {
                SignUpScreen(
                    onLoginClick = { navController.navigate("login") },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("home") {


                val isEmployeeType =
                    (preference.getString("userType") ?: "") == AppConstant.EMPLOYEE_TYPE
                val userId = preference.getString("userId") ?: ""

                if (isEmployeeType)

                    EmployeeFoodListScreen(
                        userId = userId.toString(),
                        onFabClick = { navController.navigate("AddItems") },
                        onLogOut = {
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationRoute ?: "splash") {
                                    inclusive = true
                                }
                            }
                        },

                        )
                else
                    AdminDashboardScreen(

                        onLogOut = {
                            //  val sharedPreferences = context.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
                            //  sharedPreferences.edit().clear().apply()

                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationRoute ?: "splash") {
                                    inclusive = true
                                }

                                launchSingleTop = true
                            }


                        }
                    )
            }

            composable("AddItems") {
                ItemScreenList(onArrowClick = { navController.popBackStack() })
            }
        }
    }
}








