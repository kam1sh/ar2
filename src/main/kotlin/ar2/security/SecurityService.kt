package ar2.security

import ar2.users.User
import org.http4k.core.Credentials
import org.http4k.core.Filter
import java.security.SecureRandom

interface SecurityService {
    val secureRandom: SecureRandom
    fun basicAuth(): Filter
    fun requireSession(): Filter
    fun encode(password: String): String
    fun authenticate(creds: Credentials): User?
}