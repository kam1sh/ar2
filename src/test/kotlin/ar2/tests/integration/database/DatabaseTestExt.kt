package ar2.tests.integration.database

import ar2.App
import ar2.lib.cleanAll
import ar2.services.SecurityService
import ar2.services.SecurityServiceImpl
import ch.qos.logback.classic.Level
import io.mockk.spyk
import org.hibernate.SessionFactory
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.get
import org.koin.dsl.module
import org.slf4j.LoggerFactory

class DatabaseTestExt : BeforeAllCallback, BeforeEachCallback, KoinComponent {
    private val log = LoggerFactory.getLogger(DatabaseTestExt::class.java)

    override fun beforeAll(context: ExtensionContext?) {
        context!!.getStore(ExtensionContext.Namespace.GLOBAL).put("db", CloseableDatabase())
    }

    inner class CloseableDatabase : ExtensionContext.Store.CloseableResource {
        val app: App

        init {
            startKoin {}
            app = App()
            app.setupLogging(Level.TRACE)
            app.loadConfig(null)
            val factory = app.connectToDatabase(showSql = true)
            getKoin().declare(factory)
        }

        override fun close() {
            stopKoin()
        }
    }

    override fun beforeEach(context: ExtensionContext?) {
        get<SessionFactory>().cleanAll()
        log.info("Executing test {}", context?.testMethod?.get()?.name)
    }

}
