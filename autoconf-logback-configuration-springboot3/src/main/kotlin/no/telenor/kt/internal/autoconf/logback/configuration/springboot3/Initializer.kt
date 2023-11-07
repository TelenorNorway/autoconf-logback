package no.telenor.kt.internal.autoconf.logback.configuration.springboot3

import ch.qos.logback.classic.LoggerContext
import no.telenor.kt.autoconf.logback.configure.Configure
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
	companion object {
		init {
			System.setProperty("spring.main.banner-mode", "off")
			System.setProperty("spring.main.log-startup-info", "off")
		}
	}

	override fun initialize(applicationContext: ConfigurableApplicationContext) {
		val context = LoggerFactory.getILoggerFactory() as LoggerContext
		Configure.configure(context)
	}
}
