package org.up.coroutines.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.up.coroutines.model.Product
import org.up.coroutines.model.ProductStockView
import org.up.coroutines.repository.ProductRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@RestController
class ProductController {
    @Autowired
    lateinit var webClient: WebClient

    @Autowired
    lateinit var productRepository: ProductRepository

    @GetMapping("/productsf/{id}")
    fun findOne(
        @PathVariable id: Int,
    ): Mono<Product> {
        return productRepository
            .getProductById(id)
    }

    @GetMapping("/productsf/{id}/stock")
    fun findOneInStock(
        @PathVariable id: Int,
    ): Mono<ProductStockView> {
        val product = productRepository.getProductById(id)

        val stockQuantity =
            webClient.get()
                .uri("/productsf/$id/quantity")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono<Int>()
        return product.zipWith(stockQuantity) { productInStock, stockQty ->
            ProductStockView(productInStock, stockQty)
        }
    }

    @GetMapping("/productsf/{id}/quantity")
    fun getStockQuantity(): Mono<Int> {
        return Mono.just(2)
    }

    @GetMapping(value = ["/productsf/sse"], produces = [(MediaType.TEXT_EVENT_STREAM_VALUE)])
    fun allMessages(
        @RequestParam(defaultValue = "5") size: Int = 1000,
    ): Flux<String> {
        // return this.messageService.findWithTailableCursorByRoom(room)
        return Flux.interval(Duration.ofSeconds(1)).map { i -> i.toString() }
    }

    @GetMapping("/productsf/")
    fun findAll(): Flux<Product> {
        return productRepository.getAllProducts()
    }

    @PostMapping("/productsf/{name}/{price}")
    fun postProduct(
        @PathVariable("name") name: String,
        @PathVariable("price") price: Int,
    ): Mono<Product> {
        return productRepository.addNewProduct(name, price)
    }
}
