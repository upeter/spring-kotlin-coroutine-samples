package org.up.coroutines.controller

import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.*
import org.up.blocking.model.UserJpa
import org.up.blocking.repository.BlockingAvatarService
import org.up.blocking.repository.BlockingUserDao
import org.up.coroutines.model.User
import org.up.coroutines.repository.AvatarService
import org.up.coroutines.repository.UserDao
import org.up.utils.toNullable

@RestController
open class BlockingUserController(
        private val blockingUserDao: BlockingUserDao,
        private val blockingAvatarService: BlockingAvatarService
) {


    @GetMapping("/blocking/users/{user-id}")
    @ResponseBody
    open fun getUser(@PathVariable("user-id") id: Long = 0): UserJpa? =
            blockingUserDao.findById(id).toNullable()

    @GetMapping("/blocking/users")
    @ResponseBody
    open fun getUsers(): List<UserJpa> =
            blockingUserDao.findAll()


    @PostMapping("/blocking/users")
    @ResponseBody
    open fun storeUser(@RequestBody user:UserJpa): UserJpa =
            blockingUserDao.save(user)


    @GetMapping("/blocking/users/{user-id}/sync-avatar")
    @ResponseBody
    open fun syncAvatar(@PathVariable("user-id") id: Long = 0): UserJpa? =
            blockingUserDao.findById(id).toNullable()?.let {
                val avatar = blockingAvatarService.randomAvatar()
                blockingUserDao.save(it.copy(avatarUrl = avatar.url))
            }
}
