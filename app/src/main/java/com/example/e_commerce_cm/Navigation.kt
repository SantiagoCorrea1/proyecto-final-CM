package com.example.e_commerce_cm

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.e_commerce_cm.data.local.SessionManager
import com.example.e_commerce_cm.ui.screens.AdminProductFormScreen
import com.example.e_commerce_cm.ui.screens.AdminScreen
import com.example.e_commerce_cm.ui.screens.CartScreen
import com.example.e_commerce_cm.ui.screens.HomeScreen
import com.example.e_commerce_cm.ui.screens.LoginScreen
import com.example.e_commerce_cm.ui.screens.ProductDetailScreen
import com.example.e_commerce_cm.ui.screens.ProfileScreen
import com.example.e_commerce_cm.ui.screens.RegisterScreen
import com.example.e_commerce_cm.viewmodel.AdminViewModel
import com.example.e_commerce_cm.viewmodel.AuthViewModel
import com.example.e_commerce_cm.viewmodel.CartViewModel
import com.example.e_commerce_cm.viewmodel.HomeViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
    object Admin : Screen("admin")
    object AdminProductForm : Screen("admin/product")
    object ProductDetail : Screen("product/{productId}") {
        fun createRoute(productId: Int) = "product/$productId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                homeViewModel = homeViewModel,
                cartViewModel = cartViewModel,
                onProductClick = { navController.navigate(Screen.ProductDetail.createRoute(it)) },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onProfileClick = {
                    if (SessionManager.isLoggedIn) navController.navigate(Screen.Profile.route)
                    else navController.navigate(Screen.Login.route)
                },
                onAdminClick = { navController.navigate(Screen.Admin.route) },
                isAdmin = SessionManager.isLoggedIn && SessionManager.isAdmin
            )
        }

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable
            ProductDetailScreen(
                productId = productId,
                cartViewModel = cartViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                cartViewModel = cartViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLogout = { navController.popBackStack() },
                onAccountDeleted = { navController.popBackStack() }
            )
        }

        composable(Screen.Admin.route) {
            AdminScreen(
                adminViewModel = adminViewModel,
                onBack = {
                    homeViewModel.loadProducts()
                    navController.popBackStack()
                },
                onNewProduct = { navController.navigate(Screen.AdminProductForm.route) },
                onEditProduct = { navController.navigate(Screen.AdminProductForm.route) }
            )
        }

        composable(Screen.AdminProductForm.route) {
            AdminProductFormScreen(
                adminViewModel = adminViewModel,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
