package ar2.tests.e2e

import EndToEndTest
import ar2.db.entities.User
import ar2.lib.session.APIError
import ar2.lib.session.Credentials
import ar2.lib.session.Session
import ar2.lib.session.adminSession
import ar2.services.SecurityService
import ar2.services.UsersService
import kotlin.test.*
import org.http4k.core.Method
import org.http4k.core.Status
import org.junit.jupiter.api.Test
import org.koin.core.get
import org.koin.test.KoinTest

@EndToEndTest
class UserViewsTest : KoinTest {

    @Test
    fun testCurrentWithNoSession() {
        val sess = Session()
        assertFailsWith<APIError> {
            sess.users.current()
        }
    }

    @Test
    fun testCurrent() {
        val sess = adminSession()
        val user = sess.users.current()
        assertEquals("testadmin", user.username)
        assertNull(user.passwordHash)
        assertNotNull(user.createdOn)
        assertNotNull(user.lastLogin)
    }

    @Test
    fun testList() {
        val sess = adminSession()
        val usersList = sess.users.list()
        assertTrue(usersList.count() > 0, "There is no users in list, expected more than zero")
    }

    @Test
    fun testIterator() {
        val sess = adminSession()
        val users = sess.users.iter()
        assertTrue(users.asSequence().count() > 0)
    }

    @Test
    fun testFindNotExistingUser() {
        val sess = adminSession()
        var error = assertFailsWith<APIError> { sess.users.find("notexisting") }
        assertEquals(Status.NOT_FOUND, error.resp.status)
        error = assertFailsWith<APIError> { sess.users.find(-1) }
        assertEquals(Status.NOT_FOUND, error.resp.status)
    }

    @Test
    fun testCreateDisableUser() {
        val sess = adminSession()
        val user = randomUser()
        var resp = sess.users.new(user, "test123")
        assertEquals(Status.CREATED, resp.status)
        resp = sess.request(Method.POST, "/api/v1/users/username/${user.username}/disable")
        assertEquals(Status.NO_CONTENT, resp.status)
    }

    @Test
    fun testNewUser() {
        val sess = adminSession()
        withUser(null, "test123") {
            assertFailsWith<APIError> {
                sess.users.new(it, "test456")
            }
            val found = sess.users.find(it.username)
            assertNull(found.lastLogin)
            val userSession = Session(Credentials(it.username, "test123"))
            userSession.login()
            assertEquals(userSession.users.current().id, found.id)
        }
    }
}

fun KoinTest.randomUser(): User {
    val securityService = get<SecurityService>()
    val username = "test_" + securityService.randomString(20)
    return User(
        username = username,
        email = "TEST@test.test",
        name = "TESTING USER",
        isAdmin = false
    )
}

fun <T> KoinTest.withUser(user: User?, password: String, callable: (User) -> T): T {
    val userOrRand = user ?: randomUser()
    val service = get<UsersService>()
    service.new(userOrRand.copy(), password)
    return try {
        callable(userOrRand)
    } finally {
        val usr = service.find(userOrRand.username)
        service.disable(usr, null)
    }
}
