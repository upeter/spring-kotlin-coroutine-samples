package org.up.reactorj.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.up.coroutines.model.User;
import org.up.reactorj.model.UserBuilder;
import org.up.reactorj.repository.ReactorAvatarJService;
import org.up.reactorj.repository.ReactorUserJRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.transaction.Transactional;

@RestController
public class UserJController {

    private final ReactorUserJRepository reactorUserDao;
    private final ReactorAvatarJService reactorAvatarService;

    @Autowired
    public UserJController(ReactorUserJRepository reactorUserDao, ReactorAvatarJService reactorAvatarService) {
        this.reactorUserDao = reactorUserDao;
        this.reactorAvatarService = reactorAvatarService;
    }

    @GetMapping("/reactorj/users/{user-id}")
    @ResponseBody
    public Flux<User> getUser(@PathVariable("user-id") Long id) {
        return reactorUserDao.findById(id).flux();
    }


    @GetMapping("/reactorj/{user-id}/sync-avatar")
    @ResponseBody
    @Transactional
    public Flux<User> syncAvatar(@PathVariable("user-id") Long id, @RequestParam(required = false) Long delay) {
        return reactorUserDao.findById(id)
                .flatMap(user ->
                        reactorAvatarService.randomAvatar(delay)
                                .flatMap(avatar ->
                                        reactorUserDao.save(UserBuilder
                                                .from(user)
                                                .withAvatarUrl(avatar.getUrl())
                                                .build())
                                )
                ).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find user with id=$id")))
                .flux();
    }


}

