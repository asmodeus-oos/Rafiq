## 🚀 RAFIQ - Complete Production Social Dating & Entertainment App

### Comprehensive Architecture & Feature Roadmap (Kotlin/Jetpack Compose)

---

### 📋 TABLE OF CONTENTS

1. [Project Overview & Vision](#1-project-overview--vision)
2. [Technology Stack](#2-technology-stack)
3. [App Architecture](#3-app-architecture)
4. [Core Features Breakdown](#4-core-features-breakdown)
5. [Monetization & Tier System](#5-monetization--tier-system)
6. [Database Schema](#6-database-schema)
7. [API Endpoints](#7-api-endpoints)
8. [Implementation Roadmap](#8-implementation-roadmap)
9. [Security & Compliance](#9-security--compliance)
10. [Performance Optimization](#10-performance-optimization)

---

### 1. PROJECT OVERVIEW & VISION

#### **Product Definition**

**RAFIQ** is a production-grade social, dating, and entertainment platform combining:

- **Real-time chat** with voice messages, images, stickers, emojis, and gifts
- **Random matching** for voice/video calls (peer-to-peer via WebRTC)
- **Social discovery** with recommendations based on gender, age, location, interests
- **Tiered economy** (Free/Gold/Platinum) with exclusive features
- **Content sharing** (posts, stories, live streams)
- **Safety features** (blocking, reporting, verification)

#### **Target Users**

- Ages 18-45, primarily mobile-first
- Seeking social connection, dating, and entertainment
- Privacy-conscious with safety concerns
- Ready to monetize through premium tiers and virtual currency

#### **Core Values**

- ✅ **Safety First:** Verification, reporting, blocking, content moderation
- ✅ **Inclusivity:** All gender preferences and orientations supported
- ✅ **Privacy:** End-to-end encryption for chats, secure token handling
- ✅ **Performance:** Sub-100ms latency for real-time features
- ✅ **Monetization:** Sustainable freemium model with clear value props

---

### 2. TECHNOLOGY STACK

#### **Frontend (Client)**

```
Language:           Kotlin
UI Framework:       Jetpack Compose
Architecture:       MVVM + Clean Architecture
State Management:   ViewModel + StateFlow / LiveData
Navigation:         Jetpack Navigation (NavGraph)
Design System:      Material Design 3 + Liquid Glass
```

#### **Real-time & Networking**

```
WebSocket:          OkHttp + Scarlet (or Socket.IO client)
WebRTC:             WebRTC Android (peerconnection, datachannel)
Signaling Server:   Node.js + Socket.IO
Media Server:       Mediasoup (for group calls/rooms)
```

#### **Backend (Server)**

```
Runtime:            Node.js 20 LTS
Framework:          Express.js / NestJS
Database:           MongoDB 7.0 (user data, chats, posts)
Cache:              Redis (sessions, presence, rate limiting)
Message Queue:      RabbitMQ (background jobs)
File Storage:       AWS S3 / Google Cloud Storage
Video/Audio:        Mediasoup for WebRTC peer management
Push Notifications: Firebase Cloud Messaging (FCM)
```

#### **DevOps & Deployment**

```
Containerization:   Docker + Docker Compose
Orchestration:      Kubernetes (or Firebase/Railway for MVP)
CI/CD:              GitHub Actions / GitLab CI
Monitoring:         Prometheus + Grafana
Logging:            ELK Stack (Elasticsearch, Logstash, Kibana)
```

#### **Analytics & Security**

```
Analytics:          Firebase Analytics / Mixpanel
Crash Reporting:    Firebase Crashlytics
Security:           OAuth 2.0, JWT, TLS 1.3
2FA:                TOTP (Google Authenticator)
Content Moderation: AWS Rekognition / Clarifai AI
```

---

### 3. APP ARCHITECTURE

#### **3.1 Directory Structure (Kotlin)**

```
rafiq-android/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── kotlin/com/rafiq/
│   │   │   ├── RafiqApp.kt (Application class)
│   │   │   ├── di/ (Dependency Injection - Hilt)
│   │   │   │   ├── AppModule.kt
│   │   │   │   ├── NetworkModule.kt
│   │   │   │   ├── DatabaseModule.kt
│   │   │   │   └── RepositoryModule.kt
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── database/
│   │   │   │   │   │   ├── RafiqDatabase.kt
│   │   │   │   │   │   └── dao/
│   │   │   │   │   │       ├── UserDao.kt
│   │   │   │   │   │       ├── ChatDao.kt
│   │   │   │   │   │       ├── MessageDao.kt
│   │   │   │   │   │       ├── CallLogDao.kt
│   │   │   │   │   │       └── PostDao.kt
│   │   │   │   │   └── preferences/
│   │   │   │   │       ├── UserPreferences.kt
│   │   │   │   │       └── SettingsPreferences.kt
│   │   │   │   ├── remote/
│   │   │   │   │   ├── api/
│   │   │   │   │   │   ├── RafiqApiClient.kt (Retrofit)
│   │   │   │   │   │   └── endpoints/
│   │   │   │   │   │       ├── UserApi.kt
│   │   │   │   │   │       ├── ChatApi.kt
│   │   │   │   │   │       ├── CallApi.kt
│   │   │   │   │   │       ├── PostApi.kt
│   │   │   │   │   │       └── GiftApi.kt
│   │   │   │   │   └── socket/
│   │   │   │   │       ├── SocketManager.kt
│   │   │   │   │       └── SocketListener.kt
│   │   │   │   └── repository/
│   │   │   │       ├── UserRepository.kt
│   │   │   │       ├── ChatRepository.kt
│   │   │   │       ├── CallRepository.kt
│   │   │   │       ├── PostRepository.kt
│   │   │   │       ├── RecommendationRepository.kt
│   │   │   │       └── GiftRepository.kt
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.kt
│   │   │   │   │   ├── Chat.kt
│   │   │   │   │   ├── Message.kt
│   │   │   │   │   ├── Call.kt
│   │   │   │   │   ├── Post.kt
│   │   │   │   │   ├── Gift.kt
│   │   │   │   │   ├── TierBenefit.kt
│   │   │   │   │   └── RandomCallMatch.kt
│   │   │   │   └── usecase/
│   │   │   │       ├── GetRecommendationsUseCase.kt
│   │   │   │       ├── SendMessageUseCase.kt
│   │   │   │       ├── StartRandomCallUseCase.kt
│   │   │   │       ├── MatchRandomCallUseCase.kt
│   │   │   │       └── SkipRandomCallUseCase.kt
│   │   │   ├── presentation/
│   │   │   │   ├── navigation/
│   │   │   │   │   ├── RafiqNavGraph.kt
│   │   │   │   │   └── Route.kt
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Type.kt
│   │   │   │   │   ├── Shape.kt
│   │   │   │   │   ├── Monet.kt (Dynamic color extraction)
│   │   │   │   │   └── LiquidGlass.kt (Glassmorphism)
│   │   │   │   ├── screen/
│   │   │   │   │   ├── splash/
│   │   │   │   │   │   ├── SplashScreen.kt
│   │   │   │   │   │   └── SplashViewModel.kt
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   ├── LoginScreen.kt
│   │   │   │   │   │   ├── SignupScreen.kt
│   │   │   │   │   │   ├── VerificationScreen.kt
│   │   │   │   │   │   └── AuthViewModel.kt
│   │   │   │   │   ├── home/
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   ├── HomeViewModel.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── RecommendedUserCard.kt
│   │   │   │   │   │       └── QuickActionsBar.kt
│   │   │   │   │   ├── chat/
│   │   │   │   │   │   ├── ChatListScreen.kt
│   │   │   │   │   │   ├── ChatDetailScreen.kt
│   │   │   │   │   │   ├── ChatViewModel.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── MessageBubble.kt
│   │   │   │   │   │       ├── VoiceMessagePlayer.kt
│   │   │   │   │   │       ├── StickerPicker.kt
│   │   │   │   │   │       ├── GiftSender.kt
│   │   │   │   │   │       └── ChatInputField.kt
│   │   │   │   │   ├── call/
│   │   │   │   │   │   ├── CallInitiationScreen.kt
│   │   │   │   │   │   ├── IncomingCallScreen.kt
│   │   │   │   │   │   ├── ActiveCallScreen.kt
│   │   │   │   │   │   ├── CallViewModel.kt
│   │   │   │   │   │   ├── RandomCallMatchingScreen.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── CallControlsOverlay.kt
│   │   │   │   │   │       ├── VideoFrame.kt
│   │   │   │   │   │       └── CallEndDialog.kt
│   │   │   │   │   ├── discovery/
│   │   │   │   │   │   ├── DiscoveryScreen.kt
│   │   │   │   │   │   ├── DiscoveryViewModel.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── UserCardSwiper.kt
│   │   │   │   │   │       └── FilterPanel.kt
│   │   │   │   │   ├── posts/
│   │   │   │   │   │   ├── PostFeedScreen.kt
│   │   │   │   │   │   ├── CreatePostScreen.kt
│   │   │   │   │   │   ├── PostDetailScreen.kt
│   │   │   │   │   │   ├── PostViewModel.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── PostCard.kt
│   │   │   │   │   │       └── CommentSection.kt
│   │   │   │   │   ├── profile/
│   │   │   │   │   │   ├── ProfileScreen.kt
│   │   │   │   │   │   ├── EditProfileScreen.kt
│   │   │   │   │   │   ├── ProfileViewModel.kt
│   │   │   │   │   │   ├── VisitorsScreen.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── ProfileHeader.kt
│   │   │   │   │   │       ├── ProfileBadges.kt
│   │   │   │   │   │       └── GalleryTab.kt
│   │   │   │   │   ├── shop/
│   │   │   │   │   │   ├── CoinShopScreen.kt
│   │   │   │   │   │   ├── GiftCatalogScreen.kt
│   │   │   │   │   │   ├── VIPSubscriptionScreen.kt
│   │   │   │   │   │   ├── ShopViewModel.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── CoinPackage.kt
│   │   │   │   │   │       ├── GiftCard.kt
│   │   │   │   │   │       └── VIPBenefitsList.kt
│   │   │   │   │   ├── settings/
│   │   │   │   │   │   ├── SettingsScreen.kt
│   │   │   │   │   │   ├── AppearanceScreen.kt
│   │   │   │   │   │   ├── PrivacyScreen.kt
│   │   │   │   │   │   ├── NotificationScreen.kt
│   │   │   │   │   │   ├── SettingsViewModel.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── ThemeSelector.kt
│   │   │   │   │   │       └── PermissionCard.kt
│   │   │   │   │   └── admin/
│   │   │   │   │       ├── AdminPanelScreen.kt
│   │   │   │   │       ├── ModerationScreen.kt
│   │   │   │   │       ├── AdminViewModel.kt
│   │   │   │   │       └── components/
│   │   │   │   │           ├── UserReportCard.kt
│   │   │   │   │           └── ContentModerationCard.kt
│   │   │   │   ├── components/
│   │   │   │   │   ├── common/
│   │   │   │   │   │   ├── LiquidGlassCard.kt
│   │   │   │   │   │   ├── DynamicAvatar.kt
│   │   │   │   │   │   ├── GradientButton.kt
│   │   │   │   │   │   ├── ShimmerLoader.kt
│   │   │   │   │   │   └── ModernBottomSheet.kt
│   │   │   │   │   └── bottom_nav/
│   │   │   │   │       └── RafiqBottomNavigation.kt
│   │   │   │   └── MainActivity.kt
│   │   │   ├── service/
│   │   │   │   ├── CallService.kt (Foreground service for calls)
│   │   │   │   ├── NotificationService.kt (FCM)
│   │   │   │   ├── LocationService.kt
│   │   │   │   └── MediaService.kt
│   │   │   ├── util/
│   │   │   │   ├── Constants.kt
│   │   │   │   ├── Extension.kt
│   │   │   │   ├── Logger.kt
│   │   │   │   ├── ImageUtils.kt
│   │   │   │   ├── FileUtils.kt
│   │   │   │   └── ValidationUtils.kt
│   │   │   └── worker/
│   │   │       ├── SyncWorker.kt (WorkManager)
│   │   │       └── CleanupWorker.kt
│   │   └── res/
│   │       ├── drawable/
│   │       ├── mipmap/
│   │       └── values/
│   ├── build.gradle.kts (App-level)
│   ├── proguard-rules.pro
│   └── ...
├── build.gradle.kts (Project-level)
├── settings.gradle.kts
└── gradle/
    ├── wrapper/
    └── libs.versions.toml
```

---

### 4. CORE FEATURES BREAKDOWN

#### **4.1 AUTHENTICATION & ONBOARDING**

##### **A. Registration Flow**

```kotlin
// SignupScreen.kt
// Step 1: Phone/Email verification
// Step 2: Create password (with strength meter)
// Step 3: Profile setup (name, age, gender, bio)
// Step 4: Photo upload (min 2, max 10)
// Step 5: Interests & preferences
// Step 6: Recommendation gender preference
// Step 7: Enable notifications
```

**Backend Validation:**

- ✅ Email format validation
- ✅ Phone number validation (E.164 format)
- ✅ Password strength (min 12 chars, upper, lower, digit, special)
- ✅ Age verification (18+ mandatory)
- ✅ Photo quality check (ML-based face detection)
- ✅ Duplicate phone/email prevention
- ✅ Rate limiting (5 attempts per 5 mins)

##### **B. 2FA & Security**

```kotlin
data class TwoFactorSetup(
    val method: TwoFactorMethod, // SMS, EMAIL, TOTP
    val enabled: Boolean,
    val backupCodes: List<String>
)

enum class TwoFactorMethod {
    SMS, EMAIL, TOTP
}
```

---

#### **4.2 CHAT & MESSAGING SYSTEM**

##### **A. Real-Time Chat Architecture**

```kotlin
// Message data model
data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val contentType: MessageType, // TEXT, IMAGE, VOICE, STICKER, EMOJI, GIFT
    val content: String,
    val mediaUrl: String?,
    val voiceMessageDuration: Int?, // seconds
    val reactions: Map<String, List<String>>, // emoji -> list of user IDs
    val replyTo: Message?,
    val timestamp: Long,
    val isRead: Boolean,
    val isEdited: Boolean,
    val editedAt: Long?,
    val deletedAt: Long?,
    val deliveryStatus: DeliveryStatus // SENT, DELIVERED, READ
)

enum class MessageType {
    TEXT, IMAGE, VOICE, STICKER, EMOJI, GIFT, CALL_INITIATED
}

enum class DeliveryStatus {
    SENDING, SENT, DELIVERED, READ, FAILED
}
```

##### **B. Chat Screen Features**

```kotlin
@Composable
fun ChatDetailScreen(
    chatId: String,
    viewModel: ChatViewModel = hiltViewModel()
) {
    // Features:
    // 1. Real-time message sync via Socket.IO
    // 2. Message typing indicators ("User is typing...")
    // 3. Read receipts (double checkmarks)
    // 4. Delivery status (sending animation → checkmark)
    // 5. Auto-scroll to newest message
    // 6. Pull-to-refresh older messages
    // 7. Message search
    // 8. Message reactions (long-press emoji picker)
    // 9. Message reply/quoting
    // 10. Message editing & deletion
}
```

##### **C. Rich Media Support**

```kotlin
// Voice Message Recording
@Composable
fun VoiceMessageRecorder(
    onRecordingComplete: (audioUri: Uri, duration: Int) -> Unit
) {
    // Features:
    // - Swipe to cancel
    // - Slide to lock recording (don't auto-send)
    // - Waveform animation during recording
    // - Preview playback before sending
    // - Max 120 seconds per message
    // - Opus codec (high compression)
}

// Image & File Sharing
data class SharedMedia(
    val type: MediaType, // PHOTO, VIDEO, DOCUMENT
    val uri: Uri,
    val thumbnail: Bitmap?,
    val mimeType: String,
    val sizeBytes: Long,
    val captionOptional: String? = null,
    val isEncrypted: Boolean = false
)

enum class MediaType {
    PHOTO, VIDEO, DOCUMENT, AUDIO
}
```

##### **D. Sticker & Emoji System**

```kotlin
data class Sticker(
    val id: String,
    val packId: String,
    val imageUrl: String, // WebP animated or static
    val isAnimated: Boolean,
    val categories: List<String>,
    val isFree: Boolean,
    val isPromotion: Boolean // Tier-exclusive
)

data class StickerPack(
    val id: String,
    val name: String,
    val preview: String,
    val stickers: List<Sticker>,
    val isNew: Boolean,
    val isFeatured: Boolean,
    val requiresTier: SubscriptionTier? = null
)

// Default free packs available to all
// Premium packs unlock at Gold tier
// Exclusive packs at Platinum tier
```

##### **E. Gift Sending in Chat**

```kotlin
data class Gift(
    val id: String,
    val name: String,
    val animationUrl: String, // Lottie JSON or WebP
    val price: Int, // coins
    val rarity: GiftRarity, // COMMON, RARE, EPIC, LEGENDARY
    val animationDurationMs: Int,
    val soundUrl: String? = null,
    val thumbnail: String,
    val tags: List<String>
)

enum class GiftRarity {
    COMMON, RARE, EPIC, LEGENDARY
}

// When gift sent:
// 1. Deduct coins from sender's wallet
// 2. Add 30% of gift value as coins to receiver's wallet
// 3. Display animated gift on chat screen
// 4. Notification to receiver
// 5. Gift history in profile "Glory" section
```

---

#### **4.3 RANDOM CALL MATCHING SYSTEM** ⭐

##### **A. Architecture Overview**

```
User clicks "Random Call"
     ↓
Check subscription tier
     ├─ Free: 1 call/day
     ├─ Gold: 10 calls/day
     └─ Platinum: 50 calls/day
     ↓
Enter waiting queue (Socket.IO)
     ↓
Server matches pairs
(same gender OR cross-gender based on preference)
     ↓
Both users get notification
     ↓
Accept/Decline (30 sec timeout)
     ↓
If both accept → WebRTC call initiated
If one declines → Moved to next match
```

##### **B. Data Models**

```kotlin
data class RandomCallQueue(
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val userAge: Int,
    val userGender: Gender,
    val preferredGender: Gender,
    val location: Location,
    val joinedAt: Long,
    val userPreferences: UserPreferences,
    val callCount: Int, // Today's call count
    val isVideo: Boolean // Audio or video call
)

data class UserPreferences(
    val recommendationGender: Gender,
    val ageRange: IntRange,
    val locationRadius: Int, // km
    val languages: List<String>
)

data class RandomCallMatch(
    val matchId: String,
    val user1: RandomCallQueue,
    val user2: RandomCallQueue,
    val initiatedAt: Long,
    val acceptanceTimeoutMs: Long = 30_000,
    val status: MatchStatus
)

enum class MatchStatus {
    PENDING_ACCEPTANCE,
    BOTH_ACCEPTED,
    ONE_DECLINED,
    TIMEOUT,
    CALL_ACTIVE,
    CALL_ENDED
}

data class CallDecision(
    val userId: String,
    val accepted: Boolean,
    val respondedAt: Long
)
```

##### **C. Random Call Screen**

```kotlin
@Composable
fun RandomCallMatchingScreen(
    callType: CallType, // AUDIO or VIDEO
    viewModel: RandomCallViewModel = hiltViewModel()
) {
    val state by viewModel.matchingState.collectAsState()
    
    when (state) {
        is MatchingState.Waiting -> {
            // Animated spinner
            // "Searching for a match..."
            // Show call count: "3/10 calls remaining today"
            // Cancel button
        }
        
        is MatchingState.MatchFound -> {
            // Show other user:
            // - Avatar (blur effect)
            // - Age
            // - Location distance
            // Accept/Decline buttons (30s countdown timer)
            // Can swipe right to accept, left to decline
        }
        
        is MatchingState.Matched -> {
            // Transition to ActiveCallScreen
        }
        
        is MatchingState.Declined -> {
            // "Match declined"
            // Auto-start searching for next
        }
        
        is MatchingState.Error -> {
            // Show error (no users online, limit reached, etc.)
            // Retry button
        }
    }
}
```

##### **D. Backend Matching Algorithm**

```javascript
// Node.js server logic (Pseudocode)

class RandomCallMatcher {
    matchUsers(user1, user2) {
        // Compatibility check
        if (!this.isCompatible(user1, user2)) return false
        
        // Location check (within radius)
        if (!this.checkDistance(user1, user2)) return false
        
        // Age range check
        if (!this.checkAgeRange(user1, user2)) return false
        
        // Gender preference check
        if (!this.checkGenderPreference(user1, user2)) return false
        
        // Block/report list check
        if (this.isBlocked(user1, user2)) return false
        
        // Call limit check
        if (!this.hasCallsRemaining(user1) || 
            !this.hasCallsRemaining(user2)) return false
        
        return true
    }
    
    async matchNextPair() {
        // Get oldest users in queue by tier priority
        // Try to match similar interests/age first
        // If no match in same region, expand search
        // Remove from queue on match
    }
    
    handleDecline(declineData) {
        // Put both users back in queue
        // Increment decline counter
        // If user declines >50% of matches → temporary cooldown
    }
}
```

##### **E. Skip/Decline Logic**

```kotlin
// User can decline & move to next match without penalty
// But if abuse detected (declining >80% of matches):
// - 5 min cooldown
// - Then 15 min
// - Then 1 hour
// - Eventually account flagged for review

data class SkipStatistics(
    val userId: String,
    val declineCount: Int,
    val acceptCount: Int,
    val declinePercentage: Float,
    val lastDeclineTime: Long,
    val cooldownUntil: Long?
)
```

---

#### **4.4 DISCOVERY & RECOMMENDATIONS**

##### **A. Recommendation Algorithm**

```kotlin
data class RecommendedUser(
    val id: String,
    val name: String,
    val age: Int,
    val gender: Gender,
    val avatar: String,
    val photos: List<String>,
    val bio: String,
    val interests: List<String>,
    val location: Location,
    val distance: Double, // km
    val matchScore: Float, // 0-100%
    val mutualFollows: Int,
    val isVerified: Boolean,
    val lastSeen: Long,
    val onlineStatus: OnlineStatus
)

// Scoring logic:
// - Gender preference: +30%
// - Age range match: +20%
// - Location proximity: +15%
// - Shared interests: +15%
// - Mutual connections: +10%
// - Verification status: +5%
// - Active profile: +5%
```

##### **B. Discovery Screen**

```kotlin
@Composable
fun DiscoveryScreen(
    viewModel: DiscoveryViewModel = hiltViewModel()
) {
    val recommendations by viewModel.recommendations.collectAsState()
    
    // Card-based swiper (Tinder-like)
    // - Swipe right: Like (add to favorites, they get notified)
    // - Swipe left: Skip
    // - Tap profile → Full profile view
    // - Bottom action bar: Filters, Sort, Random shuffle
    
    LazyColumn {
        items(recommendations) { user ->
            RecommendedUserCard(
                user = user,
                onLike = { viewModel.likeUser(user.id) },
                onSkip = { viewModel.skipUser(user.id) },
                onProfileTap = { navigateToProfile(user.id) }
            )
        }
    }
}

// Filter panel options:
// - Gender: Male/Female/Non-binary
// - Age range: Slider (18-65)
// - Location radius: 5-100 km
// - Verified only: Toggle
// - Online only: Toggle
// - Has photos: Toggle
// - Interests: Multi-select
// - Sorted by: Newest, Most liked, Closest, Most matches
```

##### **C. Tier-Based Recommendation Features**

```kotlin
// FREE TIER:
// - 20 recommendations/day
// - Basic filtering
// - No location distance
// - See who liked you (delayed by 24h)
// - No advanced matching

// GOLD TIER:
// - Unlimited recommendations
// - Advanced filtering
// - Exact location distance
// - See who liked you (real-time)
// - Rematch with previous users
// - Boost visibility (appear higher in others' lists for 1h)
// - No ads

// PLATINUM TIER:
// - All Gold features
// - Anonymous browsing (others can't see you browsed their profile)
// - Hide online status
// - Unlimited boosts (8 hour durations)
// - Priority matching (appear first in recommendations)
// - Super Like (1/day) - stand out with special badge
// - Rewind last action (undo last skip)
// - See all who viewed your profile (not just unseen)
// - Advanced analytics (who viewed, when, from where)
```

---

#### **4.5 VOICE & VIDEO CALLING**

##### **A. Call Initiation Flow**

```kotlin
// From Chat Screen
// - "Voice Call" button → Direct call to contact
// - "Video Call" button → Video call request
// - Other user gets notification (CallKit on iOS, ConnectionService on Android)

data class CallSession(
    val callId: String,
    val initiatorId: String,
    val receiverId: String,
    val callType: CallType, // AUDIO or VIDEO
    val startTime: Long,
    val endTime: Long?,
    val duration: Long?, // milliseconds
    val status: CallStatus,
    val recordingEnabled: Boolean = false,
    val isRandom: Boolean = false,
    val missedReason: MissedCallReason? = null
)

enum class CallType {
    AUDIO, VIDEO
}

enum class CallStatus {
    RINGING, ACCEPTED, ACTIVE, ENDED, MISSED, DECLINED, FAILED
}

enum class MissedCallReason {
    NOT_ANSWERED, DECLINED, NO_NETWORK, CALLER_CANCEL, CALLEE_BUSY
}
```

##### **B. Active Call Screen**

```kotlin
@Composable
fun ActiveCallScreen(
    callSession: CallSession,
    viewModel: CallViewModel = hiltViewModel()
) {
    val callState by viewModel.callState.collectAsState()
    val remoteVideo by viewModel.remoteVideoFrame.collectAsState()
    val localVideo by viewModel.localVideoFrame.collectAsState()
    
    // Layout options:
    // 1. Grid (both videos equal size)
    // 2. PiP (Picture-in-Picture - remote large, local small corner)
    // 3. Portrait (remote full, local small bottom)
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Remote video stream
        VideoFrame(
            videoTrack = remoteVideo,
            modifier = Modifier.fillMaxSize()
        )
        
        // Local video (PiP)
        if (callSession.callType == CallType.VIDEO) {
            VideoFrame(
                videoTrack = localVideo,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(12.dp))
            )
        }
        
        // Call duration timer
        CallDurationTimer(
            startTime = callSession.startTime,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )
        
        // Call controls (bottom overlay)
        CallControlsOverlay(
            onMuteMic = { viewModel.toggleMic() },
            onMuteVideo = { viewModel.toggleVideo() },
            onSpeaker = { viewModel.toggleSpeaker() },
            onEndCall = { viewModel.endCall() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

// Call Controls:
// - Mute/Unmute Mic (icon shows status)
// - Camera on/off (toggles local video)
// - Speaker/Earpiece toggle
// - Swap camera (front/back)
// - Screen share button (if supported)
// - Red "End Call" button
// - Notification badge for incoming messages during call
```

##### **C. WebRTC Implementation (Kotlin)**

```kotlin
class WebRTCManager(
    private val signalingService: SignalingService,
    private val context: Context
) {
    private lateinit var peerConnection: PeerConnection
    private val peerConnectionFactory: PeerConnectionFactory by lazy {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
        
        PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(rootEglContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglContext))
            .createPeerConnectionFactory()
    }
    
    fun initializeCall(
        remoteUserId: String,
        isInitiator: Boolean,
        isVideoCall: Boolean
    ) {
        // 1. Create offer/answer SDP
        // 2. Set local description
        // 3. Send via signaling server
        // 4. Handle ICE candidates
    }
    
    fun addLocalStream(isVideoCall: Boolean) {
        val audioSource = peerConnectionFactory.createAudioSource(AudioConstraints())
        val audioTrack = peerConnectionFactory.createAudioTrack("audio_track", audioSource)
        
        if (isVideoCall) {
            val surfaceTextureHelper = SurfaceTextureHelper.create(
                "CaptureThread", 
                rootEglContext
            )
            val videoCapturer = createCameraCapturer()
            val videoSource = peerConnectionFactory.createVideoSource(false)
            
            videoCapturer.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
            videoCapturer.startCapture(1280, 720, 30) // 1080p, 30fps
            
            val videoTrack = peerConnectionFactory.createVideoTrack("video_track", videoSource)
            localStream.addTrack(audioTrack)
            localStream.addTrack(videoTrack)
        } else {
            localStream.addTrack(audioTrack)
        }
        
        peerConnection.addStream(localStream)
    }
    
    fun handleRemoteStream(mediaStream: MediaStream) {
        // Add remote video/audio to UI
        val videoTrack = mediaStream.videoTracks.firstOrNull()
        val audioTrack = mediaStream.audioTracks.firstOrNull()
        
        _remoteVideoFrame.postValue(videoTrack)
        // Audio is handled by default
    }
}
```

##### **D. Signaling Protocol (Socket.IO)**

```kotlin
// Client sends to server
socket.emit("call.initiate", CallSignal(
    callId = UUID.randomUUID().toString(),
    initiatorId = currentUserId,
    receiverId = targetUserId,
    callType = CallType.VIDEO,
    offer = sdpOffer
))

// Server forwards to receiver
socket.to(receiverId).emit("call.incoming", CallSignal(...))

// Receiver responds with answer
socket.emit("call.answer", CallSignal(
    callId = callId,
    answer = sdpAnswer
))

// Both exchange ICE candidates
socket.emit("ice.candidate", IceCandidate(
    callId = callId,
    candidate = iceCandidate
))

// Call ends
socket.emit("call.end", CallEndData(
    callId = callId,
    reason = "user_hangup"
))
```

##### **E. Call Recording (Premium Feature)**

```kotlin
data class CallRecording(
    val recordingId: String,
    val callId: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Int, // seconds
    val storageUrl: String,
    val isEncrypted: Boolean,
    val canBeDeleted: Boolean, // Owner can delete after 30 days
    val privacy: RecordingPrivacy
)

enum class RecordingPrivacy {
    PRIVATE,          // Only participants can view
    SHARED_WITH_BOTH, // Both must agree to keep
    CLOUD_BACKUP      // Deleted locally after 30 days
}

// Only available to PLATINUM tier
// Requires explicit user consent at call start
// Both participants notified of recording
```

---

#### **4.6 SOCIAL POSTS & FEED**

##### **A. Post Creation**

```kotlin
data class Post(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String,
    val content: String,
    val mediaUrls: List<String>,
    val hashtags: List<String>,
    val mentions: List<String>,
    val visibility: PostVisibility, // PUBLIC, FOLLOWERS_ONLY, PRIVATE
    val allowComments: Boolean = true,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val createdAt: Long,
    val updatedAt: Long?,
    val deletedAt: Long?,
    val isPinned: Boolean = false,
    val isSponsored: Boolean = false,
    val sponsorshipTier: SubscriptionTier? = null
)

enum class PostVisibility {
    PUBLIC,          // Everyone sees
    FOLLOWERS_ONLY,  // Only followers
    PRIVATE,         // Only sender's close friends
    PAID_EXCLUSIVE   // Exclusive to Gold/Platinum followers (Platinum only feature)
}
```

##### **B. Post Feed Screen**

```kotlin
@Composable
fun PostFeedScreen(
    feedType: FeedType, // GLOBAL, FOLLOWING, TRENDING, NEARBY
    viewModel: PostViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier.fillMaxSize()
    ) {
        // Pull-to-refresh
        items(posts) { post ->
            PostCard(
                post = post,
                onLike = { viewModel.likePost(post.id) },
                onComment = { navigateToComments(post.id) },
                onShare = { sharePost(post.id) },
                onReport = { viewModel.reportPost(post.id) },
                onDelete = { if (post.authorId == currentUserId) viewModel.deletePost(post.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Pagination
        if (viewModel.hasMorePosts) {
            item {
                viewModel.loadMorePosts()
                CircularProgressIndicator()
            }
        }
    }
}
```

##### **C. Comments & Interactions**

```kotlin
data class Comment(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String,
    val content: String,
    val likeCount: Int = 0,
    val replyCount: Int = 0,
    val createdAt: Long,
    val updatedAt: Long?,
    val isEdited: Boolean = false
)

data class CommentReply(
    val id: String,
    val commentId: String,
    val authorId: String,
    val replyToUserId: String,
    val content: String,
    val likeCount: Int = 0,
    val createdAt: Long
)

// Comment features:
// - Threading (replies to comments)
// - Mention notifications (@username)
// - Like comments
// - Delete own comments
// - Report comments
// - Edit comments (max 5 min after posting)
```

##### **D. Hashtags & Trending**

```kotlin
data class Hashtag(
    val tag: String,
    val usageCount: Int,
    val trendingRank: Int?,
    val isTrending: Boolean,
    val postsCount: Int,
    val followersCount: Int
)

// Trending algorithm:
// - Posts with tag (24h window)
// - Weighted by likes, comments, shares
// - Filtered by geography
// - Daily recalculation at midnight
```

---

#### **4.7 PROFILE & USER IDENTITY**

##### **A. Profile Data**

```kotlin
data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val avatar: String,
    val coverPhoto: String,
    val gender: Gender,
    val age: Int,
    val birthDate: Long,
    val location: Location,
    val verified: Boolean,
    val badges: List<UserBadge>,
    val photos: List<UserPhoto>,
    val interests: List<String>,
    val languages: List<String>,
    val education: String?,
    val occupation: String?,
    val relationshipStatus: RelationshipStatus?,
    val lookingFor: List<String>,
    val followersCount: Int,
    val followingCount: Int,
    val postCount: Int,
    val visitorCount: Int,
    val lastVisitors: List<ProfileVisitor>,
    val phoneVerified: Boolean,
    val emailVerified: Boolean,
    val idVerified: Boolean,
    val createdAt: Long,
    val lastSeen: Long,
    val onlineStatus: OnlineStatus,
    val subscription: SubscriptionInfo
)

enum class UserBadge {
    VERIFIED,           // ID verified
    VIP,               // Paid subscription
    EARLY_ADOPTER,     // Joined in first 3 months
    TOP_CONTRIBUTOR,   // High-quality posts
    GIFT_RECEIVER,     // Received 50+ gifts
    POPULAR,           // 10k followers
    MODERATOR,         // Staff badge
    CELEBRITY          // Influencer partner
}

data class UserPhoto(
    val id: String,
    val url: String,
    val thumbnail: String,
    val uploadedAt: Long,
    val isVerified: Boolean, // Face matching with ID photo
    val order: Int,
    val isProfilePhoto: Boolean,
    val isNSFW: Boolean
)

data class ProfileVisitor(
    val userId: String,
    val username: String,
    val avatar: String,
    val visitedAt: Long,
    val isMutual: Boolean
)

enum class OnlineStatus {
    ONLINE,
    AWAY,
    DO_NOT_DISTURB,
    OFFLINE,
    INVISIBLE // Platinum feature - appear offline to everyone
}

enum class RelationshipStatus {
    SINGLE,
    IN_A_RELATIONSHIP,
    MARRIED,
    DIVORCED,
    WIDOWED,
    PREFER_NOT_TO_SAY
}
```

##### **B. Profile Screen**

```kotlin
@Composable
fun ProfileScreen(
    userId: String = currentUserId,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val isOwnProfile = userId == currentUserId
    
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Header: Cover photo + Avatar overlap
        item {
            ProfileHeader(
                profile = profile,
                onEditClick = { if (isOwnProfile) navigateToEdit() }
            )
        }
        
        // Badges & Status
        item {
            ProfileBadgesRow(badges = profile.badges)
        }
        
        // Bio & Location
        item {
            BioSection(
                bio = profile.bio,
                location = profile.location,
                occupation = profile.occupation
            )
        }
        
        // Stats bar
        item {
            StatsBar(
                followers = profile.followersCount,
                following = profile.followingCount,
                posts = profile.postCount,
                onFollowersTap = { showFollowersList() }
            )
        }
        
        // Action buttons
        if (!isOwnProfile) {
            item {
                ProfileActionButtons(
                    onMessageClick = { navigateToChat(userId) },
                    onCallClick = { initiateCall(userId) },
                    onFollowClick = { viewModel.toggleFollow(userId) },
                    onMoreClick = { showProfileMenu() }
                )
            }
        }
        
        // Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                tabs = {
                    Tab(text = { Text("Posts") })
                    Tab(text = { Text("Gallery") })
                    Tab(text = { Text("Glory") }) // Achievements
                }
            )
        }
        
        // Tab content
        when (selectedTabIndex) {
            0 -> items(profile.posts) { post ->
                PostCard(post = post)
            }
            1 -> GridPhotos(profile.photos)
            2 -> GloryTab(profile) // Gifts, achievements
        }
        
        // Visitors section (own profile only)
        if (isOwnProfile) {
            item {
                VisitorsSection(
                    visitors = profile.lastVisitors,
                    unreadCount = profile.unreadVisitors
                )
            }
        }
    }
}
```

##### **C. Glory Tab (Achievements)**

```kotlin
@Composable
fun GloryTab(profile: UserProfile) {
    // Showcase user's achievements:
    // - Received gifts (with count)
    // - Total gift value in coins
    // - Favorite gift type
    // - Achievement badges earned
    // - Followers milestone badges (100, 500, 1k, etc.)
    // - Longest streak of daily logins
    // - Most liked post
    // - Monthly ranking (top users by engagement)
    
    LazyColumn {
        item {
            Section(title = "Gifts Received") {
                GiftsReceivedGrid(profile.giftsReceived)
            }
        }
        
        item {
            Section(title = "Achievements") {
                AchievementBadgesGrid(profile.achievements)
            }
        }
        
        item {
            Section(title = "Statistics") {
                StatsCards(profile.statistics)
            }
        }
    }
}
```

---

#### **4.8 BLOCK & REPORT SYSTEM**

##### **A. Block Functionality**

```kotlin
data class BlockedUser(
    val id: String,
    val blockedUserId: String,
    val blockedAt: Long,
    val reason: BlockReason? = null,
    val isReported: Boolean = false
)

enum class BlockReason {
    HARASSMENT,
    SPAM,
    INAPPROPRIATE_CONTENT,
    FAKE_PROFILE,
    SCAM,
    OTHER
}

// Block effects:
// - Blocked user can't see profile
// - Blocked user's messages appear as "Message unavailable"
// - Can't call blocked user
// - Can't find them in search
// - Can unblock anytime
// - Unblock doesn't notify them
```

##### **B. Report & Moderation**

```kotlin
data class UserReport(
    val reportId: String,
    val reportedUserId: String,
    val reporterUserId: String,
    val reason: ReportReason,
    val description: String,
    val evidence: List<String>?, // Media URLs
    val reportedAt: Long,
    val status: ReportStatus = ReportStatus.PENDING,
    val resolution: String? = null,
    val resolvedAt: Long? = null
)

enum class ReportReason {
    HARASSMENT,
    HATE_SPEECH,
    SEXUAL_CONTENT,
    FAKE_PROFILE,
    SCAM_CATFISH,
    NUDITY,
    MINORS_INVOLVED,
    VIOLENCE,
    SPAM,
    OTHER
}

enum class ReportStatus {
    PENDING,
    UNDER_REVIEW,
    RESOLVED,
    DISMISSED,
    USER_BANNED
}

// Report process:
// 1. User submits report with evidence
// 2. Auto-flagging if multiple reports for same user
// 3. Moderator review (24h SLA)
// 4. Action taken (warning, suspend, ban)
// 5. Reported user notified (generic message)
// 6. Reporter notified of resolution (anonymous)
```

##### **C. Content Moderation Panel**

```kotlin
@Composable
fun ModerationScreen(
    viewModel: ModerationViewModel = hiltViewModel()
) {
    val pendingReports by viewModel.pendingReports.collectAsState()
    val suspiciousAccounts by viewModel.suspiciousAccounts.collectAsState()
    
    LazyColumn {
        // Quick stats
        item {
            ModerationStats(
                pendingCount = pendingReports.size,
                avgResolutionTime = viewModel.avgResolutionTime,
                banCount = viewModel.banCountThisMonth
            )
        }
        
        // Report queue
        items(pendingReports) { report ->
            ReportCard(
                report = report,
                onApproveAction = { viewModel.approveModeration(report.id) },
                onBanUser = { viewModel.banUser(report.reportedUserId) },
                onDismiss = { viewModel.dismissReport(report.id) }
            )
        }
    }
}
```

---

### 5. MONETIZATION & TIER SYSTEM

#### **5.1 Subscription Tiers**

##### **A. Tier Comparison Matrix**

```kotlin
data class TierBenefit(
    val name: String,
    val included: Boolean,
    val limitValue: Int? = null // e.g., "5 daily messages" → 5
)

data class SubscriptionTier(
    val tier: Tier,
    val displayName: String,
    val description: String,
    val monthlyPrice: Double, // USD
    val annualPrice: Double,
    val benefits: Map<String, TierBenefit>,
    val featured: Boolean = false,
    val color: Color,
    val icon: Int
)

enum class Tier(val id: String) {
    FREE("free"),
    GOLD("gold"),
    PLATINUM("platinum")
}

// FREE TIER
// - 1 random call/day
// - Basic chat features
// - 20 recommendations/day
// - Basic profile
// - 3-day message history
// - Ads enabled
// - No gift sends
// - Limited sticker packs
// - Can receive gifts (get 30% value as coins)

// GOLD TIER ($4.99/month or $39.99/year)
// - 10 random calls/day
// - Advanced chat features (voice messages, gifts)
// - Unlimited recommendations
// - Advanced filtering
// - Unlimited message history
// - No ads
// - Can send unlimited gifts
// - All premium sticker packs
// - Boost visibility 1x/week
// - See who liked you (real-time)
// - Profile badge (Gold tier indicator)
// - 1 Premium gift per week
// - Early access to new features
// - Ad-free experience

// PLATINUM TIER ($9.99/month or $79.99/year)
// - 50 random calls/day
// - All Gold features
// - Anonymous browsing
// - Hide online status
// - Unlimited boosts
// - Super Like 1x/day (stands out in recommendations)
// - Rewind last action
// - See all profile visitors
// - Advanced analytics
// - Priority matching
// - Exclusive platinum-only gifts
// - Platinum profile badge
// - Direct messaging priority (faster delivery)
// - Monthly premium gift box ($15 value)
// - 1-on-1 customer support
// - Exclusive platinum community
// - 2 premium gifts per week
```

##### **B. Subscription Management**

```kotlin
data class Subscription(
    val userId: String,
    val tier: Tier,
    val startDate: Long,
    val renewalDate: Long,
    val cancellationDate: Long?,
    val autoRenew: Boolean,
    val paymentMethod: PaymentMethod,
    val lastPaymentDate: Long,
    val lastPaymentAmount: Double,
    val status: SubscriptionStatus
)

enum class SubscriptionStatus {
    ACTIVE,
    CANCELLED,
    EXPIRED,
    PAST_DUE,
    PAUSED
}

enum class PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    GOOGLE_PLAY,
    APPLE_APP_STORE,
    PAYPAL,
    STRIPE
}

// Subscription screen features:
// - Current tier display
// - Renewal date countdown
// - Auto-renewal toggle
// - Upgrade/downgrade options
// - Billing history
// - Invoice download
// - Pause subscription (30-day hold)
// - Cancel with reason survey
// - Win-back offer (discount to re-subscribe)
```

---

#### **5.2 Virtual Currency System**

##### **A. Coins & Diamonds**

```kotlin
data class Wallet(
    val userId: String,
    val coins: Long = 0,
    val diamonds: Long = 0, // Premium currency, bought with real money
    val lastRefillDate: Long? = null,
    val lastSpendDate: Long,
    val totalSpentThisMonth: Long = 0
)

data class CoinPackage(
    val id: String,
    val coins: Long,
    val price: Double, // USD
    val bonus: Long = 0, // Extra coins
    val displayName: String,
    val icon: Int,
    val highlight: Boolean = false,
    val bestValue: Boolean = false
)

// Coin packages (example):
val COIN_PACKAGES = listOf(
    CoinPackage(id = "50", coins = 50, price = 0.99, bonus = 0, displayName = "50 Coins"),
    CoinPackage(id = "100", coins = 100, price = 1.99, bonus = 10, displayName = "110 Coins", highlight = true),
    CoinPackage(id = "500", coins = 500, price = 8.99, bonus = 75, displayName = "575 Coins", bestValue = true),
    CoinPackage(id = "1000", coins = 1000, price = 16.99, bonus = 200, displayName = "1,200 Coins"),
    CoinPackage(id = "5000", coins = 5000, price = 79.99, bonus = 1500, displayName = "6,500 Coins")
)

// Coin uses:
// - Send gifts (10-500 coins depending on rarity)
// - Unlock premium stickers
// - Boost profile visibility
// - Purchase premium features
// - Send voice messages (free now, paid later if feature gated)

// Coin acquisition:
// - Purchase with real money
// - Receive gifts (30% of gift value)
// - Daily login rewards (5-20 coins)
// - Referral bonuses (100 coins per new user)
// - Milestone achievements
// - Special events/promotions
```

##### **B. In-App Purchase Integration**

```kotlin
class InAppPurchaseManager(
    private val context: Context
) {
    private val billingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases?.forEach { processPurchase(it) }
            }
        }
        .enablePendingPurchases()
        .build()
    
    fun launchCoinShop() {
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder()
                .setProductList(COIN_PRODUCT_IDS)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, productDetailsList ->
            // Display available coin packages
        }
    }
    
    fun purchaseCoinPackage(productId: String) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }
    
    private fun processPurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Verify purchase on backend
            // Add coins to user's wallet
            // Acknowledge purchase
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            )
        }
    }
}
```

---

#### **5.3 VIP Subscription Screen**

```kotlin
@Composable
fun VIPSubscriptionScreen(
    viewModel: ShopViewModel = hiltViewModel()
) {
    val currentTier by viewModel.currentTier.collectAsState()
    val availableTiers by viewModel.availableTiers.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hero section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Unlock Premium Features",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Join GOLD or PLATINUM to enhance your Rafiq experience",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        // Tier cards
        items(availableTiers) { tier ->
            VIPTierCard(
                tier = tier,
                isCurrent = tier.tier == currentTier,
                onSubscribeClick = { viewModel.subscribeTo(tier.tier) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // FAQ section
        item {
            FAQ()
        }
    }
}

@Composable
fun VIPTierCard(
    tier: SubscriptionTier,
    isCurrent: Boolean,
    onSubscribeClick: () -> Unit
) {
    val borderColor = if (isCurrent) tier.color else Color.Transparent
    
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = tier.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = tier.color
                    )
                    Text(
                        text = tier.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (isCurrent) {
                    Badge(
                        modifier = Modifier.background(tier.color),
                        containerColor = tier.color
                    ) {
                        Text("Current", color = Color.White)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Price
            Text(
                text = "$${tier.monthlyPrice}/month or $${tier.annualPrice}/year",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Benefits
            tier.benefits.forEach { (_, benefit) ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (benefit.included) Icons.Filled.Check else Icons.Filled.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (benefit.included) Color.Green else Color.Gray
                    )
                    Text(
                        text = benefit.name,
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subscribe button
            Button(
                onClick = onSubscribeClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCurrent
            ) {
                Text(if (isCurrent) "Current Plan" else "Subscribe Now")
            }
        }
    }
}
```

---

### 6. DATABASE SCHEMA

#### **6.1 Firebase Firestore/MongoDB Collections**

```
Database: rafiq_production
├── collections/
│   ├── users/
│   │   └── {userId}/
│   │       ├── basic_info (Phone, Email, Name, Age, Gender, Bio)
│   │       ├── profile_data (Avatar, Photos, Interests, Badges)
│   │       ├── subscription (Tier, RenewalDate, PaymentMethod)
│   │       ├── preferences (RecommendationGender, AgeRange, LocationRadius)
│   │       ├── settings (Notifications, Privacy, Language, Theme)
│   │       ├── security (PhoneVerified, EmailVerified, IDVerified, 2FA)
│   │       └── stats (FollowerCount, FollowingCount, PostCount)
│   │
│   ├── chats/
│   │   └── {chatId}/
│   │       ├── metadata (Participants, CreatedAt, LastMessageAt, IsGroup)
│   │       └── messages/ (Subcollection)
│   │           └── {messageId}
│   │               (Content, SenderId, Timestamp, DeliveryStatus)
│   │
│   ├── calls/
│   │   └── {callId}/
│   │       (InitiatorId, RecipientId, Type, Duration, Status, Recording)
│   │
│   ├── random_calls/
│   │   ├── queue/
│   │   │   └── {userId} (QueueEntries)
│   │   └── matches/
│   │       └── {matchId} (User1, User2, Status, CreatedAt)
│   │
│   ├── posts/
│   │   └── {postId}/
│   │       ├── content (AuthorId, Text, MediaUrls, CreatedAt)
│   │       └── comments/ (Subcollection)
│   │           └── {commentId}
│   │
│   ├── gifts/
│   │   ├── catalog/
│   │   │   └── {giftId} (Name, ImageUrl, Price, Rarity)
│   │   └── received/
│   │       └── {userId} (ReceivedGifts List)
│   │
│   ├── wallet/
│   │   └── {userId}/
│   │       ├── coins (Current Balance)
│   │       ├── diamonds (Premium Currency)
│   │       └── transactions/ (Subcollection)
│   │           └── {transactionId}
│   │
│   ├── reports/
│   │   └── {reportId}/
│   │       (ReportedUserId, Reason, Status, Resolution)
│   │
│   ├── blocks/
│   │   └── {userId}/
│   │       └── blocked_users/ (Subcollection)
│   │           └── {blockedUserId} (Metadata)
│   │
│   ├── subscriptions/
│   │   └── {subscriptionId}/
│   │       (UserId, Tier, StartDate, RenewalDate, PaymentMethod)
│   │
│   ├── recommendations/
│   │   └── {userId}/
│   │       └── recommended_users/ (Subcollection)
│   │           └── {recommendedUserId} (Score, Reason)
│   │
│   └── analytics/
│       ├── daily_active_users/
│       │
```
6.2 MongoDB Collections
users
profiles
photos
stories
posts
comments
likes
bookmarks

conversations
conversation_members
messages
message_reactions
deleted_messages

calls
call_logs
call_queue
call_matches

friendships
matches
follows
visitors

notifications
devices
push_tokens

wallets
transactions
subscriptions
gift_catalog
gift_transactions

reports
blocks
moderation_cases
appeals

search_index

audit_logs
system_settings
feature_flags

analytics_daily
analytics_monthly
User Document
{
    _id:ObjectId,

    username,
    email,
    phone,

    displayName,
    bio,

    gender,
    interestedIn,

    birthday,
    age,

    location:{
        country,
        city,
        lat,
        lng
    },

    photos:[],
    interests:[],

    verified:{
        phone,
        email,
        identity,
        selfie
    },

    premium:{
        tier,
        expiresAt
    },

    walletId,

    statistics:{
        followers,
        following,
        posts,
        gifts,
        visitors,
        profileViews
    },

    createdAt,
    updatedAt,
    lastOnline
}
Chat Collections
Conversation

Participants

Last Message

Unread Count

Pinned

Muted

Archived
Message Document
{
   senderId,

   conversationId,

   type,

   text,

   media,

   voice,

   reactions,

   replyTo,

   deleted,

   edited,

   delivered,

   seenBy,

   createdAt
}
Notification
Like

Follow

Message

Call

Gift

Subscription

System

Admin

Promotion
Wallet
Balance

Coins

Diamonds

Pending

Lifetime Purchased

Lifetime Earned
7. REST API
Authentication
POST /auth/register

POST /auth/login

POST /auth/logout

POST /auth/refresh

POST /auth/send-otp

POST /auth/verify-otp

POST /auth/forgot-password

POST /auth/reset-password

POST /auth/verify-selfie
User
GET /users/me

PUT /users/me

GET /users/:id

DELETE /users/me

GET /users/search

GET /users/recommendations

POST /users/report

POST /users/block

DELETE /users/block/:id
Discovery
GET /discover

POST /discover/like

POST /discover/pass

POST /discover/superlike

POST /discover/boost
Chat
GET /conversations

POST /conversations

GET /messages

POST /messages

PUT /messages/:id

DELETE /messages/:id

POST /messages/react

POST /typing
Voice / Video
POST /call/start

POST /call/end

POST /call/random

POST /call/accept

POST /call/reject

POST /call/ice

POST /call/offer

POST /call/answer
Posts
GET /posts

POST /posts

DELETE /posts/:id

POST /posts/like

POST /posts/comment

POST /posts/share

POST /posts/bookmark
Stories
GET /stories

POST /stories

DELETE /stories/:id

POST /stories/view
Wallet
GET /wallet

POST /wallet/purchase

POST /wallet/gift

GET /wallet/history
Subscription
GET /subscription

POST /subscription/upgrade

POST /subscription/cancel

POST /subscription/restore
8. Socket.IO Events
Authentication
authenticate

disconnect
Presence
online

offline

typing

stop_typing
Chat
message_send

message_receive

message_read

message_edit

message_delete

reaction_add

reaction_remove
Calling
call_request

call_accept

call_reject

call_offer

call_answer

ice_candidate

call_end
Random Match
join_queue

leave_queue

match_found

match_timeout

match_cancelled

match_confirmed
Notifications
notification

gift

visitor

like

follow
9. Security & Compliance
Authentication
JWT Access Token
Refresh Token Rotation
OAuth Google
OAuth Apple
OTP Verification
Optional 2FA
Encryption
HTTPS TLS 1.3
AES-256 stored secrets
bcrypt password hashing
Secure cookies
Signed URLs for media
Anti Abuse
Device fingerprinting
Spam detection
Duplicate account detection
Rate limiting
VPN/proxy detection
Bot detection
Captcha
Content Moderation

Automatic AI moderation for:

Nudity
Violence
Hate speech
Child safety
Spam
Scams
Fake accounts
Privacy

Users can configure:

Hide Age
Hide Distance
Hide Last Seen
Hide Online Status
Hide Read Receipts
Private Account
Message Requests Only
Incognito Browsing (Platinum)
Compliance
GDPR
CCPA
COPPA (18+ enforcement)
Google Play Dating Policy
Apple App Store Review Guidelines
PCI-DSS for payments
10. Performance Optimization
Backend
Redis caching
CDN for media
Image compression
Video transcoding
Horizontal scaling
Load balancing
Queue workers
Lazy loading
Mobile
Infinite pagination
Offline caching
Background sync
Image prefetching
Video lazy loading
Compose performance optimization
Database indexing
Database Indexes
email

phone

username

location

lastOnline

createdAt

gender

interestedIn

subscriptionTier

conversationId

senderId

receiverId
11. AI Features
Smart Matching

Machine learning ranking using:

Interests
Conversation quality
Active hours
Response rate
Swipe behavior
Location
Profile completeness
AI Assistant
Icebreaker suggestions
Reply suggestions
Conversation summaries
Translation
Grammar correction
Safety warnings
AI Moderation
NSFW detection
Face verification
Duplicate photo detection
Deepfake detection
Spam detection
Toxicity scoring
12. Admin Dashboard

Modules:

User Management
Reports
Content Moderation
Subscription Analytics
Revenue Dashboard
Coin Economy
Live Calls Monitor
Server Health
Push Notifications
Promotions
Feature Flags
Support Tickets
Ban Appeals
Fraud Detection
13. Analytics

Track:

DAU / WAU / MAU
User Retention
Churn
Conversion to Premium
Revenue
ARPU
ARPPU
Average Session Length
Messages/User
Calls/User
Match Success Rate
Gift Revenue
Crash Rate
API Latency
14. DevOps

Infrastructure:

Flutter

Node.js

NGINX

Redis

MongoDB

RabbitMQ

Socket.IO

Mediasoup

Docker

Kubernetes

GitHub Actions

Prometheus

Grafana

ELK

Firebase

Cloudflare CDN

Deployment environments:

Development

QA

Staging

Production
