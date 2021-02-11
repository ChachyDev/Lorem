package club.chachy.cli.commands.launch

import club.chachy.auth.base.account.AuthType
import club.chachy.lorem.Launcher
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

object Launch : CliktCommand() {
    private val version by argument()

    private val username by option("--username", "-u")

    private val password by option("--password", "-p")

    private val microsoft by option().flag()

    override fun run() {
        runBlocking {
            try {
                Launcher {
                    version = this@Launch.version
                    authType = if (microsoft) AuthType.Microsoft else AuthType.Mojang
                    if (!microsoft) {
                        username = this@Launch.username ?: error("Username is null...")
                        password = this@Launch.password ?: error("Password is null...")
                    } else {
                        username = this@Launch.username ?: ""
                    }
                }.begin()
            } catch (e: Exception) {
                e.printStackTrace()
                exitProcess(0)
            }
        }
    }
}