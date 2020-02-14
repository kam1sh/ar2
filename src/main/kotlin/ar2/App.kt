package ar2

import ar2.cli.CreateAdmin
import ar2.cli.Maintenance
import ar2.cli.Serve
import ar2.security.SecurityService
import ar2.security.SecurityServiceImpl
import ar2.users.UsersService
import ar2.users.UsersServiceImpl
import ar2.web.views.PyPIViews
import ar2.web.views.UserViews
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.File
import java.nio.file.Paths
import java.util.*
import javax.sql.DataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.get
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val modules = module {
    single { SecurityServiceImpl() as SecurityService }
    single { UsersServiceImpl(get()) as UsersService }

    single { UserViews(get(), get()) }
    single { PyPIViews(get()) }
}

class App : KoinComponent {
    private val log = LoggerFactory.getLogger(javaClass)

    lateinit var config: Config
    lateinit var dataSource: DataSource

    fun loadConfig(file: File) {
        log.info("Using configuration file {}", file.name)
        config = file.toConfig()
        getKoin().declare(config)
    }

    fun setupLogging(level: Level = Level.INFO) {
        val lc = LoggerFactory.getILoggerFactory() as LoggerContext
        lc.getLogger(Logger.ROOT_LOGGER_NAME).level = Level.WARN
        lc.getLogger("ar2").level = level
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
        val count = migrator.migrate()
        log.info("{} migrations applied.", count)
        getKoin().declare(dataSource)
        val db = Database.connect(dataSource)
        getKoin().declare(db)
    }

    /**
     * Setups everything needed to run application. Logging, config, database, etc.
     */
    fun setup(configFile: File, logLevel: Level = Level.INFO) {
        loadConfig(configFile)
        setupLogging(logLevel)
        connectToDatabase()
        postSetup()
    }

    fun postSetup() {
        val ss = get<SecurityService>() as SecurityServiceImpl
        ss.postInit()
    }
}
class CliApp(val app: App) : CliktCommand() {
    val config: File? by option(help = "Path to configuration file", envvar = "AR2_CONFIG").file(exists = true, fileOkay = true)
    val debug: Boolean by option(help = "Enable debug logging").flag()
    val trace: Boolean by option(help = "Enable trace logging").flag()

    override fun run() {
        var level = if (debug) Level.DEBUG else Level.WARN
        level = if (trace) Level.TRACE else level
        app.setup(config ?: Paths.get("ar2.yaml").toFile(), logLevel = level)
    }
}

fun main(args: Array<String>) {

    startKoin { modules(modules) }
    val app = App()
    CliApp(app)
            .subcommands(Serve(app), CreateAdmin(app), Maintenance(app))
            .main(args)
}
