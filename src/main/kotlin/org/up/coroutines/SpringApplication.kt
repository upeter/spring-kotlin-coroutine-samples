package org.up.coroutines

import org.springframework.boot.SpringApplication.run
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class SpringApplication

fun main(args: Array<String>) {
    run(SpringApplication::class.java, *args)
}
