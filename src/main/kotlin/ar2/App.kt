package ar2

import ar2.cli.CreateAdmin
import ar2.cli.Maintenance
import ar2.cli.Serve
import ar2.db.*
import ar2.services.*
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
import java.util.*
import org.flywaydb.core.Flyway
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("USELESS_CAST")
val modules = module {
    single { SecurityServiceImpl() as SecurityService }
    single { UsersServiceImpl(get()) as UsersService }
    single { SessionsServiceImpl() as SessionsService }
    single { GroupsServiceImpl() as GroupsService }

    single { UserViews(get(), get()) }
    single { PyPIViews(get()) }
}

class App : KoinComponent {
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

    fun connectToDatabase(showSql: Boolean = false) {
        val ds = PGSimpleDataSource()
        val url = "jdbc:postgresql://${config.postgres.host}:${config.postgres.port}/${config.postgres.db}"
        ds.setURL(url)
        ds.user = config.postgres.username
        ds.password = config.postgres.password

        log.info("Succesfully connected to the database.")
        val migrator = Flyway
                .configure()
                .dataSource(ds)
                .locations("classpath:flyway")
                .load()
        val count = migrator.migrate()
        log.info("{} migrations applied.", count)
        getKoin().declare(ds)
        val props = Properties()
        props["hibernate.connection.provider_class"] = "org.hibernate.hikaricp.internal.HikariCPConnectionProvider"
        props["hibernate.hikari.dataSourceClassName"] = "org.postgresql.ds.PGSimpleDataSource"
        props["hibernate.hikari.dataSource.url"] = url
        props["hibernate.hikari.dataSource.user"] = config.postgres.username
        props["hibernate.hikari.dataSource.password"] = config.postgres.password
        props["hibernate.hikari.maximumPoolSize"] = "10"
        props["hibernate.hikari.minimumIdle"] = "5"
        props["hibernate.hikari.idleTimeout"] = "30000"
        props["hibernate.dialect"] = "org.hibernate.dialect.PostgreSQL10Dialect"
        props["hibernate.show_sql"] = showSql
        val cfg = Configuration()
        cfg.properties = props
        cfg.addAnnotatedClass(User::class.java)
        cfg.addAnnotatedClass(Group::class.java)
        cfg.addAnnotatedClass(GroupRole::class.java)
        cfg.addAnnotatedClass(Session::class.java)
        cfg.addAnnotatedClass(RepositoryRole::class.java)
        cfg.addAnnotatedClass(GroupRole::class.java)
        cfg.addAnnotatedClass(Repository::class.java)
        val builder = StandardServiceRegistryBuilder().applySettings(cfg.properties)
        sessionFactory = cfg.buildSessionFactory(builder.build())
        getKoin().declare(sessionFactory)
    }

    /**
     * Setups everything needed to run application. Logging, config, database, etc.
     */
    fun setup(configFile: File, logLevel: Level = Level.INFO) {
        loadConfig(configFile)
        setupLogging(logLevel)
        connectToDatabase(showSql = logLevel == Level.TRACE)
    }

    fun shutdown() {
        sessionFactory.close()
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
    val app = App()
    try {
        CliApp(app)
            .subcommands(Serve(app), CreateAdmin(app), Maintenance(app))
            .main(args)
    } finally {
        app.shutdown()
    }
}
