package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.GameProfile
import com.example.data.model.KeyMapping

@Database(
    entities = [GameProfile::class, KeyMapping::class],
    version = 2,
    exportSchema = false
)
abstract class MantisDatabase : RoomDatabase() {

    abstract fun mantisDao(): MantisDao

    companion object {
        @Volatile
        private var INSTANCE: MantisDatabase? = null

        fun getInstance(context: Context): MantisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MantisDatabase::class.java,
                    "mantis_gamepad_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
