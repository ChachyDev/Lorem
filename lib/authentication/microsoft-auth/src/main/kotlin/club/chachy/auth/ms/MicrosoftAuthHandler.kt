package club.chachy.auth.ms

import club.chachy.auth.base.account.AuthData
import club.chachy.auth.base.account.AuthenticationData
import club.chachy.auth.base.account.storage.Account
import club.chachy.auth.base.account.storage.Accounts
import club.chachy.auth.base.account.utils.attemptReading
import club.chachy.auth.ms.data.*
import club.chachy.auth.ms.microsoft.CodeHandler
import club.chachy.auth.ms.utils.MICROSOFT_OAUTH
import club.chachy.auth.ms.utils.TOKEN_URL
import club.chachy.auth.ms.utils.gson
import club.chachy.auth.ms.utils.http
import club.chachy.auth.service.AuthenticationService
import club.chachy.yggdrasil.wrapper.Yggdrasil
import com.sun.net.httpserver.HttpServer
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

class MicrosoftAuthHandler(private val clientId: String) : AuthenticationService {
    private val serializer = defaultSerializer()

    private fun File.saveFile(accounts: Accounts) = writeText(gson.toJson(accounts))

    override suspend fun executeTask(data: AuthenticationData): AuthData {
        // The beginning of pain
        val accountsDir = File(data.launchDir, "storage/accounts")
        val accountsJson = File(accountsDir, "accounts.json")
        accountsDir.mkdirs()
        withContext(Dispatchers.IO) { accountsJson.createNewFile() }

        val text = if (accountsJson.exists()) accountsJson.readText() else null

        val account: Accounts

        if (!text.isNullOrEmpty()) {
            println("Attempting to log in from saved session")
            val attemptedRead = attemptReading(text, data.username)
            account = attemptedRead.first
            val acc = account.accounts.find { it.username == data.username || it.uuid == data.username }
            if (acc != null) {
                // Quickly validate the token
                val isValid = Yggdrasil.validate(acc.token)
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

        val xblRequest = authenticateWithXboxLive(
            createRequestBody(
                http.get<MicrosoftAuthResponse>(
                    TOKEN_URL.format(
                        clientId,
                        code
                    )
                ).accessToken
            )
        )

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

        val mcAccessToken = mcAccessResponse.accessToken

        if (!isGameOwned(mcAccessToken)) error("Cannot authenticate as you do not own the game.")

        val profile = fetchProfile(mcAccessToken)

        val authData = AuthData(mcAccessToken, profile.id, profile.name, mcAccessResponse.expiresIn, listOf())

        val acc = Account(profile.name, profile.id, mcAccessToken, mcAccessResponse.expiresIn, listOf())

        account.accounts.removeIf { it.uuid == profile.id }
        account.accounts.add(acc)

        accountsJson.saveFile(account)

        return authData
    }

    private suspend fun isGameOwned(accessToken: String): Boolean {
        return http.get<MCStoreResponse>("https://api.minecraftservices.com/entitlements/mcstore") {
            header("Authorization", "Bearer $accessToken")
        }.items.isNotEmpty()
    }

    private suspend fun fetchProfile(accessToken: String): MCProfileResponse {
        return http.get("https://api.minecraftservices.com/minecraft/profile") {
            header("Authorization", "Bearer $accessToken")
        }
    }

    private fun createRequestBody(token: String): XboxLiveAuthRequestBody {
        return XboxLiveAuthRequestBody(
            Properties(
                "RPS",
                "user.auth.xboxlive.com",
                "d=%s".format(token)
            ), "http://auth.xboxlive.com",
            "JWT"
        )
    }

    private suspend fun authenticateWithXboxLive(req: XboxLiveAuthRequestBody): XboxLiveAuthResponse {
        return http.post("https://user.auth.xboxlive.com/user/authenticate") {
            body = serializer.write(req)
        }
    }

    companion object {
        val authCode = CompletableFuture<String>()
    }
}