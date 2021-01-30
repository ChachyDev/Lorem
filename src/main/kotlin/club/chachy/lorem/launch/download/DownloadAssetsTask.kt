package club.chachy.lorem.launch.download

import club.chachy.lorem.launch.Task
import club.chachy.lorem.launch.download.data.AssetMap
import club.chachy.lorem.launch.manifest.VersionJsonProvider
import club.chachy.lorem.utils.http.downloadAsync
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import java.io.File
import java.net.URL

class DownloadAssetsTask(private val runDir: File) : Task<VersionJsonProvider, Unit> {
    private val gson = Gson()

    override suspend fun execute(data: VersionJsonProvider) {
        val assetsFolder = File(runDir, "assets")
        assetsFolder.mkdir()
        val indexes = File(assetsFolder, "indexes")
        indexes.mkdir()
        val assetsJson = File(indexes, data.assetsIndex.id + ".json")
        downloadAsync(URL(data.assetsIndex.url), assetsJson).await()
        val assets = JsonParser.parseString(assetsJson.readText())
        val assetsMap = gson.fromJson(gson.toJson(assets.asJsonObject["objects"]), AssetMap::class.java)

        val objects = File(assetsFolder, "objects")

        objects.mkdir()
        coroutineScope {
            assetsMap.entries.map {
                val asset = it.value
                val hash = asset.hash.substring(0, 2)
                val url =
                    withContext(Dispatchers.IO) { URL("https://resources.download.minecraft.net/$hash/${asset.hash}") }
                val assetFolder = File(objects, hash)
                assetFolder.mkdir()
                val dest = File(assetFolder, asset.hash)
                if (dest.exists()) Unit
                async {
                    downloadAsync(url, dest)
                }
            }
        }.awaitAll()
    }
}