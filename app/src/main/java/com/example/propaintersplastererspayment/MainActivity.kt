package com.example.propaintersplastererspayment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.propaintersplastererspayment.app.navigation.AppNavGraph
import com.example.propaintersplastererspayment.ui.theme.ProPaintersPlasterersPaymentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProPaintersPlasterersPaymentTheme {
                AppNavGraph(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
