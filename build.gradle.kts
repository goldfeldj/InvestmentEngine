import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.4"

	kotlin("jvm") version "1.9.23"
	kotlin("plugin.spring") version "1.9.23"
	kotlin("plugin.serialization") version "1.9.23"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

kotlin {
	jvmToolchain(17)
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

	// Spring
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	// JSON
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	// Coroutines
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

	// Ktor
	val ktorVersion = "2.3.8"
	implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
	implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
	implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
	implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

	// Exposed
	val exposedVersion = "0.47.0"
	implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
	implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
	implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

	// DB
	runtimeOnly("com.h2database:h2")
	implementation("org.postgresql:postgresql:42.7.3")

	implementation("org.jsoup:jsoup:1.18.1")

	testImplementation(kotlin("test"))

	// Core Jackson and Kotlin support
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
	// The missing YAML dataformat extension
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")

	// AI clients
	implementation("com.google.genai:google-genai:1.12.0")
	implementation("com.openai:openai-java:4.17.0")
}

//dependencies {
//	// Align versions of all Kotlin components
//	implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
//
//	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
//	implementation("org.springframework.boot:spring-boot-starter-web")
//	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//	implementation("org.jetbrains.kotlin:kotlin-reflect")
//	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
//	runtimeOnly("com.h2database:h2")
//	testImplementation("org.springframework.boot:spring-boot-starter-test")
//
//	implementation("io.ktor:ktor-server-core-jvm:2.3.4")
//	implementation("io.ktor:ktor-server-netty-jvm:2.3.4")
//	implementation("io.ktor:ktor-server-content-negotiation:2.3.4")
//	implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")
//	implementation("org.jetbrains.exposed:exposed-core:0.41.1")
//	implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
//	implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
//	implementation("org.postgresql:postgresql:42.6.0")
//	testImplementation(kotlin("test"))
//	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3") // Use the latest version
//}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
