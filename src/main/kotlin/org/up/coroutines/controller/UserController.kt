package org.up.coroutines.controller

import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.*
import org.up.coroutines.model.User
import org.up.coroutines.repository.AvatarService
import org.up.coroutines.repository.EnrollmentService
import org.up.coroutines.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.springframework.http.MediaType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ServerSentEvent
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.atomic.AtomicLong
import javax.transaction.Transactional

@RestController
class UserController(
        private val userRepository: UserRepository,
        private val avatarService: AvatarService,
        private val enrollmentService: EnrollmentService
) {


    @GetMapping("/users")
    @ResponseBody
    suspend fun getUsers(): Flow<User> =
            userRepository.findAll()


    @GetMapping("/users/{user-id}")
    @ResponseBody
    suspend fun getUser(@PathVariable("user-id") id: Long = 0): User? =
            userRepository.findById(id)


    @GetMapping("/user")
    @ResponseBody
    suspend fun userByName(@RequestParam("userName") userName: String): User? =
        userRepository.findByUserName(userName)


    @GetMapping("/users/{user-id}/sync-avatar")
    @ResponseBody
    @Transactional
    suspend fun syncAvatar(@PathVariable("user-id") id: Long = 0, @RequestParam(required = false) delay:Long? = null): User =
            userRepository.findById(id)?.let {
                val avatar = avatarService.randomAvatar(delay)
                userRepository.save(it.copy(avatarUrl = avatar.url))
            } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find user with id=$id")


    @GetMapping("/infinite")
    fun infiniteFlow(): Flow<String>  = flow {
        generateSequence(0){it + 1}.forEach {
            emit(it.toString() + " \n")
        }
    }

    @GetMapping("/infinite/sse", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun sseFlow(): Flow<ServerSentEvent<String>>  = flow {
        generateSequence(0){it + 1}.forEach {
            emit(ServerSentEvent.builder<String>().event("hello-sse-event").id(it.toString()).data("Your lucky number is $it").build())
            delay(500L)
        }
    }


    private val channel = BroadcastChannel<UserAddedNotification>(Channel.CONFLATED)


    @PostMapping("/users")
    @ResponseBody
    @Transactional
    suspend fun storeUser(@RequestBody user: User, @RequestParam(required = false) delay:Long? = null): User? = withContext(MDCContext()) {
        val emailVerified = async { enrollmentService.verifyEmail(user.email,  delay, user.emailVerified) }
        val avatarUrl = async { user.avatarUrl ?: avatarService.randomAvatar(delay).url }
        userRepository.save(user.copy(avatarUrl = avatarUrl.await(), emailVerified = emailVerified.await()))
    //        .also {
//            channel.send(it)
//        }
    }

    data class UserAddedNotification(val verified:Int = 0, val nonVerified:Int = 0) {
        fun has(filterVerified:Boolean) = (if(filterVerified) verified else nonVerified) > 0
    }

    val lastId = AtomicLong(0)
    @Scheduled(fixedRate = 1000)
    fun singlePoller() = runBlocking{
        userRepository.findById_GreaterThan(lastId.get()).toList().partition { it.emailVerified }.also {(verified, notVerified) ->
            channel.send(UserAddedNotification(verified = verified.size, nonVerified = notVerified.size))
            lastId.set((verified + notVerified).map { it.id ?: 0 }.max() ?: 0)
        }
    }



    @GetMapping("/users/stream0", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    suspend fun userFlow0(): Flow<User> = userRepository.findAll()


    @GetMapping("/users/stream1", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    suspend fun userFlow1(@RequestParam("offset") offsetId: Long = 0): Flow<User> = flow {
        var latestId = offsetId
        suspend fun take() = userRepository.findById_GreaterThan(latestId).collect { user ->
            emit(user).also { latestId = user.id!! }
        }
        while (true) {
            delay(1000)
            take()
        }
    }

    @GetMapping("/users/stream2", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    suspend fun userFlow2(@RequestParam("offset") offsetId: Long = 0): Flow<User> = flow {
        var latestId = offsetId
        suspend fun take() = userRepository.findById_GreaterThan(latestId).collect { user ->
            emit(user).also { latestId = user.id!! }
        }
        take()
        channel.openSubscription().consumeAsFlow().collect {
            take()
        }
    }


    @GetMapping("/users/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    suspend fun userFlow(@RequestParam("offset") offsetId: Long? = 0, @RequestParam("verified") filterVerified: Boolean = true): Flow<User> = flow {
        var lastId = offsetId ?: 0
        suspend fun take() = userRepository.findById_GreaterThanAndEmailVerified(lastId, filterVerified).collect { user ->
            emit(user).also { lastId = user.id!! }
        }
        take()
        channel.openSubscription().consumeAsFlow().filter { it.has(filterVerified) }.collect {
            take()
        }
    }


}
