package club.chachy.yggdrasil.wrapper.data

import club.chachy.auth.base.account.AuthData


data class YggdrasilRefreshRequest(
    val accessToken: String,
    val selectedProfile: RefreshSelectedProfile,
    val requestUser: Boolean
)

data class RefreshSelectedProfile(val id: String, val name: String)

data class YggdrasilRefreshResponse(
    val accessToken: String,
    val selectedProfile: RefreshSelectedProfile,
    val user: RefreshUser?
)

data class RefreshUser(
    val id: String,
    val properties: List<AuthData.Property>
)