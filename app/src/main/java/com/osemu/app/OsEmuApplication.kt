package com.osemu.app

import android.app.Application
import com.osemu.app.data.database.AppDatabase

class OsEmuApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()

        // Create required directories
        val dirs = listOf("cores", "saves", "states", "system", "screenshots")
        for (dirName in dirs) {
            val dir = java.io.File(filesDir, dirName)
            if (!dir.exists()) dir.mkdirs()
        }
    }
}
