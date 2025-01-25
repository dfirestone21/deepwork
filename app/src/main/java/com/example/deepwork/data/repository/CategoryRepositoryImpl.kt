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
            Category.create("Coding", Color.Blue.toArgb()),
            Category.create("Design", Color.Red.toArgb()),
            Category.create("Writing", Color.Green.toArgb()),
            Category.create("Research", Color.Yellow.toArgb()),
            Category.create("Learning", Color.Magenta.toArgb()),
        )
    }
}