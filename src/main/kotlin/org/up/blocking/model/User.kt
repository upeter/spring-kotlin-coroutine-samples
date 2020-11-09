package org.up.blocking.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType


@Entity(name = "users")
data class UserJpa(
        @field: javax.persistence.Id @field: GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long,
        @field:Column("user_name")
        val userName: String,
        val email: String,
        @field:Column("email_verified")
        val emailVerified:Boolean,
        @field:Column("avatar_url")
        val avatarUrl: String?) {

}
