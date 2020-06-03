package ar2.lib.ext

import ar2.db.entities.User
import ar2.facades.UsersFacade
import ar2.lib.api.RandomAdmin
import ar2.lib.api.randomUserPassword
import ar2.services.UsersService
import ar2.tests.e2e.randomUser
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.koin.core.KoinComponent
import org.koin.core.error.NoBeanDefFoundException
import org.koin.core.get

class AdminUserParameterResolverExt : ParameterResolver, KoinComponent {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        val hasAnnotation = parameterContext.isAnnotated(RandomAdmin::class.java)
        val hasUserType = parameterContext.parameter.type.isAssignableFrom(User::class.java)
        return hasAnnotation && hasUserType
    }

    override fun resolveParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Any {
        val user = randomUser()
        user.isAdmin = true
        return try {
            get<UsersFacade>().new(user, randomUserPassword, null)
        } catch (exc: NoBeanDefFoundException) {
            get<UsersService>().new(user, randomUserPassword)
        }
    }
}
