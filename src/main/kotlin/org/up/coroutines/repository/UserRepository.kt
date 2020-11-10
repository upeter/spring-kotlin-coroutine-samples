package org.up.coroutines.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.up.coroutines.model.AvatarDto
import org.up.coroutines.model.User
import reactor.core.publisher.Flux


@Repository
interface UserRepository : CoroutineCrudRepository<User, Long> {

    //@Query("select * from users e where e.id > :id")
    fun findById_GreaterThan(id: Long) : Flow<User>

    suspend fun findByUserName(userName: String) : User?
}


//@Component
//class UserRepository(val reactiveUserRepository: ReactiveUserRepository) : CoroutineCrudRepository<User, Long> {
//    suspend fun findUsersGreatherThan(id:Long) = findUsersGreatherThan(id).asFlow()
//
//}

@Component
class AvatarService(@Value("\${remote.service.delay.ms}") val delay: Long,
                    @Value("\${remote.service.url}") val baseUrl: String) {

    private val client by lazy { WebClient.create(baseUrl) }

    suspend fun randomAvatar(): AvatarDto =
            client.get()
                    .uri("/avatar?delay=$delay")
                    .retrieve()
                    .awaitBody<AvatarDto>().also {
                        logger.debug("fetch random avatar...")
                    }

    companion object {
        val logger = LoggerFactory.getLogger(AvatarService::class.java)
    }

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
                    .toBoolean().also {
                        logger.debug("verify email $email...")
                    }

    companion object {
        val logger = LoggerFactory.getLogger(EnrollmentService::class.java)
    }


}