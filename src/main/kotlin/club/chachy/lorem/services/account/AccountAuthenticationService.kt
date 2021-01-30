package club.chachy.lorem.services.account

import club.chachy.lorem.services.Service
import club.chachy.lorem.services.default.Property
import java.io.File

data class AuthData(val token: String, val uuid: String, val username: String, val props: List<Property>)

interface AccountAuthenticationService : Service<AuthenticationData, AuthData>

data class AuthenticationData(val username: String, val password: String, val type: AuthType, val launchDir: File)