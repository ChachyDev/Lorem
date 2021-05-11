package club.chachy.auth.ms.data

import com.google.gson.annotations.SerializedName

data class MicrosoftAuthResponse(@SerializedName("access_token") val accessToken: String)
