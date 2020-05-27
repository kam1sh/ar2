package ar2.tests.e2e

import EndToEndTest
import ar2.db.entities.User
import ar2.lib.api.AdminSession
import ar2.lib.api.RandomUser
import ar2.lib.session.APIError
import ar2.lib.session.Session
import ar2.lib.session.adminSession
import ar2.services.GroupsService
import ar2.services.UsersService
import ar2.users.Role
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.http4k.core.Status
import org.junit.jupiter.api.Test
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.test.KoinTest

@EndToEndTest
class GroupsTest : KoinTest {
    @Test
    fun testCreateRemoveGroup() {
        val sess = adminSession()
        var resp = sess.groups.new("test")
        assertEquals(Status.CREATED, resp.status)
        resp = sess.groups.remove("test")
        assertEquals(Status.NO_CONTENT, resp.status)
    }

    @Test
    fun testGroupRole(@AdminSession sess: Session, @RandomUser user: User) {
        sess.groups.new("test")
        sess.groups.addRole("test", user.username, Role.DEVELOPER)
        sess.groups.remove("test")
    }

    @Test
    fun testGroupRoleActions(@AdminSession sess: Session, @RandomUser user: User) {
        var err = assertFailsWith<APIError> {
            sess.groups.addRole("test", user.username, Role.MAINTAINER)
        }
        assertEquals(Status.BAD_REQUEST, err.resp.status)
        withGroup("test", user.username) {
            sess.groups.addRole("test", user.username, Role.MAINTAINER)
            // add same group twice
            err = assertFailsWith<APIError> {
                sess.groups.addRole("test", user.username, Role.MAINTAINER)
            }
            assertEquals(Status.CONFLICT, err.resp.status)
        }
    }
}

fun <T> KoinComponent.withGroup(name: String, ownerUsername: String, supplier: () -> T): T {
    val service = get<GroupsService>()
    val owner = get<UsersService>().find(ownerUsername)
    service.new(name, owner)
    return try {
        supplier()
    } finally {
        service.remove(name)
    }
}
