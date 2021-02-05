package club.chachy.auth.ms.microsoft

import club.chachy.auth.ms.MicrosoftAuthHandler
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

object CodeHandler : HttpHandler {

    override fun handle(t: HttpExchange?) {
        if (t == null) error("Something went wrong...")
        val url = t.requestURI.toString()
        val title = "Success!"
        val message = "Successfully authenticated with Microsoft! You can now close this tab."
        val response = this::class.java.getResourceAsStream("/microsoft-response.html").use { it.bufferedReader().use { reader -> reader.readLines() } }.toMutableList()
        for (line in response) {
            if (line.trimIndent() == "%TITLE%") response[response.indexOf(line)] = title
            if (line.trimIndent() == "%MESSAGE%") response[response.indexOf(line)] = message
        }
        val joinedRes = response.joinToString("\n")
        t.sendResponseHeaders(200, joinedRes.length.toLong())
        t.responseBody.use {  it.write(joinedRes.toByteArray()) }
        MicrosoftAuthHandler.authCode.complete(url.removePrefix("/auth?code="))
    }
}