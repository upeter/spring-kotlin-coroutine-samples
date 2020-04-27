package org.up.coroutines.controller

import org.springframework.web.bind.annotation.*
import org.up.blocking.model.UserJpa
import org.up.blocking.repository.BlockingAvatarService
import org.up.blocking.repository.BlockingEnrollmentService
import org.up.blocking.repository.BlockingUserDao
import org.up.utils.toNullable
import java.util.concurrent.CompletableFuture
import javax.transaction.Transactional

@RestController
class BlockingUserController(
        private val blockingUserDao: BlockingUserDao,
        private val blockingAvatarService: BlockingAvatarService,
        private val blockingEnrollmentService: BlockingEnrollmentService
) {


    @GetMapping("/blocking/users/{user-id}")
    @ResponseBody
    fun getUser(@PathVariable("user-id") id: Long = 0): UserJpa? =
            blockingUserDao.findById(id).toNullable()

    @GetMapping("/blocking/users")
    @ResponseBody
    fun getUsers(): List<UserJpa> =
            blockingUserDao.findAll()


    @PostMapping("/blocking/users")
    @ResponseBody
    @Transactional
    fun storeUser(@RequestBody user: UserJpa): UserJpa {
        val emailVerified = blockingEnrollmentService.verifyEmail(user.email)
        val avatarUrl = user.avatarUrl ?: blockingAvatarService.randomAvatar().url
        return blockingUserDao.save(user.copy(avatarUrl = avatarUrl, emailVerified = emailVerified))
    }


    @PostMapping("/futures/users")
    @ResponseBody
    fun storeUserFutures(@RequestBody user: UserJpa): UserJpa {
        val avatarUrlF = CompletableFuture.supplyAsync { blockingAvatarService.randomAvatar().url }
        val combinedF = CompletableFuture.supplyAsync {
            blockingEnrollmentService.verifyEmail(user.email)
        }.thenCombineAsync(avatarUrlF) { emailVerified, avatarUrl -> avatarUrl to emailVerified }
        val (avatarUrl, emailVerified) = combinedF.join()
        return blockingUserDao.save(user.copy(avatarUrl = avatarUrl, emailVerified = emailVerified))
    }

    @GetMapping("/blocking/users/{user-id}/sync-avatar")
    @ResponseBody
    fun syncAvatar(@PathVariable("user-id") id: Long = 0): UserJpa? =
            blockingUserDao.findById(id).toNullable()?.let {
                val avatar = blockingAvatarService.randomAvatar()
                blockingUserDao.save(it.copy(avatarUrl = avatar.url))
            }
}
