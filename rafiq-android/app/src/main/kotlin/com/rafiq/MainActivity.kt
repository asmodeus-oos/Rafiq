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
import io.github.jan.supabase.auth.auth

import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var supabaseClient: SupabaseClient

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle Supabase Auth deep links (like email verification)
        supabaseClient.handleDeeplinks(intent)
        
        lifecycleScope.launch {
            supabaseClient.auth.sessionStatus.collectLatest { status ->
                if (status is SessionStatus.Authenticated) {
                    val sharedPrefs = getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
                    val pendingProfileJson = sharedPrefs.getString("pending_profile", null)
                    
                    if (pendingProfileJson != null) {
                        try {
                            // Use ignoreUnknownKeys to prevent crash on JSON mismatch
                            val format = Json { ignoreUnknownKeys = true }
                            val pendingUser = format.decodeFromString<User>(pendingProfileJson)
                            val finalUser = pendingUser.copy(id = status.session.user?.id ?: "")
                            supabaseClient.postgrest["users"].insert(finalUser)
                            
                            // Clear the cache
                            sharedPrefs.edit().remove("pending_profile").apply()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        
        setContent {
            RafiqTheme {
                val permissionState = rememberMultiplePermissionsState(
                    permissions = listOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.RECORD_AUDIO,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.READ_CONTACTS,
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            android.Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            android.Manifest.permission.POST_NOTIFICATIONS
                        } else null
                    ).filterNotNull()
                )

                LaunchedEffect(Unit) {
                    permissionState.launchMultiplePermissionRequest()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundPrimary
                ) {
                    RafiqNavGraph(supabaseClient = supabaseClient)
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
}
