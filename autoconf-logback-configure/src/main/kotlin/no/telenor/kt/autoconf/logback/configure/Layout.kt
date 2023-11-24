package no.telenor.kt.autoconf.logback.configure

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.LayoutBase
import no.telenor.kt.autoconf.logback.configure.internal.transformLoggingEventToPayload
import no.telenor.kt.autoconf.logback.theme.Theme

class Layout(
	private val theme: Theme,
	private val configuration: AutoconfLogbackConfiguration
) : LayoutBase<ILoggingEvent>() {
	override fun doLayout(event: ILoggingEvent) = synchronized(this) {
		try {
			theme.doLayout(transformLoggingEventToPayload(event, configuration))
		} catch (ex: Throwable) {
			if (configuration.layoutDebug) ex.printStackTrace()
			throw ex
		}
	}
}
