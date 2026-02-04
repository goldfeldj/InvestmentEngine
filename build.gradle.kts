import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.4"

	kotlin("jvm") version "1.9.23"
	kotlin("plugin.spring") version "1.9.23"
	kotlin("plugin.jpa") version "1.9.23"
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

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")

	implementation("com.github.victools:jsonschema-generator:4.37.0")
	implementation("com.github.victools:jsonschema-module-jackson:4.37.0")

	// AI clients
	implementation("com.google.genai:google-genai:1.12.0")
	implementation("com.openai:openai-java:0.1.0-beta.10") // Or the current stable 4.x/5.x
	implementation("com.squareup.okhttp3:okhttp:4.12.0")
	implementation("com.openai:openai-java:4.17.0")

	implementation("org.springframework.boot:spring-boot-starter-web") // Provides RestClient

	// This includes jakarta.persistence automatically
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// Ensure you have the SQLite driver for the runtime
	implementation("org.hibernate.orm:hibernate-community-dialects:6.4.4.Final")
	implementation("org.xerial:sqlite-jdbc:3.45.1.0")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
