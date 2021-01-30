package club.chachy.lorem.launch.download

import club.chachy.lorem.launch.Task
import club.chachy.lorem.launch.manifest.VersionJsonProvider
import club.chachy.lorem.utils.http.downloadAsync
import kotlinx.coroutines.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.net.URL
import kotlin.system.measureNanoTime

class DownloadLibrariesTask(private val runDir: File) : Task<VersionJsonProvider, Unit> {
    private val logger: Logger = LogManager.getLogger(this)

    override suspend fun execute(data: VersionJsonProvider) {
        val libs = data.libraries
        coroutineScope {
            val l = libs.map {
                async {
                    val path = File(File(runDir, it.takeIf { it.isNative }?.let { "natives" } ?: "libraries"), it.path)
                    path.parentFile.mkdirs()
                    downloadAsync(withContext(Dispatchers.IO) { URL(it.url) }, path)
                }
            }

            val time = measureNanoTime { l.awaitAll() }

            logger.info("Took ${time / 1000000}ms to download ${libs.size} librar${if (libs.size != 1) "ies" else "y"}")

            val natives = libs.filter { it.isNative }.map {
                async {
                    val path = File(File(runDir, "natives"), it.path)
                    Extractor.unzipAsync(path.absolutePath, File(runDir, "natives").absolutePath)
                }
            }

            val time1 = measureNanoTime { natives.awaitAll() }

            logger.info("Took ${time1 / 1000000}ms to extract ${natives.size} nativ${if (natives.size != 1) "es" else "e"}")
        }
    }
}