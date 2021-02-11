package club.chachy.lorem

import club.chachy.auth.base.account.AuthType
import club.chachy.auth.base.account.AuthenticationData
import club.chachy.lorem.launch.download.DownloadAssetsTask
import club.chachy.lorem.launch.download.DownloadClientTask
import club.chachy.lorem.launch.download.DownloadLibrariesTask
import club.chachy.lorem.launch.launch.LaunchTask
import club.chachy.lorem.launch.manifest.ManifestTask
import club.chachy.lorem.services.default.AccountAuthenticationService
import club.chachy.lorem.utils.toUUID
import org.apache.logging.log4j.LogManager
import java.io.File
import java.util.*

class Launcher(launcher: Launcher.() -> Unit) {
    private val logger = LogManager.getLogger(this)

    // Config values
    var authType = AuthType.Mojang
    var version = ""
    var microsoftClientId = "365ddfea-60da-4095-a1a9-55802b143ac1"
    var username: String = ""
    var password: String? = null
    var jvmArgs = arrayOf<String>()
    var isSeparateMinecraftDirectoriesPerVersion = false
    var launcherName: String? = null
    var launcherVersion: String? = null

    init {
        apply(launcher)
    }

    private var runDir = File(if (isSeparateMinecraftDirectoriesPerVersion) "lorem/$version" else "lorem")

    private var authService = AccountAuthenticationService(runDir, microsoftClientId)

    suspend fun begin() {
        logger.info("Running pre-checks to check if we can launch!")
        if (authType == AuthType.Mojang && password?.isEmpty() == true || authType == AuthType.Mojang && username.isEmpty()) error("Please specifiy a username and password")

        logger.info("Preparing for launch")
        runDir.mkdir()

        logger.info("Logging in!")
        val authData =
            authService.executeTask(AuthenticationData(username, password, authType, runDir))
        logger.info("Nice you got your password correct! Username: ${authData.username}, UUID: ${authData.uuid}")
        logger.info("Preparing Game files...")
        logger.info("Fetching manifest...")
        val manifest = ManifestTask(runDir, jvmArgs.toMutableList()).execute(version)
        logger.info("Downloading client...")
        DownloadClientTask(runDir).execute(manifest.client to manifest.id)
        logger.info("Downloading required libraries...")
        DownloadLibrariesTask(runDir).execute(manifest)
        logger.info("Downloading assets...")
        DownloadAssetsTask(runDir).execute(manifest)
        logger.info("The season finale has come, launch time!")
        LaunchTask(
            authData.username,
            authData.uuid.toUUID(),
            authData.token,
            version,
            runDir,
            File(runDir, "assets"),
            props = authData.props,
            nativesDirectory = File(runDir, "natives"),
            launcherName = launcherName ?: "Lorem",
            launcherVersion = launcherVersion ?: VERSION
        ).execute(manifest)
    }

    companion object {
        const val VERSION = "0.0.1"
    }
}

