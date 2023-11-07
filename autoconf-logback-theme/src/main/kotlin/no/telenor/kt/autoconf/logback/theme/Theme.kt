package no.telenor.kt.autoconf.logback.theme

import java.time.Instant

enum class LogLevel {
	Trace,
	Debug,
	Info,
	Warn,
	Error;

	override fun toString(): String = when (this) {
		Trace -> "TRACE"
		Debug -> "DEBUG"
		Info -> "INFO"
		Warn -> "WARN"
		Error -> "ERROR"
	}
}

interface StackTraceElement {
	val classLoaderName: String?
	val clazz: String
	val method: String
	val native: Boolean
	val module: String?
	val version: String?
	val file: String?
	val line: Int?
	val omitted: Boolean
	val alreadyShown: Boolean

	val repr: String
}

interface Error {
	val className: String
	val message: String?
	val stack: Array<StackTraceElement>
	val commonFrames: Int
	val cause: Error?
	val cyclic: Boolean

	val repr: String
}

interface LogPayload {
	val at: Instant
	val level: LogLevel
	val sequence: Long
	val loggerName: String
	val caller: Array<StackTraceElement>?
	val thread: String?
	val mdc: Map<String, String?>
	val formattedMessage: String
	val message: String
	val arguments: Array<Any?>
	val error: Error?
}

interface Theme {
	fun doLayout(payload: LogPayload): String
}
