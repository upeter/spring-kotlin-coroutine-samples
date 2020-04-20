package org.up.coroutines.controller

import org.up.coroutines.model.Product
import org.up.coroutines.repository.ProductRepositoryCoroutines
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.up.coroutines.model.ProductStockView
import reactor.core.publisher.Flux
import java.time.Duration

class ProductControllerCoroutines {
    @Autowired
    lateinit var webClient: WebClient

    @Autowired
    lateinit var productRepository: ProductRepositoryCoroutines

    @GetMapping("/products/{id}")
    suspend fun findOne(@PathVariable id: Int): Product? {
        return productRepository.getProductById(id)
    }

    @GetMapping("/products/{id}/stock")
    suspend fun findOneInStock(@PathVariable id: Int): ProductStockView {
        val product: Deferred<Product?> = GlobalScope.async {
            productRepository.getProductById(id)
        }
        val quantity: Deferred<Int> = GlobalScope.async {
            webClient.get()
              .uri("/products/$id/quantity")
              .accept(APPLICATION_JSON)
              .awaitExchange().awaitBody<Int>()
        }
        return ProductStockView(product.await()!!, quantity.await())
    }

    @GetMapping(value = ["/products/sse"], produces = [(MediaType.TEXT_EVENT_STREAM_VALUE)])
    suspend fun allMessages(@RequestParam(defaultValue = "5") size: Int = 5): Flow<String> {
        return Flux.interval(Duration.ofSeconds(1)).map { i -> i.toString() }.asFlow()
    }


    @FlowPreview
    @GetMapping("/products/")
    fun findAll(): Flow<Product> {
        return productRepository.getAllProducts()
    }
}