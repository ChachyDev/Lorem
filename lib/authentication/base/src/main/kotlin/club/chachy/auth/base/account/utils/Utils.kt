package club.chachy.auth.base.account.utils

import club.chachy.auth.base.account.storage.StorageAccounts
import com.google.gson.Gson

val gson = Gson()

inline fun <reified T> String.fromJson(): T = gson.fromJson(this, T::class.java)

fun readAccountStorage(content: String) = content.fromJson<StorageAccounts>()
