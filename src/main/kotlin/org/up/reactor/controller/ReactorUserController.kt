package org.up.reactor.controller

import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.up.coroutines.model.User
import org.up.reactor.repository.ReactorAvatarService
import org.up.reactor.repository.ReactorEnrollmentService
import org.up.reactor.repository.ReactorUserRepository
import org.up.utils.component1
import org.up.utils.component2
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
open class ReactorUserController(
    private val reactorUserDao: ReactorUserRepository,
    private val reactorAvatarService: ReactorAvatarService,
    private val reactorEnrollmentService: ReactorEnrollmentService,
) {
    @GetMapping("/reactor/users")
    @ResponseBody
    fun getUsers(): Flux<User> = reactorUserDao.findAll()

    @GetMapping("/reactor/{user-id}")
    @ResponseBody
    fun getUser(
        @PathVariable("user-id") id: Long,
    ): Mono<User> {
        return reactorUserDao.findById(id)
    }

    @PostMapping("/reactor/users")
    @ResponseBody
    @Transactional
    fun storeUser(
        @RequestBody user: User,
        @RequestParam(required = false) delay: Long? = null,
    ): Mono<User> {
        val avatarM = reactorAvatarService.randomAvatar(delay) // .subscribeOn(Schedulers.elastic())
        val verifyEmailM = reactorEnrollmentService.verifyEmail(user.email, delay) // .subscribeOn(Schedulers.elastic())
        return Mono.zip(avatarM, verifyEmailM).flatMap { (avatar, emailVerified) ->
            reactorUserDao.save(user.copy(avatarUrl = avatar.url, emailVerified = emailVerified))
        }
    }

    @GetMapping("/reactor/{user-id}/sync-avatar")
    @ResponseBody
    @Transactional
    fun syncAvatar(
        @PathVariable("user-id") id: Long,
        @RequestParam(required = false) delay: Long? = null,
    ): Flux<User> {
        return reactorUserDao.findById(id)
            .flatMap { user ->
                reactorAvatarService.randomAvatar(delay)
                    .flatMap { avatar ->
                        reactorUserDao.save(user.copy(avatarUrl = avatar.url))
                    }
            }.switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find user with id=$id")))
            .flux()
    }
}
