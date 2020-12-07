package org.up.reactorj;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.up.coroutines.model.User;
import org.up.reactorj.repository.ReactorUserJRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.transaction.Transactional;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNot.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@Transactional
@ExtendWith(SpringExtension.class)
class ReactorUserJRepositoryTest {

    @Autowired
    ReactorUserJRepository reactorUserRepository;

    @Test
    public void shouldInsertNewUserAndFindItById() {
        User newUser = new User(null, "test insert", "joe@home.nl", false, null);
        Mono<User> newUserWithId = reactorUserRepository.save(newUser);
        StepVerifier.create(newUserWithId)
                .assertNext(i -> assertNotEquals(i.getId(), null))
                .verifyComplete();

        Mono<User> user = reactorUserRepository.findById(newUserWithId.block().getId());
        StepVerifier.create(user)
                .assertNext(u -> MatcherAssert.assertThat(u.getUserName(), is("test insert")))
                .verifyComplete();
    }
}
