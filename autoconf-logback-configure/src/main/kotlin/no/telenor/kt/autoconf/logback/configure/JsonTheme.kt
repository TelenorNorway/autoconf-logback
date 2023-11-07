package no.telenor.kt.autoconf.logback.configure

import no.telenor.kt.autoconf.logback.theme.LogPayload
import no.telenor.kt.autoconf.logback.theme.Theme

class JsonTheme : Theme {
	var TIMESTAMP_KEY = jsonString("timestamp")
	var LEVEL_KEY = jsonString("level")
	var LOGGER_KEY = jsonString("logger")
	var LOCATION_KEY = jsonString("location")
	var MODULE_KEY = jsonString("module")
	var CLASS_NAME_KEY = jsonString("class")
	var METHOD_NAME_KEY = jsonString("method")
	var MESSAGE_KEY = jsonString("message")
	var ERROR_KEY = jsonString("error")
	val MDC_KEY_OPEN = "\""
	var MDC_KEY_PREFIX = "mdc."
	var MDC_KEY_SUFFIX = ""
	val MDC_KEY_CLOSE = "\""

	override fun doLayout(payload: LogPayload): String {
		return "{$TIMESTAMP_KEY:${jsonString(payload.at.toString())},$LEVEL_KEY:\"${payload.level}\",$LOGGER_KEY:${
			jsonString(payload.loggerName)
		}${locationContent(payload)},$MESSAGE_KEY:${jsonString(payload.formattedMessage)}${errorContent(payload)}${
			mdcContent(payload)
		}}\r\n"
	}

	private fun locationContent(payload: LogPayload) = if ((payload.caller?.size ?: 0) > 0) {
		val callee = payload.caller!![0]
		(callee.file?.let { ",$LOCATION_KEY:${jsonString("$it${callee.line?.let { line -> ":$line" }}")}" } ?: "") +
			(callee.module?.let { ",$MODULE_KEY:${jsonString("$it${callee.version?.let { version -> "@$version" }}")}" }
				?: "") +
			",$CLASS_NAME_KEY:${jsonString(callee.clazz)}" +
			",$METHOD_NAME_KEY:${jsonString(callee.method)}"
	} else {
		""
	}

	private fun errorContent(payload: LogPayload) = payload.error?.let { ",$ERROR_KEY:${jsonString(it.repr)}" } ?: ""

	private fun mdcContent(payload: LogPayload): String {
		var output = ""
		for ((key, value) in payload.mdc) {
			value ?: continue
			output += ",$MDC_KEY_OPEN$MDC_KEY_PREFIX${encodeJsonString(key)}$MDC_KEY_SUFFIX$MDC_KEY_CLOSE:${jsonString(value)}"
		}
		return output
	}

	private fun jsonString(value: String): String {
		return "\"${encodeJsonString(value)}\""
	}

	private fun encodeJsonString(value: String): String {
		var output = ""
		for (char in value) {
			output += when (char) {
				'"' -> "\\\""
				'\\' -> "\\\\"
				'\b' -> "\\b"
				'\u000C' -> "\\f"
				'\n' -> "\\n"
				'\r' -> "\\r"
				'\t' -> "\\t"
				else -> if (char <= '\u001F' || (char in '\u007F'..'\u009F') || (char in '\u2000'..'\u20FF')) {
					char.code.toString(16).padStart(4, '0')
				} else {
					char
				}
			}
		}
		return output
	}
}
