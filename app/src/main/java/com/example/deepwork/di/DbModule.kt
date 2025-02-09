package com.example.deepwork.di

import android.content.Context
import com.example.deepwork.data.database.db.CategoryDb
import com.example.deepwork.data.database.db.CategoryDbRoom
import com.example.deepwork.data.database.room.AppDatabase
import com.example.deepwork.data.database.room.dao.CategoryDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DbModule {

    @Binds
    fun bindCategoryDb(categoryDbRoom: CategoryDbRoom): CategoryDb
}

@Module
@InstallIn(SingletonComponent::class)
object DbModuleProvides {

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.get(context)
    }

    @Provides
    fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao {
        return appDatabase.categoryDao()
    }
}