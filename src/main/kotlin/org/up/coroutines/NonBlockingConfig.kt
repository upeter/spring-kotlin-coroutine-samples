package org.up.coroutines.config

import io.r2dbc.h2.H2ConnectionConfiguration
import io.r2dbc.h2.H2ConnectionFactory
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.reactivestreams.Subscription
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.connectionfactory.init.CompositeDatabasePopulator
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator
import org.springframework.stereotype.Component
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.CoreSubscriber
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import reactor.core.publisher.Operators
import reactor.util.context.Context
import java.util.*
import java.util.stream.Collectors
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


@Configuration
class DatastoreConfig : AbstractR2dbcConfiguration() {
    @Value("\${spring.datasource.username}")
    private val userName: String = ""

    @Value("\${spring.datasource.password}")
    private val password: String = ""

    @Value("\${spring.datasource.url}")
    private val url: String = ""

    @Bean
    override fun connectionFactory(): ConnectionFactory {
        println("init connectionFactory")
        return if(url.contains("h2")) {
             H2ConnectionFactory(H2ConnectionConfiguration.builder()
                    //.inMemory(dbName)
                    .url(url.removePrefix("jdbc:h2:"))
                    .username(userName)
                    .password(password)
                    .build())
        } else {
             PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
                    .database("demo")
                    .host("localhost")
                    .username(userName)
                    .password(password)
                    .build()

            )
        }
    }

    @Bean
    fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer? {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
        val populator = CompositeDatabasePopulator()
        val schemaSql = when(connectionFactory) {
            is PostgresqlConnectionFactory -> "postgres-schema.sql"
            is H2ConnectionFactory -> "h2-schema.sql"
            else -> throw IllegalArgumentException("no schema.sql for connection factory ${connectionFactory::class.java}")
        }
        populator.addPopulators(ResourceDatabasePopulator(ClassPathResource(schemaSql)))
        populator.addPopulators(ResourceDatabasePopulator(ClassPathResource("data.sql")))
        initializer.setDatabasePopulator(populator)
        return initializer
    }

}
@Configuration
@EnableWebFlux
class WebFluxConfig : WebFluxConfigurer {

}


@Component
class MdcWebFilter : WebFilter {
    override fun filter(serverWebExchange: ServerWebExchange,
               webFilterChain: WebFilterChain): Mono<Void> {
        val reqId = UUID.randomUUID().toString().replace("-", "").take(10)
        MDC.put(MDC_REQUEST_ID, reqId)
        return webFilterChain.filter(serverWebExchange).subscriberContext{it.put(MDC_REQUEST_ID, reqId)}
    }
    companion object {
        const val MDC_REQUEST_ID = "req-id"
    }
}


@Configuration
class MdcContextLifterConfiguration {

    companion object {
        val MDC_CONTEXT_REACTOR_KEY: String = MdcContextLifterConfiguration::class.java.name
    }

    @PostConstruct
    fun contextOperatorHook() {
        Hooks.onEachOperator(MDC_CONTEXT_REACTOR_KEY, Operators.lift { _, subscriber -> MdcContextLifter(subscriber) })
    }

    @PreDestroy
    fun cleanupHook() {
        Hooks.resetOnEachOperator(MDC_CONTEXT_REACTOR_KEY)
    }

}

/**
 * Helper that copies the state of Reactor [Context] to MDC on the #onNext function.
 */
class MdcContextLifter<T>(private val coreSubscriber: CoreSubscriber<T>) : CoreSubscriber<T> {

    override fun onNext(t: T) {
        coreSubscriber.currentContext().copyToMdc()
        coreSubscriber.onNext(t)
    }

    override fun onSubscribe(subscription: Subscription) {
        coreSubscriber.onSubscribe(subscription)
    }

    override fun onComplete() {
        coreSubscriber.onComplete()
    }

    override fun onError(throwable: Throwable?) {
        coreSubscriber.onError(throwable)
    }

    override fun currentContext(): Context {
        return coreSubscriber.currentContext()
    }
}

/**
 * Extension function for the Reactor [Context]. Copies the current context to the MDC, if context is empty clears the MDC.
 * State of the MDC after calling this method should be same as Reactor [Context] state.
 * One thread-local access only.
 */
private fun Context.copyToMdc() {
    if (!this.isEmpty) {
        val map: Map<String, String> = this.stream()
                .collect(Collectors.toMap({ e -> e.key.toString() }, { e -> e.value.toString() }))
        MDC.setContextMap(map)
    } else {
        MDC.clear()
    }
}



@Configuration
class WebClientConfiguration {

    @Bean
    fun webClient() = WebClient.builder().baseUrl("http://localhost:8080").build()
}