import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.3"
	id("io.spring.dependency-management") version "1.1.4"
	id("org.jetbrains.dokka") version "1.9.10"
	kotlin("jvm") version "1.9.22"
	kotlin("plugin.spring") version "1.9.22"
	kotlin("plugin.serialization") version "1.9.22"
}

group = "org.ivdnt"
version = "0.0.2-ALPHA-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
	gradlePluginPortal()
}

dependencies {
	// Spring
	implementation("org.springframework.boot:spring-boot-starter-web:3.2.4")
	// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-devtools
	implementation("org.springframework.boot:spring-boot-devtools:3.2.3")

	// kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
	// https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib
	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3") // JVM dependency

	// swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")


	implementation("com.beust:klaxon:5.6")
	implementation("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")

	// yaml
	// https://mvnrepository.com/artifact/org.yaml/snakeyaml
	implementation("org.yaml:snakeyaml:2.2")

	// Tests
	testImplementation ("org.springframework.boot:spring-boot-starter-test:3.2.3")
}

tasks.test {
	environment(mapOf("profile" to "dev"))
	useJUnitPlatform()
}

tasks.withType<Test> {
	useJUnitPlatform()
	// https://stackoverflow.com/questions/52733942/increase-heap-memory-for-gradle-test
//	minHeapSize = "4096m"
//	maxHeapSize = "4096m"
//	jvmArgs = listOf("-XX:MaxPermSize=1024m") // fails on some IDEs
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<DokkaTask>().configureEach {
	dokkaSourceSets {
		configureEach {
			documentedVisibilities.set(setOf(Visibility.PUBLIC, Visibility.PROTECTED, Visibility.PRIVATE, Visibility.INTERNAL))
		}
	}
}