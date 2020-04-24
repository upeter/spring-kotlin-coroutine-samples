package org.up.coroutines.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.up.coroutines.model.Avatar
import org.up.coroutines.model.User
import reactor.core.publisher.Mono


@Repository
interface UserRepository : ReactiveCrudRepository<User, Long>


@Component
class UserDao(val userRepository: UserRepository):CoroutineCrudRepository<User, Long>(userRepository) {}

@Component
open class AvatarService(@Value("\${delay.avatar.ms}")val  delay:Long) {

    private val client by lazy { WebClient.create("http://localhost:8081") }

    open suspend fun randomAvatar(): Avatar =
            client.get()
                    .uri("/avatar?delay=$delay")
                    .retrieve()
                    .awaitBody()
}