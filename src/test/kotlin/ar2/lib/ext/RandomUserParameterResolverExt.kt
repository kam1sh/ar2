package ar2.lib.ext

import ar2.db.entities.User
import ar2.lib.api.RandomUser
import ar2.lib.api.randomUserPassword
import ar2.services.UsersService
import ar2.tests.e2e.randomUser
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.koin.core.KoinComponent
import org.koin.core.get

class RandomUserParameterResolverExt : ParameterResolver, KoinComponent {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        val hasAnnotation = parameterContext.isAnnotated(RandomUser::class.java)
        val hasUserType = parameterContext.parameter.type == User::class.java
        return hasUserType && hasAnnotation
    }

    override fun resolveParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Any {
        return get<UsersService>().new(randomUser(), randomUserPassword)
    }
}
