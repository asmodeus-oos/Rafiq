package com.rafiq.presentation.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiq.domain.model.User
import com.rafiq.domain.model.VoiceRoom
import com.rafiq.domain.model.Post
import io.github.jan.supabase.SupabaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val userResults: List<User> = emptyList(),
    val roomResults: List<VoiceRoom> = emptyList(),
    val postResults: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val filters: SearchFilters = SearchFilters()
)

data class SearchFilters(
    val minAge: Int = 18,
    val maxAge: Int = 99,
    val gender: com.rafiq.domain.model.Gender? = null,
    val onlineOnly: Boolean = false,
    val verifiedOnly: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
        if (newQuery.length > 2) {
            performSearch()
        }
    }

    private fun performSearch() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Implementation for multi-type search across Supabase tables
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}
