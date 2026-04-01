package com.osemu.app.data.model

/**
 * 3DS Badge Arcade-inspired collectible badges.
 * Users earn badges by playing games and completing milestones.
 */
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val category: BadgeCategory,
    val requirement: BadgeRequirement,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val rarity: BadgeRarity = BadgeRarity.COMMON
)

enum class BadgeCategory(val displayName: String, val emoji: String) {
    COLLECTOR("Collector", "📦"),
    PLAYER("Player", "🎮"),
    EXPLORER("Explorer", "🗺"),
    MASTER("Master", "👑"),
    SOCIAL("Social", "👥"),
    SPECIAL("Special", "⭐")
}

enum class BadgeRarity(val displayName: String, val stars: Int) {
    COMMON("Common", 1),
    UNCOMMON("Uncommon", 2),
    RARE("Rare", 3),
    EPIC("Epic", 4),
    LEGENDARY("Legendary", 5)
}

sealed class BadgeRequirement {
    data class GamesPlayed(val count: Int) : BadgeRequirement()
    data class TotalPlayTime(val hours: Int) : BadgeRequirement()
    data class ConsolesUsed(val count: Int) : BadgeRequirement()
    data class FavoritesAdded(val count: Int) : BadgeRequirement()
    data class CollectionsCreated(val count: Int) : BadgeRequirement()
    data class GamesInLibrary(val count: Int) : BadgeRequirement()
    data object FirstGame : BadgeRequirement()
    data object FirstFavorite : BadgeRequirement()
    data object ThemeChanged : BadgeRequirement()
    data object AllConsoles : BadgeRequirement()
}

/**
 * All available badges in the app.
 */
object BadgeRegistry {
    val allBadges = listOf(
        // Collector badges
        Badge("first_game", "First Catch", "Add your first game", "🎣",
            BadgeCategory.COLLECTOR, BadgeRequirement.FirstGame, rarity = BadgeRarity.COMMON),
        Badge("collector_10", "Starter Pack", "Have 10 games in library", "📦",
            BadgeCategory.COLLECTOR, BadgeRequirement.GamesInLibrary(10), rarity = BadgeRarity.COMMON),
        Badge("collector_50", "Game Hoarder", "Have 50 games in library", "🏪",
            BadgeCategory.COLLECTOR, BadgeRequirement.GamesInLibrary(50), rarity = BadgeRarity.UNCOMMON),
        Badge("collector_100", "Museum Curator", "Have 100 games in library", "🏛",
            BadgeCategory.COLLECTOR, BadgeRequirement.GamesInLibrary(100), rarity = BadgeRarity.RARE),
        Badge("collector_500", "Legendary Vault", "Have 500 games in library", "💎",
            BadgeCategory.COLLECTOR, BadgeRequirement.GamesInLibrary(500), rarity = BadgeRarity.LEGENDARY),

        // Player badges
        Badge("first_play", "Player One", "Play your first game", "🕹",
            BadgeCategory.PLAYER, BadgeRequirement.GamesPlayed(1), rarity = BadgeRarity.COMMON),
        Badge("played_10", "Getting Started", "Play 10 different games", "🎮",
            BadgeCategory.PLAYER, BadgeRequirement.GamesPlayed(10), rarity = BadgeRarity.UNCOMMON),
        Badge("played_50", "Veteran Gamer", "Play 50 different games", "🏆",
            BadgeCategory.PLAYER, BadgeRequirement.GamesPlayed(50), rarity = BadgeRarity.RARE),
        Badge("playtime_10h", "Dedicated", "Play for 10 hours total", "⏰",
            BadgeCategory.PLAYER, BadgeRequirement.TotalPlayTime(10), rarity = BadgeRarity.UNCOMMON),
        Badge("playtime_100h", "Hardcore", "Play for 100 hours total", "🔥",
            BadgeCategory.PLAYER, BadgeRequirement.TotalPlayTime(100), rarity = BadgeRarity.EPIC),

        // Explorer badges
        Badge("console_3", "Multi-Platform", "Play games on 3 consoles", "🌐",
            BadgeCategory.EXPLORER, BadgeRequirement.ConsolesUsed(3), rarity = BadgeRarity.COMMON),
        Badge("console_5", "Platform Explorer", "Play games on 5 consoles", "🧭",
            BadgeCategory.EXPLORER, BadgeRequirement.ConsolesUsed(5), rarity = BadgeRarity.UNCOMMON),
        Badge("console_10", "Console Master", "Play games on 10 consoles", "🗺",
            BadgeCategory.EXPLORER, BadgeRequirement.ConsolesUsed(10), rarity = BadgeRarity.RARE),
        Badge("console_all", "Retro Completionist", "Play on every console", "👑",
            BadgeCategory.EXPLORER, BadgeRequirement.AllConsoles, rarity = BadgeRarity.LEGENDARY),

        // Social/Organization badges
        Badge("first_fav", "Heart of Gold", "Mark your first favorite", "💛",
            BadgeCategory.SOCIAL, BadgeRequirement.FirstFavorite, rarity = BadgeRarity.COMMON),
        Badge("fav_10", "Curator", "Have 10 favorites", "❤",
            BadgeCategory.SOCIAL, BadgeRequirement.FavoritesAdded(10), rarity = BadgeRarity.UNCOMMON),
        Badge("collection_1", "Organizer", "Create your first collection", "📂",
            BadgeCategory.SOCIAL, BadgeRequirement.CollectionsCreated(1), rarity = BadgeRarity.COMMON),
        Badge("collection_5", "Librarian", "Create 5 collections", "📚",
            BadgeCategory.SOCIAL, BadgeRequirement.CollectionsCreated(5), rarity = BadgeRarity.RARE),

        // Special badges
        Badge("theme_change", "Style Icon", "Change the app theme", "🎨",
            BadgeCategory.SPECIAL, BadgeRequirement.ThemeChanged, rarity = BadgeRarity.COMMON)
    )
}
