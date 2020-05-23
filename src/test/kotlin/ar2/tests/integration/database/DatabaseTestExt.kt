package ar2.tests.integration.database

import ar2.App
import ar2.lib.cleanAll
import ar2.services.SecurityService
import ar2.services.SecurityServiceImpl
import ch.qos.logback.classic.Level
import io.mockk.spyk
import java.io.File
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

class DatabaseTestExt : BeforeAllCallback, BeforeEachCallback, AfterAllCallback, KoinComponent {
    private val log = LoggerFactory.getLogger(DatabaseTestExt::class.java)

    override fun beforeAll(context: ExtensionContext?) {
        val app = App()
        app.setupLogging(Level.TRACE)
        startKoin { modules(module {
            single { spyk(SecurityServiceImpl() as SecurityService) }
        }) }
        app.loadConfig(File("ar2.yaml"))
        val factory = app.connectToDatabase(showSql = true)
        getKoin().declare(factory)
    }

    override fun beforeEach(context: ExtensionContext?) {
        get<SessionFactory>().cleanAll()
        log.info("Executing test {}", context?.testMethod?.get()?.name)
    }

    override fun afterAll(context: ExtensionContext?) {
        stopKoin()
    }
}
