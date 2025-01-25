package com.example.deepwork.data.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.deepwork.data.database.room.model.category.CategoryEntity
import com.example.deepwork.data.database.room.model.category.CategoryWithScheduledTimeBlocksEntity
import com.example.deepwork.data.database.room.model.scheduled_session.ScheduledSessionEntity
import com.example.deepwork.data.database.room.model.scheduled_session.ScheduledSessionWithTimeBlocksEntity
import com.example.deepwork.data.database.room.model.scheduled_session.ScheduledTimeBlockCategoryCrossRef
import com.example.deepwork.data.database.room.model.scheduled_session.ScheduledTimeBlockEntity
import com.example.deepwork.data.database.room.model.session_template.SessionTemplateEntity
import com.example.deepwork.data.database.room.model.session_template.SessionTemplateWithBlocks
import com.example.deepwork.data.database.room.model.session_template.TimeBlockTemplateCategoryCrossRef
import com.example.deepwork.data.database.room.model.session_template.TimeBlockTemplateEntity

@Database(
    entities = [
        SessionTemplateEntity::class,
        TimeBlockTemplateEntity::class,
        TimeBlockTemplateCategoryCrossRef::class,
        ScheduledSessionEntity::class,
        ScheduledTimeBlockEntity::class,
        ScheduledTimeBlockCategoryCrossRef::class,
        CategoryEntity::class,
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