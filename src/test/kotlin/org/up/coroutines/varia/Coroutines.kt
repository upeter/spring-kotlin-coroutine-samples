package org.up.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.reactive.ContextInjector
import kotlinx.coroutines.reactive.awaitFirst
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.lang.Thread.sleep
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.system.measureTimeMillis
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import kotlin.coroutines.CoroutineContext


data class Product(val id: Int, val name: String)

fun findProduct(id: Int): CompletableFuture<Product> =
        CompletableFuture.supplyAsync { if (id == 9999) throw IllegalArgumentException("not found") else Product(id, "cool gadget") }

fun getRating(p: Product): CompletableFuture<Int> = CompletableFuture.supplyAsync { if (p.id > 1000) throw IllegalArgumentException("not found") else 10 }

suspend fun findProductCoroutine(id: Int): Product {
    delay(1000)
    return Product(id, "Cool gadget")
}

suspend fun getRatingCoroutine(p: Product): Int {
    delay(1000)
    return 10
}


suspend fun temperatureIn(city: String): Int {
    delay(1000)
    return Random().nextInt(30)
}

suspend fun temperatureInException(city: String): Int {
    delay(1000)
    return if (city == "???")
        throw IllegalArgumentException("??? does not exist")
    else Random().nextInt(30)
}


fun temperatureInFuture(city: String): CompletableFuture<Int> =
        CompletableFuture.supplyAsync {
            Thread.sleep(1000)
            Random().nextInt(30)
        }


fun memoryUsageCoroutines() = runBlocking {
    val cr = (1..2_400_000).toList().map {
        async {
            if (it % 100000 == 0) println("$it")
            repeat(1000) {
                //print(".")
                delay(500)
            }
        }
    }
    cr.forEach { it.await() }
    println("\ndone")
    Thread.sleep(600000)

}


fun playThread(note: String, elapse: Long = 100, rep: Int = 50) = repeat(rep) {
    sleep(elapse)
    println("${Thread.currentThread().name}: $note")
}

suspend fun play(note: String, elapse: Long = 100, rep: Int = 50) = repeat(rep) {
    delay(elapse)
    println("${Thread.currentThread().name}: $note")
}

fun myFirstThread() {
    val thread = thread {
        sleep(100)
        println("Hi")
    }
    println("Bonjour")
    thread.join()
}

fun mySecondThreads() {
    val thread = (1..1_000_000).toList().map {
        thread {
            if (it % 1000 == 0) println("Created $it threads")
            sleep(10000)
            print(".")
        }
    }
    thread.forEach { it.join() }
}

suspend fun myFirstCoroutine() {
    val job = GlobalScope.launch {
        delay(100)
        println("Hi")
    }
    println("Bonjour")
    job.join()
}

suspend fun mySecondCoroutine() = coroutineScope {
    val jobs = (1..1_000_000).toList().map {
        launch {
            if (it % 1000 == 0) println("Created $it coroutines")
            delay(10000)
            print(".")
        }
    }
    //jobs.forEach { it.join() }
}


suspend fun <T> CompletableFuture<T>.await2(): T = suspendCoroutine { cont ->
    whenComplete { value, exception ->
        when {
            exception != null -> cont.resumeWithException(exception)
            else -> cont.resume(value)
        }
    }
}


suspend fun <T> Mono<T>.await(): T = suspendCoroutine { cont ->
    subscribe(object : Subscriber<T> {
        private var value: T? = null
        override fun onNext(t: T) {
            value = t
        }

        override fun onError(e: Throwable) {
            cont.resumeWithException(e)
        }

        override fun onComplete() {
            cont.resume(value as T)
        }

        override fun onSubscribe(sub: Subscription) {
            sub.request(1)

        }
    })

}


fun futureExample() {
    GlobalScope.launch(Unconfined) {
        //coroutineScope {
        val future = future {
            println("start")
            val x = findProduct(10).await2()
            println("got '$x'")
            val y = getRating(x).await()
            println("got '$y' after '$x'")
            y
        }
        val r = future.await()
        println("$r")
    }
}
//}

fun futureExample2() {
    runBlocking {
        val product = findProduct(10).await()
        println("got '$product'")
        val rating = getRating(product).await()
        println("got '$rating' after '$product'")
    }
}


