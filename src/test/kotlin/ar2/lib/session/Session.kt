package ar2.lib.session

import ar2.Config
import ar2.web.views.UserViews
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.http4k.core.*
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.cookies
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject

data class Credentials(val username: String, val password: String)

val mapper = ObjectMapper(JsonFactory())
    .registerModule(KotlinModule())
    .registerModule(JavaTimeModule())

class Session(val creds: Credentials? = null) : KoinComponent {
    val cfg: Config by inject()

    var cookie: Cookie? = null
    private val baseHeaders = mapOf(
        "Accept" to "application/json"
    )

    fun login() {
        if (creds == null) throw NullPointerException("No credentials provided.")
        val resp = request(Method.POST, "/login", UserViews.AuthRequest(creds.username, creds.password))
        cookie = resp.cookies().find { it.name == cfg.security.cookieName }
    }

    fun prepareRequest(method: Method, uri: String, data: Any? = null): Request {
        var req = Request(method, uri)
        cookie?.let {
            req = req.cookie(it)
        }
        val headers = baseHeaders.toMutableMap()
        data?.let {
            headers["Content-Type"] = "application/json; charset=UTF-8"
            req = req.body(mapper.writeValueAsString(it))
        }
        for (header in headers) req = req.header(header.key, header.value)
        return req
    }

    fun request(method: Method, uri: String, data: Any? = null): Response {
        return request(prepareRequest(method, uri, data))
    }

    fun request(reqObject: Request): Response {
        val handler = get<HttpHandler>()
        val resp = handler(reqObject)
        if (resp.status.code !in 200..399) throw APIError(resp)
        return resp
    }

    fun users(): UsersApi = UsersApi(this)
}

data class ApiResult<T>(val resp: Response, val body: T?)

fun <T> Response.deserialize(typeRef: TypeReference<T>): T = mapper.readValue(body.stream, typeRef)
fun <T> Response.deserialize(typeClass: Class<T>): T = mapper.readValue(body.stream, typeClass)
