import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.org.jetbrains.kotlin.plugin.spring) // org.jetbrains.kotlin:kotlin-allopen
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.jpa) // all open for Entity classes
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
    alias(libs.plugins.org.jetbrains.kotlinx.kover)
    alias(libs.plugins.com.linecorp.build.recipe.plugin)
//    alias(libs.plugins.org.jlleitschuh.gradle.ktlint)
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.spring.io/snapshot")
    }

    maven {
        url = uri("https://repo.spring.io/milestone")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation(libs.org.springframework.boot.spring.boot.starter.data.jpa)
    implementation(libs.org.springframework.boot.spring.boot.starter.data.r2dbc)
    implementation(libs.org.springframework.boot.spring.boot.starter.webflux)
    implementation(libs.com.fasterxml.jackson.module.jackson.module.kotlin)
    implementation(libs.io.projectreactor.kotlin.reactor.kotlin.extensions)
    implementation(libs.org.jetbrains.kotlin.kotlin.reflect)
    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.reactor)

    implementation(libs.io.r2dbc.r2dbc.h2)
    implementation(libs.io.r2dbc.r2dbc.spi)
    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.slf4j)

    testImplementation(libs.io.projectreactor.reactor.test)
    testImplementation(libs.io.kotlintest.kotlintest.assertions)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.api)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
    testImplementation(libs.org.jetbrains.kotlin.kotlin.test.junit5)
}

group = "org.up"
version = "1.0.0"
description = "spring-boot-kotlin"
java.sourceCompatibility = JavaVersion.VERSION_21

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
    test {
        useJUnitPlatform()
    }
}
