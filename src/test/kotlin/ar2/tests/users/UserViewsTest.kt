package ar2.tests.users

import ar2.App
import ar2.db.entities.User
import ar2.exceptions.UnauthorizedException
import ar2.exceptions.WebError
import ar2.facades.UsersFacade
import ar2.lib.session.Session
import ar2.lib.session.deserialize
import ar2.services.SecurityService
import ar2.services.SessionsService
import ar2.services.SessionsServiceImpl
import ar2.web.PageRequest
import ar2.web.WebHandler
import ar2.web.views.AuthenticationViews
import ar2.web.views.UserViews
import ch.qos.logback.classic.Level
import com.fasterxml.jackson.core.type.TypeReference
import io.mockk.*
import java.io.File
import kotlin.test.assertEquals
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.routing.RoutingHttpHandler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.koin.core.context.*
import org.koin.core.get
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertFailsWith

class UserViewsTest : KoinTest {

    @Suppress("USELESS_CAST")
    @BeforeEach
    fun before() {
        val facade = mockk<UsersFacade>()
        val module = module {
            single { facade }
            single { UserViews(facade) }
            single { mockk<SecurityService>() }
            single { mockk<SessionsService>() }
        }
        startKoin { modules(module) }
        val app = App()
        app.setupLogging(Level.TRACE)
        app.loadConfig(File("ar2.yaml"))
        val securityService = get<SecurityService>()
        every { securityService.randomString(any()) } returns "TESTING"
    }

    private fun mockViews(setup: (UsersFacade) -> Unit): RoutingHttpHandler {
        val facade = get<UsersFacade>()
        setup(facade)
        return get<UserViews>().views()
    }

    @Test
    fun testList() {
        val pr = PageRequest(offset = 0, limit = 10)
        var user: User
        var userList: List<User>? = null
        val views = mockViews {
            user = randomUser()
            userList = listOf(user)
            every { it.find(pr) } returns userList!!
        }
        val request = Session().prepareRequest(Method.GET, "/")
            .query("offset", "0")
            .query("limit", "10")
        val resp = views(request)
        assertEquals(Status.OK, resp.status)
        assertEquals(userList!!, resp.deserialize(object : TypeReference<List<User>>() {}))
        val facade = get<UsersFacade>()
        verify { facade.find(pr) }
        confirmVerified(facade)
    }

    @Test
    fun testNew() {
        val issuer = randomUser()
        val user = randomUser()
        val views = mockViews {
            every { it.new(form = user, password = "123", issuer = issuer) } returns user
        }
        val request = Session().prepareRequest(Method.POST, "/", UserViews.NewUserRequest(user, "123"))
        val sessionsService = get<SessionsService>()
        every { sessionsService.findUser(any() as Request) } returns issuer
        val response = views(request)
        assertEquals(Status.CREATED, response.status)
    }

    @Test
    fun testCurrentWithNoSession() {
        val handler = mockViews {}
        every { get<SessionsService>().findUser(any() as Request) } throws UnauthorizedException()
        assertFailsWith<UnauthorizedException> { handler(Request(Method.GET, "/current")) }
    }

    @AfterEach
    fun after() {
        stopKoin()
    }
}
