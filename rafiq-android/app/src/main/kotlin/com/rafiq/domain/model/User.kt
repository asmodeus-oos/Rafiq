package com.rafiq.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// Custom resilient serializers that accept null, wrong types, or missing keys without throwing SerializationException

object SafeStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SafeString", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
    override fun deserialize(decoder: Decoder): String {
        return try {
            decoder.decodeString()
        } catch (e: Exception) { "" }
    }
}

object SafeIntSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SafeInt", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: Int) = encoder.encodeInt(value)
    override fun deserialize(decoder: Decoder): Int {
        return try {
            decoder.decodeInt()
        } catch (e: Exception) { 0 }
    }
}

object SafeLongSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SafeLong", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Long) = encoder.encodeLong(value)
    override fun deserialize(decoder: Decoder): Long {
        return try {
            decoder.decodeLong()
        } catch (e: Exception) { 0L }
    }
}

object SafeBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SafeBoolean", PrimitiveKind.BOOLEAN)
    override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeBoolean(value)
    override fun deserialize(decoder: Decoder): Boolean {
        return try {
            decoder.decodeBoolean()
        } catch (e: Exception) { false }
    }
}

object SafeListStringSerializer : KSerializer<List<String>> {
    private val delegate = ListSerializer(String.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor
    override fun serialize(encoder: Encoder, value: List<String>) = delegate.serialize(encoder, value)
    override fun deserialize(decoder: Decoder): List<String> {
        return try {
            delegate.deserialize(decoder)
        } catch (e: Exception) { emptyList() }
    }
}

object SafeMapStringSerializer : KSerializer<Map<String, String>> {
    private val delegate = MapSerializer(String.serializer(), String.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor
    override fun serialize(encoder: Encoder, value: Map<String, String>) = delegate.serialize(encoder, value)
    override fun deserialize(decoder: Decoder): Map<String, String> {
        return try {
            delegate.deserialize(decoder)
        } catch (e: Exception) { emptyMap() }
    }
}

object GenderSerializer : KSerializer<Gender> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Gender", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Gender) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder): Gender {
        return try {
            val str = decoder.decodeString()
            Gender.values().firstOrNull { it.name.equals(str, ignoreCase = true) } ?: Gender.OTHER
        } catch (e: Exception) { Gender.OTHER }
    }
}

object OnlineStatusSerializer : KSerializer<OnlineStatus> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OnlineStatus", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: OnlineStatus) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder): OnlineStatus {
        return try {
            val str = decoder.decodeString()
            OnlineStatus.values().firstOrNull { it.name.equals(str, ignoreCase = true) } ?: OnlineStatus.OFFLINE
        } catch (e: Exception) { OnlineStatus.OFFLINE }
    }
}

object TierSerializer : KSerializer<Tier> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Tier", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Tier) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder): Tier {
        return try {
            val str = decoder.decodeString()
            Tier.values().firstOrNull { it.name.equals(str, ignoreCase = true) } ?: Tier.FREE
        } catch (e: Exception) { Tier.FREE }
    }
}

object RelationshipGoalSerializer : KSerializer<RelationshipGoal> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RelationshipGoal", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: RelationshipGoal) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder): RelationshipGoal {
        return try {
            val str = decoder.decodeString()
            RelationshipGoal.values().firstOrNull { it.name.equals(str, ignoreCase = true) } ?: RelationshipGoal.FRIENDSHIP
        } catch (e: Exception) { RelationshipGoal.FRIENDSHIP }
    }
}

object ReligionSerializer : KSerializer<Religion> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Religion", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Religion) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder): Religion {
        return try {
            val str = decoder.decodeString()
            Religion.values().firstOrNull { it.name.equals(str, ignoreCase = true) } ?: Religion.PREFER_NOT_TO_SAY
        } catch (e: Exception) { Religion.PREFER_NOT_TO_SAY }
    }
}

object EducationSerializer : KSerializer<Education> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Education", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Education) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder): Education {
        return try {
            val str = decoder.decodeString()
            Education.values().firstOrNull { it.name.equals(str, ignoreCase = true) } ?: Education.OTHER
        } catch (e: Exception) { Education.OTHER }
    }
}

@Serializable(with = GenderSerializer::class)
enum class Gender { MALE, FEMALE, NON_BINARY, OTHER }

