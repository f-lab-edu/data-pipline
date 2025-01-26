plugins {
    kotlin("jvm") version "2.0.21"
    id("io.spring.dependency-management") version "1.1.3" apply false
}

subprojects {
    group = "data.pipeline"

    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        testImplementation(kotlin("test"))
    }
    tasks.test {
        useJUnitPlatform()
    }

    kotlin {
        jvmToolchain(17)
    }

    repositories {
        mavenCentral()
    }
}

