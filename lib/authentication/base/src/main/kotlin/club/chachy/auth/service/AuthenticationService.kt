package club.chachy.auth.service

import club.chachy.auth.base.account.AuthData
import club.chachy.auth.base.account.storage.AccountStorage
import club.chachy.auth.base.account.utils.fromJson
import club.chachy.auth.base.account.utils.gson
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
        validate: suspend AccountStorage.StorageAccount.() -> Boolean
    ): AuthData? {
        val text = File(data.launchDir, "storage/accounts/account.json").takeIf { it.exists() }?.readText()

        if (!text.isNullOrEmpty()) {
            println("Attempting to log in from saved session")
            val storage = text.fromJson<AccountStorage>()
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

    fun readAccountStorage(data: AuthData.AuthenticationData): AccountStorage {
        return accountJson?.takeIf { it.exists() }?.readText()?.fromJson<AccountStorage>() ?: createAccountStorage()
    }

    fun createAccountStorage(): AccountStorage {
        return AccountStorage(mutableListOf())
    }

    fun AccountStorage.saveStorage(data: AuthData.AuthenticationData) {
        val file = accountJson?.takeIf { it.exists() } ?: return
        file.writeText(gson.toJson(this))
    }

    fun AccountStorage.addAccount(account: AccountStorage.StorageAccount) {
        accounts.removeIf { it.uuid == account.uuid }
        accounts.add(account)
    }

    companion object {
        private var accountJson: File? = null
    }
}