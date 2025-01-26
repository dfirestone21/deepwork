package com.example.deepwork.data.database.db

import com.example.deepwork.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryDb {

    fun getAll(): Flow<List<Category>>

    fun getById(id: String): Flow<Category?>

    suspend fun upsert(category: Category): Category

    suspend fun delete(category: Category)
}