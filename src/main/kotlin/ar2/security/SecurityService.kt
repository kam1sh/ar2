package ar2.security

import org.http4k.core.Filter

interface SecurityService {
    fun basicAuth(): Filter
    fun encode(password: String): String
}