plugins {
    // Source: https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.spring") version "2.3.21"
    // Source: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web
    id("org.springframework.boot") version "4.0.5"
    // Source: https://mvnrepository.com/artifact/io.spring.dependency-management/io.spring.dependency-management.gradle.plugin
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.ivdnt"

kotlin {
    jvmToolchain(25)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Spring
    // Versions controlled by Spring Boot plugin
    implementation("org.springframework.boot:spring-boot-starter-web")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
//    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    // mock taggers with wiremock
    // Source: https://mvnrepository.com/artifact/org.wiremock/wiremock-jetty12
    testImplementation("org.wiremock:wiremock-jetty12:3.13.2")
    // Source: https://mvnrepository.com/artifact/org.wiremock.integrations/wiremock-spring-boot
    testImplementation("org.wiremock.integrations:wiremock-spring-boot:4.2.1")

    // kotlin
    // Versions controlled by Kotlin jvm plugin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")

    // logging
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.5.0")

    // yaml
    // Source: https://mvnrepository.com/artifact/org.yaml/snakeyaml
    implementation("org.yaml:snakeyaml:2.6")

    // json
    // Source: https://mvnrepository.com/artifact/tools.jackson.core/jackson-core
    implementation("tools.jackson.core:jackson-databind:3.1.1")
    implementation("tools.jackson.core:jackson-core:3.1.1")
    implementation("tools.jackson.module:jackson-module-kotlin:3.1.1")

    // xml
    // Source: https://mvnrepository.com/artifact/com.fasterxml/aalto-xml
    implementation("com.fasterxml:aalto-xml:1.3.4")

    // cache
    // Source: https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/caffeine
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.3")

    // reading microsoft word docx
    implementation("org.apache.poi:poi-ooxml:5.4.1")

    // reading pdf
    implementation("com.itextpdf:itextpdf:5.5.13.4")

    // immutable arrays
    // implementation("com.danrusu.pods4k:pods4k:0.7.0")
}

tasks.withType<Test> {
    environment(mapOf("spring.profiles.active" to "dev"))
    systemProperty("line.separator", "\n")
    useJUnitPlatform()
}
