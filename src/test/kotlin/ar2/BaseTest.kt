package ar2

import ar2.db.Users
import ar2.users.UsersService
import ch.qos.logback.classic.Level
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.get
import org.koin.test.KoinTest
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

open class BaseTest : KoinTest {
    val log = LoggerFactory.getLogger(javaClass)

    lateinit var app: App
    lateinit var storagePath: Path

    @Before
    open fun before() {
        startKoin { modules(modules) }
        app = App()
        app.setup(File("ar2.yaml"), logLevel = Level.TRACE)
        app.get<UsersService>().newUser(
                "testadmin",
                email = "admin@localhost",
                password = "test",
                name = "admin",
                admin = true
        )
        storagePath = Files.createTempDirectory("packages")
        app.config.storage.path = storagePath.toString()
    }

    @After
    open fun after() {
        transaction {
            Users.deleteWhere{ Users.username eq "testadmin" }
        }
        stopKoin()
        log.info("Removing {}", storagePath)
        Files.walk(storagePath)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach({file -> file.delete() });
    }

}