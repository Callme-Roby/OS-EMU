package com.osemu.app.data.database

import androidx.room.*
import com.osemu.app.data.model.Console
import com.osemu.app.data.model.Game
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY title ASC")
    fun getAllGames(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE console = :console ORDER BY title ASC")
    fun getGamesByConsole(console: Console): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE favorite = 1 ORDER BY title ASC")
    fun getFavoriteGames(): Flow<List<Game>>

    @Query("SELECT * FROM games ORDER BY lastPlayed DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 20): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE title LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchGames(query: String): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameById(id: Long): Game?

    @Query("SELECT * FROM games WHERE filePath = :path")
    suspend fun getGameByPath(path: String): Game?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: Game): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<Game>)

    @Update
    suspend fun updateGame(game: Game)

    @Delete
    suspend fun deleteGame(game: Game)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGameById(id: Long)

    @Query("SELECT COUNT(*) FROM games WHERE console = :console")
    fun getGameCountByConsole(console: Console): Flow<Int>

    @Query("SELECT COUNT(*) FROM games")
    fun getTotalGameCount(): Flow<Int>

    @Query("UPDATE games SET lastPlayed = :timestamp, totalPlayTimeMs = totalPlayTimeMs + :sessionMs WHERE id = :gameId")
    suspend fun updatePlaySession(gameId: Long, timestamp: Long, sessionMs: Long)

    @Query("UPDATE games SET favorite = :favorite WHERE id = :gameId")
    suspend fun updateFavorite(gameId: Long, favorite: Boolean)
}
