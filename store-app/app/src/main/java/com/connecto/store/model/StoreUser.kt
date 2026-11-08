package com.connecto.store.model

enum class StoreRole {
    ADMIN,
    MEMBER,
    GUEST
}

data class StoreUser(
    val username: String,
    val displayName: String,
    val avatarUrl: String = "",
    val role: StoreRole = StoreRole.MEMBER,
    val token: String = "",
    val isConnectoSso: Boolean = false
)
