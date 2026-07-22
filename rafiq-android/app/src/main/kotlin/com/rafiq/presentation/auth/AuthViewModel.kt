package com.rafiq.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiq.domain.model.Gender
import com.rafiq.domain.model.OnlineStatus
import com.rafiq.domain.model.Tier
import com.rafiq.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object NeedsExtraInfo : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun loginWithEmail(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return@launch
        }

        _authState.value = AuthState.Loading
        try {
            // Validate credentials with Supabase Auth API
            supabaseClient.auth.signInWith(Email) {
                this.email = trimmedEmail
                this.password = trimmedPassword
            }

            val currentUid = supabaseClient.auth.currentUserOrNull()?.id
            if (currentUid != null) {
                ensureUserProfileExists(currentUid, trimmedEmail)
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Authentication failed: No user returned")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = when {
                e.message?.contains("Invalid login credentials", ignoreCase = true) == true -> "Invalid email or password"
                e.message?.contains("Email not confirmed", ignoreCase = true) == true -> "Please confirm your email address"
                else -> e.message ?: "Login failed. Please check your credentials."
            }
            _authState.value = AuthState.Error(errorMsg)
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
    ) = viewModelScope.launch(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val trimmedFirstName = firstName.trim()
        val trimmedLastName = lastName.trim()
        val trimmedUsername = username.trim()

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank() || trimmedFirstName.isBlank() || trimmedLastName.isBlank() || trimmedUsername.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all required fields")
            return@launch
        }

        _authState.value = AuthState.Loading
        try {
            // Sign up with Supabase Auth
            supabaseClient.auth.signUpWith(Email) {
                this.email = trimmedEmail
                this.password = trimmedPassword
            }

            val uid = supabaseClient.auth.currentUserOrNull()?.id

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

            val newProfile = User(
                id = uid ?: "",
                name = "$trimmedFirstName $trimmedLastName",
                username = trimmedUsername,
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

            // Save pending profile to SharedPreferences so email confirmation or login can restore it
            try {
                val format = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                val profileJson = format.encodeToString(User.serializer(), newProfile)
                context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("pending_profile", profileJson)
                    .apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (uid != null) {
                try {
                    supabaseClient.postgrest["users"].insert(newProfile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val isSessionActive = supabaseClient.auth.currentSessionOrNull() != null
            if (isSessionActive && uid != null) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.NeedsExtraInfo
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = when {
                e.message?.contains("User already registered", ignoreCase = true) == true -> "User with this email already exists"
                else -> e.message ?: "Registration failed. Please try again."
            }
            _authState.value = AuthState.Error(errorMsg)
        }
    }

    private suspend fun ensureUserProfileExists(uid: String, email: String) {
        try {
            val existing = supabaseClient.postgrest["users"]
                .select(Columns.list("id")) { filter { eq("id", uid) } }
                .decodeSingleOrNull<User>()

            if (existing == null) {
                val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val pendingProfileJson = sharedPrefs.getString("pending_profile", null)
                
                var userToInsert: User? = null
                if (pendingProfileJson != null) {
                    try {
                        val format = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                        val pendingUser = format.decodeFromString<User>(pendingProfileJson)
                        userToInsert = pendingUser.copy(id = uid)
                        sharedPrefs.edit().remove("pending_profile").apply()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (userToInsert == null) {
                    val fallbackName = email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                    userToInsert = User(
                        id = uid,
                        name = fallbackName,
                        username = email.substringBefore("@"),
                        age = 24,
                        gender = Gender.OTHER,
                        avatar = "",
                        bio = "Welcome to RAFIQ!",
                        onlineStatus = OnlineStatus.ONLINE,
                        tier = Tier.FREE,
                        country = ""
                    )
                }

                supabaseClient.postgrest["users"].insert(userToInsert)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
