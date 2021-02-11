package club.chachy.auth.ms

import club.chachy.auth.base.account.AuthData
import club.chachy.auth.base.account.storage.Account
import club.chachy.auth.base.account.storage.Accounts
import club.chachy.auth.base.account.utils.attemptReading
import club.chachy.auth.ms.data.*
import club.chachy.auth.ms.microsoft.CodeHandler
import club.chachy.yggdrasil.wrapper.YggdrasilAPIWrapper
import com.google.gson.Gson
import com.sun.net.httpserver.HttpServer
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
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

object MicrosoftAuthHandler {
    private val serializer = defaultSerializer()

    val authCode = CompletableFuture<String>()

    suspend fun login(runDir: File, clientId: String, username: String?): AuthData {
        // The beginning of pain
        val accountsDir = File(runDir, "storage/accounts")
        val accountsJson = File(accountsDir, "accounts.json")
        accountsDir.mkdirs()
        withContext(Dispatchers.IO) { accountsJson.createNewFile() }

        val text = if (accountsJson.exists()) accountsJson.readText() else null

        val account: Accounts

        if (!text.isNullOrEmpty()) {
            println("Attempting to log in from saved session")
            val attemptedRead = attemptReading(text, username)
            account = attemptedRead.first
            val acc = account.accounts.find { it.username == username || it.uuid == username }
            if (acc != null) {
                // Quickly validate the token
                val isValid = YggdrasilAPIWrapper.validate(acc.token)
                if (isValid) {
                    return AuthData(acc)
                }
                account.accounts.removeIf { it.username == acc.token }
            }
        } else {
            account = Accounts(ArrayList())
        }

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
            body = serializer.write(req)
        }

        // Fleep microsoft dude why do they use caps

        val xblToken = xblRequest.Token

        val uhs = xblRequest.DisplayClaims.xui.find { it.has("uhs") }?.get("uhs")?.asString ?: error("No uhs found...")

        val xstsRequest = http.post<XboxLiveAuthResponse>("https://xsts.auth.xboxlive.com/xsts/authorize") {
            body = serializer.write(
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

        val mcAccessResponse =
            http.post<LoginWithXboxResponse>("https://api.minecraftservices.com/authentication/login_with_xbox") {
                body = serializer.write(LoginWithXboxRequest("XBL3.0 x=%s;%s".format(uhs, xstsRequest.Token)))
            }


        // Finally!!!! now lets check if they have the game

        val mcAccessToken = mcAccessResponse.accessToken

        val isGameOwned = http.get<MCStoreResponse>("https://api.minecraftservices.com/entitlements/mcstore") {
            header("Authorization", "Bearer $mcAccessToken")
        }.items.isNotEmpty()

        if (!isGameOwned) error("Cannot authenticate as you do not own the game.")

        val profile = http.get<MCProfileResponse>("https://api.minecraftservices.com/minecraft/profile") {
            header("Authorization", "Bearer $mcAccessToken")
        }

        val authData = AuthData(mcAccessToken, profile.id, profile.name, mcAccessResponse.expiresIn, listOf())

        val acc = Account(profile.name, profile.id, mcAccessToken, mcAccessResponse.expiresIn, listOf())

        account.accounts.removeIf { it.uuid == profile.id }
        account.accounts.add(acc)

        accountsJson.saveFile(account)

        return authData
    }

    private fun File.saveFile(accounts: Accounts) = writeText(gson.toJson(accounts))
}