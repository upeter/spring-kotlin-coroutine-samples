package org.up.coroutines.config

import org.up.coroutines.handlers.ProductsHandler
import kotlinx.coroutines.FlowPreview
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class RouterConfiguration {

    @FlowPreview
    @Bean
    fun productRoutes(productsHandler: ProductsHandler) = coRouter {
        GET("/products/", productsHandler::findAll)
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