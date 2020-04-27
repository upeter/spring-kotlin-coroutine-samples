package org.up.coroutines.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.up.coroutines.model.AvatarDto
import org.up.coroutines.model.User


@Repository
interface UserRepository : ReactiveCrudRepository<User, Long>


@Component
class UserDao(val userRepository: UserRepository) : CoroutineCrudRepository<User, Long>(userRepository) {}

@Component
class AvatarService(@Value("\${remote.service.delay.ms}") val delay: Long,
                    @Value("\${remote.service.url}") val baseUrl: String) {

    private val client by lazy { WebClient.create(baseUrl) }

    suspend fun randomAvatar(): AvatarDto =
            client.get()
                    .uri("/avatar?delay=$delay")
                    .retrieve()
                    .awaitBody()
}

@Component
class EnrollmentService(@Value("\${remote.service.delay.ms}") val delay: Long,
                        @Value("\${remote.service.url}") val baseUrl: String) {

    private val client by lazy { WebClient.create(baseUrl) }

    suspend fun verifyEmail(email: String): Boolean =
            client.get()
                    .uri("/echo?email=$email&value=true&delay=$delay")
                    .retrieve()
                    .awaitBody<String>()
                    .toBoolean()

}