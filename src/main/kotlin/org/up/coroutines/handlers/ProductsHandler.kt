package org.up.coroutines.handlers

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.server.*
import org.up.coroutines.model.Product
import org.up.coroutines.model.ProductStockView
import org.up.coroutines.repository.ProductRepositoryCoroutines
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.*
import kotlin.math.absoluteValue

@Component
class ProductsHandler(
    @Autowired var webClient: WebClient,
    @Autowired var productRepository: ProductRepositoryCoroutines,
) {
    @FlowPreview
    suspend fun findAll(request: ServerRequest): ServerResponse =
        ServerResponse.ok().json().bodyAndAwait(productRepository.getAllProducts())

    suspend fun findOneInStock(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toInt()

        val product: Deferred<Product?> =
            GlobalScope.async {
                productRepository.getProductById(id)
            }
        val quantity: Deferred<Int> =
            GlobalScope.async {
                webClient.get()
                    .uri("/products/$id/quantity")
                    .accept(MediaType.APPLICATION_JSON)
                    .awaitExchange().awaitBody<Int>()
            }
        return ServerResponse.ok().json().bodyValueAndAwait(ProductStockView(product.await()!!, quantity.await()))
    }

    suspend fun findOne(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toInt()
        return ServerResponse.ok().json().bodyValueAndAwait(productRepository.getProductById(id)!!)
    }

    suspend fun allMessagesFlux(request: ServerRequest): ServerResponse {
        val flux = Flux.interval(Duration.ofSeconds(1)).map { i -> i.toString() } // .asFlow()
        return ServerResponse.ok().sse().body(flux).awaitFirst()
    }

    suspend fun allMessagesFlow(request: ServerRequest): ServerResponse {
        val size = request.queryParam("size").map { it.toInt() }.orElse(10)
        val f =
            flow {
                (1..size).forEach {
                    delay(1000)
                    emit(it.toString())
                }
            }
        return ServerResponse.ok().sse().bodyAndAwait(f)
    }

    suspend fun forwardingEndpoint(request: ServerRequest): ServerResponse {
        val size = request.queryParam("size").map { it.toInt() }.orElse(10)
        val delay = request.queryParam("delay").map { it.toInt() }.orElse(500)
        val type: ParameterizedTypeReference<ServerSentEvent<String>> =
            object : ParameterizedTypeReference<ServerSentEvent<String>>() {}
        val stream =
            webClient.get()
                .uri("/products/sse/delayed?size=$size&delay=$delay")
                .retrieve()
                .bodyToFlux(type)
                .map { it.data()!! }
                .asFlow()
        return ServerResponse.ok().sse().bodyAndAwait(stream)
    }

    suspend fun delayedEndpoint(request: ServerRequest): ServerResponse {
        val size = request.queryParam("size").map { it.toInt() }.orElse(10)
        val delay = request.queryParam("delay").map { it.toInt() }.orElse(500)
        val f =
            flow {
                (1..size).forEach {
                    delay(delay.toLong())
                    emit(it.toString())
                }
            }
        return ServerResponse.ok().sse().bodyAndAwait(f)
    }

    private val channel = MutableSharedFlow<String>(replay = 128)

    suspend fun produceChannel(request: ServerRequest): ServerResponse {
        val size = request.queryParam("size").map { it.toInt() }.orElse(10)
        val wait = request.queryParam("delay").map { it.toInt() }.orElse(500)
        val close = request.queryParam("close").map { it.toBoolean() }.orElse(false)

        if (close) {
            channel.resetReplayCache()
        } else {
            GlobalScope.launch {
                (1..size).forEach {
                    println("producing values $it")
                    delay(wait.toLong())
                    channel.emit("channelmsg=$it")
                }
            }
        }

        return ServerResponse.ok().bodyValueAndAwait("feed channel")
    }

    suspend fun consumeChannel(request: ServerRequest): ServerResponse {
        val flow = channel.asSharedFlow()
        return ServerResponse.ok().sse().bodyAndAwait(flow)
    }

    // =================================================
    suspend fun consumeDynamic(request: ServerRequest): ServerResponse {
        val seq: Sequence<Int?> = generateSequence(5) { getItem() }
        val f: Flow<String> =
            flow {
                suspend fun take() =
                    seq.takeWhile { it != null }.forEach {
                        println("emitting")
                        delay(500)
                        emit(it.toString())
                    }
                take()

                channel.collect {
                    println("notification")
                    take()
                }
            }
        return ServerResponse.ok().sse().bodyAndAwait(f)
    }

    fun getItem(): Int? =
        (Random().nextInt().absoluteValue % 5).run {
            if (this != 0) this else null
        }
}

@Configuration
class RouterConfiguration {
    @FlowPreview
    @Bean
    fun productRoutes(productsHandler: ProductsHandler) =
        coRouter {
            GET("/products", productsHandler::findAll)
            accept(MediaType.TEXT_EVENT_STREAM).nest {
                GET("/products/sse", productsHandler::allMessagesFlux)
            }
            accept(MediaType.TEXT_EVENT_STREAM).nest {
                GET("/products/sse2", productsHandler::allMessagesFlow)
            }
            accept(MediaType.TEXT_EVENT_STREAM).nest {
                GET("/products/sse/forwarding", productsHandler::forwardingEndpoint)
            }
            accept(MediaType.TEXT_EVENT_STREAM).nest {
                GET("/products/sse/delayed", productsHandler::delayedEndpoint)
            }

            accept(MediaType.TEXT_EVENT_STREAM).nest {
                GET("/products/sse/produce", productsHandler::produceChannel)
            }
            accept(MediaType.TEXT_EVENT_STREAM).nest {
                GET("/products/sse/consume", productsHandler::consumeChannel)
            }

            accept(MediaType.TEXT_EVENT_STREAM).nest {
                GET("/products/sse/consume-dynamic", productsHandler::consumeDynamic)
            }
            GET("/products/{id}", productsHandler::findOne)
            GET("/products/{id}/stock", productsHandler::findOneInStock)
        }
}
