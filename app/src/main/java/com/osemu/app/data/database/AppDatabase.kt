package com.osemu.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.osemu.app.data.model.CollectionGame
import com.osemu.app.data.model.GameCollection
import com.osemu.app.data.model.Game
import com.osemu.app.data.model.GameConverters
import com.osemu.app.data.model.SaveState

@Database(
    entities = [
        Game::class,
        SaveState::class,
        GameCollection::class,
        CollectionGame::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(GameConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao
    abstract fun saveStateDao(): SaveStateDao
    abstract fun collectionDao(): CollectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "osemu_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
