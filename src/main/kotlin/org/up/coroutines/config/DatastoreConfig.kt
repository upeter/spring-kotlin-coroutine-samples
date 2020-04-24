package org.up.coroutines.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.r2dbc.h2.H2ConnectionConfiguration
import io.r2dbc.h2.H2ConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.connectionfactory.init.CompositeDatabasePopulator
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer


@Configuration
@EnableR2dbcRepositories
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
        return H2ConnectionFactory(H2ConnectionConfiguration.builder()

                //.inMemory(dbName)
                .url(url)
          .username(userName)
          .password(password)
          .build())
    }

    @Bean
    fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer? {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
        val populator = CompositeDatabasePopulator()
        populator.addPopulators(ResourceDatabasePopulator(ClassPathResource("schema.sql")))
        populator.addPopulators(ResourceDatabasePopulator(ClassPathResource("data.sql")))
        initializer.setDatabasePopulator(populator)
        return initializer
    }

//
//    @Bean
//    fun objectMapper():ObjectMapper =
//        ObjectMapper().apply { registerKotlinModule() }
//
//    @Bean
//    fun javatimeModule(): JavaTimeModule {
//        return  JavaTimeModule();
//    }
//
//
//    @Bean
//    fun jackson2JsonEncoder(mapper:ObjectMapper ): Jackson2JsonEncoder {
//        return  Jackson2JsonEncoder(mapper);
//    }
//
//    @Bean
//    fun jackson2JsonDecoder(mapper:ObjectMapper ): Jackson2JsonDecoder {
//        return  Jackson2JsonDecoder(mapper);
//    }
//
////    @Bean
////     fun webFluxConfigurer(encoder:Jackson2JsonEncoder , decoder:Jackson2JsonDecoder ):WebFluxConfigurer {
////        return WebFluxConfigurer() {
////            override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
////                configurer.defaultCodecs().jackson2JsonEncoder(encoder);
////                configurer.defaultCodecs().jackson2JsonDecoder(decoder);
////            }
////        }
////    }

}
@Configuration
@EnableWebFlux
class WebFluxConfig : WebFluxConfigurer {
    @Bean
    fun objectMapper():ObjectMapper =
            ObjectMapper().run { registerKotlinModule() }

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        configurer.defaultCodecs().jackson2JsonEncoder(
                Jackson2JsonEncoder(objectMapper())
        )
        configurer.defaultCodecs().jackson2JsonDecoder(
                Jackson2JsonDecoder(objectMapper())
        )
    }
}