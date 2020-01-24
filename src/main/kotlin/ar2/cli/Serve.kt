package ar2.cli

import ar2.App
import com.github.ajalt.clikt.core.CliktCommand

class Serve(val app: App): CliktCommand() {
    override fun run() {
        app.startServer().block()
    }
}