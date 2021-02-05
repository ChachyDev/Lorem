package club.chachy.auth.mojang.wrapper

import club.chachy.auth.mojang.wrapper.data.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.util.*

/**
 * Generalised API Wrapper for Yggdrasil, can be used for anything and you can thank me later :)
 */

object YggdrasilAPIWrapper {
    private const val MOJANG_SERVERS = "https://authserver.mojang.com"

    private val randomClientToken = UUID.randomUUID()

    private val http = HttpClient(Apache) {
        Json {
            serializer = GsonSerializer()
        }
    }

    private val serializer = defaultSerializer()


    suspend fun authenticate(
        username: String,
        password: String,
        isRequestUser: Boolean = true,
        game: String = "Minecraft",
        version: Int = 1
    ) =
        http.post<YggdrasilAuthenticationResponse>("$MOJANG_SERVERS/authenticate") {
            body = serializer.write(
                YggdrasilAuthenticationRequest(
                    Agent(game, version),
                    username,
                    password,
                    isRequestUser
                )
            )
        }

    suspend fun refresh(
        accessToken: String,
        id: String,
        name: String,
        isRequestUser: Boolean = true
    ) = http.post<YggdrasilRefreshResponse>("$MOJANG_SERVERS/refresh") {
        body = serializer.write(
            YggdrasilRefreshRequest(
                accessToken,
                RefreshSelectedProfile(id, name),
                isRequestUser
            )
        )
    }

    suspend fun validate(accessToken: String) =
        http.post<HttpResponse>("$MOJANG_SERVERS/validate") {
            body = serializer.write(YggdrasilValidationInvalidationRequest(accessToken))
        }.status == HttpStatusCode.NoContent

    suspend fun signout(username: String, password: String) = http.post<String>("$MOJANG_SERVERS/signout") {
        body = serializer.write(YggdrasilSignoutRequest(username, password))
    }.isEmpty()

    suspend fun invalidate(accessToken: String, clientToken: UUID = randomClientToken) =
        http.post<String>("$MOJANG_SERVERS/invalidate") {
            body = serializer.write(YggdrasilValidationInvalidationRequest(accessToken))
        }.isEmpty()
}