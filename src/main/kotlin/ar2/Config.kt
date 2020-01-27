package ar2

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import java.io.File


data class ListenSettings(val host: String, val port: Int)
data class StorageSettings(val path: String, val maxFileSize: String)
data class SecuritySettings(val secret: String)
data class PostgresSettings(val host: String, val port: Int, val db: String, val username: String, val password: String)

data class Config (
        val listen: ListenSettings,
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