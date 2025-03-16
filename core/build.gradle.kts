version = "1.0.0"

plugins {
    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-validation")

    // Serialize kotlin
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    api("net.logstash.logback:logstash-logback-encoder:7.4")

    // coroutine
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.0")

    // kafka
    api("org.springframework.kafka:spring-kafka")
    api("io.projectreactor.kafka:reactor-kafka:1.3.22")

    // redis
    api("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
    }
}