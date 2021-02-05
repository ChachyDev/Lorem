package club.chachy.auth.mojang.utils

import com.google.gson.Gson

val gson = Gson()

inline fun <reified T> createDataClass(string: String): T = gson.fromJson(string, T::class.java)