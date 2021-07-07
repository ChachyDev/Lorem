package club.chachy.lorem.config

import club.chachy.auth.base.account.AuthType

data class LauncherConfig(
    var authType: AuthType,
    var version: String,
    var username: String,
    var password: String,
    var microsoftClientId: String? = null,
    var jvmArgs: MutableList<String> = mutableListOf(),
    var isSplitInstances: Boolean = false,
    var launcherBrand: LauncherBrand = LauncherBrand("Lorem", "0.1.0"),
    var isCustomMinecraft: Boolean = false,
    var closeHandlers: List<() -> Unit> = emptyList()
) {
    data class LauncherBrand(val name: String, val version: String)
}