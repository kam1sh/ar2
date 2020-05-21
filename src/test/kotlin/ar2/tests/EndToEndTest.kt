package ar2.tests

import ar2.App
import ar2.db.entities.User
import ar2.facades.UsersFacade
import ar2.services.UsersService
import ch.qos.logback.classic.Level
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.get
import org.koin.test.KoinTest
import org.slf4j.LoggerFactory

class EndToEndTest : KoinComponent, BeforeAllCallback, AfterAllCallback {

    val log = LoggerFactory.getLogger(EndToEndTest::class.java)

    lateinit var app: App
    lateinit var storagePath: Path

    override fun beforeAll(context: ExtensionContext?) {
        startKoin { modules(ar2.modules) }
        app = App()
        app.setup(File("ar2.yaml"), logLevel = Level.TRACE)
        val user = User(
            username = "testadmin",
            email = "admin@localhost",
            name = "admin",
            isAdmin = true
        )
        get<UsersService>().newOrEnable(
            request = user,
            password = "test"
        )
        storagePath = Files.createTempDirectory("packages")
        app.config.storage.path = storagePath.toString()
    }

    override fun afterAll(context: ExtensionContext?) {
        get<UsersFacade>().disable("testadmin")
        stopKoin()
        log.info("Removing {}", storagePath)
        Files.walk(storagePath)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach { file -> file.delete() }
    }
}
