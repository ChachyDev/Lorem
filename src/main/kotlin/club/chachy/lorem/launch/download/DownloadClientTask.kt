package club.chachy.lorem.launch.download

import club.chachy.lorem.launch.Task
import club.chachy.lorem.launch.manifest.ClientProperty
import club.chachy.lorem.utils.downloadAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class DownloadClientTask(private val runDir: File) : Task<Pair<ClientProperty, String>, Unit> {
    override suspend fun executeTask(data: Pair<ClientProperty, String>) {
        val versionInfo = data.first
        val version = data.second

        val versionDir = File(File(runDir, "versions"), version)
        val versionJar = File(versionDir, "$version.jar")

        // Download the jar
        withContext(Dispatchers.IO) {
            URL(versionInfo.url)
        }.let { url ->
            downloadAsync(url, versionJar).await()
        }

        // Make sure that we received the correct file
        if (versionJar.length() != versionInfo.size) error("Failed to successfully download client jar. Expected ${versionInfo.size}. Got ${versionJar.length()}")
    }
}
