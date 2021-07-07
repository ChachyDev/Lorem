package club.chachy.auth.ms

import club.chachy.auth.base.account.AuthData
import club.chachy.auth.base.account.storage.StorageAccounts
import club.chachy.auth.ms.data.*
import club.chachy.auth.ms.microsoft.CodeHandler
import club.chachy.auth.ms.utils.MICROSOFT_OAUTH
import club.chachy.auth.ms.utils.TOKEN_URL
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
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.CompletableFuture

class MicrosoftAuthHandler(private val clientId: String, private val port: Int = 4892) : AuthenticationService {
    private val serializer = defaultSerializer()

    override suspend fun execute(data: AuthData.AuthenticationData): AuthData {
        return retrieveAuthData(data) { Yggdrasil.validate(token) } ?: let {
            withContext(Dispatchers.IO) {
                HttpServer.create(InetSocketAddress(port), 0).apply {
                    createContext("/auth", CodeHandler)
                    executor = null
                    start()
                }
            }

            val authURL = MICROSOFT_OAUTH.format(clientId, "http://localhost:$port/auth")

            withContext(Dispatchers.IO) {
                Desktop.getDesktop().browse(URI(authURL))
            }

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

            val uhs =
                xblRequest.DisplayClaims.xui.find { it.has("uhs") }?.get("uhs")?.asString ?: error("No uhs found...")

            val xstsRequest = createXstsAuthentication(xblToken)

            val mcAccessResponse = loginToMinecraft(uhs, xstsRequest.Token)

            val mcAccessToken = mcAccessResponse.accessToken

            if (!isGameOwned(mcAccessToken)) error("Cannot authenticate as you do not own the game.")

            val profile = fetchProfile(mcAccessToken)

            val authData = AuthData(mcAccessToken, profile.id, profile.name, mcAccessResponse.expiresIn, listOf())

            with(readAccountStorage(data) ?: StorageAccounts(mutableListOf())) {
                addAccount(
                    StorageAccounts.StorageAccount(
                        profile.name,
                        profile.id,
                        mcAccessToken,
                        mcAccessResponse.expiresIn,
                        listOf()
                    )
                )

                saveStorage(data)
            }

            authData
        }
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

    private suspend fun createXstsAuthentication(xblToken: String): XboxLiveAuthResponse {
        return http.post("https://xsts.auth.xboxlive.com/xsts/authorize") {
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
    }

    private suspend fun loginToMinecraft(uhs: String, token: String): LoginWithXboxResponse {
        return http.post("https://api.minecraftservices.com/authentication/login_with_xbox") {
            body = serializer.write(LoginWithXboxRequest("XBL3.0 x=%s;%s".format(uhs, token)))
        }
    }

    companion object {
        val authCode = CompletableFuture<String>()
    }
}