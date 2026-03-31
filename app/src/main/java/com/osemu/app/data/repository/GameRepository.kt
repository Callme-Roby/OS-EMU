package com.osemu.app.data.repository

import com.osemu.app.data.database.GameDao
import com.osemu.app.data.database.SaveStateDao
import com.osemu.app.data.model.Console
import com.osemu.app.data.model.Game
import com.osemu.app.data.model.SaveState
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val gameDao: GameDao,
    private val saveStateDao: SaveStateDao
) {
    // --- Games ---
    fun getAllGames(): Flow<List<Game>> = gameDao.getAllGames()

    fun getGamesByConsole(console: Console): Flow<List<Game>> =
        gameDao.getGamesByConsole(console)

    fun getFavorites(): Flow<List<Game>> = gameDao.getFavoriteGames()

    fun getRecentlyPlayed(limit: Int = 20): Flow<List<Game>> =
        gameDao.getRecentlyPlayed(limit)

    fun searchGames(query: String): Flow<List<Game>> =
        gameDao.searchGames(query)

    suspend fun getGameById(id: Long): Game? = gameDao.getGameById(id)

    suspend fun getGameByPath(path: String): Game? = gameDao.getGameByPath(path)

    suspend fun addGame(game: Game): Long = gameDao.insertGame(game)

    suspend fun addGames(games: List<Game>) = gameDao.insertGames(games)

    suspend fun updateGame(game: Game) = gameDao.updateGame(game)

    suspend fun deleteGame(game: Game) = gameDao.deleteGame(game)

    suspend fun recordPlaySession(gameId: Long, sessionMs: Long) {
        gameDao.updatePlaySession(gameId, System.currentTimeMillis(), sessionMs)
    }

    suspend fun toggleFavorite(gameId: Long, favorite: Boolean) {
        gameDao.updateFavorite(gameId, favorite)
    }

    fun getGameCount(): Flow<Int> = gameDao.getTotalGameCount()

    fun getGameCountByConsole(console: Console): Flow<Int> =
        gameDao.getGameCountByConsole(console)

    // --- Save States ---
    fun getSaveStates(gameId: Long): Flow<List<SaveState>> =
        saveStateDao.getSaveStatesForGame(gameId)

    suspend fun getLatestAutoSave(gameId: Long): SaveState? =
        saveStateDao.getLatestAutoSave(gameId)

    suspend fun saveSaveState(saveState: SaveState): Long =
        saveStateDao.insertSaveState(saveState)

    suspend fun deleteSaveState(saveState: SaveState) =
        saveStateDao.deleteSaveState(saveState)

    fun getSaveStateCount(gameId: Long): Flow<Int> =
        saveStateDao.getSaveStateCount(gameId)
}
