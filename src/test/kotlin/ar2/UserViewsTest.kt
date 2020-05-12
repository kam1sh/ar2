package ar2

import ar2.db.entities.User
import ar2.lib.session.APIError
import ar2.lib.session.Credentials
import ar2.lib.session.Session
import ar2.services.UsersService
import ar2.web.views.UserViews
import kotlin.test.*
import org.http4k.core.Method
import org.http4k.core.Status
import org.koin.test.get
import org.testng.annotations.Test
import java.lang.AssertionError
import java.lang.Exception

class UserViewsTest : EndToEndTest() {

    @Test
    fun testCurrentWithNoSession() {
        val sess = Session()
        assertFailsWith<APIError> {
            sess.request(Method.GET, "/users/current")
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
    fun testCurrent() {
        val sess = adminSession()
        val user = sess.users.current()
        assertEquals("testadmin", user.username)
        assertNull(user.passwordHash)
        assertNotNull(user.createdOn)
        assertNotNull(user.lastLogin)
    }

    @Test
    fun testUserList() {
        val sess = adminSession()
        val usersList = sess.users.list()
        assertTrue(usersList.count() > 0, "There is no users in list, expected more than zero")
    }

    @Test
    fun testCreateDeleteUser() {
        val sess = adminSession()
        val user = User(
            username = "test",
            email = "test@test",
            name = "testuser",
            isAdmin = false
        )
        var resp = sess.request(Method.POST, "/users", UserViews.NewUserRequest(
            user, "test123"
        ))
        assertEquals(Status.CREATED, resp.status)
        resp = sess.request(Method.DELETE, "/users/username/${user.username}")
        assertEquals(Status.NO_CONTENT, resp.status)
    }

    @Test
    fun testNewUser() {
        val sess = adminSession()
        var user = User(
            username = "test",
            email = "test@test",
            name = "testuser",
            isAdmin = false
        )
        get<UsersService>().new(user, "test123")
        try {
            assertFailsWith<APIError> {
                sess.request(Method.POST, "/users", UserViews.NewUserRequest(
                    user, "test456"
                ))
            }
            user = sess.users.find("test") ?: throw AssertionError("Created user cannot be found")
            assertNotNull(user)
            assertNull(user.lastLogin)
            val userSession = Session(Credentials(user.username, "test123"))
            userSession.login()
            assertEquals(userSession.users.current().id, user.id)
        } finally {
            get<UsersService>().remove(user.username)
        }
    }

}
