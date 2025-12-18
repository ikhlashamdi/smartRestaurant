package com.example.smartshop.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val auth: FirebaseAuth = Firebase.auth

    // ✅ Vérifier si l'utilisateur est déjà connecté au démarrage
    init {
        auth.currentUser?.let { user ->
            _uiState.value = AuthUiState.Success(user.email ?: "", user.uid)
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Veuillez remplir tous les champs")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user

                if (user != null) {
                    // ✅ Retourner l'email ET l'ID utilisateur
                    _uiState.value = AuthUiState.Success(user.email ?: email, user.uid)
                } else {
                    _uiState.value = AuthUiState.Error("Connexion échouée")
                }

            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    when {
                        e.message?.contains("invalid-email") == true -> "Email invalide"
                        e.message?.contains("wrong-password") == true -> "Mot de passe incorrect"
                        e.message?.contains("user-not-found") == true -> "Aucun compte avec cet email"
                        e.message?.contains("network") == true -> "Erreur de connexion réseau"
                        else -> "Erreur de connexion : ${e.message}"
                    }
                )
            }
        }
    }

    fun logout() {
        auth.signOut()
        _uiState.value = AuthUiState.Idle
    }
}