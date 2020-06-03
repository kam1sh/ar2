package ar2.lib.ext

import ar2.facades.UsersFacade
import ar2.facades.impl.UsersFacadeImpl
import ar2.services.GroupsService
import ar2.services.SecurityService
import ar2.services.UsersService
import ar2.services.impl.GroupsServiceImpl
import ar2.services.impl.SecurityServiceImpl
import ar2.services.impl.UsersServiceImpl
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class UsersTestExt : BeforeAllCallback, AfterAllCallback {
    val module = module {
        single<SecurityService> { SecurityServiceImpl() }
        single<UsersService> { UsersServiceImpl(get()) }
        single<GroupsService> { GroupsServiceImpl() }
        single<UsersFacade> { UsersFacadeImpl(get(), get()) }
    }
    override fun beforeAll(context: ExtensionContext?) {
        loadKoinModules(module)
    }

    override fun afterAll(context: ExtensionContext?) {
        unloadKoinModules(module)
    }
}
