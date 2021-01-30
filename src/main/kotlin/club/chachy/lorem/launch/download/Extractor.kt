package club.chachy.lorem.launch.download

import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

object Extractor {
    suspend fun unzipAsync(
        zipFilePath: String,
        destDirectory: String,
    ): Deferred<Unit> {
        return coroutineScope {
            async {
                withContext(Dispatchers.IO) { ZipFile(zipFilePath) }.use { zip ->
                    zip.entries().asSequence().forEach { entry ->
                        if (entry.name.endsWith(".dll") || entry.name.endsWith(".so") || entry.name.endsWith(".dylib")) {
                            zip.getInputStream(entry).use { input ->
                                val file = File(destDirectory, entry.name)
                                if (file.exists()) return@async
                                file.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun extractFile(zipIn: ZipInputStream, filePath: String) {
        val out = FileOutputStream(filePath).buffered()
        zipIn.use { it.copyTo(out) }
        out.close()
    }
}

fun String.contains(collection: Collection<String>): Boolean {
    collection.forEach {
        if (contains(it)) return true
    }

    return false
}