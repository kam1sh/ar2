package ar2.services.impl

import ar2.db.entities.User
import ar2.exceptions.user.NoSuchUserException
import ar2.services.*
import at.favre.lib.crypto.bcrypt.BCrypt
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies
import java.util.concurrent.ThreadLocalRandom
import org.http4k.core.*
import org.http4k.core.Credentials
import org.http4k.filter.ServerFilters
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory

const val ITERATIONS = 6
val BCRYPT_VERSION: BCrypt.Version = BCrypt.Version.VERSION_2B

class SecurityServiceImpl : SecurityService, KoinComponent {
    val log = LoggerFactory.getLogger(SecurityServiceImpl::class.java)

    private val usersService: UsersService by inject()
    private val sessionsService: SessionsService by inject()

    private val bCrypt = BCrypt.with(LongPasswordStrategies.hashSha512(BCRYPT_VERSION))
    private val verifier = BCrypt.verifyer(
        BCRYPT_VERSION, LongPasswordStrategies.hashSha512(
            BCRYPT_VERSION
        ))
    private val random get() = ThreadLocalRandom.current()

    override fun checkAttempts() = Filter { next ->
        { request ->
            // TODO implement storing address in request
            next(request)
        }
    }

    override fun basicAuth() = ServerFilters.BasicAuth(
            "ar2 authentication", key = contextKey, lookup = ::authenticate
    )

    override fun requireSession() = Filter { next ->
        { request ->
            val usr = extractUser(request)
            log.trace("Current user: {}", usr)
            next(request)
        }
    }

    override fun encode(password: String): String = bCrypt.hashToString(ITERATIONS, password.toCharArray())

    override fun authenticate(creds: Credentials): User? {
        val user = try {
            usersService.find(creds.user)
        } catch (exc: NoSuchUserException) {
            return null
        }
        if (user.disabled) return null
        val result = verifier.verify(creds.password.toCharArray(), user.passwordHash!!.toCharArray()).verified
        return if (result) user else null
    }

    override fun randomString(length: Int): String {
        return random.nextString(length)
    }
}

fun ThreadLocalRandom.nextString(length: Int): String {
    fun nextChar(start: Int, end: Int): Char = nextInt(start, end).toChar()
    val sb = StringBuilder()
    for (x in 0..length) {
        val chars = listOf(
            nextChar(48, 57), // numbers
            nextChar(65, 90), // uppercase
            nextChar(97, 122), // lowercase
            '_' // underscore
        )
        sb.append(chars[nextInt(0, chars.size)])
    }
    return sb.toString()
}
