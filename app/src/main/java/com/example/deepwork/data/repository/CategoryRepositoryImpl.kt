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
        return flowOf(testCategories())
    }

    override fun getById(id: String): Flow<Category?> {
        return flowOf(null)
    }

    override fun upsert(category: Category): Category {
        return category
    }

    override fun delete(category: Category) {
        // no-op
    }

    private fun testCategories(): List<Category> {
        return listOf(
            Category("1", "Coding", Color.Blue.toArgb()),
            Category("2", "Design", Color.Red.toArgb()),
            Category("3", "Writing", Color.Green.toArgb()),
            Category("4", "Research", Color.Yellow.toArgb()),
            Category("5", "Learning", Color.Magenta.toArgb()),
        )
    }
}