package com.example.deepwork.data.database.db

import com.example.deepwork.data.database.room.dao.CategoryDao
import com.example.deepwork.data.database.room.model.category.CategoryEntity
import com.example.deepwork.domain.exception.DatabaseException
import com.example.deepwork.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

class CategoryDbRoom @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryDb {

    override fun getAll(): Flow<List<Category>> {
        return categoryDao.getAll()
            .map { categoryEntities ->
                categoryEntities.map { it.toDomain() }
            }.catch { e ->
                Timber.d("get all categories failed: ${e.message}")
                throw DatabaseException("Error fetching categories: ${e.message}")
            }
    }

    override fun getById(id: String): Flow<Category?> {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(category: Category): Category {
        val preparedCategory = prepareCategoryForSave(category)
        val entity = CategoryEntity.toEntity(preparedCategory)

        try {
            categoryDao.upsert(entity)
        } catch (e: Exception) {
            Timber.d("upsert category failed: ${e.message}")
            throw DatabaseException("Error saving category: ${e.message}")
        }
        return preparedCategory
    }

    private fun prepareCategoryForSave(category: Category): Category {
        val hasBeenSavedBefore = category.createdAt > 0
        val currentTime = Calendar.getInstance().timeInMillis
        return if (hasBeenSavedBefore) {
            category.copy(updatedAt = currentTime)
        } else {
            category.copy(createdAt = currentTime)
        }
    }

    override suspend fun delete(category: Category) {
        TODO("Not yet implemented")
    }
}