package ar2.services

import ar2.db.Users
import ar2.db.toUser
import ar2.users.Role
import ar2.users.User
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

const val ITERATIONS = 6
val BCRYPT_VERSION: BCrypt.Version = BCrypt.Version.VERSION_2B

class SecurityServiceImpl() : SecurityService, KoinComponent {
    val log = LoggerFactory.getLogger(SecurityServiceImpl::class.java)

    val usersService: UsersService by inject()

    val bCrypt = BCrypt.with(LongPasswordStrategies.hashSha512(BCRYPT_VERSION))
    val verifier = BCrypt.verifyer(
        BCRYPT_VERSION, LongPasswordStrategies.hashSha512(
            BCRYPT_VERSION
        ))

    override fun basicAuth() = ServerFilters.BasicAuth(
            "ar2 authentication", key = userKey, lookup = ::authenticate
    )

    override fun requireSession() = Filter { next ->
        { request ->
            val usr = request.currentUser
            log.trace("Current user: {}", usr)
            if (usr != null) {
                next(request)
            } else {
                Response(Status.UNAUTHORIZED)
            }
        }
    }

    override fun encode(password: String): String = bCrypt.hashToString(ITERATIONS, password.toCharArray())

    override fun authenticate(creds: Credentials): User? {
        val rawUser = usersService.findByUsernameRaw(creds.user)
        val result = rawUser != null && verifier.verify(creds.password.toCharArray(), rawUser[Users.passwordHash].toCharArray()).verified
        if (result) {
            return rawUser?.toUser()
        }
        return null
    }

    override fun giveGroupRole(user: User, group: String, role: Role) {
//        val groupId = Groups.findIdByName(group)
//         TODO
    }
}
