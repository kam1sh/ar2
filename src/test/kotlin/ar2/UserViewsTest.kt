package ar2

import ar2.web.views.UserViews
import kotlin.test.assertEquals
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.junit.Test
import org.koin.test.get

class UserViewsTest : EndToEndTest() {
    @Test
    fun testLogin() {
        val reqDef = Request(Method.POST, "/login")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
        val request = get<UserViews>().authLens(UserViews.AuthRequest("testadmin", "test"), reqDef)
        val resp = handler(request)
        assertEquals(Status.OK, resp.status)
    }
}
