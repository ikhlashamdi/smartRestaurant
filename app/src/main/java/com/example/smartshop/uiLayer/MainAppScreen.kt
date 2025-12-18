package com.example.smartshop.uiLayer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.smartshop.ViewModel.CartViewModel
import com.example.smartshop.ViewModel.ProductViewModel
import com.example.smartshop.data.Product
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.smartshop.R


enum class Screen {
    PRODUCTS,
    CART,
    ORDERS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    userEmail: String,
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.PRODUCTS) }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddedToCartSnackbar by remember { mutableStateOf(false) }

    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartItemCount = cartItems.size

    // Snackbar pour confirmer l'ajout au panier
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showAddedToCartSnackbar) {
        if (showAddedToCartSnackbar) {
            snackbarHostState.showSnackbar("Plat ajouté au panier")
            showAddedToCartSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            when (currentScreen) {
                Screen.PRODUCTS -> TopAppBar(
                        title = {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "SmartRestaurant Logo",
                                modifier = Modifier
                                    .height(100.dp)
                            )
                        },
                        actions = {
                            // Badge du panier
                            BadgedBox(
                                badge = {
                                    if (cartItemCount > 0) {
                                        Badge { Text(cartItemCount.toString()) }
                                    }
                                }
                            ) {
                                IconButton(onClick = { currentScreen = Screen.CART }) {
                                    Icon(Icons.Default.ShoppingCart, "Panier")
                                }
                            }

                            IconButton(onClick = { currentScreen = Screen.ORDERS }) {
                                Icon(Icons.Default.Receipt, "Commandes")
                            }

                            IconButton(onClick = onLogout) {
                                Icon(Icons.Default.ExitToApp, "Se déconnecter")
                            }
                        }
                    )

                    else -> {} // Les autres écrans ont leur propre TopBar
            }
        },
        floatingActionButton = {
            if (currentScreen == Screen.PRODUCTS) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter un produit")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (currentScreen) {
            Screen.PRODUCTS -> {
                ProductListScreen(
                    viewModel = productViewModel,
                    modifier = Modifier.padding(padding),
                    onProductClick = { product ->
                        productToEdit = product
                        showAddDialog = true
                    },
                    onAddToCart = { product ->
                        cartViewModel.addToCart(product)
                        showAddedToCartSnackbar = true
                    }
                )
            }

            Screen.CART -> {
                CartScreen(
                    viewModel = cartViewModel,
                    onBack = { currentScreen = Screen.PRODUCTS }
                )
            }

            Screen.ORDERS -> {
                OrderHistoryScreen(
                    viewModel = cartViewModel,
                    onBack = { currentScreen = Screen.PRODUCTS }
                )
            }
        }

        // Dialogue pour ajouter/modifier un produit
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    productToEdit = null
                },
                title = {
                    Text(if (productToEdit == null) "Nouveau plat" else "Modifier le plat")
                },
                text = {
                    ProductFormWithUrl(
                        product = productToEdit,
                        onSave = { product ->
                            if (productToEdit == null) {
                                productViewModel.addProduct(product)
                            } else {
                                productViewModel.updateProduct(product)
                            }
                            showAddDialog = false
                            productToEdit = null
                        },
                        onCancel = {
                            showAddDialog = false
                            productToEdit = null
                        }
                    )
                },
                confirmButton = {},
                dismissButton = {}
            )
        }
    }
}