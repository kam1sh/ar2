package ar2

import ar2.db.entities.User
import ar2.facades.UsersFacade
import ar2.services.UsersService
import ar2.web.WebHandler
import ch.qos.logback.classic.Level
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import org.http4k.core.HttpHandler
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.get
import org.koin.test.KoinTest
import org.slf4j.LoggerFactory
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeSuite

open class EndToEndTest : KoinTest {

    val log = LoggerFactory.getLogger(EndToEndTest::class.java)

    lateinit var app: App
    lateinit var storagePath: Path
    lateinit var handler: HttpHandler

    @BeforeSuite
    fun before() {
        startKoin { modules(modules) }
        app = App()
        app.setup(File("ar2.yaml"), logLevel = Level.TRACE)
        val user = User(
            username = "testadmin",
            email = "admin@localhost",
            name = "admin",
            isAdmin = true
        )
        get<UsersService>().new(
            request = user,
            password = "test"
        )
        storagePath = Files.createTempDirectory("packages")
        app.config.storage.path = storagePath.toString()
        handler = WebHandler().toHttpHandler()
        app.getKoin().declare(handler)
    }

    @AfterSuite
    fun after() {
        get<UsersFacade>().disable("testadmin")
        stopKoin()
        log.info("Removing {}", storagePath)
        Files.walk(storagePath)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach { file -> file.delete() }
    }
}
