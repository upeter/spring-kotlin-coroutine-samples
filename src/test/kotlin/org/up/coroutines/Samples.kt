package org.up.coroutines

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.data.annotation.Id
import org.up.coroutines.model.User
import java.util.*
import kotlin.math.absoluteValue


data class Users(
                val userName: String,
                val email: String)
fun main() {

    val mapper = ObjectMapper()
            mapper.registerKotlinModule()
   println(mapper.writeValueAsString(User(null, "Jack", "Rabbit")))
println(mapper.readValue<User>("""{"firstName":"Jack","lastName":"Rabbit"}"""))

    fun getItem():Int? =
            (Random().nextInt().absoluteValue % 5).run {
                if (this != 0) this else null
            }

    val i:Int? = null
    val seq:Sequence<Int?> = generateSequence(3){getItem()}
    fun take() =seq.takeWhile { it != null }.forEach { print(it.toString()) }.also {  println("") }

    take()
    take()
}

