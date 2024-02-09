package org.up.reactor

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import org.up.coroutines.model.User
import org.up.reactor.repository.ReactorUserRepository
import reactor.test.StepVerifier

@SpringBootTest
@Transactional
@ExtendWith(SpringExtension::class)
class ReactorUserRepositoryTest
    @Autowired
    constructor(
        val reactorUserRepository: ReactorUserRepository,
    ) {
        @Test
        fun `should insert new user and find it by Id`() {
            val newUser = reactorUserRepository.save(User(userName = "Joe", email = "joe@home.nl"))
            StepVerifier.create(newUser)
                .expectNextCount(1)
                .expectComplete()
                .verify()
            val foundUser = reactorUserRepository.findById(newUser.block()?.id!!)
            StepVerifier.create(foundUser)
                .assertNext { user -> user.userName shouldBe newUser.block()!!.userName }
                .expectComplete()
                .verify()
        }
    }
