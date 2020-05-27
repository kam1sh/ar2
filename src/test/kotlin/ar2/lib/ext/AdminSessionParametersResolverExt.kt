package ar2.lib.ext

import ar2.lib.api.AdminSession
import ar2.lib.session.Session
import ar2.lib.session.adminSession
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

class AdminSessionParametersResolverExt : ParameterResolver {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        val hasAnnotation = parameterContext.isAnnotated(AdminSession::class.java)
        val hasSessionType = parameterContext.parameter.type == Session::class.java
        return hasSessionType && hasAnnotation
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return adminSession()
    }
}
