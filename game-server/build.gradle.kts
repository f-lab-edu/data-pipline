version = "1.0.0"

plugins {
    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":core"))

    // Logstash game
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    // oauth lobby
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // kafka infra
    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.projectreactor.kafka:reactor-kafka:1.3.22")

    // redis infra
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // database infra
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("com.github.jasync-sql:jasync-r2dbc-mysql:2.2.4")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
    }
}