package com.rafiq

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rafiq.presentation.navigation.RafiqNavGraph
import com.rafiq.presentation.navigation.Route
import com.rafiq.presentation.theme.BackgroundPrimary
import com.rafiq.presentation.theme.RafiqTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.rafiq.domain.model.User
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.auth.auth
import com.rafiq.domain.manager.DeepLinkManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var supabaseClient: SupabaseClient

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Handle Supabase Auth deep links
        supabaseClient.handleDeeplinks(intent)
        
        lifecycleScope.launch {
            supabaseClient.auth.sessionStatus.collectLatest { status ->
                if (status is SessionStatus.Authenticated) {
                    val serviceIntent = android.content.Intent(this@MainActivity, com.rafiq.service.NotificationService::class.java)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }

                    lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            val userId = status.session.user?.id
                            val userEmail = status.session.user?.email ?: ""
                            if (userId != null) {
                                val sharedPrefs = getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
                                val pendingProfileJson = sharedPrefs.getString("pending_profile", null)
                                
                                val userExists = supabaseClient.postgrest["users"]
                                    .select(Columns.list("id")) { filter { eq("id", userId) } }
                                    .decodeSingleOrNull<User>()
                                
                                if (userExists == null) {
                                    var userToInsert: User? = null
                                    if (pendingProfileJson != null) {
                                        try {
                                            val format = Json { ignoreUnknownKeys = true }
                                            val pendingUser = format.decodeFromString<User>(pendingProfileJson)
                                            userToInsert = pendingUser.copy(id = userId)
                                            sharedPrefs.edit().remove("pending_profile").apply()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    
                                    if (userToInsert == null) {
                                        val fallbackName = userEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                                        userToInsert = User(
                                            id = userId,
                                            name = fallbackName,
                                            username = userEmail.substringBefore("@"),
                                            age = 24,
                                            gender = com.rafiq.domain.model.Gender.OTHER,
                                            onlineStatus = com.rafiq.domain.model.OnlineStatus.ONLINE
                                        )
                                    }
                                    
                                    supabaseClient.postgrest["users"].insert(userToInsert)
                                    
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        android.widget.Toast.makeText(this@MainActivity, "Email confirmed successfully! Welcome to RAFIQ", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    supabaseClient.postgrest["users"]
                                        .update({ set("onlineStatus", com.rafiq.domain.model.OnlineStatus.ONLINE) }) {
                                            filter { eq("id", userId) }
                                        }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        setContent {
            RafiqTheme {
                val isAuthenticated = supabaseClient.auth.currentSessionOrNull() != null
                var showApp by remember { mutableStateOf(isAuthenticated) }
                var sessionChecked by remember { mutableStateOf(false) }

                // Parse initial deep link / app link
                val parsedTarget = remember(intent) { DeepLinkManager.parseIntent(intent) }
                val initialRoute = remember(parsedTarget) { parsedTarget?.toRouteString() }

                // If user is unauthenticated, save deep link to restore after login
                LaunchedEffect(initialRoute, isAuthenticated) {
                    if (initialRoute != null && !isAuthenticated) {
                        DeepLinkManager.savePendingDeepLink(this@MainActivity, initialRoute)
                    }
                }

                val permissionsToRequest = remember {
                    val list = mutableListOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.RECORD_AUDIO,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        list.add(android.Manifest.permission.POST_NOTIFICATIONS)
                        list.add(android.Manifest.permission.READ_MEDIA_IMAGES)
                        list.add(android.Manifest.permission.READ_MEDIA_VIDEO)
                        list.add(android.Manifest.permission.READ_MEDIA_AUDIO)
                    } else {
                        list.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    list
                }

                val permissionsState = rememberMultiplePermissionsState(permissionsToRequest)

                LaunchedEffect(Unit) {
                    sessionChecked = true
                    if (isAuthenticated) {
                        showApp = true
                    }
                    if (!permissionsState.allPermissionsGranted) {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    color = BackgroundPrimary
                ) {
                    if (!sessionChecked) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            androidx.compose.foundation.layout.Column(
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = com.rafiq.R.drawable.logo),
                                    contentDescription = "Rafiq",
                                    modifier = Modifier.size(96.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                CircularProgressIndicator(color = com.rafiq.presentation.theme.PrimaryAccent)
                            }
                        }
                    } else if (!showApp) {
                        com.rafiq.presentation.auth.AuthScreen(
                            onAuthSuccess = {
                                showApp = true
                            }
                        )
                    } else {
                        RafiqNavGraph(
                            supabaseClient = supabaseClient,
                            deepLinkRoute = initialRoute
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        supabaseClient.handleDeeplinks(intent)

        val target = DeepLinkManager.parseIntent(intent)
        val route = target?.toRouteString()
        if (route != null) {
            if (supabaseClient.auth.currentSessionOrNull() != null) {
                // Application in foreground — refresh content or navigate
                Log.d("MainActivity", "onNewIntent deep link route: $route")
            } else {
                DeepLinkManager.savePendingDeepLink(this, route)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                if (userId != null) {
                    supabaseClient.postgrest["users"]
                        .update({ set("onlineStatus", com.rafiq.domain.model.OnlineStatus.OFFLINE) }) {
                            filter { eq("id", userId) }
                        }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
