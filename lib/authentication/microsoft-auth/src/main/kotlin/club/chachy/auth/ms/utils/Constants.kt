package club.chachy.auth.ms.utils

const val MICROSOFT_OAUTH = "https://login.live.com/oauth20_authorize.srf" +
    "?client_id=%s" +
    "&response_type=code" +
    "&redirect_uri=%s" +
    "&scope=XboxLive.signin+offline_access"

const val TOKEN_URL = "http://localhost:8583/token?client_id=%s&code=%s"
