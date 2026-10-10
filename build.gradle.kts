plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.concatstudio.onegym"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "com.concatstudio.onegym.MainKt"
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation("io.ktor:ktor-server-cors:3.6.0")
    implementation(ktorLibs.server.netty)
    implementation("io.ktor:ktor-server-auth-jwt:3.6.0")
    implementation(libs.logback.classic)
    implementation("io.insert-koin:koin-ktor:4.2.2")
    implementation("io.insert-koin:koin-logger-slf4j:4.2.2")
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.spring.security.crypto)
    implementation(libs.spring.core)
    implementation("com.zaxxer:HikariCP:7.0.2")
    runtimeOnly("org.postgresql:postgresql:42.7.8")

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}
