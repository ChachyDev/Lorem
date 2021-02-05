package club.chachy.auth.base.account.storage

import club.chachy.auth.base.account.Property

data class Accounts(val accounts: MutableList<Account>)

data class Account(val username: String, val uuid: String, val token: String, val expiresIn: Long, val properties: List<Property>)