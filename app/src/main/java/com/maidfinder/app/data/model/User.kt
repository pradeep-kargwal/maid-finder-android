package com.maidfinder.app.data.model

/**
 * Represents a user in the MaidFinder system.
 * Both clients and maids share this base model.
 */
data class User(
    val id: String,
    val phone: String,
    val role: UserRole,
    val displayName: String,
    val photoUrl: String? = null,
    val language: String = "en",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

enum class UserRole {
    CLIENT, MAID
}
