package playground


data class ListenSettings(val host: String, val port: Int)
data class StorageSettings(val path: String, val maxFileSize: String)
data class SecuritySettings(val secret: String)

data class Config (val listen: ListenSettings, val storage: StorageSettings, val security: SecuritySettings)
