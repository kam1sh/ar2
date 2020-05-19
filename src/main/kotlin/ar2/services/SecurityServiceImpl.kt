package ar2.services

import ar2.db.entities.User
import ar2.exceptions.WebError
import ar2.web.currentUser
import ar2.web.userKey
import at.favre.lib.crypto.bcrypt.BCrypt
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies
import org.http4k.core.*
import org.http4k.core.Credentials
import org.http4k.filter.ServerFilters
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory
import java.util.concurrent.ThreadLocalRandom

const val ITERATIONS = 6
val BCRYPT_VERSION: BCrypt.Version = BCrypt.Version.VERSION_2B

class SecurityServiceImpl : SecurityService, KoinComponent {
    val log = LoggerFactory.getLogger(SecurityServiceImpl::class.java)

    private val usersService: UsersService by inject()

    private val bCrypt = BCrypt.with(LongPasswordStrategies.hashSha512(BCRYPT_VERSION))
    private val verifier = BCrypt.verifyer(
        BCRYPT_VERSION, LongPasswordStrategies.hashSha512(
            BCRYPT_VERSION
        ))
    private val random get() = ThreadLocalRandom.current()

    override fun basicAuth() = ServerFilters.BasicAuth(
            "ar2 authentication", key = userKey, lookup = ::authenticate
    )

    override fun requireSession() = Filter { next ->
        { request ->
            val usr = request.currentUser
            log.trace("Current user: {}", usr)
            next(request)
        }
    }

    override fun encode(password: String): String = bCrypt.hashToString(ITERATIONS, password.toCharArray())

    override fun authenticate(creds: Credentials): User? {
        val user = usersService.find(creds.user)
        if (user.disabled) return null
        val result = verifier.verify(creds.password.toCharArray(), user.passwordHash!!.toCharArray()).verified
        return if (result) user else null
    }

    override fun randomString(length: Int): String {
        val sb = StringBuilder()
        for (x in 0..length) {
            val chars = listOf(
                randomChar(48, 57),
                randomChar(65, 90),
                randomChar(97, 122),
                '_'
            )
            sb.append(chars[random.nextInt(0, chars.size)])
        }
        return sb.toString()
    }
    private fun randomChar(start: Int, end: Int): Char {
        val out = random.nextInt(start, end)
        return out.toChar()
    }
}
