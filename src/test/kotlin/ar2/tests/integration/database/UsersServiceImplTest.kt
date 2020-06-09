package ar2.tests.integration.database

import ar2.db.entities.User
import ar2.exceptions.IllegalActionException
import ar2.exceptions.NoPermissionException
import ar2.exceptions.user.UserExistsException
import ar2.lib.api.RandomAdmin
import ar2.lib.api.RandomUser
import ar2.lib.api.RequiresUsers
import ar2.services.UsersService
import ar2.tests.e2e.randomUser
import ar2.web.PageRequest
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.get
import org.koin.test.KoinTest

@ExtendWith(DatabaseTestExt::class)
@RequiresUsers
class UsersServiceImplTest : KoinTest {

    lateinit var service: UsersService

    @BeforeEach
    fun before() {
        service = get()
    }

    @Test
    fun testNew(@RandomUser user: User) {}

    @Test
    fun testNewTwice(@RandomUser user: User) {
        assertThrows<UserExistsException> { service.new(user, password = "TEST2") }
    }

    @Test
    fun testListByUser(@RandomUser user: User) {
        assertThrows<NoPermissionException> { service.find(PageRequest(0, 1), user) }
    }

    @Test
    fun testListByAdmin(@RandomAdmin admin: User) {
        val users = service.find(PageRequest(offset = 0, limit = 10), admin)
        assertEquals(1, users.size)
    }

    @Test
    fun testDisableNewOrEnable(@RandomUser user: User) {
        service.disable(user, null)
        assertThrows<UserExistsException> { service.new(user, "TEST") }
        service.newOrEnable(user, "TEST")
    }

    @Test
    fun testFindByUsername(@RandomUser user: User) {
        service.find(user.username)
    }

    @Test
    fun testFindById(@RandomUser user: User) {
        service.find(user.id!!)
    }

    @Test
    fun testUpdate(@RandomUser user: User) {
        val id = user.id!!
        val form = randomUser()
        form.name = "ANOTHER TESTING USER"
        val newUser = service.update(id, form, "TEST2", user)
        assertEquals(user.username, newUser.username)
        assertEquals(user.isAdmin, newUser.isAdmin)
        assertEquals(user.lastLogin, newUser.lastLogin)
        assertNotEquals(user.name, newUser.name)
        assertNotEquals(user.passwordHash, newUser.passwordHash)
        assertNotEquals(user.email, newUser.email)
    }

    @Test
    fun testUpdateFromAdmin(@RandomUser user: User) {
        val id = user.id!!
        val form = randomUser()
        val issuer = randomUser()
        issuer.id = id + 10
        issuer.isAdmin = false
        assertThrows<IllegalActionException> { service.update(id, form, "TEST", issuer) }
        issuer.isAdmin = true
        service.update(id, form, "TEST", issuer)
    }

    @Test
    fun testDisable(@RandomUser user: User, @RandomAdmin issuer: User) {
        issuer.isAdmin = false
        assertThrows<NoPermissionException> { service.disable(user, issuer) }
        issuer.isAdmin = true
        service.disable(user, issuer)
        val userFromService = service.find(user.id!!)
        assertTrue(userFromService.disabled)
    }

    @Test
    fun testEnable(@RandomUser user: User, @RandomAdmin issuer: User) {
        service.disable(user, issuer)
        service.enable(user, issuer)
        val userFromService = service.find(user.id!!)
        assertTrue(!userFromService.disabled)
    }
}
