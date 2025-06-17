package com.salesflo.snackflo

import PlatformSettingsProvider
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.Firebase
import com.google.firebase.initialize


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener {splashScreenView ->
                splashScreenView.remove()
            }
        }
        setContent {
            Firebase.initialize(this)
            initializeContext(applicationContext)
            initContext(applicationContext)
            PlatformSettingsProvider.applicationContext = applicationContext
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}