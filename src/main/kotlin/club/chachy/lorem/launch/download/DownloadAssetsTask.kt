package club.chachy.lorem.launch.download

import club.chachy.lorem.launch.Task
import club.chachy.lorem.launch.download.data.AssetMap
import club.chachy.lorem.launch.manifest.VersionJsonProvider
import club.chachy.lorem.utils.http.download
import club.chachy.lorem.utils.http.downloadAsync
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class DownloadAssetsTask(private val runDir: File) : Task<VersionJsonProvider, Unit> {
    override suspend fun execute(data: VersionJsonProvider) {
        // Make necessary directories
        val assetsFolder = File(runDir, "assets")
        val indexesFolder = File(assetsFolder, "indexes")
        val objectsFolder = File(assetsFolder, "objects")
        val assetsJsonFile = File(indexesFolder, data.assetsIndex.id + ".json")

        assetsFolder.mkdir()
        indexesFolder.mkdir()
        objectsFolder.mkdir()

        // Download the asset json
        withContext(Dispatchers.IO) {
            URL(data.assetsIndex.url)
        }.let {
            downloadAsync(it, assetsJsonFile).await()
        }

        val assetsJson = JsonParser.parseString(assetsJsonFile.readText())
        val assetsMap = Gson().fromJson(assetsJson.asJsonObject["objects"], AssetMap::class.java)

        // Download all assets
        coroutineScope {
            assetsMap.entries.filter {
                !File(objectsFolder, it.value.shortHash).exists()
            }.map {
                // Get asset info and hash
                val asset = it.value
                val hash = asset.shortHash

                // Make the asset folder
                val assetFolder = File(objectsFolder, hash)
                assetFolder.mkdir()

                // Prepare the destination for the asset
                val dest = File(assetFolder, asset.hash)

                // Download the asset
                withContext(Dispatchers.IO) {
                    URL("https://resources.download.minecraft.net/$hash/${asset.hash}")
                }.let { url ->
                    downloadAsync(url, dest)
                }
            }.awaitAll()
        }
    }
}
