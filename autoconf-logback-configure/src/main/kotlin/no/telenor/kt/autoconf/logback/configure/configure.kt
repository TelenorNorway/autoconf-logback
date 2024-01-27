package no.telenor.kt.autoconf.logback.configure

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import no.telenor.kt.autoconf.logback.theme.Theme
import no.telenor.kt.env.Construct
import no.telenor.kt.panic
import org.slf4j.event.Level
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.nio.charset.Charset
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

// private val isDev = System.getProperty("java.class.path", "").contains(".gradle/caches")

class Configure {
	companion object {
		fun configure(loggerContext: LoggerContext): AutoconfLogbackConfiguration {
			val configuration = try {
				Construct.from<AutoconfLogbackConfiguration>()
			} catch (ex: Throwable) {
				panic("Could not load logback-autoconf configuration", ex)
			}

			configuration.debug("Configuration = $configuration")

			val theme: Theme = getTheme(
				configuration,
				whichTheme(configuration.theme, configuration.themePath),
				configuration.fallbackToJsonOnError
			)

			configuration.debug("Activating theme ${theme.javaClass.name}")

			val appender = ConsoleAppender<ILoggingEvent>()
			appender.context = loggerContext
			appender.name = "console"

			val pattern = Layout(theme, configuration)
			pattern.context = loggerContext
			pattern.start()

			val encoder = LayoutWrappingEncoder<ILoggingEvent>()
			encoder.context = loggerContext
			encoder.charset = Charset.forName("utf-8")
			encoder.layout = pattern

			appender.encoder = encoder
			appender.start()

			val mainLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)
			mainLogger.level = actualLevel(configuration.logLevel)
			mainLogger.detachAndStopAllAppenders()
			mainLogger.isAdditive = false
			mainLogger.addAppender(appender)

			loggerContext.maxCallerDataDepth = 2
			loggerContext.isPackagingDataEnabled = true

			configuration.logLevels.forEach {
				mainLogger.loggerContext.getLogger(it.key).level = actualLevel(it.value)
			}

			configuration.debug("Configuration over")

			return configuration
		}
	}
}

private fun actualLevel(level: Level) = when (level) {
	Level.ERROR -> ch.qos.logback.classic.Level.ERROR
	Level.WARN -> ch.qos.logback.classic.Level.WARN
	Level.INFO -> ch.qos.logback.classic.Level.INFO
	Level.DEBUG -> ch.qos.logback.classic.Level.DEBUG
	Level.TRACE -> ch.qos.logback.classic.Level.TRACE
}

private fun whichTheme(themeName: String?, directories: List<String>): Path? {
	themeName ?: return null
	for (directory in directories) {
		val jarPath = Path(directory, "$themeName.jar")
		val zipPath = Path(directory, "$themeName.zip")
		if (jarPath.exists()) return jarPath
		if (zipPath.exists()) return zipPath
	}
	return null
}

private fun getTheme(configuration: AutoconfLogbackConfiguration, path: Path?, fallbackToJsonOnError: Boolean): Theme {
	if (path == null) {
		configuration.debug("getTheme(): no path to theme, falling back to JsonTheme")
		return JsonTheme()
	}
	try {
		val classLoader = URLClassLoader(arrayOf(path.toUri().toURL()))
		configuration.debug("Created class loader for $path")
		Construct.scan(classLoader)
		configuration.debug("Scanned class loader for env-parsers, now getting theme resources")
		for (resource in classLoader.getResources("META-INF/services/no.telenor.kt.autoconf.logback.theme.Theme")) {
			val className = resource.readText().split(newLineRegex).getOrNull(0) ?: continue
			configuration.debug("Loading $className")
			val clazz = classLoader.loadClass(className)
			configuration.debug("Loaded, constructing")
			val constructor = clazz.constructors
				.ifEmpty { arrayOf(clazz.getDeclaredConstructor()) }
				.firstOrNull { it.parameters.isEmpty() && Modifier.isPublic(it.modifiers) }
				?: throw Exception("${clazz.name} is needs a constructor that accepts zero parameters")
			val instance = constructor.newInstance()
			configuration.debug("Created instance")
			if (instance !is Theme) throw Exception("Provided theme-class is not a Theme")
			configuration.debug("Returning theme ${clazz.name}")
			return instance
		}
		configuration.debug("No theme found, falling back to json")
		return JsonTheme()
	} catch (ex: Throwable) {
		if (!fallbackToJsonOnError) throw ex
		configuration.debug("Error occured, falling back to json: $ex")
		return JsonTheme()
	}
}

private val newLineRegex = Regex("[\\r\\n]+")
