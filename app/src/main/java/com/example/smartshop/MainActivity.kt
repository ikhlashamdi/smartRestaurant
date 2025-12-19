package com.example.smartshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartshop.ViewModel.CartViewModel
import com.example.smartshop.ViewModel.ProductViewModel
import com.example.smartshop.auth.AuthUiState
import com.example.smartshop.ViewModel.AuthViewModel
import com.example.smartshop.screen.LoginScreen
import com.example.smartshop.screen.MainAppScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val authViewModel: AuthViewModel = viewModel()
                val productViewModel: ProductViewModel = viewModel()
                val cartViewModel: CartViewModel = viewModel()

                val authState by authViewModel.uiState.collectAsState()

                Surface(modifier = Modifier.fillMaxSize()) {
                    when (authState) {
                        is AuthUiState.Success -> {
                            val successState = authState as AuthUiState.Success

                            LaunchedEffect(successState.userId) {
                                productViewModel.setUser(successState.userId)
                                cartViewModel.setUser(successState.userId)
                            }

                            MainAppScreen(
                                productViewModel = productViewModel,
                                cartViewModel = cartViewModel,
                                userEmail = successState.email,
                                onLogout = {
                                    authViewModel.logout()
                                }
                            )
                        }

                        else -> {
                            LoginScreen(viewModel = authViewModel)
                        }
                    }
                }
            }
        }
    }
}