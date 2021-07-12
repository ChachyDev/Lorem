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
    @Deprecated("Providing a SHA1 is now deprecated", level = DeprecationLevel.WARNING)
    val sha1: String,
    @Deprecated("Providing a size is now deprecated", level = DeprecationLevel.WARNING)
    val size: Long = 0,
    val url: String?,
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