fun futureExampleTemperature() {


    runBlocking {
        val time = measureTimeMillis {
            val future = GlobalScope.future {
                val ams = temperatureInFuture("Amsterdam")
                val zrh = temperatureInFuture("Zurich")
                "The answer is ams: ${ams.await()} zrh: ${zrh.await()}"

            }
            println(future.await())

        }
        println("Completed in $time ms")
    }
}

fun futureExampleTemperature2() {
    runBlocking {
        val time = measureTimeMillis {
            val ams = temperatureInFuture("Amsterdam")
            val zrh = temperatureInFuture("Zurich")
            "The answer is ams: ${ams.await()} zrh: ${zrh.await()}"
        }

        println("Completed in $time ms")
    }
}


fun coRoutineExampleTemperature() {
    runBlocking {
        val time = measureTimeMillis {
            val ams = async { temperatureIn("Amsterdam") }
            val zrh = async { temperatureIn("Zurich") }
            println("The answer is ams: ${ams.await()} zrh: ${zrh.await()}")

        }
        println("Completed in $time ms")
    }
}

fun coRoutineExampleTemperatureException() {
    runBlocking {
        val time = measureTimeMillis {
            try {
                val ams = async { temperatureInException("???") }
                val zrh = async { temperatureInException("Zurich") }
                println("The answer is ams: ${ams.await()} zrh: ${zrh.await()}")
            } catch (ex: Exception) {
                println("Oeps ${ex.message}")
            }

        }
        println("Completed in $time ms")
    }
}


fun coRoutineExample() {
    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        println(throwable)
    }

    val job = GlobalScope.launch(Dispatchers.Default + exceptionHandler) {
        val time = measureTimeMillis {
            val rating = async {
                val product = findProductCoroutine(1000)
                getRatingCoroutine(product)
            }
            println("The answer is ${rating.await()}")
        }
        println("Completed in $time ms")
    }
    runBlocking {
        job.join()
    }
}


fun coRoutineSequentalExample() {
    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        println(throwable)
    }
    runBlocking {
        val job = launch(context = exceptionHandler) {
            val product = findProductCoroutine(1000)
            val rating = getRatingCoroutine(product)
            println("$product has rating: $rating")
        }

        println("done")
    }
}

suspend fun sendReceive(id: Int = 0,  channel:Channel<String>):Unit = coroutineScope{
    val msg = "${this.coroutineContext[CoroutineName.Key]} send msg ${id+1}"
    channel.send(msg)
    println(msg)
    delay(500)
    val received = channel.receive()
    println("${this.coroutineContext[CoroutineName.Key]} received=[$received]")
    sendReceive(id + 1, channel)
}

suspend fun send(channel:Channel<String>, msg:String) = coroutineScope{
    val msg = "${Thread.currentThread().name} $msg"
    channel.send(msg)
    println(msg)
    delay(500)
}


suspend fun receive(channel:Channel<String>) = coroutineScope{
    val received = channel.receive()
    println("${Thread.currentThread().name} received=[$received]")
}



suspend fun pingPong() = coroutineScope{
    val channel = Channel<String>()

    launch {
        suspend fun pongPing() {
            receive(channel)
            send(channel, "Pong")
            pongPing()
        }
        pongPing()
    }
    val v1 = GlobalScope.launch {
        suspend fun pingPong() {
            send(channel, "Ping")
            receive(channel)
            pingPong()
        }
        pingPong()
    }
    //v1.join()


}
suspend fun simpleChannel() = coroutineScope{
    val channel = Channel<String>()
    launch {
        println("Send: Rabbit")
        channel.send("Rabbit")
        delay(500)
        println("Send: Bear")
        channel.send("Bear")
        channel.close()

    }
    channel.consumeEach { println("Turtle meets: ${it}") }
}

suspend fun receiveAll() =  coroutineScope{
    val channel = Channel<String>()
    launch {
        channel.send("Rabbit")
        delay(500)
        channel.send("Bear")
        delay(500)
        channel.close()
    }
    channel.consumeEach { println("Turtle meets: ${it}") }
//    repeat (2) {
//        println("Turtle meets: ${channel.receive()}")
//    }
}

