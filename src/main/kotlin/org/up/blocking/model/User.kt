package org.up.blocking.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType


@Entity(name = "users")
data class UserJpa(
        @field: javax.persistence.Id @field: GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long,
        val userName: String,
        val email: String,
        val emailVerified:Boolean,
        val avatarUrl: String?) {

}
