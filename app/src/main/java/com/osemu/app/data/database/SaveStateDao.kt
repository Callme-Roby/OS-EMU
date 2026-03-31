package com.osemu.app.data.database

import androidx.room.*
import com.osemu.app.data.model.SaveState
import kotlinx.coroutines.flow.Flow

@Dao
interface SaveStateDao {
    @Query("SELECT * FROM save_states WHERE gameId = :gameId ORDER BY timestamp DESC")
    fun getSaveStatesForGame(gameId: Long): Flow<List<SaveState>>

    @Query("SELECT * FROM save_states WHERE gameId = :gameId AND isAutoSave = 1 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestAutoSave(gameId: Long): SaveState?

    @Query("SELECT * FROM save_states WHERE gameId = :gameId AND slotIndex = :slot")
    suspend fun getSaveStateBySlot(gameId: Long, slot: Int): SaveState?

    @Query("SELECT * FROM save_states WHERE id = :id")
    suspend fun getSaveStateById(id: Long): SaveState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaveState(saveState: SaveState): Long

    @Update
    suspend fun updateSaveState(saveState: SaveState)

    @Delete
    suspend fun deleteSaveState(saveState: SaveState)

    @Query("DELETE FROM save_states WHERE gameId = :gameId AND isAutoSave = 1")
    suspend fun deleteAutoSaves(gameId: Long)

    @Query("SELECT COUNT(*) FROM save_states WHERE gameId = :gameId")
    fun getSaveStateCount(gameId: Long): Flow<Int>
}
