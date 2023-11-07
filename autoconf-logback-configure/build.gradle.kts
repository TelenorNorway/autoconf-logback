dependencies {
	implementation("no.telenor.kt:env:0.6.0")
	implementation("no.telenor.kt:panic:0.1.2")
	compileOnly("ch.qos.logback:logback-classic:1.4.11")
	implementation(project(":autoconf-logback-theme"))
}
