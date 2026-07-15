package com.rafiq.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiq.domain.model.Gender
import com.rafiq.domain.model.OnlineStatus
import com.rafiq.domain.model.Tier
import com.rafiq.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            supabaseClient.auth.sessionStatus.collectLatest { status ->
                if (status is SessionStatus.Authenticated) {
                    val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    val pendingProfileJson = sharedPrefs.getString("pending_profile", null)
                    
                    // We don't need tripwire here anymore, handled in MainActivity
                    _authState.value = AuthState.Success
                }
            }
        }
    }

    fun loginWithEmail(email: String, password: String) = viewModelScope.launch {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return@launch
        }
        _authState.value = AuthState.Loading
        try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            _authState.value = AuthState.Success
        } catch (e: Exception) {
            e.printStackTrace()
            _authState.value = AuthState.Error(e.message ?: "Login failed")
        }
    }

    fun signupWithEmail(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        username: String,
        genderStr: String,
        countryStr: String,
        day: String,
        month: String,
        year: String
    ) = viewModelScope.launch {
        if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank() || username.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all required fields")
            return@launch
        }
        _authState.value = AuthState.Loading
        try {
            val user = supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            // Generate the user object but cache it instead of inserting immediately
            val birthYear = year.toIntOrNull() ?: 2000
            val birthMonth = month.toIntOrNull() ?: 1
            val birthDay = day.toIntOrNull() ?: 1
            
            val calendar = java.util.Calendar.getInstance()
            val currentYear = calendar.get(java.util.Calendar.YEAR)
            val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            
            var calculatedAge = currentYear - birthYear
            if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
                calculatedAge--
            }
            
            val parsedGender = try {
                Gender.valueOf(genderStr.uppercase())
            } catch (e: Exception) {
                Gender.OTHER
            }
            
            val pendingUser = User(
                id = "", // Will be assigned after email confirmation
                name = "$firstName $lastName",
                username = username,
                age = calculatedAge,
                gender = parsedGender,
                avatar = "",
                bio = "I'm new to RAFIQ!",
                isVerified = false,
                onlineStatus = OnlineStatus.ONLINE,
                tier = Tier.FREE,
                diamonds = 0L,
                isOwner = false,
                country = countryStr
            )
            
            val uid = supabaseClient.auth.currentUserOrNull()?.id
            if (uid != null) {
                // Confirm email is OFF, we are instantly logged in
                // Use a separate scope so navigation doesn't cancel the database insertion!
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        val finalUser = pendingUser.copy(id = uid)
                        supabaseClient.postgrest["users"].insert(finalUser)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                _authState.value = AuthState.Success
            } else {
                // Confirm email is ON, cache locally for MainActivity to pick up via deep link
                val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val jsonString = Json.encodeToString(pendingUser)
                sharedPrefs.edit().putString("pending_profile", jsonString).apply()
                
                _authState.value = AuthState.NeedsExtraInfo // Ask them to check their email
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            _authState.value = AuthState.Error(e.message ?: "Signup failed")
        }
    }

    fun setAuthError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun setAuthLoading() {
        _authState.value = AuthState.Loading
    }

    fun setAuthIdle() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object NeedsExtraInfo : AuthState()
    data class Error(val message: String) : AuthState()
}
