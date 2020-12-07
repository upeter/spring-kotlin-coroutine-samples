package org.up.reactorj.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.up.coroutines.model.User;
import reactor.core.publisher.Flux;

@Repository
public interface ReactorUserJRepository extends ReactiveCrudRepository<User, Long> {

    @Query("select * from users e where e.id > :id")
    public Flux<User> findUsersGreatherThan(Long id);

}