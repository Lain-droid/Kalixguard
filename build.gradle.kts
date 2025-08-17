plugins {
	java
}

group = "com.apexguard"
version = "1.0.3"
description = "ApexGuard Anti-Cheat"

val includeProtocolLib: Boolean = (findProperty("includeProtocolLib") as String?)?.toBoolean()
	?: System.getenv("INCLUDE_PROTOCOL_LIB")?.toBoolean()
	?: false

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

repositories {
	mavenCentral()
	maven("https://repo.papermc.io/repository/maven-public/")
	maven("https://repo.codemc.io/repository/maven-public/")
	maven("https://repo.codemc.org/repository/maven-public/")
	maven("https://jitpack.io")
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
	
	// Adventure API for rich text components
	compileOnly("net.kyori:adventure-api:4.14.0")
	compileOnly("net.kyori:adventure-text-serializer-legacy:4.14.0")
	
	// Core dependencies
	implementation("com.google.guava:guava:32.1.3-jre")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
	implementation("org.apache.commons:commons-math3:3.6.1")
	implementation("org.apache.commons:commons-lang3:3.12.0")
	
	// ML and Statistics (using available alternatives)
	implementation("org.apache.commons:commons-math3:3.6.1")
	// Removed smile-core and smile-math due to availability issues
	
	// Redis for multi-instance support
	implementation("redis.clients:jedis:5.0.2")
	
	// HikariCP for connection pooling
	implementation("com.zaxxer:HikariCP:5.0.1")
	
	// Protocol handling (optional - will be loaded at runtime if available)
	if (includeProtocolLib) {
		compileOnly("com.github.dmulloy2:ProtocolLib:5.2.0")
	}
	
	// Metrics and monitoring
	implementation("io.micrometer:micrometer-core:1.12.2")
	
	// Logging
	implementation("ch.qos.logback:logback-classic:1.4.11")
	
	// Testing
	testImplementation(platform("org.junit:junit-bom:5.10.2"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testImplementation("org.mockito:mockito-core:5.7.0")
	testImplementation("org.mockito:mockito-junit-jupiter:5.7.0")
}

sourceSets {
	main {
		java {
			if (!includeProtocolLib) {
				exclude("com/apexguard/network/ProtocolLib*.java")
			}
		}
	}
}

// Expand plugin.yml with project properties
tasks.processResources {
	filesMatching("plugin.yml") {
		expand(mapOf(
			"project" to mapOf(
				"version" to project.version
			)
		))
	}
}

tasks.test {
	useJUnitPlatform()
}

// Ensure reproducible builds
tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	options.compilerArgs.addAll(listOf("-parameters"))
}

tasks.jar {
	archiveClassifier.set("")
	manifest {
		attributes(
			"Implementation-Title" to "ApexGuard Anti-Cheat",
			"Implementation-Version" to project.version,
			"Implementation-Vendor" to "ApexGuard Team"
		)
	}
}