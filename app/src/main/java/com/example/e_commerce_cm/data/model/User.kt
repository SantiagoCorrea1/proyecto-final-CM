package com.example.e_commerce_cm.data.model

enum class UserRole { CUSTOMER, ADMIN }

data class User(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val password: String = ""
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String
)
