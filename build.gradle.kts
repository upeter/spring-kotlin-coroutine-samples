@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.org.jetbrains.kotlin.plugin.spring) // org.jetbrains.kotlin:kotlin-allopen
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.jpa) // all open for Entity classes
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.io.spring.dependency.management)
    alias(libs.plugins.org.jetbrains.kotlinx.kover)
    alias(libs.plugins.com.linecorp.build.recipe.plugin)
    alias(libs.plugins.org.jlleitschuh.gradle.ktlint)
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
    api(libs.org.jetbrains.kotlin.kotlin.script.runtime)
    api(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
    api(libs.org.jetbrains.kotlinx.kotlinx.coroutines.reactor)
    api(libs.org.jetbrains.kotlinx.kotlinx.coroutines.jdk8)
    api(libs.org.jetbrains.kotlinx.kotlinx.coroutines.slf4j)
    api(libs.org.springframework.boot.spring.boot.starter.webflux)
    api(libs.com.fasterxml.jackson.module.jackson.module.kotlin)
    api(libs.org.springframework.boot.spring.boot.starter.data.r2dbc)
    api(libs.io.r2dbc.r2dbc.h2)
    api(libs.io.r2dbc.r2dbc.spi)
    api(libs.org.springframework.boot.spring.boot.starter.data.jpa)
    testImplementation(libs.io.kotlintest.kotlintest.assertions)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.api)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
    testImplementation(libs.io.projectreactor.reactor.test)
    testImplementation(libs.org.jetbrains.kotlin.kotlin.test.junit5)
    testImplementation(libs.junit.junit)
}

group = "org.up"
version = "1.0.0"
description = "spring-boot-kotlin"
java.sourceCompatibility = JavaVersion.VERSION_17

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