suspend fun broadcast() =  coroutineScope{
    val channel = BroadcastChannel<String>(Channel.CONFLATED)
    launch {
        channel.send("Rabbit")
        delay(500)
        channel.close()
    }
    launch {
        channel.openSubscription().consumeEach { println("Turtle 1 meets: ${it}") }
    }
    launch {
        channel.openSubscription().consumeEach { println("Turtle 2 meets: ${it}") }
    }
    val args = arrayOf("")
    GlobalScope.produce { args.forEach{send(it)} }
}


fun simpleFlow() = flow{
        emit("~ Go with ")
        delay(1000L)
        emit("the Flow ~")
    }.onEach { println("On each $it") }
        .onStart { println("Starting flow") }
        .onCompletion { println("Flow completed") }
        .catch { ex -> println("Exception message: ${ex.message}") }



fun main(args: Array<String>): Unit = runBlocking(Dispatchers.Default) {
    (1..10).forEach{println(it)}
    simpleFlow().collect{
        print(it)
    }


    //broadcast()
    //pingPong()


    //doIt_A()
    //    myFirstThread()
//mySecondThreads()
    //mySecondCoroutine()

    //memoryUsageCoroutines()
//    futureExample2()
//    coRoutineSequentalExample()
//    futureExampleTemperature()
//    coRoutineExampleTemperatureException()
    //coRoutineExampleTemperature()

}

class CurrencyService(val returnRate: Int, val latency: Long) {
    suspend fun rateUSD(): Int {
        delay(latency)
        return returnRate
    }

    fun rateEUR(): Int {
        Thread.sleep(latency)
        return returnRate
    }
}


val serviceBankA = CurrencyService(120, 1000)
val serviceBankB = CurrencyService(123, 2000)
val serviceBankC = CurrencyService(125, 3000)
val servicesBankABC = listOf(serviceBankA, serviceBankB, serviceBankC)


suspend fun doIt_A() = coroutineScope {
    val ctx = newSingleThreadContext("single-threaded-context")
    val executorService = ctx.executor as ExecutorService

    //Threads
    println("multiple threads threaded")
    val r0 = measureTimeMillis {
        val threads = servicesBankABC.map {
            thread {
                it.rateEUR()
                println(Thread.currentThread().name)
            }
        }
        threads.map { it.join() }.toSet()
    }
    println(r0)

    //Threads
    println("single thread threaded")
    val r0_ = measureTimeMillis {
        val threads = executorService.invokeAll(
                servicesBankABC.map {
                    Executors.callable {
                        it.rateEUR()
                        println(Thread.currentThread().name)
                    }
                })
        threads.map { it.get() }.toSet()
    }
    println(r0_)

    println("coroutines")
    val r1 = measureTimeMillis {
        val jobs = servicesBankABC.map {
            launch(ctx) {
                it.rateUSD()
                println(Thread.currentThread().name)
            }
        }
        jobs.map { it.join() }.toSet()
    }
    println(r1)
}


suspend fun doIt(): Unit {
    coroutineScope {
        //        launch(Dispatchers.Default) {
        val ms = measureTimeMillis {
            val rates = servicesBankABC.map { async { it.rateUSD() } }
            val avg = rates.map {
                println("${Thread.currentThread().name}")
                it.await()
            }.sum() / rates.size
            println("${Thread.currentThread().name} - avg: $avg")
        }
        println(ms)
//        }
    }
}


//@InternalCoroutinesApi
//fun main(args: Array<String>): Unit {
//    runBlocking {
//        val result = Mono.just("Hi").await()
//        println(result)
//        try {
//            Mono.error<Any>(java.lang.IllegalArgumentException("oeps")).await()
//        } catch (ex: Exception) {
//            println(ex.message)
//        }
//
//
//        //    myFirstThread()
////    //mySecondThreads()
////    mySecondCoroutine()
//
//        //memoryUsageCoroutines()
////    futureExample2()
////    coRoutineSequentalExample()
////    futureExampleTemperature()
////    coRoutineExampleTemperatureException()
//        //coRoutineExampleTemperature()
//
//    }
//}
