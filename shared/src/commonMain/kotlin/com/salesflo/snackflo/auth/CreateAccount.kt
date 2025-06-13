package com.salesflo.snackflo.auth

import com.salesflo.snackflo.repository.AuthFunc
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.salesflo.snackflo.showToast
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource
import kotlinx.serialization.encodeToString
import snackflo.shared.generated.resources.Res
import snackflo.shared.generated.resources.bg


@Composable
fun SignUpScreen(
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var deviceInfo by remember { mutableStateOf<FullDeviceInfo?>(null) }
    var isLoading by remember { mutableStateOf(false)}
    var mobileNo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }



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


        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
              //  .imePadding()
                .padding(top = 200.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Sign Up", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                    Spacer(Modifier.height(16.dp))

                    Text("Mobile Number", fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color.Black)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
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
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    Text("Username", fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color.Black)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF7F50),
                            disabledBorderColor = Color.Transparent,
                            focusedLabelColor = Color(0xFFFF7F50),
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    Text("Password", fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color.Black)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF7F50),
                            disabledBorderColor = Color.Transparent,
                            focusedLabelColor = Color(0xFFFF7F50),
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon = if (passwordVisible)  Icons.Default.VisibilityOff else Icons.Default.Visibility
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = icon, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                            }
                        },

                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (mobileNo.isEmpty())
                                showToast("Mobile no is required")
                            else if (password.isEmpty())
                                showToast("Password should not be empty")
                            else if(password.length < 6 )
                                showToast("Password must be of 6 characters")
                            else{
                                isLoading = true
                                scope.launch {
                                    try{
                                        deviceInfo = getFullDeviceInfo()

                                        val jsonString = Json.encodeToString<FullDeviceInfo>(deviceInfo!!)
                                        AuthFunc.isMobileAlreadyRegistered(
                                            username = username,
                                            mobileNo = mobileNo,
                                            password = password,
                                            deviceName = jsonString,
                                            //  context,
                                            onSuccess = {
                                                username = ""
                                                mobileNo = ""
                                                password = ""

                                                onBackClick()
                                            },
                                            onFailure = {
                                                username = ""
                                                mobileNo = ""
                                                password = ""
                                            }
                                        )
                                    }
                                    catch (e: Exception) {
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
                            Text("Sign Up", color = Color.White)
                        }

                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row {
                Text("Already have an account? " )
                Text(
                    "Login here",
                    color = Color(0xFFFF7F50),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
        }
    }
}


