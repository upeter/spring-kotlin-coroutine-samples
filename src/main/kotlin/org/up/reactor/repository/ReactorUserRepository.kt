package org.up.reactor.repository

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import org.up.coroutines.model.AvatarDto
import org.up.coroutines.model.User
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface ReactorUserRepository : ReactiveCrudRepository<User, Long> {
    @Query("select * from users e where e.id > :id")
    fun findUsersGreatherThan(id: Long): Flux<User>
}

@Component
class ReactorAvatarService(
    @Value("\${remote.service.delay.ms}") val delayCfg: Long,
    @Value("\${remote.service.url}") val baseUrl: String,
) {
    private val client by lazy { WebClient.create(baseUrl) }

    fun randomAvatar(delay: Long? = null): Mono<AvatarDto> =
        client.get()
            .uri("/avatar?delay=${delay ?: delayCfg}")
            .retrieve()
            .bodyToMono(AvatarDto::class.java).also {
                logger.debug("fetch random avatar...")
            }

    companion object {
        val logger = LoggerFactory.getLogger(ReactorAvatarService::class.java)
    }
}

@Component
class ReactorEnrollmentService(
    @Value("\${remote.service.delay.ms}") val delayCfg: Long,
    @Value("\${remote.service.url}") val baseUrl: String,
) {
    private val client by lazy { WebClient.create(baseUrl) }

    fun verifyEmail(
        email: String,
        delay: Long? = null,
    ): Mono<Boolean> =
        client.get()
            .uri("/echo?email=$email&value=true&delay=${delay ?: delayCfg}")
            .retrieve()
            .bodyToMono(String::class.java)
            .map { it.toBoolean() }
            .also {
                logger.debug("verify email $email...")
            }

    companion object {
        val logger = LoggerFactory.getLogger(ReactorEnrollmentService::class.java)
    }
}
