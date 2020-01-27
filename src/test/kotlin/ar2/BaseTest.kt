package ar2

import ar2.users.Users
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
import java.io.File

open class BaseTest : KoinTest {

    lateinit var app: App

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
    }

    @After
    open fun after() {
        transaction {
            Users.deleteWhere{ Users.username eq "testadmin" }
        }
        stopKoin()
    }

}