package ar2

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

data class LoginAttemptsSettings(var duration: String, var count: Int)
data class StorageSettings(var path: String, var maxFileSize: String)
data class SecuritySettings(var secret: String, var cookieName: String, var sessionLifetimeDays: Int, var loginAttemptsWindow: LoginAttemptsSettings)
data class PostgresSettings(var host: String, var port: Int, var db: String, var username: String, var password: String)

data class Config(
    val listen: Int,
    val storage: StorageSettings,
    val security: SecuritySettings,
    val postgres: PostgresSettings
)

fun File.toConfig(): Config {
    val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
    return this
            .bufferedReader()
            .use { mapper.readValue(it, Config::class.java) }
}
