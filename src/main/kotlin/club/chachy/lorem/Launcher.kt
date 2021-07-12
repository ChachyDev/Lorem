package club.chachy.lorem

import club.chachy.auth.base.account.AuthData
import club.chachy.lorem.config.LauncherConfig
import club.chachy.lorem.launch.download.DownloadAssetsTask
import club.chachy.lorem.launch.download.DownloadClientTask
import club.chachy.lorem.launch.download.DownloadLibrariesTask
import club.chachy.lorem.launch.launch.LaunchTask
import club.chachy.lorem.launch.manifest.CustomManifestTask
import club.chachy.lorem.launch.manifest.ManifestTask
import club.chachy.lorem.launch.manifest.VersionJsonProvider
import club.chachy.lorem.progress.event.*
import club.chachy.lorem.utils.launchCoroutine
import club.chachy.lorem.utils.toUUID
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class Launcher(private val config: LauncherConfig) {
    suspend fun launch() {
        config.runDir.mkdir()

        if (config.monitor.onProgress(LoginEvent(config.authenticationService))) return
        val authData =
            config.authenticationService.executeTask(
                AuthData.AuthenticationData(
                    config.username,
                    config.password,
                    config.runDir
                )
            )

        val manifest = findManifest(config.isCustomMinecraft) ?: return

        if (!config.isCustomMinecraft) {
            launchCoroutine("Download Coroutine") {
                listOf(
                    async {
                        with(DownloadLibrariesTask(config.nativesDir, config.librariesDir)) {
                            if (config.monitor.onProgress(DownloadLibrariesEvent(this, manifest))) return@async
                            executeTask(manifest)
                        }
                    },
                    async {
                        with(DownloadAssetsTask(config.runDir)) {
                            if (config.monitor.onProgress(DownloadAssetsEvent(this, manifest))) return@async
                            executeTask(manifest)
                        }
                    },
                    async {
                        with(DownloadClientTask(config.runDir)) {
                            val clientVersionPair = manifest.client to manifest.id

                            if (config.monitor.onProgress(
                                    DownloadClientEvent(
                                        this,
                                        clientVersionPair.first,
                                        clientVersionPair.second
                                    )
                                )
                            ) return@async
                            executeTask(clientVersionPair)
                        }
                    }
                ).awaitAll()
            }.join()
        } else {
            with(DownloadClientTask(config.runDir)) {
                val clientVersionPair = manifest.client to manifest.id

                if (config.monitor.onProgress(
                        DownloadClientEvent(
                            this,
                            clientVersionPair.first,
                            clientVersionPair.second
                        )
                    )
                ) return
                executeTask(clientVersionPair)
            }
        }

        with(
            LaunchTask(
                authData.username,
                authData.uuid.toUUID(),
                authData.token,
                config.version,
                config.runDir,
                config.assetsDir,
                props = authData.props,
                nativesDirectory = config.nativesDir,
                brand = config.launcherBrand,
                closeHandlers = config.closeHandlers,
                javaPath = config.javaPath
            )
        ) {
            if (config.monitor.onProgress(LaunchEvent(this, config.version, config.nativesDir, config.assetsDir, config.javaPath, config.runDir))) return
            executeTask(manifest)
        }
    }

    private suspend fun findManifest(custom: Boolean): VersionJsonProvider? {
        return if (custom) {
            with(CustomManifestTask(config.runDir, config.jvmArgs)) {
                if (config.monitor.onProgress(FindManifestEvent(this))) return null
                executeTask(config.version)
            }
        } else {
            with(ManifestTask(config.runDir, config.jvmArgs)) {
                if (config.monitor.onProgress(FindManifestEvent(this))) return null
                executeTask(config.version)
            }
        }
    }
}

suspend fun main() {
    Launcher(
        LauncherConfig(
            "1.8.9",
            System.getProperty("lorem.username"),
            System.getProperty("lorem.password"),
            javaPath = "C:\\Program Files\\Java\\jdk-16.0.1\\bin\\java.exe"
        )
    ).launch()
}
