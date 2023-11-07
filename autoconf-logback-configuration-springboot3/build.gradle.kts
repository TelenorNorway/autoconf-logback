dependencies {
	implementation(project(":autoconf-logback-configure"))
	compileOnly("org.springframework:spring-context:6.0.13")
	compileOnly("ch.qos.logback:logback-classic:1.4.11")
	compileOnly("org.slf4j:slf4j-api:2.0.9")
}
