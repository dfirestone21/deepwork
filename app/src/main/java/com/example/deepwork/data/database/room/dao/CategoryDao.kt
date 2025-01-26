package com.example.deepwork.data.database.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.deepwork.data.database.room.model.category.CategoryEntity

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: CategoryEntity): CategoryEntity

    @Delete
    suspend fun delete(category: CategoryEntity)
}