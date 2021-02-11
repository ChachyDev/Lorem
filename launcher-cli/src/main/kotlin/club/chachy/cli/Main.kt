package club.chachy.cli

import club.chachy.cli.commands.launch.Launch
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) = Lorem
    .subcommands(Launch)
    .main(args)

// Practically a noop
object Lorem : CliktCommand() {
    override fun run() { }
}