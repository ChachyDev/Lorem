package club.chachy.lorem.launch.launch

import club.chachy.auth.base.account.AuthData
import club.chachy.lorem.config.LauncherConfig
import club.chachy.lorem.launch.Task
import club.chachy.lorem.launch.manifest.VersionJsonProvider
import club.chachy.lorem.utils.discoverValue
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

    private val props: List<AuthData.Property>,

    private val brand: LauncherConfig.LauncherBrand,

    private val closeHandlers: List<() -> Unit>,

    private val javaPath: String
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
                brand.name,
                brand.version
            )
            args[args.indexOf(it)] = newValue
        }

        val result = (arrayOf(javaPath) + args.toTypedArray())

        val builder = ProcessBuilder()
            .command(*result)
            .directory(gameDir)
            .redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)

        val process = builder.start()

        Runtime.getRuntime().addShutdownHook(thread(false) {
            File(gameDir, "natives/$version").deleteRecursively()
            process.destroy()
            closeHandlers.forEach { it.invoke() }
        })
        process.waitFor()
    }
}

