package no.telenor.kt.autoconf.logback.configure.internal

import ch.qos.logback.classic.spi.IThrowableProxy
import no.telenor.kt.autoconf.logback.configure.AutoconfLogbackConfiguration
import no.telenor.kt.autoconf.logback.theme.Error
import no.telenor.kt.autoconf.logback.theme.StackTraceElement

class ErrorProxy(
	override val className: String,
	override val message: String?,
	override val stack: Array<StackTraceElement>,
	override val commonFrames: Int,
	override val cyclic: Boolean,
) : Error {
	override fun hashCode(): Int {
		var result = cause?.hashCode() ?: 0
		result = 31 * result + className.hashCode()
		result = 31 * result + commonFrames
		result = 31 * result + (message?.hashCode() ?: 0)
		result = 31 * result + stack.contentHashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as ErrorProxy

		if (className != other.className) return false
		if (message != other.message) return false
		if (!stack.contentEquals(other.stack)) return false
		if (commonFrames != other.commonFrames) return false
		if (cause != other.cause) return false
		if (cyclic != other.cyclic) return false
		if (repr != other.repr) return false

		return true
	}

	internal var _cause: Error? = null
	override val cause: Error? get() = _cause

	override val repr: String
		get() {
			var representation = "$className${message?.let { ": $it" }}"

			var omitted = 0
			var hidden = 0

			for (index in 0..<stack.size - commonFrames) {
				val el = stack[index]

				if (el.omitted) {
					omitted++
					continue
				}

				if (el.alreadyShown) {
					hidden++
					continue
				}

				var preline: String? = null
				if (omitted > 0) preline = "$omitted omitted"
				if (hidden > 0) preline = preline?.let { "$it, $hidden more" } ?: "$hidden more"
				if (preline != null) representation += "\r\n  ... $preline"

				representation += "\r\n  at ${el.repr}"
			}

			if (_cause != null) representation += "\r\nCaused by: ${_cause!!.repr}"
			return representation
		}
}

internal fun transformErrorProxy(
	throwable: IThrowableProxy,
	configuration: AutoconfLogbackConfiguration,
	seen: MutableMap<IThrowableProxy, ErrorProxy> = mutableMapOf()
): Error {
	val stackTraceElements: MutableList<StackTraceElement> = mutableListOf()

	for (index in throwable.stackTraceElementProxyArray.indices) {
		val ste = throwable.stackTraceElementProxyArray[index].stackTraceElement
		stackTraceElements.add(
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
				index >= throwable.stackTraceElementProxyArray.size - throwable.commonFrames
			)
		)
	}

	val proxy = ErrorProxy(
		throwable.className,
		throwable.message,
		stackTraceElements.toTypedArray(),
		throwable.commonFrames,
		throwable.isCyclic,
	)
	seen[throwable] = proxy
	if (throwable.cause != null) {
		proxy._cause = seen[throwable.cause] ?: transformErrorProxy(throwable.cause, configuration, seen)
	}
	return proxy
}
