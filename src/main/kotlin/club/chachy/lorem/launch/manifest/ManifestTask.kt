package club.chachy.lorem.launch.manifest

import club.chachy.lorem.launch.Task
import club.chachy.lorem.launch.manifest.data.MojangManifestResponse
import club.chachy.lorem.utils.downloadAsync
import club.chachy.lorem.utils.http
import com.google.gson.JsonParser
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.net.URL
import kotlin.system.measureNanoTime

const val MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json"

class ManifestTask(private val runDir: File, private val jvmArgs: MutableList<String>) :
    Task<String, VersionJsonProvider> {
    private val logger: Logger = LogManager.getLogger(this)

    override suspend fun execute(data: String): VersionJsonProvider {
        val res = http.get<MojangManifestResponse>(MANIFEST_URL)
        val version = res.versions.find { it.id == data } ?: error("Failed to find version: $data")
        val versionFolder = File(File(runDir, "versions"), version.id)
        versionFolder.mkdirs()
        val versionFile = File(versionFolder, version.id + ".json")
        val url = withContext(Dispatchers.IO) { URL(version.url) }
        val time = measureNanoTime { downloadAsync(url, versionFile).await() }
        logger.info("Took ${time / 1000000.0}ms to download ${version.id} manifest.")
        val file = JsonParser.parseString(versionFile.readText()).asJsonObject
        return DefaultVersionJsonProvider(file["minimumLauncherVersion"].asInt, file, runDir, jvmArgs)
    }
}