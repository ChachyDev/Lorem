package club.chachy.lorem

import club.chachy.auth.base.account.AuthData
import club.chachy.auth.base.account.AuthType
import club.chachy.auth.mojang.MojangAuthHandler
import club.chachy.auth.ms.MicrosoftAuthHandler
import club.chachy.auth.service.AuthenticationService
import club.chachy.lorem.config.LauncherConfig
import club.chachy.lorem.launch.download.DownloadAssetsTask
import club.chachy.lorem.launch.download.DownloadClientTask
import club.chachy.lorem.launch.download.DownloadLibrariesTask
import club.chachy.lorem.launch.launch.LaunchTask
import club.chachy.lorem.launch.manifest.CustomManifestTask
import club.chachy.lorem.launch.manifest.ManifestTask
import club.chachy.lorem.launch.manifest.VersionJsonProvider
import club.chachy.lorem.utils.launchCoroutine
import club.chachy.lorem.utils.toUUID
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.apache.logging.log4j.LogManager
import java.io.File

class Launcher(private val config: LauncherConfig) {
    private val logger = LogManager.getLogger(this)

    private var runDir = File(if (config.isSplitInstances) "lorem/${config.version}" else "lorem")

    private var authService = findAuthService(config.authType)

    suspend fun launch() {
        logger.info("Preparing for launch")
        runDir.mkdir()

        logger.info("Logging in!")
        val authData =
            authService.executeTask(
                AuthData.AuthenticationData(
                    config.username,
                    config.password,
                    config.authType,
                    runDir
                )
            )

        logger.info("Nice you got your password correct! Username: ${authData.username}, UUID: ${authData.uuid}")
        logger.info("Preparing Game files...")
        logger.info("Fetching manifest...")

        val manifest = findManifestTask(config.isCustomMinecraft)

        if (!config.isCustomMinecraft) {
            launchCoroutine("Download Coroutine") {
                listOf(
                    async {
                        logger.info("Downloading Libraries")
                        DownloadLibrariesTask(runDir).execute(manifest)
                    },
                    async {
                        logger.info("Downloading Assets")
                        DownloadAssetsTask(runDir).execute(manifest)
                    },
                    async {
                        logger.info("Downloading Client")
                        DownloadClientTask(runDir).execute(manifest.client to manifest.id)
                    }
                ).awaitAll()
            }.join()
        } else {
            DownloadLibrariesTask(runDir).execute(manifest)
        }

        logger.info("Downloading required libraries...")
        logger.info("The season finale has come, launch time!")

        LaunchTask(
            authData.username,
            authData.uuid.toUUID(),
            authData.token,
            config.version,
            runDir,
            File(runDir, "assets"),
            props = authData.props,
            nativesDirectory = File(runDir, "natives"),
            brand = config.launcherBrand,
            closeHandlers = config.closeHandlers
        ).execute(manifest)
    }

    private suspend fun findManifestTask(custom: Boolean): VersionJsonProvider {
        return if (custom) {
            CustomManifestTask(runDir, config.jvmArgs).execute(config.version)
        } else {
            ManifestTask(runDir, config.jvmArgs).execute(config.version)
        }
    }

    private fun findAuthService(authType: AuthType): AuthenticationService {
        return if (authType == AuthType.Microsoft) {
            MicrosoftAuthHandler(config.microsoftClientId ?: error("No Microsoft Client ID passed"))
        } else {
            MojangAuthHandler
        }
    }
}

suspend fun main() {
    Launcher(
        LauncherConfig(
            AuthType.Mojang,
            "1.8.9",
            System.getProperty("lorem.username"),
            System.getProperty("lorem.password")
        )
    ).launch()
}
