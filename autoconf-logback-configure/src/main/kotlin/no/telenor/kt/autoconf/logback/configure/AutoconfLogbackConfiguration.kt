package no.telenor.kt.autoconf.logback.configure

import no.telenor.kt.autoconf.logback.theme.StackTraceElement
import no.telenor.kt.env.Env
import no.telenor.kt.env.EnvConstructor
import no.telenor.kt.env.ListEnv
import no.telenor.kt.env.MapEnv
import org.slf4j.event.Level

data class AutoconfLogbackConfiguration @EnvConstructor("LB_") constructor(
	/**
	 * The name of the theme to load.
	 *
	 * `null` is the default value, which means JSON logging compatible
	 * with Loki's json parser.
	 *
	 * **Environment variable name**: `LB_THEME`
	 *
	 * **Defaults to**: `null`
	 */
	@Env val theme: String? = null,

	internal val defaultThemePath: String = "~/.config/autoconfigure/logback/themes",

	/**
	 * A list (separated by `:`) of possible directories where
	 * `<[theme]>.jar` or `<[theme]>.zip` might be found in.
	 *
	 * **Environment variable name**: `LB_THEME_PATH`
	 *
	 * **Defaults to**: `$HOME/.config/logback/themes`
	 */
	@Env val themePath: @ListEnv(
		separator = "[:]+",
		regex = true
	) List<String> = listOf(defaultThemePath),

	/**
	 * Ignore any errors thrown by the loaded the [theme] and fallback
	 * to JSON logging.
	 *
	 * **Environment variable name**: `LB_FALLBACK_TO_JSON_ON_ERROR`
	 *
	 * **Defaults to**: `true`
	 */
	@Env val fallbackToJsonOnError: Boolean = true,

	/**
	 * Stacktrace frame elements to exclude from the log output.
	 *
	 * **Environment variable name**: `LB_STACKTRACE_EXCLUDE`
	 *
	 * **Example**
	 *
	 * ```
	 * ^jdk\.internal\.
	 * ^java\.lang\.reflect\.Method\.invoke\(
	 * ```
	 */
	@Env val stacktraceExclude: @ListEnv(separator = "[\\s\\r\\n\\t]+", regex = true) List<Regex> = emptyList(),

	/**
	 * The default log level applied to all loggers.
	 *
	 * **Environment variable name**: `LB_LOG_LEVEL`
	 *
	 * **Possible values** (see [Level])
	 *
	 * - `ERROR` ([Level.ERROR])
	 * - `WARN` ([Level.WARN])
	 * - `INFO` ([Level.INFO])
	 * - `DEBUG` ([Level.DEBUG])
	 * - `TRACE` ([Level.TRACE])
	 */
	@Env val logLevel: Level = Level.INFO,

	/**
	 * Log level overrides for specific loggers.
	 *
	 * **Environment variable name**: `LB_LOG_LEVELS`
	 *
	 * **Possible values** (see [Level])
	 *
	 * - `ERROR` ([Level.ERROR])
	 * - `WARN` ([Level.WARN])
	 * - `INFO` ([Level.INFO])
	 * - `DEBUG` ([Level.DEBUG])
	 * - `TRACE` ([Level.TRACE])
	 *
	 * **Example**
	 * ```
	 * org.apache.catalina.core.StandardEngine=WARN
	 * org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration=ERROR
	 * ```
	 */
	@Env val logLevels: @MapEnv(
		eq = "=",
		eqRegex = false,
		separator = "[\\s\\r\\n\\t]+",
		separatorRegex = true
	) Map<String, Level> = emptyMap(),

	/** Print exceptions thrown from themes. */
	@Env val layoutDebug: Boolean = false,
) {

	fun isStackTraceIgnored(element: StackTraceElement): Boolean =
		stacktraceExclude.any { it.containsMatchIn(element.repr) }

}
