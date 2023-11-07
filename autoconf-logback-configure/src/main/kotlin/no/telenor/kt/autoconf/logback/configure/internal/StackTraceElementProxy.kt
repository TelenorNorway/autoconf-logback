package no.telenor.kt.autoconf.logback.configure.internal

import no.telenor.kt.autoconf.logback.configure.AutoconfLogbackConfiguration
import no.telenor.kt.autoconf.logback.theme.StackTraceElement

class StackTraceElementProxy(
	configuration: AutoconfLogbackConfiguration,
	override val classLoaderName: String?,
	override val clazz: String,
	override val method: String,
	override val native: Boolean,
	override val module: String?,
	override val version: String?,
	override val file: String?,
	override val line: Int?,
	override val alreadyShown: Boolean,
) : StackTraceElement {
	override val repr: String =
		"$clazz.$method${file?.let { "($it${line?.let { line -> ":$line" }})" }}${module?.let { " ~[$it${version?.let { version -> ":$version" }}]" }}"

	override val omitted: Boolean = configuration.isStackTraceIgnored(this)

	override fun toString() = repr
}
