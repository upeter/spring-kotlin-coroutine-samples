package org.up.blocking.repository

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.up.blocking.model.UserJpa
import org.up.coroutines.model.AvatarDto


@Repository
interface BlockingUserDao : JpaRepository<UserJpa, Long>

@Component
class BlockingAvatarService(@Value("\${remote.service.delay.ms}")val  delay:Long,
                                 @Value("\${remote.service.url}")val baseUrl:String) {

    val restTemplate = RestTemplate()

    fun randomAvatar(): AvatarDto =
            restTemplate.getForEntity("$baseUrl/avatar?delay=$delay", AvatarDto::class.java).body!!.also {
                logger.debug("fetch random avatar...")
            }

    companion object {
        val logger = LoggerFactory.getLogger(BlockingAvatarService::class.java)
    }

}

@Component
class BlockingEnrollmentService(@Value("\${remote.service.delay.ms}")val  delay:Long,
                                     @Value("\${remote.service.url}")val baseUrl:String) {

    val restTemplate = RestTemplate()

    fun verifyEmail(email:String): Boolean =
            restTemplate.getForEntity("$baseUrl/echo?email=$email&value=true&delay=$delay", String::class.java).body!! == "true".also {
                        logger.debug("verify email $email...")
                    }

    companion object {
        val logger = LoggerFactory.getLogger(BlockingAvatarService::class.java)
    }

}