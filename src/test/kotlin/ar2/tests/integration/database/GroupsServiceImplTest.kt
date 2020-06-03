package ar2.tests.integration.database

import ar2.db.entities.User
import ar2.facades.UsersFacade
import ar2.lib.api.RandomAdmin
import ar2.lib.api.RandomUser
import ar2.lib.api.RequiresUsers
import ar2.services.*
import ar2.services.impl.GroupsServiceImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.get
import org.koin.test.KoinTest

@ExtendWith(DatabaseTestExt::class)
@RequiresUsers
class GroupsServiceImplTest : KoinTest {
    lateinit var service: GroupsService

    @BeforeEach
    fun before() {
        service = GroupsServiceImpl()
    }

    @Test
    fun testUserHasItsOwnGroup(@RandomUser user: User) {
        service.find(user.username)
    }

    /**
     * Group should stay after user has been disabled.
     */
    @Test
    fun testGroupStays(@RandomUser user: User, @RandomAdmin admin: User) {
        get<UsersFacade>().disable(user.id!!, admin)
        service.find(user.username)
    }
}
