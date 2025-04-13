version = "1.0.0"

dependencies {
    implementation(project(":core"))

    // kafka infra
    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.projectreactor.kafka:reactor-kafka:1.3.22")

    // redis infra
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // database infra
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("com.github.jasync-sql:jasync-r2dbc-mysql:2.2.4")

    implementation("org.springframework.boot:spring-boot-starter-logging")
}