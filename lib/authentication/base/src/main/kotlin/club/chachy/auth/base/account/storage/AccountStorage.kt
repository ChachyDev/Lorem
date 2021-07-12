package club.chachy.auth.base.account.storage

import club.chachy.auth.base.account.AuthData

data class AccountStorage(val accounts: MutableList<StorageAccount>) {
    data class StorageAccount(
        val username: String,
        val uuid: String,
        val token: String,
        val expiresIn: Long,
        val properties: List<AuthData.Property>
    )
}