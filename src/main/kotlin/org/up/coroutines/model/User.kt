package org.up.coroutines.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.net.URL
import java.util.*
//https://us02web.zoom.us/j/89308253544

@Table("users")
data class User(@Id val id: Long? = null,
                val userName: String,
                val email: String,
                val emailVerified: Boolean = false,
                val avatarUrl: String? = null) {

}


data class AvatarDto @JsonCreator constructor(@JsonProperty("url") val url: String)

