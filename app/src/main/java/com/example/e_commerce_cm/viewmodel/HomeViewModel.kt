package com.example.e_commerce_cm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerce_cm.data.model.Product
import com.example.e_commerce_cm.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Category { ALL, MENS, WOMENS }

data class HomeUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: Category = Category.ALL
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allProducts: List<Product> = emptyList()

    init { loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                allProducts = repository.getAllClothing()
                _uiState.value = _uiState.value.copy(
                    products = filterByCategory(_uiState.value.selectedCategory),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar productos: ${e.message}"
                )
            }
        }
    }

    fun selectCategory(category: Category) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            products = filterByCategory(category)
        )
    }

    private fun filterByCategory(category: Category) = when (category) {
        Category.ALL -> allProducts
        Category.MENS -> allProducts.filter { it.category == "men's clothing" }
        Category.WOMENS -> allProducts.filter { it.category == "women's clothing" }
    }

    fun retry() = loadProducts()
}
