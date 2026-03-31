package com.osemu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "games")
@TypeConverters(GameConverters::class)
data class Game(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val console: Console,
    val boxArtPath: String? = null,
    val lastPlayed: Long? = null,
    val totalPlayTimeMs: Long = 0,
    val favorite: Boolean = false,
    val rating: Int = 0, // 0-5 stars
    val dateAdded: Long = System.currentTimeMillis(),
    val fileSize: Long = 0,
    val crc32: String? = null,
    val region: GameRegion = GameRegion.UNKNOWN
)

enum class GameRegion(val displayName: String, val flag: String) {
    USA("USA", "🇺🇸"),
    EUROPE("Europe", "🇪🇺"),
    JAPAN("Japan", "🇯🇵"),
    WORLD("World", "🌍"),
    UNKNOWN("Unknown", "❓")
}

class GameConverters {
    @TypeConverter
    fun fromConsole(console: Console): String = console.name

    @TypeConverter
    fun toConsole(name: String): Console = Console.valueOf(name)

    @TypeConverter
    fun fromRegion(region: GameRegion): String = region.name

    @TypeConverter
    fun toRegion(name: String): GameRegion = GameRegion.valueOf(name)
}
