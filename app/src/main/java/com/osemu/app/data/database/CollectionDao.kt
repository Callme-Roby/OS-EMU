package com.osemu.app.data.database

import androidx.room.*
import com.osemu.app.data.model.CollectionGame
import com.osemu.app.data.model.GameCollection
import com.osemu.app.data.model.Game
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY sortOrder ASC, name ASC")
    fun getAllCollections(): Flow<List<GameCollection>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getCollectionById(id: Long): GameCollection?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: GameCollection): Long

    @Update
    suspend fun updateCollection(collection: GameCollection)

    @Delete
    suspend fun deleteCollection(collection: GameCollection)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGameToCollection(collectionGame: CollectionGame)

    @Query("DELETE FROM collection_games WHERE collectionId = :collectionId AND gameId = :gameId")
    suspend fun removeGameFromCollection(collectionId: Long, gameId: Long)

    @Query("""
        SELECT g.* FROM games g
        INNER JOIN collection_games cg ON g.id = cg.gameId
        WHERE cg.collectionId = :collectionId
        ORDER BY cg.sortOrder ASC, g.title ASC
    """)
    fun getGamesInCollection(collectionId: Long): Flow<List<Game>>

    @Query("SELECT COUNT(*) FROM collection_games WHERE collectionId = :collectionId")
    fun getCollectionGameCount(collectionId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM collections")
    fun getTotalCollectionCount(): Flow<Int>

    @Query("SELECT DISTINCT console FROM games WHERE lastPlayed IS NOT NULL")
    suspend fun getPlayedConsoles(): List<String>

    @Query("SELECT COUNT(DISTINCT id) FROM games WHERE lastPlayed IS NOT NULL")
    suspend fun getPlayedGameCount(): Int
}
