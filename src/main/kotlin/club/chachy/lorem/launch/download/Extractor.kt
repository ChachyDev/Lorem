package club.chachy.lorem.launch.download

import kotlinx.coroutines.*
import java.io.File
import java.util.zip.ZipFile

object Extractor {
    private val nativeExtensions = arrayOf("dll", "so", "dylib")

    suspend fun unzipAsync(
        zipFilePath: String,
        destinationDirectory: String,
    ): Deferred<Unit> {
        return coroutineScope {
            async {
                withContext(Dispatchers.IO) {
                    ZipFile(zipFilePath).let { zip ->
                        // Get all the entries that are a valid native
                        val entries = zip.entries().asSequence().filter {
                            // Check if the extension is a "native" (.dll, .so, .dylib)
                            nativeExtensions.contains(it.name.substringAfterLast("."))
                        }

                        entries.forEach { entry ->
                            val file = File(destinationDirectory, entry.name)
                            if (!file.exists()) {
                                // Write the bytes from the zip to the destination
                                zip.getInputStream(entry).use {
                                    file.writeBytes(it.readBytes())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun String.contains(collection: Collection<String>) = collection.any { contains(it) }
