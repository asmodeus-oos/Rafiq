package com.rafiq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rafiq.presentation.navigation.RafiqNavGraph
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

import androidx.compose.runtime.LaunchedEffect
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
        
        // Handle Supabase Auth deep links (like email verification)
        supabaseClient.handleDeeplinks(intent)
        
        lifecycleScope.launch {
            supabaseClient.auth.sessionStatus.collectLatest { status ->
                if (status is SessionStatus.Authenticated) {
                    // Start Background Push Notification Service
                    val serviceIntent = android.content.Intent(this@MainActivity, com.rafiq.service.NotificationService::class.java)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }

                    // Real-Time Online Presence Sync & Profile Restorer
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
                                    
                                    // Notify user on Main thread that email is confirmed
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

        setContent {
            RafiqTheme {

                val targetPostId = intent.getStringExtra("POST_ID")
                val targetUserId = intent.getStringExtra("USER_ID")
                val isChat = intent.getBooleanExtra("IS_CHAT", false)
                
                val startRoute = if (targetPostId != null) {
                    com.rafiq.presentation.navigation.Route.PostDetails.createRoute(targetPostId)
                } else if (targetUserId != null) {
                    if (isChat) {
                        com.rafiq.presentation.navigation.Route.ChatDetail.createRoute(targetUserId)
                    } else {
                        com.rafiq.presentation.navigation.Route.Profile.createRoute(targetUserId)
                    }
                } else null

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundPrimary
                ) {
                    RafiqNavGraph(
                        supabaseClient = supabaseClient,
                        deepLinkRoute = startRoute
                    )
                }
            }
        }
    }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handle deep links when app is already in foreground/background
        supabaseClient.handleDeeplinks(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Set presence to OFFLINE when app is killed
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
