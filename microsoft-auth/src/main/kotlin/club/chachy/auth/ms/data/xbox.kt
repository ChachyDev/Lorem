package club.chachy.auth.ms.data

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class XboxLiveAuthRequestBody(
    val Properties: Properties,
    val RelyingParty: String,
    val TokenType: String
)

data class Properties(
    val AuthMethod: String,
    val SiteName: String,
    val RpsTicket: String
)

data class XboxLiveAuthResponse(
    val Token: String,
    val DisplayClaims: DisplayClaims
)

data class DisplayClaims(val xui: List<JsonObject>)

data class LoginWithXboxRequest(val identityToken: String)

data class LoginWithXboxResponse(@SerializedName("access_token") val accessToken: String, @SerializedName("expires_in") val expiresIn: Long)
