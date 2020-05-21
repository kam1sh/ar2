package ar2

import ar2.cli.CreateAdmin
import ar2.cli.Maintenance
import ar2.cli.Serve
import ar2.db.doConnectToDatabase
import ar2.facades.UsersFacade
import ar2.facades.UsersFacadeImpl
import ar2.services.*
import ar2.web.views.AuthenticationViews
import ar2.web.views.GroupViews
import ar2.web.views.PyPIViews
import ar2.web.views.UserViews
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import java.nio.file.Paths
import org.hibernate.SessionFactory
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("USELESS_CAST")
val modules = module {
    single { SecurityServiceImpl() as SecurityService }
    single { UsersServiceImpl(get()) as UsersService }
    single { SessionsServiceImpl() as SessionsService }
    single { GroupsServiceImpl() as GroupsService }

    single { UsersFacadeImpl(get()) as UsersFacade }

    single { AuthenticationViews(get(), get()) }
    single { UserViews(get()) }
    single { PyPIViews(get()) }
    single { GroupViews(get(), get()) }
}

class App : KoinComponent, AutoCloseable {
    private val log = LoggerFactory.getLogger(javaClass)

    lateinit var config: Config
    lateinit var sessionFactory: SessionFactory

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

    fun connectToDatabase(showSql: Boolean) =
        doConnectToDatabase(config.postgres, showSql)

    /**
     * Setups everything needed to run application. Logging, config, database, etc.
     */
    fun setup(configFile: File, logLevel: Level = Level.INFO) {
        loadConfig(configFile)
        setupLogging(logLevel)
        val factory = connectToDatabase(showSql = logLevel == Level.TRACE)
        getKoin().declare(factory)
    }

    override fun close() {
        if (::sessionFactory.isInitialized) sessionFactory.close()
    }
}

class CliApp(val app: App) : CliktCommand() {
    private val config: File? by option(help = "Path to configuration file", envvar = "AR2_CONFIG").file(exists = true, fileOkay = true)
    private val debug: Boolean by option(help = "Enable debug logging").flag()
    private val trace: Boolean by option(help = "Enable trace logging").flag()

    override fun run() {
        var level = if (debug) Level.DEBUG else Level.WARN
        level = if (trace) Level.TRACE else level
        app.setup(config ?: Paths.get("ar2.yaml").toFile(), logLevel = level)
    }
}

fun main(args: Array<String>) {
    startKoin { modules(modules) }
    App().use {
        CliApp(it)
            .subcommands(Serve(it), CreateAdmin(it), Maintenance(it))
            .main(args)
    }
}
