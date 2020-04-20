package org.up.coroutines

import java.util.*
import kotlin.math.absoluteValue

fun main() {


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

