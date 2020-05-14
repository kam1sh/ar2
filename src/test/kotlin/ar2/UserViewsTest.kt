package ar2

import ar2.db.entities.User
import ar2.lib.session.APIError
import ar2.lib.session.Credentials
import ar2.lib.session.Session
import ar2.services.UsersService
import kotlin.test.*
import org.http4k.core.Method
import org.http4k.core.Status
import org.http4k.core.cookie.cookie
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.test.get
import org.testng.annotations.Test

class UserViewsTest : EndToEndTest() {

    @Test
    fun testCurrentWithNoSession() {
        val sess = Session()
        assertFailsWith<APIError> {
            sess.users.current()
        }
    }

    @Test
    fun testLogin() {
        var sess = Session(Credentials("", ""))
        assertFailsWith<APIError>("Login with invalid credentials did not returned error!") {
            sess.login()
        }

        sess = adminSession()
        assertNotNull(sess.cookie)
    }

    @Test
    fun testInvalidCookie() {
        val sess = Session()
        val request = sess.prepareRequest(Method.GET, "/api/v1/users/current")
            .cookie(get<Config>().security.cookieName, "123")
        val error = assertFailsWith<APIError> {
            sess.request(request)
        }
        assertEquals(Status.UNAUTHORIZED, error.resp.status)
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

    val testUser = User(
        username = "test",
        email = "test@test",
        name = "testuser",
        isAdmin = false
    )

    @Test
    fun testCreateDeleteUser() {
        val sess = adminSession()
        var resp = sess.users.new(testUser, "test123")
        assertEquals(Status.CREATED, resp.status)
        resp = sess.request(Method.DELETE, "/api/v1/users/username/${testUser.username}")
        assertEquals(Status.NO_CONTENT, resp.status)
    }

    @Test
    fun testNewUser() {
        val sess = adminSession()
        withUser(testUser, "test123") {
            assertFailsWith<APIError> {
                sess.users.new(testUser, "test456")
            }
            val user = sess.users.find("test")
            assertNull(user.lastLogin)
            val userSession = Session(Credentials(user.username, "test123"))
            userSession.login()
            assertEquals(userSession.users.current().id, user.id)
        }
    }
}

fun <T> KoinComponent.withUser(user: User, password: String, callable: () -> T): T {
    val service = get<UsersService>()
    service.new(user, password)
    return try {
        callable()
    } finally {
        service.remove(user.username)
    }
}
