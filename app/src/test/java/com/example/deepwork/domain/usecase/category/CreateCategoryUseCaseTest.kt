package com.example.deepwork.domain.usecase.category

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.deepwork.domain.business.CategoryValidator
import com.example.deepwork.domain.exception.CategoryException
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.repository.CategoryRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CreateCategoryUseCaseTest {
    private lateinit var createCategory: CreateCategoryUseCase
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var categoryValidator: CategoryValidator
    private lateinit var category: Category

    @Before
    fun setup() {
        category = Category.create("Coding", Color.Red.toArgb())
        categoryRepository = mockk() {
            coEvery { upsert(any()) } returns category
        }
        categoryValidator = mockk() {
            every { validate(any()) } just Runs
        }
    }

    private fun initUseCase() {
        createCategory = CreateCategoryUseCase(categoryRepository, categoryValidator)
    }

    @Test
    fun `when category is valid, should save in database`() = runTest {
        // given
        initUseCase()

        // when
        val result = createCategory(category)

        // then
        assert(result.isSuccess)
        coVerify { categoryRepository.upsert(category) }
    }

    @Test
    fun `when error saving category in database, should return error result`() = runTest {
        // given
        val expectedException = Exception()
        coEvery { categoryRepository.upsert(any()) } throws expectedException
        initUseCase()

        // when
        val result = createCategory(category)

        // then
        assert(result.isError)
        assertEquals(expectedException, result.exceptionOrNull())
    }

    @Test
    fun `when category is not valid, should return error result`() = runTest {
        // given
        val expectedException = CategoryException.InvalidNameException("Wrong name!")
        every { categoryValidator.validate(any()) } throws expectedException
        initUseCase()

        // when
        val result = createCategory(category)

        // then
        assert(result.isError)
        assertEquals(expectedException, result.exceptionOrNull())
    }
}