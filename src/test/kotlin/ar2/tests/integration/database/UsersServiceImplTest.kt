package ar2.tests.integration.database

import ar2.exceptions.IllegalActionException
import ar2.exceptions.NoPermissionException
import ar2.exceptions.user.UserExistsException
import ar2.services.SecurityService
import ar2.services.UsersService
import ar2.services.impl.SecurityServiceImpl
import ar2.services.impl.UsersServiceImpl
import ar2.tests.e2e.randomUser
import ar2.web.PageRequest
import io.mockk.every
import io.mockk.spyk
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.get
import org.koin.dsl.module
import org.koin.test.KoinTest

val module = module {
    single { spyk(SecurityServiceImpl()) as SecurityService }
}
@ExtendWith(DatabaseTestExt::class)
class UsersServiceImplTest : KoinTest {

    lateinit var service: UsersService

    @BeforeEach
    fun before() {
        loadKoinModules(module)
        service = UsersServiceImpl(get())
        every { get<SecurityService>().encode("TEST") } returns "TEST"
    }

    @AfterEach
    fun after() {
        unloadKoinModules(module)
    }

    @Test
    fun testNewDisable() {
        val user = randomUser()
        service.new(user, password = "TEST")
    }

    @Test
    fun testNewTwice() {
        val user = randomUser()
        service.new(user, password = "TEST")
        assertThrows<UserExistsException> { service.new(user, password = "TEST2") }
    }

    @Test
    fun testList() {
        service.new(randomUser(), password = "TEST")
        val users = service.find(PageRequest(offset = 0, limit = 10))
        assertEquals(1, users.size)
    }

    @Test
    fun testNewOrEnable() {
        var user = randomUser()
        user = service.new(user, "TEST")
        service.disable(user, null)
        assertThrows<UserExistsException> { service.new(user, "TEST") }
        service.newOrEnable(user, "TEST")
    }

    @Test
    fun testFindByUsername() {
        var user = randomUser()
        user = service.new(user, "TEST")
        service.find(user.username)
    }

    @Test
    fun testFindById() {
        val user = randomUser()
        val id = service.new(user, "TEST").id!!
        service.find(id)
    }

    @Test
    fun testUpdate() {
        val user = randomUser()
        val id = service.new(user, "TEST").id!!
        val form = randomUser()
        form.name = "ANOTHER TESTING USER"
        user.id = id
        every { get<SecurityService>().encode("TEST2") } returns "TEST2"
        val newUser = service.update(id, form, "TEST2", user)
        assertEquals(user.username, newUser.username)
        assertEquals(user.isAdmin, newUser.isAdmin)
        assertEquals(user.lastLogin, newUser.lastLogin)
        assertNotEquals(user.name, newUser.name)
        assertNotEquals(user.passwordHash, newUser.passwordHash)
        assertNotEquals(user.email, newUser.email)
    }

    @Test
    fun testUpdateFromAdmin() {
        val user = randomUser()
        val id = service.new(user, "TEST").id!!
        val form = randomUser()
        val issuer = randomUser()
        issuer.id = id + 10
        issuer.isAdmin = false
        assertThrows<IllegalActionException> { service.update(id, form, "TEST", issuer) }
        issuer.isAdmin = true
        service.update(id, form, "TEST", issuer)
    }

    @Test
    fun testDisable() {
        var user = randomUser()
        val issuer = randomUser()
        issuer.isAdmin = true
        user = service.new(user, "TEST", issuer)
        issuer.isAdmin = false
        assertThrows<NoPermissionException> { service.disable(user, issuer) }
        issuer.isAdmin = true
        service.disable(user, issuer)
        user = service.find(user.id!!)
        assertTrue(user.disabled)
    }

    @Test
    fun testEnable() {
        var user = randomUser()
        val issuer = randomUser()
        issuer.isAdmin = true
        user = service.new(user, "TEST", issuer)
        service.disable(user, issuer)
        service.enable(user, issuer)
        user = service.find(user.id!!)
        assertTrue(!user.disabled)
    }
}
