package com.example.deepwork.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor() : CategoryRepository {

    override fun getAll(): Flow<List<Category>> {
        TODO()
    }

    override fun getById(id: String): Flow<Category?> {
        return flowOf(null)
    }

    override suspend fun upsert(category: Category): Category {
        return category
    }

    override suspend fun delete(category: Category) {
        // no-op
    }
}