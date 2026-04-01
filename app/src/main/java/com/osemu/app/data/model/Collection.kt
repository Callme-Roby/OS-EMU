package com.osemu.app.data.model

import androidx.room.*

/**
 * Custom game collection (e.g., "RPGs", "Favorites", "Beat 'em ups").
 */
@Entity(tableName = "collections")
data class GameCollection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconEmoji: String = "📁",
    val coverGameId: Long? = null,
    val sortOrder: Int = 0,
    val dateCreated: Long = System.currentTimeMillis(),
    val isSystem: Boolean = false
)

/**
 * Junction table for many-to-many relationship between collections and games.
 */
@Entity(
    tableName = "collection_games",
    primaryKeys = ["collectionId", "gameId"],
    foreignKeys = [
        ForeignKey(
            entity = GameCollection::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Game::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("collectionId"), Index("gameId")]
)
data class CollectionGame(
    val collectionId: Long,
    val gameId: Long,
    val addedAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)

data class CollectionWithGames(
    val collection: GameCollection,
    val games: List<Game>
)
