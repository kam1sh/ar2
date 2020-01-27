package ar2.security

import ar2.users.User
import org.http4k.core.Credentials
import org.http4k.core.Filter

interface SecurityService {
    fun basicAuth(): Filter
    fun encode(password: String): String
    fun authenticate(creds: Credentials): User?
}