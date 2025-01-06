package com.example.deepwork.domain.repository

import com.example.deepwork.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getAll(): Flow<List<Category>>

    fun getById(id: String): Flow<Category?>

    fun upsert(category: Category): Category

    fun delete(category: Category)
}