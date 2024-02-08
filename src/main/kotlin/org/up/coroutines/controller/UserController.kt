package org.up.coroutines.controller

import jakarta.transaction.Transactional
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.up.coroutines.model.User
import org.up.coroutines.repository.AvatarService
import org.up.coroutines.repository.EnrollmentService
import org.up.coroutines.repository.UserRepository

@RestController
class UserController(
    private val userRepository: UserRepository,
    private val avatarService: AvatarService,
    private val enrollmentService: EnrollmentService,
) {
    @GetMapping("/users")
    @ResponseBody
    suspend fun getUsers(): Flow<User> = userRepository.findAll()

    @GetMapping("/users/{user-id}")
    @ResponseBody
    suspend fun getUser(
        @PathVariable("user-id") id: Long = 0,
    ): User? = userRepository.findById(id)

    @GetMapping("/user")
    @ResponseBody
    suspend fun userByName(
        @RequestParam("userName") userName: String,
    ): User? = userRepository.findByUserName(userName)

    @PostMapping("/users")
    @ResponseBody
    @Transactional
    suspend fun storeUser(
        @RequestBody user: User,
        @RequestParam(required = false) delay: Long? = null,
    ): User? =
        withContext(MDCContext()) {
            val emailVerified = async { enrollmentService.verifyEmail(user.email, delay) }
            val avatarUrl = async { user.avatarUrl ?: avatarService.randomAvatar(delay).url }
            userRepository.save(user.copy(avatarUrl = avatarUrl.await(), emailVerified = emailVerified.await())).also {
                channel.send(user.email)
            }
        }

    @GetMapping("/users/{user-id}/sync-avatar")
    @ResponseBody
    @Transactional
    suspend fun syncAvatar(
        @PathVariable("user-id") id: Long = 0,
        @RequestParam(required = false) delay: Long? = null,
    ): User =
        userRepository.findById(id)?.let {
            val avatar = avatarService.randomAvatar(delay)
            userRepository.save(it.copy(avatarUrl = avatar.url))
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find user with id=$id")

    @GetMapping("/fibanocci/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun fibanocciFlow(): Flow<Long> {
        val fibonacci: Sequence<Long> = generateSequence(Pair(0L, 1L), { Pair(it.second, it.first + it.second) }).map { it.first }
        return flow {
            fibonacci.forEach { next ->
                if (next >= 0L) {
                    delay(800)
                    emit(next)
                } else {
                    return@flow
                }
            }
        }
    }

    @GetMapping("/infinite", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun infiniteFlow(): Flow<String> =
        flow {
            generateSequence(0) { it + 1 }.forEach {
                emit(it.toString() + " \n")
            }
        }

    @GetMapping("/infinite/sse", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun sseFlow(): Flow<ServerSentEvent<String>> =
        flow {
            generateSequence(0) { it + 1 }.map { it.toString() }.forEach {
                emit(ServerSentEvent.builder<String>().id(it).data(it).build())
                delay(it.toLong() % 1000)
            }
        }

    private val channel = BroadcastChannel<String>(128)

    @GetMapping("/users/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    suspend fun userFlow(
        @RequestParam("offset") offsetId: Long = 0,
    ): Flow<User> {
        val userFlow: Flow<User> =
            flow {
                var latestId = offsetId

                suspend fun take() =
                    userRepository.findById_GreaterThan(latestId).collect { user ->
                        emit(user).also { latestId = user.id!! }
                    }
                take()
                channel.consumeEach {
                    take()
                    channel.cancel()
                }
            }
        return userFlow
    }
}
