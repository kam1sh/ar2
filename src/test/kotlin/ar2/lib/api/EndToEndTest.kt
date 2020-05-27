import ar2.lib.ext.AdminSessionParametersResolverExt
import ar2.lib.ext.EndToEndTestExt
import ar2.lib.ext.RandomUserParameterResolverExt
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Tag("e2e")
@Extensions(
    ExtendWith(EndToEndTestExt::class),
    ExtendWith(AdminSessionParametersResolverExt::class),
    ExtendWith(RandomUserParameterResolverExt::class)
)
annotation class EndToEndTest
