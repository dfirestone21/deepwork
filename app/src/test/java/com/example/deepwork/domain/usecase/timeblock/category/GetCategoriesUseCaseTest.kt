package com.example.deepwork.domain.usecase.timeblock.category

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


class GetCategoriesUseCaseTest {
    private lateinit var getCategories: GetCategoriesUseCase
    private lateinit var categoryRepository: CategoryRepository

    @Before
    fun setup() {
        categoryRepository = mockk() {
            coEvery { getAll() } returns flowOf(testCategories())
        }
        getCategories = GetCategoriesUseCase(categoryRepository)
    }

    @Test
    fun `invoke() should return list of categories`() = runTest {
        // given
        val expectedCategories = testCategories()

        // when
        val result = getCategories().first()
        val actualCategories = result.getOrThrow()

        // then
        assert(actualCategories == expectedCategories)
    }

    @Test
    fun `invoke when fetching categories throws exception, should return error result`() = runTest {
        // given
        val exception = Exception("Test exception")
        categoryRepository = mockk() {
            coEvery { getAll() } returns flow { throw exception }
        }
        getCategories = GetCategoriesUseCase(categoryRepository)

        // when
        val result = getCategories().first()

        // then
        assert(result.isError)
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