package ar2.security

import at.favre.lib.crypto.bcrypt.BCrypt
import org.http4k.core.Credentials
import org.http4k.core.Request
import org.http4k.filter.ServerFilters
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory
import ar2.Config
import ar2.users.User
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class SecurityServiceImpl: SecurityService, KoinComponent {

    val log = LoggerFactory.getLogger(SecurityServiceImpl::class.java)
    val config: Config by inject()

    val ITERATIONS = 4
    val BCRYPT_VERSION = BCrypt.Version.VERSION_2B

    val bCrypt = BCrypt.with(LongPasswordStrategies.hashSha512(BCRYPT_VERSION))
    val verifier = BCrypt.verifyer(BCRYPT_VERSION, LongPasswordStrategies.hashSha512(BCRYPT_VERSION))

    val currentUser = ThreadLocal<ResultRow>()

    override fun basicAuth() = ServerFilters.BasicAuth(
            "ar2 authentication", {authenticate(it)}
    )

    override fun encode(password: String): String = bCrypt.hashToString(ITERATIONS, password.toCharArray())

    fun authenticate(creds: Credentials): Boolean {
        val user = transaction {
            User.select {User.username eq creds.user}.singleOrNull()
        }
        val result = user != null && verifier.verify(creds.password.toCharArray(), user[User.passwordHash].toCharArray()).verified
        if (result) currentUser.set(user)
        return result
    }
}