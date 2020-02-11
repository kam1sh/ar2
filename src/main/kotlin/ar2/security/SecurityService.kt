package ar2.security

import ar2.users.User
import java.security.SecureRandom
import org.http4k.core.Credentials
import org.http4k.core.Filter

interface SecurityService {
    val secureRandom: SecureRandom
    fun basicAuth(): Filter
    fun requireSession(): Filter
    fun encode(password: String): String
    fun authenticate(creds: Credentials): User?
}
