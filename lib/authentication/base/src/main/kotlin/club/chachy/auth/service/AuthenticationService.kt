package club.chachy.auth.service

import club.chachy.auth.base.account.AuthData
import club.chachy.auth.base.account.storage.StorageAccounts
import club.chachy.auth.base.account.utils.gson
import club.chachy.auth.base.account.utils.readAccountStorage
import club.chachy.lorem.services.Service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

interface AuthenticationService : Service<AuthData.AuthenticationData, AuthData> {
    suspend fun execute(data: AuthData.AuthenticationData): AuthData

    override suspend fun executeTask(data: AuthData.AuthenticationData): AuthData {
        init(data)
        return execute(data)
    }

    override suspend fun init(data: AuthData.AuthenticationData) {
        val accountsDir = File(data.launchDir, "storage/accounts")
        accountJson = File(accountsDir, "accounts.json")
        accountsDir.mkdirs()

        withContext(Dispatchers.IO) { accountJson!!.createNewFile() }
    }

    suspend fun retrieveAuthData(
        data: AuthData.AuthenticationData,
        validate: suspend StorageAccounts.StorageAccount.() -> Boolean
    ): AuthData? {
        val text = File(data.launchDir, "storage/accounts/account.json").takeIf { it.exists() }?.readText()

        if (!text.isNullOrEmpty()) {
            println("Attempting to log in from saved session")
            val storage = readAccountStorage(text)
            val acc = storage.accounts.find { it.username == data.username || it.uuid == data.username }
            if (acc != null) {
                // Quickly validate the token
                if (validate(acc)) {
                    return AuthData(acc)
                }
                storage.accounts.removeIf { it.username == acc.token }
            }
        }

        return null
    }

    fun readAccountStorage(data: AuthData.AuthenticationData): StorageAccounts? {
        return readAccountStorage(
            accountJson?.takeIf { it.exists() }?.readText() ?: return null
        )
    }

    fun StorageAccounts.saveStorage(data: AuthData.AuthenticationData) {
        val file = accountJson?.takeIf { it.exists() } ?: return
        file.writeText(gson.toJson(this))
    }

    fun StorageAccounts.addAccount(account: StorageAccounts.StorageAccount) {
        accounts.removeIf { it.uuid == account.uuid }
        accounts.add(account)
    }

    companion object {
        private var accountJson: File? = null
    }
}