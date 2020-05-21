package ar2.tests

import ar2.Config
import ar2.lib.session.APIError
import ar2.lib.session.Session
import ar2.services.SecurityService
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.http4k.core.Method
import org.http4k.core.Status
import org.http4k.core.cookie.cookie
import org.koin.test.get
import org.koin.test.inject
import org.testng.annotations.Test

class AppTest : EndToEndTest() {
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
}
