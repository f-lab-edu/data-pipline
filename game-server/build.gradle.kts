version = "1.0.0"

plugins {
    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Serialize kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    // oauth
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
    }
}