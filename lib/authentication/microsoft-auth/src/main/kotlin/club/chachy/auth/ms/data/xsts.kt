package club.chachy.auth.ms.data

import com.google.gson.annotations.SerializedName

data class XSTSAuthRequestBody(
    @SerializedName("Properties") val properties: XSTSProperties,
    @SerializedName("RelyingParty") val relyingParty: String,
    @SerializedName("TokenType") val tokenType: String
)

data class XSTSProperties(
    @SerializedName("SandboxId") val sandboxId: String,
    @SerializedName("UserTokens") val userTokens: List<String>
)