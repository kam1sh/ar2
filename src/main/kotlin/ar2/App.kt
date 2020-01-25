package ar2

import ar2.cli.CreateAdmin
import ar2.cli.Serve
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.http4k.core.*
import org.http4k.core.Method.POST
import org.http4k.filter.GzipCompressionMode
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.get
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ar2.web.views.PyPIViews
import ar2.security.SecurityService
import ar2.security.SecurityServiceImpl
import ar2.users.UsersService
import ar2.users.UsersServiceImpl
import ar2.web.ExceptionHandler
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.impossibl.postgres.jdbc.PGDataSource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.postgresql.ds.PGSimpleDataSource
import java.io.File
import java.lang.Exception
import java.nio.file.Paths
import java.util.*
import javax.sql.DataSource


val modules = module {
    single { SecurityServiceImpl() as SecurityService }
    single { PyPIViews() }
    single { UsersServiceImpl(get()) as UsersService }
}

class App : KoinComponent {

    val log = LoggerFactory.getLogger(App::class.java)

    lateinit var config: Config

    lateinit var dataSource: DataSource

    val pypiViews: PyPIViews = get()

    val securityService: SecurityService = get()

    fun getHandler(): HttpHandler =  ServerFilters.GZip(compressionMode = GzipCompressionMode.Streaming).then(
                ExceptionHandler()().then(routes(
//                        "/users"
                        "/py/{group}/{repo}/upload" bind POST to securityService.basicAuth()
                                .then(ServerFilters.CatchLensFailure())
                                .then(pypiViews.upload())
                )
            )
        )

    fun loadConfig(file: File?) {
        val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
        config = (file ?: Paths.get("config.yaml").toFile())
                .bufferedReader()
                .use { mapper.readValue(it, Config::class.java) }
        getKoin().declare(config)
    }

    fun connectToDatabase() {
        val props = Properties()
        props["dataSourceClassName"] = "org.postgresql.ds.PGSimpleDataSource"
        props["dataSource.serverName"] = config.postgres.host
        props["dataSource.portNumber"] = config.postgres.port
        props["dataSource.databaseName"] = config.postgres.db
        props["dataSource.user"] = config.postgres.username
        props["dataSource.password"] = config.postgres.password
        val hikariConfig = HikariConfig(props)
        dataSource = HikariDataSource(hikariConfig)
        log.info("Succesfully connected to the database.")
        val migrator = Flyway
                .configure()
                .dataSource(dataSource)
                .locations("classpath:flyway")
                .load()
        migrator.migrate()
        log.info("Migrations applied.")
        getKoin().declare(dataSource)
        val db = Database.connect(dataSource)
        getKoin().declare(db)
    }

    fun startServer(): Http4kServer {
        val server = getHandler().asServer(Undertow(config.listen.port));
        log.info("Launching server at port {}", config.listen.port)
        return server.start()
    }
}
class CliApp(val app: App): CliktCommand() {
    val config: File? by option(help = "Path to configuration file").file(exists = true, fileOkay = true)
    override fun run() {
        app.loadConfig(config)
        app.connectToDatabase()
    }
}

fun main(args: Array<String>) {
    val lc = LoggerFactory.getILoggerFactory() as LoggerContext
    lc.getLogger(Logger.ROOT_LOGGER_NAME).level = Level.WARN
    lc.getLogger("ar2").level = Level.INFO

    val koinApp = startKoin { modules(modules) }
    val app = App()
    CliApp(app)
            .subcommands(Serve(app), CreateAdmin(app))
            .main(args)
}
