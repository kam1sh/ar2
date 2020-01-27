package ar2

import org.http4k.core.Method
import org.http4k.core.Request
import org.junit.Test

class UserViewsTest : BaseTest() {

    @Test
    fun testLogin() {
        val resp = app.getWebHandler()(Request(Method.GET, "/login"))
    }
}