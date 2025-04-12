version = "1.0.0"

plugins {
    `java-library`
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.1.4"))

    // WebFlux, Validation 등 프레임워크 관련 의존성이지만 core 모듈에서는 공통 API 역할로만 제공합니다.
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-validation")

    // Jackson Kotlin 모듈 (역/직렬화)
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    // 코루틴과 Reactor 연동
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.0")

    // aop
    api("org.springframework.boot:spring-boot-starter-aop")
}
