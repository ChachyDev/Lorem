package club.chachy.auth.base.account.utils

import club.chachy.auth.base.account.storage.Account
import club.chachy.auth.base.account.storage.Accounts
import com.google.gson.Gson

val gson = Gson()

inline fun <reified T> createDataClass(string: String): T = gson.fromJson(string, T::class.java)

fun attemptReading(content: String, username: String?): Pair<Accounts, Account?> {
    val accounts = createDataClass<Accounts>(content)
    if (accounts.accounts.size > 1 && username != null) return accounts to null

    return accounts to if (accounts.accounts.size == 1) accounts.accounts[0] else accounts.accounts.find { it.username == username }
}