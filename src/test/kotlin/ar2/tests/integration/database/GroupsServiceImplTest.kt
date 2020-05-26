package ar2.tests.integration.database

import ar2.services.*
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.test.KoinTest
import org.koin.test.get

@ExtendWith(DatabaseTestExt::class)
class GroupsServiceImplTest : KoinTest {
    lateinit var service: GroupsService
    lateinit var usersService: UsersService

    @BeforeEach
    fun before() {
        val securityService = mockk<SecurityService>()
        every { securityService.encode("TEST") }
        usersService = UsersServiceImpl(securityService)
        service = GroupsServiceImpl()
    }

    @Test
    fun testNew() {
        assertEquals(true, true)
    }
}
