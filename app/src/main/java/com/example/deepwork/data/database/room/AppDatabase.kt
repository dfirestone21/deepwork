package com.example.deepwork.data.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.deepwork.data.database.room.model.CategoryEntity
import com.example.deepwork.data.database.room.model.TimeBlockEntity

@Database(
    entities = [
        TimeBlockEntity::class,
        CategoryEntity::class
    ], version = AppDatabase.VERSION
)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "deep_work-db"
        const val VERSION = 1
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            if (instance != null) {
                return instance!!
            }
            instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            ).build()
            return instance!!
        }
    }
}