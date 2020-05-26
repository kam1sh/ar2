package ar2.tests.integration.database

import ar2.App
import ar2.lib.cleanAll
import ch.qos.logback.classic.Level
import org.hibernate.SessionFactory
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.get
import org.slf4j.LoggerFactory

class DatabaseTestExt : BeforeAllCallback, BeforeEachCallback, KoinComponent {
    private val log = LoggerFactory.getLogger(DatabaseTestExt::class.java)

    override fun beforeAll(context: ExtensionContext) {
        context.getStore(ExtensionContext.Namespace.GLOBAL).getOrComputeIfAbsent("db") {
            CloseableDatabase()
        }
    }

    class CloseableDatabase : ExtensionContext.Store.CloseableResource {
        val app: App
        private val log = LoggerFactory.getLogger(CloseableDatabase::class.java)
        init {
            startKoin {}
            app = App()
            app.setupLogging(Level.TRACE)
            app.loadConfig(null)
            val factory = app.connectToDatabase(showSql = true)
            app.getKoin().declare(factory)
            log.info("Database ready.")
        }

        override fun close() {
            app.close()
        }
    }

    override fun beforeEach(context: ExtensionContext) {
        get<SessionFactory>().cleanAll()
        log.info("Executing test {}", context.testMethod?.get()?.name)
    }
}
