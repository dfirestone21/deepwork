package com.example.deepwork.domain.usecase.category

import com.example.deepwork.domain.business.CategoryValidator
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.repository.CategoryRepository
import javax.inject.Inject

class CreateCategoryUseCase @Inject constructor(
    repository: CategoryRepository,
    validator: CategoryValidator
) {
    operator fun invoke(category: Category): Result<Category> {
        return Result.Success(category)
    }
}