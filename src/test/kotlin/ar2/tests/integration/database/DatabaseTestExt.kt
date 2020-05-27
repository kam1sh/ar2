package ar2.tests.integration.database

import ar2.App
import ar2.lib.util.cleanAll
import ar2.lib.util.getApp
import ar2.lib.util.loadTestConfig
import org.hibernate.SessionFactory
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.get
import org.slf4j.LoggerFactory

class DatabaseTestExt : BeforeAllCallback, BeforeEachCallback, AfterAllCallback, KoinComponent {
    private val log = LoggerFactory.getLogger(DatabaseTestExt::class.java)
    lateinit var app: App

    override fun beforeAll(context: ExtensionContext) {
        app = context.getApp()
        startKoin {}
        app.loadTestConfig()
        val factory = app.sessionFactory ?: app.connectToDatabase(showSql = true)
        app.getKoin().declare(factory)
        log.info("Database ready.")
    }

    override fun afterAll(context: ExtensionContext) {
        get<SessionFactory>().close()
        app.sessionFactory = null
        app.stopDI()
    }

    override fun beforeEach(context: ExtensionContext) {
        get<SessionFactory>().cleanAll()
    }
}
