package com.homio.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MaintenanceEntity::class], version = 1, exportSchema = false)
abstract class HomioDatabase : RoomDatabase() {

    abstract fun maintenanceDao(): MaintenanceDao

    companion object {
        @Volatile private var instance: HomioDatabase? = null

        fun get(context: Context): HomioDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HomioDatabase::class.java,
                    "homio.db"
                )
                    // It's only a cache, so rebuilding it after a schema change is safe
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}