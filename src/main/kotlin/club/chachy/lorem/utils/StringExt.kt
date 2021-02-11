package club.chachy.lorem.utils

import club.chachy.auth.base.account.Property
import com.google.gson.Gson
import java.io.File
import java.util.*

private val gson = Gson()


fun String.replacePlaceholder(place: String, new: String) = replace("\${$place}", new).replace("{$place}", new)

fun String.discoverValue(
    username: String,
    uuid: UUID,
    accessToken: String,
    version: String,
    gameDir: File,
    assets: File,
    userType: String,
    props: List<Property>,
    assetsIndex: String,
    nativesDir: File,
    versionType: String,
    launcherName: String,
    launcherVersion: String
): String {
    return replacePlaceholder("auth_player_name", username)
        .replacePlaceholder("version_name", version)
        .replacePlaceholder("game_directory", gameDir.absolutePath)
        .replacePlaceholder("assets_root", assets.absolutePath)
        .replacePlaceholder("assets_index_name", assetsIndex)
        .replacePlaceholder("auth_uuid", uuid.toString())
        .replacePlaceholder("auth_access_token", accessToken)
        .replacePlaceholder("user_properties", gson.toJson(props))
        .replacePlaceholder("natives_directory", nativesDir.absolutePath)
        .replacePlaceholder("user_type", userType)
        .replacePlaceholder("launcher_name", launcherName)
        .replacePlaceholder("version_type", versionType)
        .replacePlaceholder("launcher_version", launcherVersion)
}

private val dashesRegex = "(.{8})(.{4})(.{4})(.{4})(.{12})".toRegex()

fun String.toUUID(): UUID = UUID.fromString(replace(dashesRegex, "\$1-\$2-\$3-\$4-\$5"))