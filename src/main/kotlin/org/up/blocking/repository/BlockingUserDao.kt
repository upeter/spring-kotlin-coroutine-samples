package org.up.blocking.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.up.blocking.model.UserJpa
import org.up.coroutines.model.Avatar


@Repository
interface BlockingUserDao : JpaRepository<UserJpa, Long>

@Component
open class BlockingAvatarService {

    val restTemplate = RestTemplate()
    val baseUrl = "http://localhost:8081"

    fun randomAvatar(): Avatar =
            restTemplate.getForEntity("$baseUrl/avatar?delay=500", Avatar::class.java).body!!
}