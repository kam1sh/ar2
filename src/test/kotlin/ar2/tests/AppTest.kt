package ar2.tests

import ar2.Config
import ar2.lib.session.APIError
import ar2.lib.session.Credentials
import ar2.lib.session.Session
import ar2.lib.session.adminSession
import ar2.services.SecurityService
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.http4k.core.Method
import org.http4k.core.Status
import org.http4k.core.cookie.cookie
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject

@ExtendWith(EndToEndTest::class)
class AppTest : KoinTest {
    private val securityService: SecurityService by inject()

    @Test
    fun testStringGenerator() {
        val str = securityService.randomString(12)
        println(str)
        val match = "([a-z]|[A-Z]|[0-9]|_)+".toRegex().matches(str)
        assertTrue(match)
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
    fun testLogin() {
        var sess = Session(Credentials("", ""))
        val error = assertFailsWith<APIError>("Login with invalid credentials did not returned error!") {
            sess.login()
        }
        assertEquals(Status.BAD_REQUEST, error.resp.status)

        sess = adminSession()
        assertNotNull(sess.cookie)
    }
}
