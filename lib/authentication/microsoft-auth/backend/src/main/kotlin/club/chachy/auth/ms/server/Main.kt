package club.chachy.auth.ms.server

import club.chachy.auth.ms.server.handlers.TokenHandler
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

fun main(args: Array<String>) {
    val secret = args.getOrNull(args.indexOf("--secret") + 1) ?: error("Please enter a secret")
    println("Creating server for ${generateAsterisks(secret)}")
    val server = HttpServer.create(InetSocketAddress(8583), 0)
    server.createContext("/token", TokenHandler(secret))
    server.executor = null
    server.start()
}

fun generateAsterisks(string: String): String {
    val asterisks = string.length - 2
    var s = "${string[0]}${string[1]}"
    repeat(asterisks) {
        s += "*"
    }
    return s
}