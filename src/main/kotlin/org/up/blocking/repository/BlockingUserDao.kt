package org.up.blocking.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.up.blocking.model.UserJpa
import org.up.coroutines.model.Avatar


@Repository
interface BlockingUserDao : JpaRepository<UserJpa, Long>

@Component
open class BlockingAvatarService(@Value("\${delay.avatar.ms}")val  delay:Long) {

    val restTemplate = RestTemplate()
    val baseUrl = "http://localhost:8081"

    fun randomAvatar(): Avatar =
            restTemplate.getForEntity("$baseUrl/avatar?delay=$delay", Avatar::class.java).body!!
}