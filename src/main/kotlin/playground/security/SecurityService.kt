package playground.security

import org.http4k.core.Filter

interface SecurityService {
    fun basicAuth(): Filter
}