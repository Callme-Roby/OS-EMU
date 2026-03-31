package com.osemu.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "save_states",
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId")]
)
data class SaveState(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: Long,
    val slotIndex: Int, // -1 = auto-save, 0-9 = manual slots
    val filePath: String,
    val screenshotPath: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val label: String? = null,
    val isAutoSave: Boolean = false
) {
    companion object {
        const val AUTO_SAVE_SLOT = -1
        const val MAX_MANUAL_SLOTS = 10
    }
}
