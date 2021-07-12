package club.chachy.lorem.progress.event

import club.chachy.auth.base.account.AuthData
import club.chachy.auth.service.AuthenticationService
import club.chachy.lorem.launch.manifest.ClientProperty
import club.chachy.lorem.launch.manifest.VersionJsonProvider
import club.chachy.lorem.services.Service
import java.io.File

data class LaunchEvent(
    override val task: Service<VersionJsonProvider, Unit>,
    val version: String,
    val nativesDir: File,
    val assetsDir: File,
    val javaPath: String,
    val runDir: File
) :
    ProgressEvent<VersionJsonProvider, Unit>(task)

@Suppress("MemberVisibilityCanBePrivate")
data class LoginEvent(val authenticationService: AuthenticationService) :
    ProgressEvent<AuthData.AuthenticationData, AuthData>(authenticationService)

data class FindManifestEvent(override val task: Service<String, VersionJsonProvider>) :
    ProgressEvent<String, VersionJsonProvider>(task)

data class DownloadLibrariesEvent(
    override val task: Service<VersionJsonProvider, Unit>,
    val manifest: VersionJsonProvider
) : ProgressEvent<VersionJsonProvider, Unit>(task)

data class DownloadAssetsEvent(
    override val task: Service<VersionJsonProvider, Unit>,
    val manifest: VersionJsonProvider
) : ProgressEvent<VersionJsonProvider, Unit>(task)

data class DownloadClientEvent(
    override val task: Service<Pair<ClientProperty, String>, Unit>,
    val client: ClientProperty,
    val version: String
) : ProgressEvent<Pair<ClientProperty, String>, Unit>(task)
