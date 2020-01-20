package playground.security

import org.http4k.core.Credentials
import org.http4k.core.Request
import java.util.*

class SecurityServiceImpl: SecurityService {
    override fun isAuthorized(request: Request): Boolean {
        val creds = getCredentials(request)
        return creds == Credentials("0", "1")
    }

    fun getCredentials(request: Request): Credentials? {
        val auth = request.header("Authorization")
                ?.trim()
                ?.takeIf { it.startsWith("Basic") }
                ?.substringAfter("Basic")
                ?.trim()
        return String(Base64.getDecoder().decode(auth)).split(":").let {
            Credentials(it.getOrElse(0) { "" }, it.getOrElse(1) { "" })
        }
    }

    override fun hasAccessToProject(userId: Int, project: String): Boolean {
        return true
    }
}