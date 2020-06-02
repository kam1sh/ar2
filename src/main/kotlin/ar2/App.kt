package ar2

import ar2.cli.CreateAdmin
import ar2.cli.Maintenance
import ar2.cli.Serve
import ar2.db.doConnectToDatabase
import ar2.facades.UsersFacade
import ar2.facades.impl.UsersFacadeImpl
import ar2.services.*
import ar2.services.impl.GroupsServiceImpl
import ar2.services.impl.SecurityServiceImpl
import ar2.services.impl.SessionsServiceImpl
import ar2.services.impl.UsersServiceImpl
import ar2.web.WebHandler
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
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("USELESS_CAST")
val modules = module {
    single<SecurityService> { SecurityServiceImpl() }
    single<UsersService> { UsersServiceImpl(get()) }
    single<SessionsService> { SessionsServiceImpl() }
    single<GroupsService> { GroupsServiceImpl() }

    single<UsersFacade> { UsersFacadeImpl(get(), get()) }

    single { AuthenticationViews(get(), get()) }
    single { UserViews(get()) }
    single { PyPIViews(get()) }
    single { GroupViews(get(), get()) }

    single { WebHandler() }
}

class App : KoinComponent, AutoCloseable {
    private val log = LoggerFactory.getLogger(javaClass)

    private var _config: Config? = null
    var sessionFactory: SessionFactory? = null

    val config: Config
        get() = _config ?: throw IllegalStateException("Configuration is not loaded yet.")

    fun loadConfig(file: File?) {
        val fileOrDef = file ?: File("ar2.yaml")
        log.info("Using configuration file {}", fileOrDef.name)
        _config = _config ?: fileOrDef.toConfig()
        getKoin().declare(_config)
    }

    fun unloadConfig() {
        _config = null
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
    fun setup(configFile: File?, logLevel: Level = Level.INFO) {
        startDI()
        loadConfig(configFile)
        setupLogging(logLevel)
        val factory = connectToDatabase(showSql = logLevel == Level.TRACE)
        getKoin().declare(factory)
    }

    fun startDI() = startKoin { modules(modules) }
    fun stopDI() = stopKoin()

    override fun close() {
        sessionFactory?.close()
        stopDI()
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
    App().use {
        CliApp(it)
            .subcommands(Serve(it), CreateAdmin(it), Maintenance(it))
            .main(args)
    }
}
