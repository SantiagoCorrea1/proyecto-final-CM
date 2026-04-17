package com.example.e_commerce_cm.data.repository

import com.example.e_commerce_cm.data.local.SessionManager
import com.example.e_commerce_cm.data.model.LoginRequest
import com.example.e_commerce_cm.data.model.User
import com.example.e_commerce_cm.data.network.RetrofitInstance

class AuthRepository {
    private val api = RetrofitInstance.api

    suspend fun login(username: String, password: String): String {
        // Intentar login con la API; si falla, validar contra credenciales locales
        return try {
            val response = api.login(LoginRequest(username, password))
            SessionManager.token = response.token
            response.token
        } catch (e: Exception) {
            if (SessionManager.matchesLocalCredentials(username, password)) {
                val localToken = "local_token_${System.currentTimeMillis()}"
                SessionManager.token = localToken
                localToken
            } else {
                throw e
            }
        }
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        role: com.example.e_commerce_cm.data.model.UserRole
    ): User {
        val user = User(username = username, email = email, password = password)
        val created = api.createUser(user)
        SessionManager.userId = created.id
        SessionManager.localUsername = username
        SessionManager.localPassword = password
        SessionManager.localEmail = email
        SessionManager.userRole = role
        return created
    }

    suspend fun getUser(id: Int): User {
        // Si el usuario se registró localmente, construir perfil desde datos guardados
        if (SessionManager.hasLocalUser) {
            return User(
                id = id,
                username = SessionManager.localUsername ?: "",
                email = SessionManager.localEmail ?: "",
                password = SessionManager.localPassword ?: ""
            )
        }
        return api.getUserById(id)
    }

    suspend fun updateUser(id: Int, username: String, email: String, password: String): User {
        val user = User(id = id, username = username, email = email, password = password)
        val updated = api.updateUser(id, user)
        if (SessionManager.hasLocalUser) {
            SessionManager.localUsername = username
            SessionManager.localEmail = email
            SessionManager.localPassword = password
        }
        return updated
    }

    suspend fun deleteUser(id: Int): User = api.deleteUser(id)
}
