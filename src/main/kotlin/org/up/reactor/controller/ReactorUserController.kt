package org.up.reactor.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.up.coroutines.model.User
import org.up.reactor.repository.ReactorAvatarService
import org.up.reactor.repository.ReactorEnrollmentService
import org.up.reactor.repository.ReactorUserRepository
import org.up.utils.component1
import org.up.utils.component2
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import javax.transaction.Transactional


@RestController
open class ReactorUserController(
        private val reactorUserDao: ReactorUserRepository,
        private val reactorAvatarService: ReactorAvatarService,
        private val reactorEnrollmentService: ReactorEnrollmentService
) {

    @GetMapping("/reactor/users")
    @ResponseBody
    fun getUsers(): Flux<User> =
            reactorUserDao.findAll()

    @GetMapping("/reactor/{user-id}")
    @ResponseBody
    fun getUser(@PathVariable("user-id") id: Long): Mono<User> {
        return reactorUserDao.findById(id)
    }

    @PostMapping("/reactor/users")
    @ResponseBody
    @Transactional
    fun storeUser(@RequestBody user: User): Mono<User> {
        val avatarM = reactorAvatarService.randomAvatar().subscribeOn(Schedulers.elastic())
        val verifyEmailM = reactorEnrollmentService.verifyEmail(user.email).subscribeOn(Schedulers.elastic())
        return Mono.zip(avatarM, verifyEmailM).flatMap { (avatar, emailVerified) ->
            reactorUserDao.save(user.copy(avatarUrl = avatar.url, emailVerified = emailVerified))
        }
    }

    @GetMapping("/reactor/{user-id}/sync-avatar")
    @ResponseBody
    @Transactional
    fun syncAvatar(@PathVariable("user-id") id: Long): Flux<User> =
            reactorUserDao.findById(id)
                    .flatMap { user ->
                        reactorAvatarService.randomAvatar()
                                .flatMap { avatar ->
                                    reactorUserDao.save(user.copy(avatarUrl = avatar.url))
                                }
                    }.flux()


//    val emitterProcessor  = EmitterProcessor.create<String>()
//
//    @GetMapping(value = ["/reactor/users/stream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
//    fun userFlux(): Flux<User> {
//        reactorUserDao.findUsersGreatherThan(0)
//        val emitterProcessor = EmitterProcessor.create<String>()
//        val autoConnect = emitterProcessor.publish().autoConnect()
//        val sink = emitterProcessor.sink()
//        Flux.create(emitterProcessor.sink()).f
//        //storing randomId and processor sink details
//        randomIdMap.putIfAbsent(randomId, emitterProcessor)
//        /** This will return ping status to notify client as
//         * connection is alive until the randomId message received.  */
//        sendPingStatus(sink, randomId)
//    }
//
//
//    @KafkaListener(topics = "some-subscription-id", containerFactory = "kafkaListenerContainerFactory")
//    fun pushMessage(message: SomeMessage?, acknowledgment: Acknowledgment) {
//        val emitter: EmitterProcessor<*> = randomIdMap.get("randomId")
//        if (emitter != null) {
//            emitter.onNext(message)
//            emitter.onComplete()
//            randomIdMap.remove("randomId")
//            acknowledgment.acknowledge()
//        }
//    }
//
//    @GetMapping("/reactor/users/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
//    @ResponseBody
//    fun getUserStream(): Flux<User> {
//        Flux
//                .interval(Duration.ofMillis(1000))
//                .flatMap(tick -> repository.findAll())
//        .map(image -> {
//            Comment comment = new Comment();
//            comment.setImageId(image.getId());
//            comment.setComment(
//                    "Comment #" + counter.getAndIncrement());
//            return Mono.just(comment);
//        })
//        .flatMap(newComment ->
//        Mono.defer(() ->
//        commentController.addComment(newComment)))
//        .subscribe();
//        reactorUserDao.findAll()
//    }


//    fun <TRequest, TResponse> manyToMany(
//            rxRequest: Flux<TRequest>?,
//            delegate: Function<StreamObserver<TResponse>?, StreamObserver<TRequest>?>): Flux<TResponse>? {
//        return try {
//            val consumerStreamObserver: ReactorProducerConsumerStreamObserver<TRequest, TResponse> = ReactorProducerConsumerStreamObserver(rxRequest)
//            delegate.apply(CancellableStreamObserver(consumerStreamObserver, consumerStreamObserver::cancel))
//            consumerStreamObserver.rxSubscribe()
//            (consumerStreamObserver.getRxConsumer() as Flux<TResponse>)
//                    .transform(Operators.lift(SubscribeOnlyOnceLifter<TResponse>()))
//        } catch (throwable: Throwable) {
//            Flux.error(throwable)
//        }
//    }

}

