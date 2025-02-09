package com.example.deepwork.data.repository

import com.example.deepwork.data.database.db.CategoryDb
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val db: CategoryDb
) : CategoryRepository {

    override fun getAll(): Flow<List<Category>> {
        return db.getAll()
    }

    override fun getById(id: String): Flow<Category?> {
        return db.getById(id)
    }

    override suspend fun upsert(category: Category): Category {
        return db.upsert(category)
    }

    override suspend fun delete(category: Category) {
        return db.delete(category)
    }
}