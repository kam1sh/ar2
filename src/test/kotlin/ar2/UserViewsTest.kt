package ar2

import ar2.web.views.UserViews
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertEquals

class UserViewsTest : BaseTest() {
    @Test
    fun testLogin() {
        val reqDef = Request(Method.POST, "/login")
        val request = get<UserViews>().authLens(UserViews.AuthRequest("testadmin", "test"), reqDef)
        val resp = app.getWebHandler()(request)
        assertEquals(Status.OK, resp.status)
    }
}