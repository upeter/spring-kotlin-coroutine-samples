package org.up.coroutines.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.net.URL
import java.util.*


@Table("users")
data class User(@Id var id: Long? = null,
                val firstName: String,
                val lastName: String,
                val avatarUrl: String? = null) {

}

data class Avatar @JsonCreator constructor(@JsonProperty("url") val url: String)