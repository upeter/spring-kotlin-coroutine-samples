package org.up.coroutines.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class Product(
    @Id
    var id: Int? = null,
    var name: String = "",
    var price: Int = 0,
)

data class ProductStockView(val product: Product, var stockQuantity: Int) {
    var id: Int? = 0
    var name: String = ""
    var price: Int = 0

    init {
        this.id = product.id
        this.name = product.name
        this.price = product.price
    }
}
