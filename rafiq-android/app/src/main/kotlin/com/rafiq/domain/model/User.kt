package com.rafiq.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class Gender { MALE, FEMALE, NON_BINARY, OTHER }
@Serializable
enum class OnlineStatus { ONLINE, OFFLINE, IN_CALL }
@Serializable
enum class Tier { FREE, GOLD, PLATINUM, DIAMOND }

@Serializable
data class Location(val latitude: Double, val longitude: Double)

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val age: Int = 0,
    val gender: Gender = Gender.OTHER,
    @SerialName("avatar_url") val avatar: String = "",
    val bio: String = "",
    @SerialName("isverified") val isVerified: Boolean = false,
    @SerialName("onlinestatus") val onlineStatus: OnlineStatus = OnlineStatus.OFFLINE,
    val tier: Tier = Tier.FREE,
    val diamonds: Long = 0,
    @SerialName("is_admin") val isOwner: Boolean = false,
    val hobbies: List<String> = emptyList(),
    val country: String = "",
    val governorate: String = "",
    val phone: String = "",
    val links: Map<String, String> = emptyMap(),
    @SerialName("showage") val showAge: Boolean = true,
    @SerialName("showlocation") val showLocation: Boolean = true,
    @SerialName("followers_count") val followersCount: Int = 0,
    @SerialName("following_count") val followingCount: Int = 0,
    @SerialName("cover_url") val coverPhoto: String = ""
)
