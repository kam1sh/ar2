package playground.security

import org.http4k.core.Request

interface SecurityService {
    fun isAuthorized(request: Request): Boolean
    fun hasAccessToProject(userId: Int, project: String): Boolean
}