package club.chachy.auth.mojang

import club.chachy.auth.base.account.AccountJson
import club.chachy.auth.base.account.AuthData
import club.chachy.auth.base.account.Profile
import club.chachy.auth.base.account.User
import club.chachy.auth.base.account.storage.Account
import club.chachy.auth.base.account.storage.Accounts
import club.chachy.auth.mojang.utils.createDataClass
import club.chachy.auth.mojang.utils.gson
import club.chachy.auth.mojang.wrapper.YggdrasilAPIWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import java.io.File

// Expires in 100 years, so in other words it'll never expire.
const val EXPIRES_IN = 3153600000

object MojangAuthHandler {
    private val logger = LogManager.getLogger(this)

    suspend fun login(runDir: File, username: String?, password: String?): AuthData {
        val accountsDir = File(runDir, "storage/accounts")
        val accountsJson = File(accountsDir, "accounts.json")
        accountsDir.mkdirs()
        withContext(Dispatchers.IO) { accountsJson.createNewFile() }

        val text = if (accountsJson.exists()) accountsJson.readText() else null

        val account: Accounts

        if (!text.isNullOrEmpty()) {
            val attemptedRead = attemptReading(text, username)
            account = attemptedRead.first
            if (attemptedRead.second != null) {
                // Quickly validate the token
                val isValid = YggdrasilAPIWrapper.validate(attemptedRead.second?.token ?: error("Token is null..."))
                if (isValid) {
                    return AuthData(attemptedRead.second ?: error("Account is null..."))
                }
                account.accounts.removeIf { it.token == attemptedRead.second?.token ?: error("Token is null...") }
            }
        } else {
            account = Accounts(ArrayList())
        }

        logger.info("Authenticating with Mojang servers as your token is invalid...")

        if (username == null || password == null) {
            error("Username and Password required to authenticate.")
        }

        val authenticate = YggdrasilAPIWrapper.authenticate(username, password, true)

        val acc = Account(authenticate.selectedProfile.name, authenticate.selectedProfile.id, authenticate.accessToken, EXPIRES_IN, authenticate.user?.properties ?: listOf())

        account.accounts.add(acc)

        saveFile(accountsJson, account)

        return AuthData(acc)
    }

    private fun attemptReading(content: String, username: String?): Pair<Accounts, Account?> {
        val accounts = createDataClass<Accounts>(content)
        if (accounts.accounts.size > 1 && username != null) return accounts to null

        return accounts to if (accounts.accounts.size == 1) accounts.accounts[0] else accounts.accounts.find { it.username == username }
    }

    private fun saveFile(accountJson: File, accounts: Accounts) = accountJson.writeText(gson.toJson(accounts))
}

data class MojangAuthenticationResponse(val accessToken: String, val availableProfiles: List<Profile>, val user: User)

