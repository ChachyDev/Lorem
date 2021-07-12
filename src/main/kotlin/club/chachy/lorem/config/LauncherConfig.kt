package club.chachy.lorem.config

import club.chachy.auth.base.account.AuthType
import java.io.File

data class LauncherConfig(
    var authType: AuthType,
    var version: String,
    var username: String,
    var password: String,
    var microsoftClientId: String? = null,
    var jvmArgs: MutableList<String> = mutableListOf(),
    var isSplitInstances: Boolean = false,
    var launcherBrand: LauncherBrand = LauncherBrand("Lorem", "0.1.1"),
    var isCustomMinecraft: Boolean = false,
    var closeHandlers: List<() -> Unit> = emptyList(),
    var javaPath: String = "javaw",
    var launcherDirectoryName: String = "lorem",
    var runDir: File = File(if (isSplitInstances) "$launcherDirectoryName/$version" else launcherDirectoryName),
    var nativesDir: File = File(runDir, "natives"),
    var librariesDir: File = File(runDir, "libraries"),
    var assetsDir: File = File(runDir, "assets"),
) {
    data class LauncherBrand(val name: String, val version: String)
}