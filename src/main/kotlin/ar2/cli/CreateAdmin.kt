package ar2.cli

import ar2.App
import ar2.db.User
import ar2.services.UserExists
import ar2.services.UsersService
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.required
import java.lang.Exception
import org.koin.core.KoinComponent
import org.koin.core.get
import org.slf4j.LoggerFactory

class CreateAdmin(val app: App) : CliktCommand(), KoinComponent {
    val log = LoggerFactory.getLogger(CreateAdmin::class.java)

    val email: String by option(help = "Email for the admin").required()
    val username: String by option(help = "Admin username").default("admin")
    val password: String by option().prompt("Enter password", hideInput = true)

    var usersService: UsersService = get()

    override fun run() {
        try {
            usersService.newUser(User(username = username, email = email, name = "Admin", isAdmin = true), password)
        } catch (exist: UserExists) {
            usersService.changePassword(username, password)
            log.warn("User {} already exists, changed password.", username)
            return
        } catch (exc: Exception) {
            log.error("Error creating admin:", exc)
            return
        }
        log.info("Admin {} created.", username)
    }
}
