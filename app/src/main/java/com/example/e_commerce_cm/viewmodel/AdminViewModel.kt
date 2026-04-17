package com.example.e_commerce_cm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerce_cm.data.model.Product
import com.example.e_commerce_cm.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AdminOpState {
    object Idle : AdminOpState()
    object Loading : AdminOpState()
    object Success : AdminOpState()
    data class Error(val message: String) : AdminOpState()
}

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AdminRepository(application)

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _opState = MutableStateFlow<AdminOpState>(AdminOpState.Idle)
    val opState: StateFlow<AdminOpState> = _opState.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    init { loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _products.value = repository.getAllProducts()
            } catch (e: Exception) {
                _error.value = "Error al cargar productos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    fun createProduct(title: String, price: Double, description: String, category: String, image: String) {
        viewModelScope.launch {
            _opState.value = AdminOpState.Loading
            try {
                repository.createProduct(title, price, description, category, image)
                loadProducts()
                _opState.value = AdminOpState.Success
            } catch (e: Exception) {
                _opState.value = AdminOpState.Error("Error al crear producto: ${e.message}")
            }
        }
    }

    fun updateProduct(id: Int, title: String, price: Double, description: String, category: String, image: String) {
        viewModelScope.launch {
            _opState.value = AdminOpState.Loading
            try {
                repository.updateProduct(id, title, price, description, category, image)
                loadProducts()
                _opState.value = AdminOpState.Success
            } catch (e: Exception) {
                _opState.value = AdminOpState.Error("Error al actualizar producto: ${e.message}")
            }
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            _opState.value = AdminOpState.Loading
            try {
                repository.deleteProduct(id)
                _products.value = _products.value.filter { it.id != id }
                _opState.value = AdminOpState.Success
            } catch (e: Exception) {
                _opState.value = AdminOpState.Error("Error al eliminar producto: ${e.message}")
            }
        }
    }

    fun resetOpState() { _opState.value = AdminOpState.Idle }
}
