package ar2.tests.integration

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class IntegrationTestExtention : BeforeAllCallback, AfterAllCallback {
    override fun beforeAll(context: ExtensionContext?) {
    }

    override fun afterAll(context: ExtensionContext?) {
    }
}
