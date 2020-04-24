package org.up.coroutines

import org.springframework.boot.SpringApplication.run
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication
@EnableR2dbcRepositories
class SpringApplication

fun main(args: Array<String>) {
    run(SpringApplication::class.java, *args)
}
