package org.up

import org.springframework.boot.SpringApplication.run
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication
@EnableR2dbcRepositories(value = arrayOf("org.up.coroutines", "org.up.reactor"))
@EnableJpaRepositories("org.up.blocking")
class SpringApplication

fun main(args: Array<String>) {
    run(SpringApplication::class.java, *args)
}
