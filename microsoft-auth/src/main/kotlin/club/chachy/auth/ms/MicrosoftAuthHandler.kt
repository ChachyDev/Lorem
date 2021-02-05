package club.chachy.auth.ms

import club.chachy.auth.base.account.AuthData
import club.chachy.auth.ms.microsoft.CodeHandler
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.sun.net.httpserver.HttpServer
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.CompletableFuture

private const val MICROSOFT_OAUTH = "https://login.live.com/oauth20_authorize.srf" +
        "?client_id=%s" +
        "&response_type=code" +
        "&redirect_uri=%s" +
        "&scope=XboxLive.signin+offline_access"

private const val TOKEN_URL = "http://localhost:8583/token?client_id=%s&code=%s"

private val gson = Gson()

private val http = HttpClient(Apache) {
    Json {
        serializer = GsonSerializer()
    }
}

private val dashesRegex = "(.{8})(.{4})(.{4})(.{4})(.{12})".toRegex()

object MicrosoftAuthHandler {
    val authCode = CompletableFuture<String>()

    suspend fun login(clientId: String): AuthData {
        // The beginning of pain
        val server = withContext(Dispatchers.IO) { HttpServer.create(InetSocketAddress(4892), 0) }
        server.createContext("/auth", CodeHandler)
        server.executor = null
        server.start()

        val link = MICROSOFT_OAUTH.format(clientId, "http://localhost:4892/auth")

        withContext(Dispatchers.IO) { Desktop.getDesktop().browse(URI(link)) }


        val code = authCode.await()

        val res = http.get<String>(TOKEN_URL.format(clientId, code))

        val parsedRes = gson.fromJson(res, MicrosoftAuthResponse::class.java)

        val req = XboxLiveAuthRequestBody(
            Properties(
                "RPS",
                "user.auth.xboxlive.com",
                "d=" + parsedRes.accessToken
            ), "http://auth.xboxlive.com",
            "JWT"
        )

        val xblRequest = http.post<XboxLiveAuthResponse>("https://user.auth.xboxlive.com/user/authenticate") {
            body = defaultSerializer().write(req)
        }

        // Fleep microsoft dude why do they use caps

        val xblToken = xblRequest.Token

        val uhs = xblRequest.DisplayClaims.xui.find { it.has("uhs") }?.get("uhs")?.asString ?: error("No uhs found...")

        val xstsRequest = http.post<XboxLiveAuthResponse>("https://xsts.auth.xboxlive.com/xsts/authorize") {
            body = defaultSerializer().write(
                XSTSAuthRequestBody(
                    XSTSProperties(
                        "RETAIL",
                        listOf(xblToken)
                    ),
                    "rp://api.minecraftservices.com/",
                    "JWT"
                )
            )
        }

        val mcAccessToken = http.post<LoginWithXboxResponse>("https://api.minecraftservices.com/authentication/login_with_xbox") {
            body = defaultSerializer().write(LoginWithXboxRequest("XBL3.0 x=%s;%s".format(uhs, xstsRequest.Token)))
        }.accessToken

        // Finally!!!! now lets check if they have the game

        val isGameOwned = http.get<MCStoreResponse>("https://api.minecraftservices.com/entitlements/mcstore") {
            header("Authorization", "Bearer $mcAccessToken")
        }.items.isNotEmpty()

        if (!isGameOwned) error("Cannot authenticate as you do not own the game.")

        val profile = http.get<MCProfileResponse>("https://api.minecraftservices.com/minecraft/profile") {
            header("Authorization", "Bearer $mcAccessToken")
        }

        return AuthData(mcAccessToken, profile.id, profile.name, listOf())
    }
}

data class MicrosoftAuthResponse(@SerializedName("access_token") val accessToken: String)

data class XboxLiveAuthRequestBody(
    val Properties: Properties,
    val RelyingParty: String,
    val TokenType: String
)

data class Properties(
    val AuthMethod: String,
    val SiteName: String,
    val RpsTicket: String
)

data class XboxLiveAuthResponse(
    val Token: String,
    val DisplayClaims: DisplayClaims
)

data class DisplayClaims(val xui: List<JsonObject>)

data class XSTSAuthRequestBody(
    @SerializedName("Properties") val properties: XSTSProperties,
    @SerializedName("RelyingParty") val relyingParty: String,
    @SerializedName("TokenType") val tokenType: String
)

data class XSTSProperties(@SerializedName("SandboxId") val sandboxId: String, @SerializedName("UserTokens") val userTokens: List<String>)

data class LoginWithXboxRequest(val identityToken: String)

data class LoginWithXboxResponse(@SerializedName("access_token") val accessToken: String)

data class MCStoreResponse(val items: List<Product>)

data class Product(val name: String, val signature: String) // In a real world situation we don't actually care about these

data class MCProfileResponse(val id: String, val name: String, )