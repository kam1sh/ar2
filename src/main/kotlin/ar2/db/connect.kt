package ar2.db

import ar2.PostgresSettings
import ar2.db.entities.*
import java.util.*
import org.flywaydb.core.Flyway
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("ar2.db.entities.connect")

fun doConnectToDatabase(config: PostgresSettings, showSql: Boolean = false): SessionFactory {
    val ds = PGSimpleDataSource()
    val url = "jdbc:postgresql://${config.host}:${config.port}/${config.db}"
    ds.setURL(url)
    ds.user = config.username
    ds.password = config.password

    log.info("Successfully connected to the database.")
    val migrator = Flyway
        .configure()
        .dataSource(ds)
        .locations("classpath:flyway")
        .load()
    val count = migrator.migrate()
    log.info("{} migrations applied.", count)
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
    return cfg.buildSessionFactory(builder.build())
}
