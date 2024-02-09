### Relevant Articles:
- [Non-blocking Spring Boot with Kotlin Coroutines](http://www.baeldung.com/non-blocking-spring-boot-with-kotlin-coroutines)


- Talk setup
Question
- experience with Threads, CompleteableFutures, Reactor/RxJava

- tell a story:

- there was once upon a time a happy programmer
- sequential code

- multicore revolution broke out
- fiddled with threads: and failed
- fiddled with better concurrency abstraction: and kind of lived
- but secretly he remembered the good old times

- then Kotlin gave him Coroutines the ultimate weapon 
- the weapon was kind of invisible 

- he went to the battle field called production and exposed himself to a heavy request traffic salvo while his collegues were watching him from a save distance
- and he succeeded


What is a coroutine?
- image processes/threads/coroutines
-- run example

Key ingredients for coroutines
- Coroutine Builders
- suspend functions
- Coroutine Scope 
- Coroutine Context


##Examples
- Get User
```
 curl http://localhost:8080/users/1
```

- Insert User
```
curl -X POST -H "Content-Type: application/json" -d '{"id":null,"userName":"JackRabbit","email":"Jack@Rabbit.com","avatarUrl":null}'  http://localhost:8080/users/
```

- Sync avatar
```
curl -X PATCH  http://localhost:8080/users/1/sync-avatar
```


##Perf tests
See: https://github.com/parkghost/gohttpbench
To build:
```
 go build -v -o gob github.com/parkghost/gohttpbench
```
- Get Performance
```
gob  -c 100 -n 500 -k http://localhost:8080/blocking/users/3/sync-avatar

```

- Post Performance
```
 gob  -c 100 -n 500 -T "application/json" -p user.json  -k   http://localhost:8080/blocking/users/

```


https://stackoverflow.com/questions/35628764/completablefuture-vs-spring-transactions

