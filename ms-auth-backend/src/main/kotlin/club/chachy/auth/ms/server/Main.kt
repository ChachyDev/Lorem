package club.chachy.auth.ms.server

import club.chachy.auth.ms.server.handlers.TokenHandler
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

fun main(args: Array<String>) {
    val server = HttpServer.create(InetSocketAddress(8583), 0)
    server.createContext("/token", TokenHandler(args[args.indexOf("--secret") + 1]))
    server.executor = null
    server.start()
}