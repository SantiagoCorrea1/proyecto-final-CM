package com.example.e_commerce_cm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerce_cm.data.local.SessionManager
import com.example.e_commerce_cm.data.model.User
import com.example.e_commerce_cm.data.model.UserRole
import com.example.e_commerce_cm.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Loaded(val user: User) : ProfileUiState()
    object Updated : ProfileUiState()
    object Deleted : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val registerState: StateFlow<AuthUiState> = _registerState.asStateFlow()

    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            try {
                repository.login(username, password)
                _loginState.value = AuthUiState.Success
            } catch (e: Exception) {
                _loginState.value = AuthUiState.Error("Usuario o contraseña incorrectos")
            }
        }
    }

    fun register(username: String, email: String, password: String, role: UserRole) {
        viewModelScope.launch {
            _registerState.value = AuthUiState.Loading
            try {
                repository.register(username, email, password, role)
                _registerState.value = AuthUiState.Success
            } catch (e: Exception) {
                _registerState.value = AuthUiState.Error("Error al registrar: ${e.message}")
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            try {
                val user = repository.getUser(SessionManager.userId)
                _profileState.value = ProfileUiState.Loaded(user)
            } catch (e: Exception) {
                _profileState.value = ProfileUiState.Error("Error al cargar perfil: ${e.message}")
            }
        }
    }

    fun updateProfile(username: String, email: String, password: String) {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            try {
                repository.updateUser(SessionManager.userId, username, email, password)
                _profileState.value = ProfileUiState.Updated
            } catch (e: Exception) {
                _profileState.value = ProfileUiState.Error("Error al actualizar: ${e.message}")
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _profileState.value = ProfileUiState.Loading
            try {
                repository.deleteUser(SessionManager.userId)
                SessionManager.clear()
                _profileState.value = ProfileUiState.Deleted
            } catch (e: Exception) {
                _profileState.value = ProfileUiState.Error("Error al eliminar cuenta: ${e.message}")
            }
        }
    }

    fun logout() {
        SessionManager.logout()
        _loginState.value = AuthUiState.Idle
        _registerState.value = AuthUiState.Idle
        _profileState.value = ProfileUiState.Idle
    }

    fun resetLoginState() { _loginState.value = AuthUiState.Idle }
    fun resetRegisterState() { _registerState.value = AuthUiState.Idle }
    fun resetProfileState() { _profileState.value = ProfileUiState.Idle }
}
