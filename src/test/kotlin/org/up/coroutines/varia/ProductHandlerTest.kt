package org.up.coroutines.varia

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import org.springframework.web.reactive.function.client.WebClient
import org.up.coroutines.handlers.ProductsHandler
import org.up.coroutines.handlers.RouterConfiguration
import org.up.coroutines.model.Product
import org.up.coroutines.repository.ProductRepositoryCoroutines
import reactor.core.publisher.Flux

@WebFluxTest(
    excludeAutoConfiguration = [ReactiveUserDetailsServiceAutoConfiguration::class, ReactiveSecurityAutoConfiguration::class],
)
@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [ProductsHandler::class, RouterConfiguration::class])
class ProductHandlerTest {
    @Autowired
    private lateinit var client: WebTestClient

    @MockBean
    private lateinit var webClient: WebClient

    @MockBean
    private lateinit var productsRepository: ProductRepositoryCoroutines

    @FlowPreview
    @Test
    public fun `get all products`() {
        val productsFlow =
            Flux.just(
                Product(1, "product1", 1000),
                Product(2, "product2", 2000),
                Product(3, "product3", 3000),
            ).asFlow()
        given(productsRepository.getAllProducts()).willReturn(productsFlow)
        client.get()
            .uri("/products")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<Product>()
    }

//    @Test
//    public fun `insert product`() {
//                val product = Product(1, "product1", 1000)
//        productsRepository.addNewProduct(product.name, product.price)
//    }

    @Test
    fun `flow sample 1`() {
        fun foo(): Flow<Int> =
            flow {
                println("Flow started")
                for (i in 1..3) {
                    delay(1000)
                    emit(i)
                }
            }

        suspend fun performRequest(request: Int): String {
            delay(1000) // imitate long-running asynchronous work
            return "response $request"
        }
        println("Calling foo...")
        val flow = foo()
        runBlocking<Unit> {
            println("Calling collect...")
            flow.collect { value -> println(value) }
            println("Calling collect again...")
            flow.collect { value -> println(value) }

            withTimeoutOrNull(250) { // Timeout after 250ms
                foo().collect { value -> println(value) }
            }

            (1..3).asFlow() // a flow of requests
                .transform { request ->
                    emit("abc")
                    emit(performRequest(request))
                }
                .collect { response -> println(response) }
        }
    }
}
