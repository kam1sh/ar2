package ar2.lib.api

import ar2.lib.ext.AdminUserParameterResolverExt
import ar2.lib.ext.RandomUserParameterResolverExt
import ar2.lib.ext.UsersTestExt
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions

@Retention(AnnotationRetention.RUNTIME)
@Extensions(
    ExtendWith(UsersTestExt::class),
    ExtendWith(RandomUserParameterResolverExt::class),
    ExtendWith(AdminUserParameterResolverExt::class)
)
annotation class RequiresUsers
