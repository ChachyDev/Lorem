package club.chachy.lorem.launch.launch

import club.chachy.auth.base.account.Property
import club.chachy.lorem.launch.Task
import club.chachy.lorem.launch.manifest.VersionJsonProvider
import club.chachy.lorem.utils.discoverValue
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.concurrent.thread

class LaunchTask(
    private val username: String,

    private val uuid: UUID,

    private val accessToken: String,

    private val version: String,

    private val gameDir: File,

    private val assets: File,

    private val userType: String = "mojang",

    private val nativesDirectory: File,

    private val props: List<Property>,

    private val launcherName: String,

    private val launcherVersion: String,

    private val closeHandlers: List<() -> Unit>
) : Task<VersionJsonProvider, Unit> {
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun execute(data: VersionJsonProvider) {
        val args = data.arguments.toMutableList()
        args.forEach {
            val newValue = it.discoverValue(
                username,
                uuid,
                accessToken,
                version,
                gameDir,
                assets,
                userType,
                props,
                data.assetsIndex.id,
                nativesDirectory,
                data.type,
                launcherName,
                launcherVersion
            )
            args[args.indexOf(it)] = newValue
        }

        val result = (arrayOf("java") + args.toTypedArray())

        val builder = ProcessBuilder()
            .command(*result)
            .directory(gameDir)
            .redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)

        val process = withContext(Dispatchers.IO) { builder.start() }
        Runtime.getRuntime().addShutdownHook(thread(false) { process.destroy() })
        process.waitFor()
        coroutineScope {
            File(gameDir, "natives").listFiles()?.map { async { it.delete() } }
        }?.awaitAll()
        closeHandlers.forEach { it.invoke() }
    }
}

