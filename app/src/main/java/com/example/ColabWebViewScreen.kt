package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ColabWebViewScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("OJColabPrefs", Context.MODE_PRIVATE) }
    val savedUrl = remember { sharedPrefs.getString("notebook_url", "") ?: "" }
    
    var isLoading by remember { mutableStateOf(true) }
    var isAuthCustomTabOpened by remember { mutableStateOf(false) }
    
    // Remember the WebView instance across recompositions
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            setInitialScale(100)
        }
    }

    // Handles the Android Back back press
    BackHandler(enabled = true) {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            // Do nothing, preventing accidental app exit as requested
        }
    }

    // Observe Lifecycle to trigger reload of cookies/session after coming back from Chrome Custom Tab Google Sign-in
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (isAuthCustomTabOpened) {
                    isAuthCustomTabOpened = false
                    webView.reload()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Configure the WebViewClient and WebChromeClient
    LaunchedEffect(webView, savedUrl) {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isLoading = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isLoading = false
                
                // Inject visual optimizations once Colab page finishes loading
                val cleanUpCss = """
                    (function() {
                      var style = document.createElement('style');
                      style.innerHTML = `
                        /* Hide top menu bar File Edit View Insert etc */
                        #top-toolbar { display: none !important; }
                        .menu-bar { display: none !important; }
                        
                        /* Hide left sidebar */
                        #left-sidebar { display: none !important; }
                        .left-sidebar { display: none !important; }
                        colab-left-pane { display: none !important; }
                        
                        /* Hide right sidebar */
                        #right-sidebar { display: none !important; }
                        colab-right-pane { display: none !important; }
                        
                        /* Hide share and top right buttons */
                        .share-button { display: none !important; }
                        #top-right-buttons { display: none !important; }
                        colab-connect-button { display: none !important; }
                        
                        /* Make notebook fill full width */
                        #notebook-container { 
                          width: 100% !important; 
                          margin: 0 !important;
                          padding: 4px !important;
                        }
                        
                        /* Make cell run buttons bigger for touch */
                        .run-button-container {
                          transform: scale(1.4) !important;
                          transform-origin: center !important;
                        }
                        
                        /* Increase cell padding for easier touch targeting */
                        .cell { 
                          padding: 8px 4px !important; 
                          margin-bottom: 8px !important;
                        }
                        
                        /* Make output text readable on phone */
                        .output { 
                          font-size: 13px !important; 
                        }
                      `;
                      document.head.appendChild(style);
                    })();
                """.trimIndent()
                view?.evaluateJavascript(cleanUpCss, null)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    val isGoogleAuthFlow = url.contains("accounts.google.com") || 
                                           url.contains("oauth2") || 
                                           url.contains("signin")
                    
                    if (isGoogleAuthFlow) {
                        isAuthCustomTabOpened = true
                        val customTabsIntent = CustomTabsIntent.Builder()
                            .setShowTitle(false)
                            .setColorScheme(CustomTabsIntent.COLOR_SCHEME_DARK)
                            .build()
                        try {
                            customTabsIntent.launchUrl(context, Uri.parse(url))
                        } catch (e: Exception) {
                            // Fallback to loading standard if custom tab fail
                            view?.loadUrl(url)
                        }
                        return true
                    }
                }
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress >= 95) {
                    isLoading = false
                }
            }
        }

        if (savedUrl.isNotEmpty()) {
            webView.loadUrl(savedUrl)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // WebView fills entire space
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator at top of screen in primary Orange/Silver color
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .testTag("loading_indicator"),
                color = Color(0xFF4285F4),
                trackColor = Color(0xFF1E2126)
            )
        }

        // Floating Action Button row pinned to BOTTOM CENTER
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding() // Excludes touch navigation overlay zone
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .background(
                        color = Color(0xCC000000), // #CC000000 (80% opacity dark)
                        shape = RoundedCornerShape(32.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("floating_control_panel"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // [⚙] Settings button: small, white icon
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // [▶ Run All] Primary Button, Green (#27AE60)
                Button(
                    onClick = {
                        val runAllJs = """
                            (function() {
                              var menuItems = document.querySelectorAll(
                                'paper-item, .goog-menuitem'
                              );
                              menuItems.forEach(function(item) {
                                if ((item.textContent || '').trim()
                                     .includes('Run all')) {
                                  item.click();
                                }
                              });
                              // Also try the keyboard shortcut approach
                              var event = new KeyboardEvent('keydown', {
                                key: 'F9', keyCode: 120,
                                ctrlKey: false, bubbles: true
                              });
                              document.dispatchEvent(event);
                            })();
                        """.trimIndent()
                        webView.evaluateJavascript(runAllJs, null)
                    },
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .testTag("run_all_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF27AE60),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Run All Icon",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Text(
                            text = "Run All",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            fontSize = 13.sp
                        )
                    }
                }

                // [⬛ Stop] Secondary Button, Red (#E74C3C)
                Button(
                    onClick = {
                        val interruptJs = """
                            (function() {
                              var menuItems = document.querySelectorAll(
                                'paper-item, .goog-menuitem'  
                              );
                              menuItems.forEach(function(item) {
                                if ((item.textContent || '').trim()
                                     .includes('Interrupt execution')) {
                                  item.click();
                                }
                              });
                            })();
                        """.trimIndent()
                        webView.evaluateJavascript(interruptJs, null)
                    },
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .testTag("stop_execution_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE74C3C),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Icon",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Text(
                            text = "Stop",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            fontSize = 13.sp
                        )
                    }
                }

                // [↺ Reload] Icon button, Grey (#7F8C8D)
                IconButton(
                    onClick = { webView.reload() },
                    modifier = Modifier
                        .size(38.dp)
                        .background(color = Color(0xFF7F8C8D), shape = CircleShape)
                        .testTag("reload_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
