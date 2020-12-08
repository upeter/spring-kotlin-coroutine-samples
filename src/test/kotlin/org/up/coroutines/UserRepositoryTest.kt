package org.up.coroutines

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import org.up.coroutines.model.User
import org.up.coroutines.repository.UserRepository


@SpringBootTest
@Transactional
@ExtendWith(SpringExtension::class)
class UserRepositoryTest @Autowired constructor(
        val userRepository: UserRepository
) {

    @Test
    fun `should insert new user and find it by Id`() = runBlocking{
        val newUser = userRepository.save(User(userName = "Joe", email = "joe@home.nl"))
        newUser.id shouldNotBe null
        val foundUser = userRepository.findById(newUser.id!!)
        foundUser shouldBe newUser

    }
}