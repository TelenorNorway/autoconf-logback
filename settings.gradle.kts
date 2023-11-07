pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "autoconf-logback"

include(
	"autoconf-logback-configure",
	"autoconf-logback-theme",
	"autoconf-logback-configuration-springboot3"
)
