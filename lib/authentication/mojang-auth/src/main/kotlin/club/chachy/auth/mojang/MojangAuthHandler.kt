package club.chachy.auth.mojang

import club.chachy.auth.base.account.AuthData
import club.chachy.auth.base.account.storage.AccountStorage
import club.chachy.auth.service.AuthenticationService
import club.chachy.yggdrasil.wrapper.Yggdrasil
import org.apache.logging.log4j.LogManager

// Expires in 100 years, so in other words it'll never expire.
const val EXPIRES_IN = 3153600000

object MojangAuthHandler : AuthenticationService {
    private val logger = LogManager.getLogger(this)

    override suspend fun execute(data: AuthData.AuthenticationData): AuthData {
        return retrieveAuthData(data) { Yggdrasil.validate(token) } ?: let {
            logger.info("Authenticating with Mojang servers as your token is invalid...")

            val authenticate =
                Yggdrasil.authenticate(
                    data.username,
                    data.password ?: error("You must specify a password to log in"),
                    true
                )

            val account = AccountStorage.StorageAccount(
                authenticate.selectedProfile.name,
                authenticate.selectedProfile.id,
                authenticate.accessToken,
                EXPIRES_IN,
                authenticate.user?.properties ?: listOf()
            )

            with(readAccountStorage(data)) {
                addAccount(account)
                saveStorage(data)
            }

            return AuthData(account)
        }
    }
}
