package ar2.lib

import ar2.App
import ch.qos.logback.classic.Level
import org.junit.jupiter.api.extension.ExtensionContext
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("ar2.lib.setup")
val appContextKey = "ar2app"

fun ExtensionContext.setupApp(): App {
    val app = root.getStore(ExtensionContext.Namespace.GLOBAL).getOrComputeIfAbsent(appContextKey) {
        doSetupApp()
    } as App
    root.getStore(ExtensionContext.Namespace.GLOBAL).getOrComputeIfAbsent("appResource") { AppResource(app) }
    return app
}

fun ExtensionContext.getApp(): App {
    return getStore(ExtensionContext.Namespace.GLOBAL).get(appContextKey, App::class.java)
}

private fun doSetupApp(): App {
    log.info("Launching application")
    val ar2App = App()
    ar2App.setupLogging(level = Level.TRACE)
    return ar2App
}
class AppResource(val app: App) : ExtensionContext.Store.CloseableResource {
    override fun close() {
        app.close()
    }
}
