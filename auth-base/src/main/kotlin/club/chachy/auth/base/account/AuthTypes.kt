package club.chachy.auth.base.account

import java.io.File

/**
 * Compat layer
 */

data class AuthData(val token: String, val uuid: String, val username: String, val expiresOn: Long, val props: List<Property>) {
    constructor(account: club.chachy.auth.base.account.storage.Account) : this(account.token, account.uuid, account.username, account.expiresIn, account.properties)
}

data class User(val username: String, val properties: List<Property>?)

data class Property(val name: String, val value: String)

data class Profile(val id: String, val name: String)

data class Account(
    val username: String,
    val uuid: String,
    val token: String,
    val properties: List<Property>,
    val email: String // Username for emigrated accounts
)

data class AccountJson(val accounts: List<Account>)

data class AuthenticationData(val username: String, val password: String, val type: AuthType, val launchDir: File)
