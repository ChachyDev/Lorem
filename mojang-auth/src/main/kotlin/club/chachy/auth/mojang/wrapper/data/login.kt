package club.chachy.auth.mojang.wrapper.data

import club.chachy.auth.base.account.Property
import com.google.gson.annotations.SerializedName

data class YggdrasilAuthenticationRequest(
    val agent: Agent,
    val username: String,
    val password: String,
    val requestUser: Boolean
)

data class Agent(val name: String, val version: Int)

data class YggdrasilAuthenticationResponse(
    val accessToken: String,
    val availableProfiles: List<AvailableProfile>,
    val selectedProfile: SelectedProfile,
    val user: User?
)

data class AvailableProfile(
    val agent: String,
    val id: String,
    val userId: String,
    val createdAt: Long,
    @SerializedName("legacyProfile") val isLegacyProfile: Boolean,
    @SerializedName("suspended") val isSuspended: Boolean,
    @SerializedName("paid") val isPaid: Boolean,
    @SerializedName("migrated") val isMigrated: Boolean,
    @SerializedName("legacy") val isLegacy: Boolean
)

data class SelectedProfile(
    val id: String,
    val name: String,
    val createdAt: Long,
    @SerializedName("legacyProfile") val isLegacyProfile: Boolean,
    @SerializedName("suspended") val isSuspended: Boolean,
    @SerializedName("paid") val isPaid: Boolean,
    @SerializedName("migrated") val isMigrated: Boolean,
    @SerializedName("legacy") val isLegacy: Boolean
)

data class User(
    val id: String,
    val email: String,
    val username: String,
    val registerIp: String,
    val migratedFrom: String?,
    val migratedAt: Long?,
    val registeredAt: Long,
    val passwordChangedAt: Boolean,
    val dateOfBirth: Boolean,
    @SerializedName("suspended") val isSuspended: Boolean,
    @SerializedName("blocked") val isBlocked: Boolean,
    @SerializedName("secured") val isSecured: Boolean,
    @SerializedName("migrated") val isMigrated: Boolean, // Don't use this, it will always return false, rather check if migratedFrom and migratedAt are not null
    @SerializedName("emailVerified") val isEmailVerified: Boolean,
    @SerializedName("legacyUser") val isLegacyUser: Boolean,
    val properties: List<Property>
)