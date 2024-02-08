package org.up.blocking.model

import jakarta.persistence.*

@Entity(name = "users")
data class UserJpa(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @Column(name = "user_name")
    val userName: String,
    val email: String,
    @Column(name = "email_verified")
    val emailVerified: Boolean,
    @Column(name = "avatar_url")
    val avatarUrl: String?,
)