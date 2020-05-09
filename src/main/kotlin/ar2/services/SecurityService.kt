package ar2.services

import ar2.db.User
import ar2.users.Role
import org.http4k.core.Credentials
import org.http4k.core.Filter

interface SecurityService {
    // auth methods
    fun basicAuth(): Filter
    fun requireSession(): Filter
    fun encode(password: String): String
    fun authenticate(creds: Credentials): User?
    // permissions methods
    fun giveGroupRole(user: User, group: String, role: Role)
//    fun getGroupEffectiveRole(user: User, group: String): Role
}
