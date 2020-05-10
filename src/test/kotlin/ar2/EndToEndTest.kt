package ar2

import ar2.db.User
import ar2.services.UsersService
import ar2.web.WebHandler
import ch.qos.logback.classic.Level
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import org.hibernate.SessionFactory
import org.http4k.core.HttpHandler
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.get
import org.koin.test.KoinTest
import org.slf4j.LoggerFactory

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class EndToEndTest : KoinTest {

    val log = LoggerFactory.getLogger(EndToEndTest::class.java)

    lateinit var app: App
    lateinit var storagePath: Path
    lateinit var handler: HttpHandler

    @BeforeAll
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
        app.get<UsersService>().newUser(
            request = user,
            password = "test"
        )
        storagePath = Files.createTempDirectory("packages")
        app.config.storage.path = storagePath.toString()
        handler = WebHandler(app).toHttpHandler()
        app.getKoin().declare(handler)
    }

    @AfterAll
    fun after() {
        app.getKoin().get<SessionFactory>().openSession().use {
            val tr = it.beginTransaction()
            it.createQuery("delete from User where username = :username")
                .setParameter("username", "testadmin")
                .executeUpdate()
            tr.commit()
        }
        stopKoin()
        log.info("Removing {}", storagePath)
        Files.walk(storagePath)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach { file -> file.delete() }
    }
}
