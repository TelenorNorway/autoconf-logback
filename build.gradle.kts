plugins {
	kotlin("jvm") version "1.9.20"
	id("no.ghpkg") version "0.3.3"
	`maven-publish`
}

allprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "no.ghpkg")

	group = "no.telenor.kt"
	version = versioning.environment()

	repositories {
		mavenCentral()
		git.hub("telenornorway")
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
