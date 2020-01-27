package ar2.security

import at.favre.lib.crypto.bcrypt.BCrypt
import org.http4k.core.Credentials
import org.http4k.filter.ServerFilters
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory
import ar2.Config
import ar2.users.User
import ar2.users.Users
import ar2.users.UsersService
import ar2.users.toUser
import ar2.web.userKey
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies
import java.security.SecureRandom

val ITERATIONS = 4
val BCRYPT_VERSION = BCrypt.Version.VERSION_2B

class SecurityServiceImpl: SecurityService, KoinComponent {
    val log = LoggerFactory.getLogger(SecurityServiceImpl::class.java)

    val config: Config by inject()
    val usersService: UsersService by inject()


    lateinit var secureRandom: SecureRandom

    fun postInit() {
        secureRandom = SecureRandom(config.security.secret.toByteArray())
    }

    val bCrypt = BCrypt.with(LongPasswordStrategies.hashSha512(BCRYPT_VERSION))
    val verifier = BCrypt.verifyer(BCRYPT_VERSION, LongPasswordStrategies.hashSha512(BCRYPT_VERSION))

    override fun basicAuth() = ServerFilters.BasicAuth(
            "ar2 authentication", key = userKey, lookup = ::authenticate
    )

    override fun encode(password: String): String = bCrypt.hashToString(ITERATIONS, password.toCharArray())

    override fun authenticate(creds: Credentials): User? {
        val rawUser = usersService.findByUsernameRaw(creds.user)
        val result = rawUser != null && verifier.verify(creds.password.toCharArray(), rawUser[Users.passwordHash].toCharArray()).verified
        if (result) {
            return rawUser?.toUser()
        }
        return null
    }
}