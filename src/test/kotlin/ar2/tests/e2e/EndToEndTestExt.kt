package ar2.tests.e2e

import ar2.App
import ar2.db.entities.User
import ar2.lib.cleanAll
import ar2.services.UsersService
import ch.qos.logback.classic.Level
import java.nio.file.Files
import java.nio.file.Path
import org.hibernate.SessionFactory
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.KoinComponent
import org.koin.core.get
import org.slf4j.LoggerFactory

class EndToEndTestExt : KoinComponent, BeforeAllCallback, BeforeEachCallback {

    val log = LoggerFactory.getLogger(EndToEndTestExt::class.java)

    val adminUser = User(
        username = "testadmin",
        email = "admin@localhost",
        name = "admin",
        isAdmin = true
    )

    override fun beforeAll(context: ExtensionContext) {
        context.getStore(ExtensionContext.Namespace.GLOBAL).getOrComputeIfAbsent("app") {
            CloseableApp()
        }
    }

    override fun beforeEach(context: ExtensionContext) {
        get<SessionFactory>().cleanAll()
        get<UsersService>().new(
            form = adminUser,
            password = "test"
        )
    }

    class CloseableApp : ExtensionContext.Store.CloseableResource {
        val log = LoggerFactory.getLogger(CloseableApp::class.java)
        val storagePath: Path
        val app = App()

        init {
            app.setup(null, logLevel = Level.TRACE)
            storagePath = Files.createTempDirectory("packages")
            app.config.storage.path = storagePath.toString()
            log.info("Application ready.")
        }

        override fun close() {
            app.close()
            log.info("Removing {}", storagePath)
            Files.walk(storagePath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach { file -> file.delete() }
        }
    }
}
