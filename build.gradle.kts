plugins {
	java
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

repositories {
	mavenCentral()
	maven("https://repo.papermc.io/repository/maven-public/")
	maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")

	testImplementation(platform("org.junit:junit-bom:5.10.2"))
	testImplementation("org.junit.jupiter:junit-jupiter")
}

sourceSets {
	main {
		java {
			exclude("com/apexguard/network/ProtocolLib*.java")
		}
	}
}

tasks.test {
	useJUnitPlatform()
}

// Ensure reproducible builds
tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}

tasks.jar {
	archiveClassifier.set("")
}