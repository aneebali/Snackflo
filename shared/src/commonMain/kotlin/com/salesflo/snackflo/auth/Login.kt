package com.salesflo.snackflo.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salesflo.snackflo.FullDeviceInfo
import com.salesflo.snackflo.getFullDeviceInfo
import com.salesflo.snackflo.repository.AuthFunc
import com.salesflo.snackflo.repository.AuthResult
import com.salesflo.snackflo.showToast
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource
import snackflo.shared.generated.resources.Res
import snackflo.shared.generated.resources.bg


@Composable
fun LoginScreen(onSignUpClick: () -> Unit,  onLoginSuccess: (AuthResult) -> Unit,
                onForgotPassClick : () -> Unit

) {
    val scope = rememberCoroutineScope()
    var mobileNo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var deviceInfo by remember { mutableStateOf<FullDeviceInfo?>(null) }


    Box(
        modifier = Modifier
            .fillMaxSize().background(color = Color.White)
    ) {

        Image(
            painter = painterResource(Res.drawable.bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding() // Handles keyboard push-up
                .padding(top = 200.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Login", fontSize = 24.sp, fontWeight = FontWeight.Bold ,)

                    Spacer(Modifier.height(16.dp))

                    Text("Mobile Number", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black,    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        singleLine = true,
                        value = mobileNo,
                        onValueChange = {
                            mobileNo = it
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF7F50),
                            disabledBorderColor = Color.Transparent,
                            focusedLabelColor = Color(0xFFFF7F50),
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),

                        //label = { Text("Confirm Password") },
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    )


                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Password",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,

                        )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        singleLine = true,
                        value = password,
                        onValueChange = {
                            password = it
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF7F50),
                            disabledBorderColor = Color.Transparent,
                            focusedLabelColor = Color(0xFFFF7F50),
                        ),
                        // label = { Text("Password") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon = if (passwordVisible)  Icons.Default.VisibilityOff else Icons.Default.Visibility
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = icon, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    )

//                    Text(
//                        "Forgot Password?",
//                        fontSize = 14.sp,
//                        color = Color(0xFFFF7F50),
//
//                        fontWeight = FontWeight.SemiBold,
//                        modifier = Modifier
//                            .align(Alignment.End)
//                            .padding(top = 4.dp).clickable {
//                                onForgotPassClick()
//                            }
//                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (mobileNo.isEmpty())
                                showToast("Mobile no is required")
                            else if (password.isEmpty())
                                showToast("Password should not be empty")

                            else {
                                isLoading = true
                                scope.launch {
                                    try {
                                        deviceInfo = getFullDeviceInfo()
                                        val jsonString = Json.encodeToString<FullDeviceInfo>(deviceInfo!!)

                                        val authResult  = AuthFunc.authenticateUser(
                                            password = password,
                                            mobileNo = mobileNo,
                                            deviceName = jsonString
                                        )

                                        if(authResult.success){
                                            onLoginSuccess(authResult)

                                        }

                                        showToast(if (authResult.success) "Login successful" else "User not Found",)
                                    } catch (e: Exception) {
                                        showToast("Something went wrong")

                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7F50)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Login", color = Color.White, )
                        }
                    }



                }
            }

            Spacer(Modifier.height(12.dp))

            Row {
                Text("Don’t have an account? ", )
                Text(
                    "Signup",
                    color = Color(0xFFFF7F50),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }
        }
    }


}