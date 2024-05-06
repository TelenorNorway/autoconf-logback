plugins {
	kotlin("jvm") version "1.9.20"
	id("sh.tnn") version "0.3.0"
	`maven-publish`
}

allprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "sh.tnn")

	group = "no.telenor.kt"

	repositories {
		mavenCentral()
		telenor.public()
	}

	dependencies {
		testImplementation(kotlin("test"))
	}

	tasks.test {
		useJUnitPlatform()
	}

	kotlin.jvmToolchain(17)
}

subprojects {
	apply(plugin = "maven-publish")

	publishing {
		repositories.github.actions()
		publications.register<MavenPublication>("gpr") {
			from(components["kotlin"])
		}
	}
}
