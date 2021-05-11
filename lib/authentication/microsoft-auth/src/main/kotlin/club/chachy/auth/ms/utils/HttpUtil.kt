package club.chachy.auth.ms.utils

import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*

val gson = Gson()

val http = HttpClient(Apache) {
    Json {
        serializer = GsonSerializer()
    }
}