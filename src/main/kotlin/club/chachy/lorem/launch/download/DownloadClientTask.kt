package club.chachy.lorem.launch.download

import club.chachy.lorem.launch.Task
import club.chachy.lorem.launch.manifest.ClientProperty
import club.chachy.lorem.utils.http.downloadAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class DownloadClientTask(private val runDir: File) : Task<Pair<ClientProperty, String>, Unit> {
    override suspend fun execute(data: Pair<ClientProperty, String>) {
        val dir = File(File(runDir, "versions"), data.second)
        val file = File(dir, data.second + ".jar")
        downloadAsync(withContext(Dispatchers.IO) { URL(data.first.url) }, file).await()
        if (file.length() != data.first.size) {
            error("Failed to successfully download client jar.")
        }
    }
}