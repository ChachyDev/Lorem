package club.chachy.auth.mojang

import club.chachy.auth.base.account.AuthData
import club.chachy.auth.base.account.AuthenticationData
import club.chachy.auth.base.account.storage.Account
import club.chachy.auth.base.account.storage.Accounts
import club.chachy.auth.base.account.utils.attemptReading
import club.chachy.auth.service.AuthenticationService
import club.chachy.yggdrasil.wrapper.Yggdrasil
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import java.io.File

// Expires in 100 years, so in other words it'll never expire.
const val EXPIRES_IN = 3153600000

private val gson = Gson()

class MojangAuthHandler : AuthenticationService {
    private val logger = LogManager.getLogger(this)

    override suspend fun executeTask(data: AuthenticationData): AuthData {
        val accountsDir = File(data.launchDir, "storage/accounts")
        val accountsJson = File(accountsDir, "accounts.json")
        accountsDir.mkdirs()
        withContext(Dispatchers.IO) { accountsJson.createNewFile() }

        val json = if (accountsJson.exists()) accountsJson.readText() else null

        val account: Accounts

        if (!json.isNullOrEmpty()) {
            val attemptedRead = attemptReading(json, data.username)
            account = attemptedRead.first
            if (attemptedRead.second != null) {
                // Quickly validate the token
                val isValid = Yggdrasil.validate(attemptedRead.second?.token ?: error("Token is null..."))
                if (isValid) {
                    return AuthData(attemptedRead.second ?: error("Account is null..."))
                }
                account.accounts.removeIf { it.token == attemptedRead.second?.token ?: error("Token is null...") }
            }
        } else {
            account = Accounts(ArrayList())
        }

        logger.info("Authenticating with Mojang servers as your token is invalid...")

        val authenticate =
            Yggdrasil.authenticate(data.username, data.password ?: error("You must specify a password to log in"), true)

        val acc = Account(
            authenticate.selectedProfile.name,
            authenticate.selectedProfile.id,
            authenticate.accessToken,
            EXPIRES_IN,
            authenticate.user?.properties ?: listOf()
        )

        account.accounts.add(acc)

        saveFile(accountsJson, account)

        return AuthData(acc)
    }

    private fun saveFile(accountJson: File, accounts: Accounts) = accountJson.writeText(gson.toJson(accounts))
}
