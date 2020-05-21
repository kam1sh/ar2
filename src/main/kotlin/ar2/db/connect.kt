package ar2.db

import ar2.PostgresSettings
import ar2.db.entities.*
import java.util.*
import org.flywaydb.core.Flyway
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("ar2.db.entities.connect")

fun doConnectToDatabase(config: PostgresSettings, showSql: Boolean = false): SessionFactory {
    val url = "jdbc:postgresql://${config.host}:${config.port}/${config.db}"
    log.info("Successfully connected to the database.")
    val migrator = Flyway
        .configure()
        .dataSource(url, config.username, config.password)
        .locations("classpath:flyway")
        .load()
    val count = migrator.migrate()
    if (count > 0) log.info("{} migrations applied.", count)
    val props = Properties()
    props["hibernate.connection.provider_class"] = "org.hibernate.hikaricp.internal.HikariCPConnectionProvider"
    props["hibernate.hikari.dataSourceClassName"] = "org.postgresql.ds.PGSimpleDataSource"
    props["hibernate.hikari.dataSource.url"] = url
    props["hibernate.hikari.dataSource.user"] = config.username
    props["hibernate.hikari.dataSource.password"] = config.password
    props["hibernate.hikari.maximumPoolSize"] = "10"
    props["hibernate.hikari.minimumIdle"] = "5"
    props["hibernate.hikari.idleTimeout"] = "30000"
    props["hibernate.dialect"] = "org.hibernate.dialect.PostgreSQL10Dialect"
    props["hibernate.show_sql"] = showSql
    val cfg = Configuration().apply {
        properties = props
        addAnnotatedClass(User::class.java)
        addAnnotatedClass(Group::class.java)
        addAnnotatedClass(GroupRole::class.java)
        addAnnotatedClass(Session::class.java)
        addAnnotatedClass(ProjectRole::class.java)
        addAnnotatedClass(Project::class.java)
    }
    val builder = StandardServiceRegistryBuilder()
        .applySettings(cfg.properties)
        .build()
    return cfg.buildSessionFactory(builder)
}
