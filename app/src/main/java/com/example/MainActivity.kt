package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    val sharedPrefs = remember { context.getSharedPreferences("OJColabPrefs", Context.MODE_PRIVATE) }
                    val savedUrl = remember { sharedPrefs.getString("notebook_url", "") ?: "" }
                    
                    val navController = rememberNavController()
                    val startDest = if (savedUrl.isEmpty()) "settings" else "viewer"
                    
                    NavHost(
                        navController = navController,
                        startDestination = startDest
                    ) {
                        composable("settings") {
                            SettingsScreen(
                                onNavigateToViewer = {
                                    navController.navigate("viewer") {
                                        popUpTo("settings") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("viewer") {
                            ColabWebViewScreen(
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
