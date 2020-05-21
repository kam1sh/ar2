package ar2.tests

import ar2.lib.session.APIError
import ar2.lib.session.adminSession
import ar2.services.GroupsService
import ar2.services.UsersService
import ar2.users.Role
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.http4k.core.Status
import org.koin.core.KoinComponent
import org.koin.core.get
import org.testng.annotations.Test

class GroupsTest : EndToEndTest() {
    @Test
    fun testCreateRemoveGroup() {
        val sess = adminSession()
        var resp = sess.groups.new("test")
        assertEquals(Status.CREATED, resp.status)
        resp = sess.groups.remove("test")
        assertEquals(Status.NO_CONTENT, resp.status)
    }

    @Test
    fun testGroupRole() {
        val sess = adminSession()
        val user = randomUser()
        withUser(user, "test123") {
            sess.groups.new("test")
            try {
                sess.groups.addRole("test", user.username, Role.DEVELOPER)
                null
            } finally {
                sess.groups.remove("test")
            }
        }
    }

    @Test
    fun testGroupRoleActions() {
        val sess = adminSession()
        val user = randomUser()
        var err = assertFailsWith<APIError> {
            sess.groups.addRole("test", user.username, Role.MAINTAINER)
        }
        assertEquals(Status.BAD_REQUEST, err.resp.status)
        withUser(user, "test123") {
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
