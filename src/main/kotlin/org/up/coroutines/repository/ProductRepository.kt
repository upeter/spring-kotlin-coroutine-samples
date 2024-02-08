package org.up.coroutines.repository

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import org.up.coroutines.model.Product
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class ProductRepository(private val client: DatabaseClient) {

    fun getProductById(id: Int): Mono<Product> {
        return client.sql("SELECT * FROM product WHERE id = $1")
          .bind(0, id)
          .`as`(Product::class.java)
          .fetch()
          .one()
    }

    fun addNewProduct(name: String, price: Int): Mono<Product> {
        val product = Product(name =  name, price = price)
        return client.insert()
                .into(Product::class.java)
                .using(product)
                .map{i -> i.get("id", Integer::class.java)}
                .first()
                .map { id -> product.copy(id = id?.toInt()) }
    }

    fun getAllProducts(): Flux<Product> {
        return client.select().from("product")
          .`as`(Product::class.java)
          .fetch()
          .all()
    }
}

@Repository
class ProductRepositoryCoroutines(private val client: DatabaseClient) {

    suspend fun getProductById(id: Int): Product? =
            client.execute("SELECT * FROM product WHERE id = $1")
                    .bind(0, id)
                    .`as`(Product::class.java)
                    .fetch()
                    .one()
                    .awaitFirstOrNull()

    suspend fun addNewProduct(name: String, price: Int) =
            client.execute("INSERT INTO product (name, price) VALUES($1, $2)")
                    .bind(0, name)
                    .bind(1, price)
                    .then()
                    .awaitFirstOrNull()

    @FlowPreview
    fun getAllProducts(): Flow<Product> =
            client.select()
                    .from("products")
                    .`as`(Product::class.java)
                    .fetch()
                    .all()
                    .log()
                    .asFlow()
}