@Serializable(with = OnlineStatusSerializer::class)
enum class OnlineStatus { ONLINE, OFFLINE, IN_CALL, IDLE, DND }

@Serializable(with = TierSerializer::class)
enum class Tier { FREE, GOLD, PLATINUM, DIAMOND, ELITE }

@Serializable(with = RelationshipGoalSerializer::class)
enum class RelationshipGoal { FRIENDSHIP, DATING, MARRIAGE, NETWORKING, GAMING, STUDY, TRAVEL }

@Serializable(with = ReligionSerializer::class)
enum class Religion { ISLAM, CHRISTIANITY, JUDAISM, HINDUISM, BUDDHISM, OTHER, PREFER_NOT_TO_SAY }

@Serializable(with = EducationSerializer::class)
enum class Education { HIGH_SCHOOL, BACHELORS, MASTERS, PHD, OTHER }

@Serializable
@SerialName("personality_matrix")
data class PersonalityMatrix(
    val lifestyle: Float = 0.5f,  // 0: Morning person, 1: Night owl
    val intellect: Float = 0.5f,  // 0: Creative, 1: Analytical
    val ambition: Float = 0.5f,   // 0: Relaxed, 1: Career-driven
    val social: Float = 0.5f,     // 0: Introvert, 1: Extrovert
    val values: Float = 0.5f      // 0: Traditional, 1: Progressive
)

@Serializable
data class Location(val latitude: Double = 0.0, val longitude: Double = 0.0)

@Serializable
data class User(
    @Serializable(with = SafeStringSerializer::class) val id: String = "",
    @Serializable(with = SafeStringSerializer::class) val name: String = "",
    @Serializable(with = SafeStringSerializer::class) val username: String = "",
    @Serializable(with = SafeIntSerializer::class) val age: Int = 0,
    val gender: Gender = Gender.OTHER,
    @SerialName("avatar_url") @Serializable(with = SafeStringSerializer::class) val avatar: String = "",
    @Serializable(with = SafeStringSerializer::class) val bio: String = "",
    @SerialName("isverified") @Serializable(with = SafeBooleanSerializer::class) val isVerified: Boolean = false,
    @SerialName("onlinestatus") val onlineStatus: OnlineStatus = OnlineStatus.OFFLINE,
    val tier: Tier = Tier.FREE,
    @Serializable(with = SafeLongSerializer::class) val diamonds: Long = 0,
    @SerialName("is_admin") @Serializable(with = SafeBooleanSerializer::class) val isOwner: Boolean = false,
    @Serializable(with = SafeListStringSerializer::class) val hobbies: List<String> = emptyList(),
    @Serializable(with = SafeStringSerializer::class) val country: String = "",
    @Serializable(with = SafeStringSerializer::class) val governorate: String = "",
    @Transient @Serializable(with = SafeStringSerializer::class) val city: String = "",
    @Serializable(with = SafeStringSerializer::class) val phone: String = "",
    @Serializable(with = SafeMapStringSerializer::class) val links: Map<String, String> = emptyMap(),
    @SerialName("showage") @Serializable(with = SafeBooleanSerializer::class) val showAge: Boolean = true,
    @SerialName("showlocation") @Serializable(with = SafeBooleanSerializer::class) val showLocation: Boolean = true,
    @SerialName("followers_count") @Serializable(with = SafeIntSerializer::class) val followersCount: Int = 0,
    @SerialName("following_count") @Serializable(with = SafeIntSerializer::class) val followingCount: Int = 0,
    @SerialName("cover_url") @Serializable(with = SafeStringSerializer::class) val coverPhoto: String = "",
    
    // Dating & Compatibility Expansion (Marked @Transient so they don't break queries against public.users table)
    @Transient val relationshipGoal: RelationshipGoal = RelationshipGoal.FRIENDSHIP,
    @Transient val religion: Religion = Religion.PREFER_NOT_TO_SAY,
    @Transient val education: Education = Education.OTHER,
    @Transient val height: Int = 0, // in cm
    @Transient val personality: PersonalityMatrix = PersonalityMatrix(),
    @Transient val lastActive: Long = 0L,
    @Transient val hasVerifiedBadge: Boolean = false,
    @Transient val profileStrength: Int = 0 // 0-100
)
