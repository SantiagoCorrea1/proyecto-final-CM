package com.example.e_commerce_cm.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.e_commerce_cm.data.model.UserRole

object SessionManager {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }

    var token: String?
        get() = prefs.getString("token", null)
        set(value) { prefs.edit().putString("token", value).apply() }

    var userId: Int
        get() = prefs.getInt("userId", 1)
        set(value) { prefs.edit().putInt("userId", value).apply() }

    var localUsername: String?
        get() = prefs.getString("local_username", null)
        set(value) { prefs.edit().putString("local_username", value).apply() }

    var localPassword: String?
        get() = prefs.getString("local_password", null)
        set(value) { prefs.edit().putString("local_password", value).apply() }

    var localEmail: String?
        get() = prefs.getString("local_email", null)
        set(value) { prefs.edit().putString("local_email", value).apply() }

    val hasLocalUser: Boolean get() = localUsername != null

    var userRole: UserRole
        get() = UserRole.valueOf(prefs.getString("user_role", UserRole.CUSTOMER.name) ?: UserRole.CUSTOMER.name)
        set(value) { prefs.edit().putString("user_role", value.name).apply() }

    val isAdmin: Boolean get() = userRole == UserRole.ADMIN

    val isLoggedIn: Boolean get() = token != null

    fun matchesLocalCredentials(username: String, password: String): Boolean =
        username == localUsername && password == localPassword

    fun logout() {
        prefs.edit().remove("token").apply()
    }

    fun clear() = prefs.edit().clear().apply()
}
