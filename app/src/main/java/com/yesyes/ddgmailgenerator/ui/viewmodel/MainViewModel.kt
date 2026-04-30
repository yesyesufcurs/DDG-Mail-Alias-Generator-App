package com.yesyes.ddgmailgenerator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yesyes.ddgmailgenerator.data.DataStoreManager
import com.yesyes.ddgmailgenerator.data.DuckDuckGoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class MainUiState {
    object Idle : MainUiState()
    object Loading : MainUiState()
    data class Success(val email: String) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

class MainViewModel(
    private val repository: DuckDuckGoRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Idle)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _token = MutableStateFlow("")
    val token: StateFlow<String> = _token.asStateFlow()

    init {
        viewModelScope.launch {
            // Load initial token
            val savedToken = dataStoreManager.tokenFlow.first()
            _token.value = savedToken
        }
    }

    fun onTokenChange(newToken: String) {
        _token.value = newToken
        // Save on every change for persistence
        viewModelScope.launch {
            dataStoreManager.saveToken(newToken)
        }
    }

    fun generateEmail() {
        val currentToken = _token.value
        if (currentToken.isBlank()) return

        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            
            val result = repository.generateEmail(currentToken)
            result.onSuccess { email ->
                _uiState.value = MainUiState.Success(email)
            }.onFailure { error ->
                _uiState.value = MainUiState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _uiState.value = MainUiState.Idle
    }

    companion object {
        fun Factory(
            repository: DuckDuckGoRepository,
            dataStoreManager: DataStoreManager
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(repository, dataStoreManager)
            }
        }
    }
}
