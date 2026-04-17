package com.example.e_commerce_cm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.e_commerce_cm.data.local.SessionManager
import com.example.e_commerce_cm.ui.theme.EcommerceCMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            EcommerceCMTheme {
                AppNavigation()
            }
        }
    }
}
