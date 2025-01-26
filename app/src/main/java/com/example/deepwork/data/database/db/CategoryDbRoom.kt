package com.example.deepwork.data.database.db

import android.util.Log
import com.example.deepwork.data.database.room.dao.CategoryDao
import com.example.deepwork.data.database.room.model.category.CategoryEntity
import com.example.deepwork.domain.exception.DatabaseException
import com.example.deepwork.domain.model.Category
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

class CategoryDbRoom @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryDb {

    private val TAG = "CategoryDbRoom"

    override fun getAll(): Flow<List<Category>> {
        TODO("Not yet implemented")
    }

    override fun getById(id: String): Flow<Category?> {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(category: Category): Category {
        val preparedCategory = prepareCategoryForSave(category)
        val entity = CategoryEntity.toEntity(preparedCategory)

        val upsertedCategory = try {
            categoryDao.upsert(entity)
        } catch (e: Exception) {
            Timber.d("upsert category failed: ${e.message}")
            throw DatabaseException("Error saving category: ${e.message}")
        }
        return upsertedCategory.toDomain()
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