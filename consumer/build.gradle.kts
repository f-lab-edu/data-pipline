import org.springframework.boot.gradle.tasks.run.BootRun

version = "1.0.0"

plugins {
    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management")
}

tasks.register<BootRun>("bootRunConsumer") {
    group = "application"
    mainClass.set("com.game.ConsumerApplicationKt")
    classpath = sourceSets["main"].runtimeClasspath
}

dependencies {
    implementation(project(":core"))
    runtimeOnly(project(":infra"))
}