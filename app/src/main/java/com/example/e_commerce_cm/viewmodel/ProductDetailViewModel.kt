package com.example.e_commerce_cm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.e_commerce_cm.data.model.Product
import com.example.e_commerce_cm.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProductDetailViewModel(application: Application, private val productId: Int) : AndroidViewModel(application) {
    private val repository = ProductRepository(application)

    private val _uiState = MutableStateFlow(DetailUiState(isLoading = true))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init { loadProduct() }

    private fun loadProduct() {
        viewModelScope.launch {
            try {
                val product = repository.getProductById(productId)
                _uiState.value = DetailUiState(product = product)
            } catch (e: Exception) {
                _uiState.value = DetailUiState(error = "Error al cargar el producto: ${e.message}")
            }
        }
    }

    class Factory(private val application: Application, private val productId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            ProductDetailViewModel(application, productId) as T
    }
}
