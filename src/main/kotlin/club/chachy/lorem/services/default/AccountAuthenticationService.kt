package club.chachy.lorem.services.default

import club.chachy.auth.base.account.AuthData
import club.chachy.auth.base.account.AuthType
import club.chachy.auth.base.account.AuthenticationData
import club.chachy.auth.mojang.MojangAuthHandler
import club.chachy.auth.ms.MicrosoftAuthHandler
import club.chachy.lorem.services.Service
import club.chachy.lorem.toUUID
import java.io.File

class AccountAuthenticationService(private val runDir: File, private val clientId: String?) : Service<AuthenticationData, AuthData> {
    override suspend fun executeTask(data: AuthenticationData): AuthData {
        return if (data.type == AuthType.Microsoft) {
            microsoft(data.username)
        } else {
            mojang(data.username, data.password ?: error("..."))
        }
    }

    private suspend fun microsoft(username: String?) = MicrosoftAuthHandler.login(runDir, clientId ?: error("Client ID cannot be null to execute Microsoft Authentication"), username)

    private suspend fun mojang(username: String, password: String) = MojangAuthHandler.login(runDir, username, password)
}