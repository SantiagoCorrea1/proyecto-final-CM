package com.example.e_commerce_cm.data.repository

import com.example.e_commerce_cm.data.local.SessionManager
import com.example.e_commerce_cm.data.model.LoginRequest
import com.example.e_commerce_cm.data.model.User
import com.example.e_commerce_cm.data.model.UserRole
import com.example.e_commerce_cm.data.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val api       = RetrofitInstance.api
    private val fireAuth  = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // ── Login con EMAIL + contraseña ─────────────────────────────────────────
    suspend fun login(email: String, password: String): String {
        return try {
            val result = fireAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val uid = result.user?.uid ?: throw Exception("Error al iniciar sesión")

            // Cargar rol desde Firestore
            val doc = firestore.collection("users").document(uid).get().await()
            val role = doc.getString("role") ?: UserRole.CUSTOMER.name
            val username = doc.getString("username") ?: ""

            val token = "firebase_$uid"
            SessionManager.token         = token
            SessionManager.userId        = doc.getLong("apiId")?.toInt() ?: 0
            SessionManager.userRole      = UserRole.valueOf(role)
            SessionManager.localUsername = username
            SessionManager.localEmail    = email.trim()
            token
        } catch (e: Exception) {
            // Fallback: FakeStoreAPI (usuario admin de la API)
            try {
                val response = api.login(LoginRequest(email, password))
                SessionManager.token = response.token
                response.token
            } catch (e2: Exception) {
                if (SessionManager.matchesLocalCredentials(email, password)) {
                    val localToken = "local_token_${System.currentTimeMillis()}"
                    SessionManager.token = localToken
                    localToken
                } else throw Exception("Correo o contraseña incorrectos")
            }
        }
    }

    // ── Registro ─────────────────────────────────────────────────────────────
    suspend fun register(
        username: String,
        email: String,
        password: String,
        role: UserRole
    ): User {
        val result = fireAuth.createUserWithEmailAndPassword(email.trim(), password).await()
        val uid = result.user?.uid ?: throw Exception("No se pudo crear el usuario")

        val apiUser = try {
            api.createUser(User(username = username, email = email, password = password))
        } catch (e: Exception) {
            User(id = 0, username = username, email = email, password = "")
        }

        val userData = mapOf(
            "uid"       to uid,
            "apiId"     to apiUser.id,
            "username"  to username,
            "email"     to email.trim(),
            "role"      to role.name,
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("users").document(uid).set(userData).await()

        SessionManager.token         = "firebase_$uid"
        SessionManager.userId        = apiUser.id
        SessionManager.localUsername = username
        SessionManager.localPassword = password
        SessionManager.localEmail    = email.trim()
        SessionManager.userRole      = role

        return apiUser.copy(username = username, email = email)
    }

    // ── Obtener perfil ────────────────────────────────────────────────────────
    suspend fun getUser(id: Int): User {
        val uid = fireAuth.currentUser?.uid
        if (uid != null) {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                return User(
                    id       = doc.getLong("apiId")?.toInt() ?: id,
                    username = doc.getString("username") ?: "",
                    email    = doc.getString("email")    ?: "",
                    password = ""
                )
            }
        }
        if (SessionManager.hasLocalUser) {
            return User(
                id       = id,
                username = SessionManager.localUsername ?: "",
                email    = SessionManager.localEmail    ?: "",
                password = ""
            )
        }
        return api.getUserById(id)
    }

    // ── Actualizar perfil ─────────────────────────────────────────────────────
    suspend fun updateUser(id: Int, username: String, email: String, password: String): User {
        val uid = fireAuth.currentUser?.uid
        if (uid != null) {
            if (password.isNotBlank()) {
                fireAuth.currentUser?.updatePassword(password)?.await()
            }
            firestore.collection("users").document(uid)
                .update(mapOf("username" to username, "email" to email)).await()
        }
        return try {
            api.updateUser(id, User(id = id, username = username, email = email, password = password))
                .also {
                    SessionManager.localUsername = username
                    SessionManager.localEmail    = email
                    SessionManager.localPassword = password
                }
        } catch (e: Exception) {
            SessionManager.localUsername = username
            SessionManager.localEmail    = email
            SessionManager.localPassword = password
            User(id = id, username = username, email = email, password = "")
        }
    }

    // ── Eliminar cuenta ───────────────────────────────────────────────────────
    suspend fun deleteUser(id: Int): User {
        val uid = fireAuth.currentUser?.uid
        if (uid != null) {
            firestore.collection("users").document(uid).delete().await()
            fireAuth.currentUser?.delete()?.await()
        }
        return try { api.deleteUser(id) }
        catch (e: Exception) { User(id = id, username = "", email = "", password = "") }
    }
}