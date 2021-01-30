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
        val libraries = data.libraries

        coroutineScope {
            // Download required libraries / natives
            val downloads = libraries.map {
                async {
                    // Get the path for the file depending on if it's a native or a library
                    val path = File(File(runDir, if (it.isNative) "natives" else "libraries"), it.path)
                    path.parentFile.mkdirs()

                    withContext(Dispatchers.IO) {
                        URL(it.url)
                    }.let {
                        downloadAsync(it, path)
                    }
                }
            }

            val libraryTime = measureNanoTime { downloads.awaitAll() }
            logger.info("Took ${libraryTime / 1000000}ms to download ${libraries.size} librar${if (libraries.size != 1) "ies" else "y"}")

            // Extract natives
            val natives = libraries.filter { it.isNative }.map {
                async {
                    val nativePath = File(File(runDir, "natives"), it.path)
                    Extractor.unzipAsync(nativePath.absolutePath, File(runDir, "natives").absolutePath)
                }
            }

            val nativesTime = measureNanoTime { natives.awaitAll() }
            logger.info("Took ${nativesTime / 1000000}ms to extract ${natives.size} nativ${if (natives.size != 1) "es" else "e"}")
        }
    }
}