package no.telenor.kt.internal.autoconf.logback.configuration.standalone

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.core.spi.ContextAwareBase
import no.telenor.kt.autoconf.logback.configure.Configure

internal class Initializer : ContextAwareBase(), Configurator {
	override fun configure(ctx: LoggerContext): Configurator.ExecutionStatus {
		Configure.configure(ctx)
		return Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY
	}
}
