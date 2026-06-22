package com.example

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToViewer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("OJColabPrefs", Context.MODE_PRIVATE) }
    
    var urlInput by remember { 
        mutableStateOf(sharedPrefs.getString("notebook_url", "") ?: "") 
    }
    
    var showError by remember { mutableStateOf(false) }
    val isUrlValid = urlInput.trim().startsWith("http://") || urlInput.trim().startsWith("https://")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1117), // Rich, deep sleek background
                        Color(0xFF151821)  // Smooth gradient edge
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Branding Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "OJColab",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF4285F4), // Sleek Blue accent
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Google Colab Mobile Companion",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF9AA0A6), // Slate gray
                    textAlign = TextAlign.Center
                )
            }

            // Central Card for settings
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E2126) // Sleek card background
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Notebook Configuration",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { 
                            urlInput = it
                            showError = false
                        },
                        label = { Text("Colab Notebook URL", color = Color(0xFF4285F4)) },
                        placeholder = { Text("https://colab.research.google.com/drive/...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Link Icon",
                                tint = Color(0xFF4285F4)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4285F4),
                            unfocusedBorderColor = Color(0xFF3A3F4B),
                            focusedLabelColor = Color(0xFF4285F4),
                            unfocusedLabelColor = Color(0xFF9AA0A6),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("colab_notebook_url_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Help Info",
                            tint = Color(0xFF9AA0A6),
                            modifier = Modifier.size(18.dp).offset(y = 2.dp)
                        )
                        Text(
                            text = "Open your notebook in a browser, copy the URL and paste it here. It never changes.",
                            color = Color(0xFF9AA0A6),
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 16.sp
                        )
                    }

                    AnimatedVisibility(visible = showError) {
                        Text(
                            text = "Please enter a valid https:// URL to save.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Button
            Button(
                onClick = {
                    if (isUrlValid) {
                        sharedPrefs.edit()
                            .putString("notebook_url", urlInput.trim())
                            .apply()
                        onNavigateToViewer()
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_and_open_notebook_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4285F4), // Sleek blue action button
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Save & Open Notebook",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Navigate Forward"
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
