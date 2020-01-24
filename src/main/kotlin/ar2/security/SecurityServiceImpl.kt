package ar2.security

import at.favre.lib.crypto.bcrypt.BCrypt
import org.http4k.core.Credentials
import org.http4k.core.Request
import org.http4k.filter.ServerFilters
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory
import ar2.Config
import java.util.*

class SecurityServiceImpl: SecurityService, KoinComponent {

    val log = LoggerFactory.getLogger(SecurityServiceImpl::class.java)
    val config: Config by inject()

    val bCrypt = BCrypt.withDefaults()
    val verifier = BCrypt.verifyer()

    override fun basicAuth() = ServerFilters.BasicAuth(
            "ar2 authentication", {authenticate(it)}
    )

    fun authenticate(creds: Credentials): Boolean {
        val passwordHash = bCrypt.hashToString(4, "1".toCharArray())
        if (creds.user == "0") {
            val result = verifier.verify(creds.password.toCharArray(), passwordHash)
            return result.verified
        }
        return false
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

}