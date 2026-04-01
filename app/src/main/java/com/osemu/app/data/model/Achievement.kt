package com.osemu.app.data.model

/**
 * RetroAchievements integration data models.
 */
data class Achievement(
    val id: Long,
    val gameId: Long,
    val title: String,
    val description: String,
    val points: Int,
    val badgeUrl: String? = null,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val type: AchievementType = AchievementType.NORMAL
)

enum class AchievementType {
    NORMAL,
    PROGRESSION,
    WIN_CONDITION,
    MISSABLE
}

data class RetroAchievementsProfile(
    val username: String = "",
    val totalPoints: Int = 0,
    val rank: Int = 0,
    val avatarUrl: String? = null,
    val isLoggedIn: Boolean = false
)

data class GameAchievementInfo(
    val gameId: Long,
    val gameTitle: String,
    val consoleName: String,
    val achievements: List<Achievement>,
    val totalPoints: Int,
    val earnedPoints: Int,
    val completionPercentage: Float
)
