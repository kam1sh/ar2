package ar2.services

import ar2.db.entities.User
import org.http4k.core.Credentials
import org.http4k.core.Filter

interface SecurityService {
    fun basicAuth(): Filter
    fun requireSession(): Filter
    fun encode(password: String): String
    fun authenticate(creds: Credentials): User?
    fun randomString(length: Int): String
}
