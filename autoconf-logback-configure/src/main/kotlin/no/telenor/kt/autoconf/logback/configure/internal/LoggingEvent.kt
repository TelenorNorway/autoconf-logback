package no.telenor.kt.autoconf.logback.configure.internal

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxy
import no.telenor.kt.autoconf.logback.configure.AutoconfLogbackConfiguration
import no.telenor.kt.autoconf.logback.theme.Error
import no.telenor.kt.autoconf.logback.theme.LogLevel
import no.telenor.kt.autoconf.logback.theme.LogPayload
import no.telenor.kt.autoconf.logback.theme.StackTraceElement
import no.telenor.kt.panic
import java.time.Instant

data class LoggingEvent(
	override val at: Instant,
	override val level: LogLevel,
	override val sequence: Long,
	override val loggerName: String,
	override val caller: Array<StackTraceElement>?,
	override val thread: String?,
	override val mdc: Map<String, String?>,
	override val formattedMessage: String,
	override val message: String,
	override val arguments: Array<Any?>,
	override val error: Error?
) : LogPayload {
	// region equals & hashCode
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as LoggingEvent

		if (at != other.at) return false
		if (level != other.level) return false
		if (sequence != other.sequence) return false
		if (loggerName != other.loggerName) return false
		if (caller != null) {
			if (other.caller == null) return false
			if (!caller.contentEquals(other.caller)) return false
		} else if (other.caller != null) return false
		if (thread != other.thread) return false
		if (mdc != other.mdc) return false
		if (formattedMessage != other.formattedMessage) return false
		if (message != other.message) return false
		if (!arguments.contentEquals(other.arguments)) return false
		if (error != other.error) return false

		return true
	}

	override fun hashCode(): Int {
		var result = at.hashCode()
		result = 31 * result + level.hashCode()
		result = 31 * result + sequence.hashCode()
		result = 31 * result + loggerName.hashCode()
		result = 31 * result + (caller?.contentHashCode() ?: 0)
		result = 31 * result + (thread?.hashCode() ?: 0)
		result = 31 * result + mdc.hashCode()
		result = 31 * result + formattedMessage.hashCode()
		result = 31 * result + message.hashCode()
		result = 31 * result + arguments.contentHashCode()
		result = 31 * result + (error?.hashCode() ?: 0)
		return result
	}
	// endregion
}

internal fun transformLoggingEventToPayload(
	event: ILoggingEvent,
	configuration: AutoconfLogbackConfiguration
): LogPayload {
	return LoggingEvent(
		event.instant,
		when (event.level.levelInt) {
			Level.ERROR_INT -> LogLevel.Error
			Level.WARN_INT -> LogLevel.Warn
			Level.INFO_INT -> LogLevel.Info
			Level.DEBUG_INT -> LogLevel.Debug
			Level.TRACE_INT -> LogLevel.Trace
			Level.ALL_INT -> LogLevel.Trace
			Level.OFF_INT -> LogLevel.Error
			else -> panic("Invalid log level, this should never happen")
		},
		event.sequenceNumber,
		event.loggerName,
		event.callerData?.let {
			it.map { ste ->
				StackTraceElementProxy(
					configuration,
					ste.classLoaderName,
					ste.className,
					ste.methodName,
					ste.isNativeMethod,
					ste.moduleName,
					ste.moduleVersion,
					ste.fileName,
					ste.lineNumber,
					false
				)
			}
		}?.toTypedArray() ?: emptyArray(),
		event.threadName,
		event.mdcPropertyMap,
		// todo(James Bradlee): Use a custom formatted here.
		event.formattedMessage,
		event.message,
		event.argumentArray ?: emptyArray(),
		(event.throwableProxy as ThrowableProxy?)?.let { transformErrorProxy(it, configuration) }
	)
}
