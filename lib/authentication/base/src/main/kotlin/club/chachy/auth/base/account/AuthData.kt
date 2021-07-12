package club.chachy.auth.base.account

import club.chachy.auth.base.account.storage.AccountStorage.StorageAccount
import java.io.File

data class AuthData(
    val token: String,
    val uuid: String,
    val username: String,
    val expiresOn: Long,
    val props: List<Property>
) {
    constructor(account: StorageAccount) : this(
        account.token,
        account.uuid,
        account.username,
        account.expiresIn,
        account.properties
    )

    data class Property(val name: String, val value: String)

    data class AuthenticationData(val username: String, val password: String?, val launchDir: File)
}
