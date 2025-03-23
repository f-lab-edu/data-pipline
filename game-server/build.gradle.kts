import org.springframework.boot.gradle.tasks.run.BootRun

version = "1.0.0"

plugins {
    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management")
}


tasks.register<BootRun>("bootRunGameServer") {
    group = "application"
    mainClass.set("game.server.GameServerApplicationKt")
    classpath = sourceSets["main"].runtimeClasspath
}

dependencies {
    implementation(project(":core"))
    runtimeOnly(project(":infra"))

    // Logstash game
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    // oauth lobby
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // database
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    // aop
    implementation("org.springframework.boot:spring-boot-starter-aop")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
    }
}