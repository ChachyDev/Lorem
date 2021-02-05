package club.chachy.auth.ms.server.handlers

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.util.HashMap

val http = HttpClient(Apache) {
    Json {
        serializer = GsonSerializer()
    }

}

private const val OAUTH_TOKEN =
    "https://login.live.com/oauth20_token.srf"
//            "client_id=%s" +
//            "&client_secret=%s" +
//            "&code=%s" +
//            "&grant_type=authorization_code" +
//            "&redirect_uri=%s"

class TokenHandler(private val clientSecret: String) : HttpHandler {
    override fun handle(p0: HttpExchange?) {
        if (p0 == null) error("Something went wrong...")

        println(p0)

        val queryParams = p0.requestURI.query.queryToMap()
        val clientId = queryParams["client_id"] ?: error("No client ID was passed...")
        val code = queryParams["code"] ?: error("No authentication code was passed...")
        val redirectUri = "http://localhost:4892/auth" // We will never redirect here :)

        val res = runBlocking {
            try {
                http.post("https://login.live.com/oauth20_token.srf") {
                    body = FormDataContent(Parameters.build {
                        append("client_id", clientId)
                        append("client_secret", clientSecret)
                        append("code", code)
                        append("grant_type", "authorization_code")
                        append("redirect_uri", redirectUri)
                    })

                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@runBlocking ""
            }
        }
        if (res.isEmpty()) error("Failed to successfully convert Authorization Code -> Authorization Token")
        p0.sendResponseHeaders(200, res.length.toLong())
        p0.responseBody.use { it.write(res.toByteArray()) }
    }

    private fun String.queryToMap(): Map<String, String> {
        val result: MutableMap<String, String> = HashMap()
        for (param in split("&").toTypedArray()) {
            val entry = param.split("=").toTypedArray()
            result[entry[0]] = if (entry.size > 1) entry[1] else ""
        }
        return result
    }
}