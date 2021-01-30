package club.chachy.lorem.services.default

import club.chachy.lorem.services.account.AccountAuthenticationService
import club.chachy.lorem.services.account.AuthData
import club.chachy.lorem.services.account.AuthType
import club.chachy.lorem.services.account.AuthenticationData
import club.chachy.lorem.utils.http.http
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import java.io.File

const val MOJANG_SERVERS = "https://authserver.mojang.com"

private val gson = Gson()

data class Account(
    val username: String,
    val uuid: String,
    val token: String,
    val properties: List<Property>,
    val email: String // Username for emigrated accounts
)

class AccountAuthenticationService(private val runDir: File) : AccountAuthenticationService {
    private val logger = LogManager.getLogger(this)

    override suspend fun executeTask(data: AuthenticationData): AuthData {
        return if (data.type == AuthType.Microsoft) {
            microsoft()
        } else {
            mojang(data.username, data.password)
        }
    }

    private fun microsoft(): Nothing = TODO("Implement Microsoft Authentication")

    private suspend fun mojang(username: String, password: String): AuthData {
        val account = File(runDir, "storage/accounts")
        account.mkdirs()
        val accounts = File(account, "accounts.json")
        if (accounts.exists()) {
            val parsed = JsonParser.parseString(accounts.readText()).asJsonObject
            val accountsArray = parsed["accounts"].asJsonArray
            val accountObject = accountsArray.find { it.asJsonObject["email"].asString == username }
            if (accountObject != null) {
                logger.info("It seems like you are already logged in, let's quickly validate this though. :)")
                val accountJsonObject = accountObject.asJsonObject
                try {
                    val res = http.get<HttpResponse>("$MOJANG_SERVERS/validate") {
                        val obj = JsonObject()
                        obj.add("accessToken", accountJsonObject["token"])
                        body = defaultSerializer().write(obj)
                    }

                    if (res.status == HttpStatusCode.NoContent) {
                        val ad = gson.fromJson(accountJsonObject, Account::class.java)
                        return AuthData(
                            ad.username,
                            ad.token,
                            ad.uuid,
                            ad.properties
                        )
                    }
                } catch (e: ClientRequestException) {
                    logger.error("Doesn't look like that token works anymore :( don't worry we will sort it out :)")
                    accountsArray.remove(accountJsonObject)
                }
            }
        }

        val res = http.post<MojangAuthenticationResponse>("$MOJANG_SERVERS/authenticate") {
            val mainObj = JsonObject().apply {
                add("agent", JsonObject().apply {
                    addProperty("name", "Minecraft")
                    addProperty("version", 1)
                })

                addProperty("username", username)
                addProperty("password", password)
                addProperty("requestUser", true)
            }

            body = defaultSerializer().write(mainObj)
        }

        withContext(Dispatchers.IO) { accounts.createNewFile() }

        val text = accounts.readText()

        if (text.isEmpty()) {
            accounts.writeText(
                gson.toJson(
                    AccountJson(
                        listOf(
                            Account(
                                res.availableProfiles[0].name,
                                res.availableProfiles[0].id,
                                res.accessToken,
                                res.user.properties,
                                res.user.username
                            )
                        )
                    )
                )
            )
        } else {
            val p = JsonParser.parseString(text).asJsonObject
            val acc = p["accounts"].asJsonArray
            acc.add(
                gson.toJsonTree(
                    Account(
                        res.availableProfiles[0].name,
                        res.availableProfiles[0].id,
                        res.accessToken,
                        res.user.properties,
                        res.user.username
                    )
                )
            )

            accounts.writeText(gson.toJson(p))
        }

        return AuthData(
            res.accessToken,
            res.availableProfiles[0].id,
            res.availableProfiles[0].name,
            res.user.properties
        )
    }
}

data class MojangAuthenticationResponse(val accessToken: String, val availableProfiles: List<Profile>, val user: User)

data class User(val username: String, val properties: List<Property>)

data class Property(val name: String, val value: String)

data class Profile(val id: String, val name: String)

data class AccountJson(val accounts: List<Account>)