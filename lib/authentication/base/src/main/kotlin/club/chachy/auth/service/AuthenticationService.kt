package club.chachy.auth.service

import club.chachy.auth.base.account.AuthData
import club.chachy.auth.base.account.AuthenticationData
import club.chachy.lorem.services.Service

interface AuthenticationService : Service<AuthenticationData, AuthData>