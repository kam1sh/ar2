package ar2.lib.ext

import ar2.App
import ar2.db.entities.User
import ar2.lib.util.cleanAll
import ar2.lib.util.getApp
import ar2.lib.util.loadTestConfig
import ar2.services.UsersService
import java.nio.file.Files
import java.nio.file.Path
import org.hibernate.SessionFactory
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.KoinComponent
import org.koin.core.get
import org.slf4j.LoggerFactory

class EndToEndTestExt : KoinComponent, BeforeAllCallback, AfterAllCallback, BeforeEachCallback {

    val log = LoggerFactory.getLogger(EndToEndTestExt::class.java)

    val adminUser = User(
        username = "testadmin",
        email = "admin@localhost",
        name = "admin",
        isAdmin = true
    )

    override fun beforeAll(context: ExtensionContext) {
        val app = context.getApp()
        log.info("Launching application.")
        app.startDI()
        context.getStore(ExtensionContext.Namespace.GLOBAL).getOrComputeIfAbsent("app") {
            CloseableApp(app)
        }
    }

    override fun afterAll(context: ExtensionContext) {
        val app = context.getApp()
        app.stopDI()
    }

    override fun beforeEach(context: ExtensionContext) {
        get<SessionFactory>().cleanAll()
        get<UsersService>().new(
            form = adminUser,
            password = "test"
        )
    }

    class CloseableApp(val app: App) : ExtensionContext.Store.CloseableResource {
        val log = LoggerFactory.getLogger(CloseableApp::class.java)
        val storagePath: Path

        init {
            app.loadTestConfig()
            app.sessionFactory = app.sessionFactory ?: app.connectToDatabase(showSql = true)
            app.getKoin().declare(app.sessionFactory!!)
            storagePath = Files.createTempDirectory("packages")
            app.config.storage.path = storagePath.toString()
            log.info("Application ready.")
        }

        override fun close() {
            app.stopDI()
            log.info("Removing {}", storagePath)
            Files.walk(storagePath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach { file -> file.delete() }
        }
    }
}
