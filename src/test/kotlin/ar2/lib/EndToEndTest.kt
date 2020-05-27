import ar2.lib.EndToEndTestExt
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Tag("e2e")
@ExtendWith(EndToEndTestExt::class)
annotation class EndToEndTest
