package ar2

import ar2.db.Users
import ar2.lib.session.Credentials
import ar2.lib.session.Session
import ar2.services.UsersService
import ar2.users.BaseUser
import ar2.web.WebHandler
import ch.qos.logback.classic.Level
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import org.http4k.core.HttpHandler
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.get
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import org.slf4j.LoggerFactory

open class EndToEndTest : KoinTest {
    val log = LoggerFactory.getLogger(javaClass)

    lateinit var app: App
    lateinit var storagePath: Path
    lateinit var handler: HttpHandler

    @Before
    open fun before() {
        startKoin { modules(modules) }
        app = App()
        app.setup(File("ar2.yaml"), logLevel = Level.TRACE)
        val user = BaseUser(
            "testadmin",
            email = "admin@localhost",
            name = "admin",
            admin = true
        )
        app.get<UsersService>().newUser(
                request = user,
                password = "test"
        )
        storagePath = Files.createTempDirectory("packages")
        app.config.storage.path = storagePath.toString()
        handler = WebHandler(app).toHttpHandler()
        getKoin().declare(handler)
    }

    @After
    open fun after() {
        transaction {
            Users.deleteWhere { Users.username eq "testadmin" }
        }
        stopKoin()
        log.info("Removing {}", storagePath)
        Files.walk(storagePath)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach { file -> file.delete() }
    }

    fun adminSession(): Session {
        val sess = Session(Credentials("testadmin", "test"))
        sess.login()
        return sess
    }
}
