package com.example.deepwork.domain.usecase.category

import com.example.deepwork.domain.business.CategoryValidator
import com.example.deepwork.domain.repository.CategoryRepository
import io.mockk.mockk
import org.junit.Before

class CreateCategoryUseCaseTest {
    private lateinit var createCategory: CreateCategoryUseCase
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var categoryValidator: CategoryValidator

    @Before
    fun setup() {
        categoryRepository = mockk()
        categoryValidator = mockk()
    }

    private fun initUseCase() {
        createCategory = CreateCategoryUseCase(categoryRepository, categoryValidator)
    }
}