package com.salesflo.snackflo

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
        setContent {
            Firebase.initialize(this)
            initializeContext(applicationContext)
            initContext(applicationContext)
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}