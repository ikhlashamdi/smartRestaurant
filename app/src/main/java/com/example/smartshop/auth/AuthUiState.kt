package com.example.smartshop.auth

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val email: String, val userId: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}