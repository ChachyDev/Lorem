package club.chachy.lorem.utils

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.*
import java.io.File
import java.net.URL


val http = HttpClient(Apache) {
    Json {
        serializer = GsonSerializer()
    }
}

suspend fun downloadAsync(url: URL, dest: File): Deferred<Long> {
    return coroutineScope {
        async {
            if (dest.exists()) return@async 0
            withContext(Dispatchers.IO) { url.openStream() }.buffered().use {
                dest.outputStream().use { file ->
                    it.copyTo(file)
                }
            }
        }
    }
}