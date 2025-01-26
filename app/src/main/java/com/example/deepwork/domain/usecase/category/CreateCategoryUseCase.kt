package com.example.deepwork.domain.usecase.category

import com.example.deepwork.domain.business.CategoryValidator
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.repository.CategoryRepository
import javax.inject.Inject

class CreateCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
    private val validator: CategoryValidator
) {
    suspend operator fun invoke(category: Category): Result<Category> {
        return try {
            validator.validate(category)
            val insertedCategory = repository.upsert(category)
            Result.Success(insertedCategory)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}