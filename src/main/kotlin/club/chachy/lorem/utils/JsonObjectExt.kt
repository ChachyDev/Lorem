package club.chachy.lorem.utils

import com.google.gson.JsonObject

fun JsonObject.getOrNull(key: String) = runCatching { get(key) }.getOrNull()