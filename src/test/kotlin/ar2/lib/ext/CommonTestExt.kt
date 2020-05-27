package ar2.lib.ext

import ar2.lib.util.setupApp
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.slf4j.LoggerFactory

class CommonTestExt : BeforeAllCallback, BeforeEachCallback {

    private val log = LoggerFactory.getLogger(CommonTestExt::class.java)

    override fun beforeAll(context: ExtensionContext) {
        context.setupApp()
    }

    override fun beforeEach(context: ExtensionContext) {
        if (!context.testMethod.isPresent) return

        val method = context.testMethod.get()
        val className = method.declaringClass.name
        log.info("Testing {}.{}", className, method.name)
    }
}
