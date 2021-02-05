package club.chachy.cli

import com.github.ajalt.clikt.core.CliktCommand

fun main(args: Array<String>) = Lorem
    .main(args)

// Practically a noop
object Lorem : CliktCommand() { override fun run() { println(commandHelp) } }