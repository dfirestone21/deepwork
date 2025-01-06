package com.example.deepwork.domain.usecase.timeblock.category

import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {

    operator fun invoke(): Flow<Result<List<Category>>> {
        return categoryRepository.getAll()
            .map { Result.Success(it) as Result<List<Category>> }
            .catch { e -> emit(Result.Error(e)) }
    }
}