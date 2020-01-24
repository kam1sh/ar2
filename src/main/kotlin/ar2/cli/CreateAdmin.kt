package ar2.cli

import ar2.App
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.required
import org.koin.core.KoinComponent

class CreateAdmin(val app: App): CliktCommand(), KoinComponent {

    val email: String by option(help = "Email for the admin").required()
    val username: String by option(help = "Admin username").required()
    val password: String by option().prompt("Enter password:", hideInput = true)

    override fun run() {
        TODO("WIP")
    }
}