package club.chachy.lorem.launch.manifest

interface VersionJsonProvider {
    val assetsIndex: AssetsIndex

    val minimumLauncherVersion: Int

    val assets: String

    val client: ClientProperty

    val id: String

    val libraries: List<Library>

    val logging: Logging

    val mainClass: String

    val arguments: Array<String>

    val type: String
}

data class AssetsIndex(val id: String, val sha1: String, val size: Long, val totalSize: Long, val url: String)

data class ClientProperty(val sha1: String, val size: Long, val url: String)

data class Library(
    val path: String,
    val sha1: String,
    val size: Long,
    val url: String,
    val isAllowed: Boolean,
    val isNative: Boolean,
)

data class Logging(
    val isClient: Boolean,
    val argument: String,
    val id: String,
    val sha1: String,
    val size: Long,
    val url: String,
    val type: String
)