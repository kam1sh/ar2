package ar2

import ar2.db.User
import ar2.lib.session.APIError
import ar2.lib.session.Credentials
import ar2.lib.session.Session
import ar2.lib.session.deserialize
import ar2.web.views.UserViews
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.http4k.core.Method
import org.http4k.core.Status
import org.junit.jupiter.api.Test

class UserViewsTest : EndToEndTest() {
    @Test
    fun testLogin() {
        var sess = Session(Credentials("", ""))
        assertFailsWith<APIError>("Login with invalid credentials did not returned error!") {
            sess.login()
        }

        sess = adminSession()
        assertNotNull(sess.cookie)
        val resp = sess.request(Method.GET, "/users/_current", null).deserialize(User::class.java)
        assertEquals("testadmin", resp.username)
    }

    @Test
    fun testUserList() {
        val sess = adminSession()
        val usersList = sess.users().list()
        assertTrue(usersList.count() > 0, "There is no users in list, expected more than zero")
    }

    @Test
    fun testCreateDeleteUser() {
        val sess = adminSession()
        var resp = sess.request(Method.POST, "/users", UserViews.NewUserRequest(
            User(username = "test", email = "test@test", name = "testuser", isAdmin = false), "test123"
        ))
        assertEquals(Status.CREATED, resp.status)
        val user = sess.users().find("test")
        assertNotNull(user)
        resp = sess.request(Method.DELETE, "/users/${user.id}")
        assertEquals(Status.NO_CONTENT, resp.status)
    }
}
