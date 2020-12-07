package org.up.blocking

import io.kotlintest.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import org.up.blocking.model.UserJpa
import org.up.blocking.repository.BlockingUserDao
import org.up.coroutines.model.User
import org.up.coroutines.repository.UserRepository


@SpringBootTest
@Transactional
@ExtendWith(SpringExtension::class)
@ActiveProfiles("default")
class BlockingUserRepositoryTest @Autowired constructor(
        val userRepository: BlockingUserDao
) {

    @Test
    fun `should insert new user and find it by Id`() {
        val newUser = userRepository.save(UserJpa(0, userName = "Joe", email = "joe@home.nl", emailVerified =  false, avatarUrl =  null))
        val foundUser = userRepository.findById(newUser.id).get()
        foundUser shouldBe newUser

    }
}