package org.up.coroutines.controller

import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.*
import org.up.coroutines.model.User
import org.up.coroutines.repository.AvatarService
import org.up.coroutines.repository.UserDao

@RestController
open class UserController(
        private val userDao: UserDao,
        private val avatarService: AvatarService
) {


    @GetMapping("/users/{user-id}")
    @ResponseBody
    open suspend fun getUser(@PathVariable("user-id") id: Long = 0): User? =
            userDao.findById(id)

    @GetMapping("/users")
    @ResponseBody
    open suspend fun getUsers(): Flow<User> =
            userDao.findAll()


    @PostMapping("/users")
    @ResponseBody
    open suspend fun storeUser(@RequestBody user:User): User? =
            userDao.save(user)


    @GetMapping("/users/{user-id}/sync-avatar")
    @ResponseBody
    open suspend fun syncAvatar(@PathVariable("user-id") id: Long = 0): User? =
            userDao.findById(id)?.let {
                val avatar = avatarService.randomAvatar()
                userDao.save(it.copy(avatarUrl = avatar.url))
            }
}
